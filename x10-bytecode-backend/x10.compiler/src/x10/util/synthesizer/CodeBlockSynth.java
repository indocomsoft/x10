/*
 *  This file is part of the X10 project (http://x10-lang.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  (C) Copyright IBM Corporation 2006-2010.
 */
package x10.util.synthesizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import polyglot.ast.Block;
import polyglot.ast.Expr;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.ast.Stmt;
import polyglot.types.ClassDef;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.types.Name;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import x10.ast.AnnotationNode;
import x10.ast.X10NodeFactory;
import x10.types.X10Context;

/**
 * Synthesizer to construct a code block
 * 
 * User could add statement one by one or add statement in batch.
 * 
 * Every local decl added into will be recorded as one local variables
 * And then user could query local variables in the current block by name.
 * If no local variables found, it will query its container if its container
 * is a code block synth.
 * 
 * And it provides query accessible field APIs to return a accessible field
 * The mechanism is it will locate the class container and locate the class's
 * field as a expr
 * 
 *
 */
public class CodeBlockSynth extends AbstractStateSynth implements IStmtSynth{

    /**
     * @author Haichuan
     * Inner class for wrapping a statement as a IStmtSynth
     */
    class SimpleStmtSynth implements IStmtSynth{
        Stmt stmt;
        SimpleStmtSynth(Stmt stmt){
            this.stmt = stmt;
        }
        public Stmt genStmt(Position pos) throws SemanticException {
            return (Stmt) stmt.position(pos);
        }
        public Stmt genStmt() throws SemanticException {
            return stmt;
        }
    }
    
    protected Block block; //the result
    protected AbstractStateSynth containerSynth; //The current block's parent
    protected List<IStmtSynth> stmtSythns; //all synthesizers for generate the code block
    protected HashMap<String, Local> localVarMap; //name to local var map
    
    protected HashMap<Expr, Stmt>refToDeclMap; //used to add additional local declare at the beginning of a block
    
    /**
     * Create a code block synth and specify its container
     * @param xnf
     * @param xct
     * @param containerSynth
     * @param pos
     */
    public CodeBlockSynth(X10NodeFactory xnf, X10Context xct, AbstractStateSynth containerSynth, Position pos) {
        super(xnf, xct, pos);
        this.containerSynth = containerSynth;
        stmtSythns = new ArrayList<IStmtSynth>();
        localVarMap = new HashMap<String, Local>(); 
    }
    
    /**
     * Create a stand alone codeblock synth
     * @param xnf
     * @param xct
     * @param pos
     */
    public CodeBlockSynth(X10NodeFactory xnf, X10Context xct, Position pos) {
        this(xnf, xct, null, pos);
    }
    
    /**
     * Only be used in MethodSynth/Constructor Synth
     * @param containerSynth
     */
    protected void setContainerSynth(AbstractStateSynth containerSynth) {
        this.containerSynth = containerSynth;
    }

    /**
     * Add locals to the code block map.
     * It's should be only invoked by this class and method/constructor synthesizer (add formal)
     * @param local
     */
    protected void addLocal(Local local){
        localVarMap.put(local.name().id().toString(), local);
    }
    
    public Local getLocal(String name){
        
        AbstractStateSynth synth = this;
        while(synth != null && synth instanceof CodeBlockSynth){
            CodeBlockSynth codeBlockSynth = (CodeBlockSynth)synth;
            if(codeBlockSynth.localVarMap.containsKey(name)){
                return codeBlockSynth.localVarMap.get(name);
            }
            //not found, to its parent
            synth = codeBlockSynth.containerSynth;
        }
        return null;  
    }
    
    
    /**
     * Add one statement at the end of the code block
     * @param stmt
     */
    public void addStmt(Stmt stmt) { 
        try {
            checkClose();
            stmtSythns.add(new SimpleStmtSynth(stmt));
            
            if(stmt instanceof LocalDecl){
                //add this local
                addLocal(synth.createLocal(compilerPos, (LocalDecl)stmt));
            }
        } catch (StateSynthClosedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Add one statement (statement synth) at the end of the code block
     * @param iss
     */
    public void addStmt(IStmtSynth iss) {
        try {
            checkClose();
            stmtSythns.add(iss);

            if(iss instanceof NewLocalVarSynth){
                //add this local
                addLocal(((NewLocalVarSynth)iss).getLocal());
            }
        } catch (StateSynthClosedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Add statements at the end of the code block
     * @param stmts
     */
    public void addStmts(List<Stmt> stmts) {
        for(Stmt stmt : stmts){
            addStmt(stmt);
        }
    }
    
    /**
     * Add one statement in the front of the code block.
     * @param stmt
     */
    public void addStmtInFront(Stmt stmt) {
        stmtSythns.add(0, new SimpleStmtSynth(stmt));
    }
    
    /**
     * Add statements in the front of the code block
     * @param stmts
     */
    public void addStmtsInFront(List<Stmt> stmts){
        for(int i = stmts.size() - 1; i >=0; i--){
            addStmtInFront(stmts.get(i));
        } 
    }

    public InstanceCallSynth createInstanceCallStmt(Position pos, Receiver insRef, String methodName) {
        InstanceCallSynth synth = new InstanceCallSynth(xnf, xct, pos, insRef, methodName);
        addStmt(synth);
        return synth;
    }

    public NewInstanceSynth createNewInstaceStmt(Position pos, ClassType classType) {
        NewInstanceSynth synth = new NewInstanceSynth(xnf, xct, pos, classType);
        addStmt(synth);
        return synth;
    }

    public SwitchSynth createSwitchStmt(Position pos, Expr switchCond) {
        SwitchSynth synth = new SwitchSynth(xnf, xct, pos, switchCond);
        addStmt(synth);
        return synth;
    }

    public NewLocalVarSynth createLocalVar(Position pos, Name name, Flags flags, 
                                           Expr initializer,List<AnnotationNode> annotations){
        NewLocalVarSynth synth = new NewLocalVarSynth(xnf, xct, pos, name, flags,
                                                      initializer, annotations);
        addStmt(synth);
        return synth;
    }
    
    public NewLocalVarSynth createLocalVar(Position pos, Expr initializer) {
        NewLocalVarSynth synth = new NewLocalVarSynth(xnf, xct, pos, initializer);
        addStmt(synth);
        return synth;
    }
    
    public SuperCallSynth createSuperCall(Position pos, ClassDef classDef){
        SuperCallSynth synth = new SuperCallSynth(xnf, xct, pos, classDef);
        addStmt(synth);
        return synth;
    }

    public CodeBlockSynth createCodeBlock(Position pos){
        CodeBlockSynth synth = new CodeBlockSynth(xnf, xct, this, pos);
        addStmt(synth);
        return synth;
    }
    
    
    public Block close() throws SemanticException {
        if(closed){
            return block; //the result;
        }
        closed = true;
        
        ArrayList<Stmt> stmts = new ArrayList<Stmt>();
        for(IStmtSynth iss : stmtSythns){
            stmts.add(iss.genStmt());
        }
        block = xnf.Block(pos, stmts);
        
        //special process if refToDeclMap is not null
        if(refToDeclMap != null){
            //first detect all locals;
            LocalExprFinder lef = new LocalExprFinder();
            block.visit(lef);//no replacement, just detect
            
            HashSet<Stmt> localDecls = new HashSet<Stmt>();
            for(Local local : lef.localList){
                Stmt s = refToDeclMap.get(local);
                if(s != null){
                    localDecls.add(s);
                }
            }
            //finally, add all these into the block
            if(localDecls.size() > 0){
                ArrayList<Stmt> nStmts = new ArrayList<Stmt>();
                nStmts.addAll(localDecls);
                nStmts.addAll(block.statements());
                block = xnf.Block(pos, nStmts);
            }
        }
        
        return block;
    }

    public Stmt genStmt() throws SemanticException {
        return close();
    }

    /**
     * If user set the map, the close method will search all locals
     * And if find one local is in the map, it will add the declares in the code body
     * @param refToDeclMap
     */
    public void setRefToDeclMap(HashMap<Expr, Stmt> refToDeclMap) {
        this.refToDeclMap = refToDeclMap;
    }
    
    
    static class LocalExprFinder extends NodeVisitor {
        ArrayList<Local> localList;
        public LocalExprFinder(){
            localList = new ArrayList<Local>();
        }
        
        public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
            if (n instanceof Local) {
                localList.add((Local) n);
            }
            return n;
        }
    }

}