/*
 *
 * (C) Copyright IBM Corporation 2006-2008
 *
 *  This file is part of X10 Language.
 *
 */
/**
 * 
 */
package x10.ast;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Assign;
import polyglot.ast.Expr;
import polyglot.ast.FieldAssign;
import polyglot.ast.Node;
import polyglot.ast.Stmt;
import polyglot.ast.Stmt_c;
import polyglot.ast.Term;
import polyglot.ast.TypeNode;
import polyglot.frontend.Job;
import polyglot.types.Context;
import polyglot.types.FieldInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.Types;
import polyglot.types.UnknownType;
import polyglot.util.Position;
import polyglot.util.TypedList;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import x10.constraint.XConstraint;
import x10.constraint.XConstraint_c;
import x10.constraint.XFailure;
import x10.constraint.XRef_c;
import x10.constraint.XRoot;
import x10.constraint.XTerm;
import x10.constraint.XVar;
import x10.types.X10ConstructorDef;
import x10.types.X10Context;
import x10.types.X10ParsedClassType;
import x10.types.X10TypeMixin;
import x10.types.X10TypeSystem;
import x10.types.XTypeTranslator;

/**
 * @author vj
 *
 */
public class AssignPropertyCall_c extends Stmt_c implements AssignPropertyCall {

	List<Expr> arguments;

	
	/**
	 * @param pos
	 * @param arguments
	 * @param target
	 * @param name
	 */
	public AssignPropertyCall_c(Position pos, List<Expr> arguments) {
		super(pos);
		this.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
		
	}
	  public Term firstChild() {
		  return  listChild(arguments, null);
	    }

	/* (non-Javadoc)
	 * @see polyglot.ast.Term#acceptCFG(polyglot.visit.CFGBuilder, java.util.List)
	 */
	  public List acceptCFG(CFGBuilder v, List succs) {
		  v.visitCFGList(arguments, this, EXIT);
		  return succs;
	    }

	  
	  /** Return a copy of this node with this.expr equal to the given expr.
	   * @see x10.ast.Await#expr(polyglot.ast.Expr)
	   */
	  public AssignPropertyCall args( List<Expr> args ) {
		  if (args == arguments) return this;
		  AssignPropertyCall_c n = (AssignPropertyCall_c) copy();
		  n.arguments = TypedList.copyAndCheck(args, Expr.class, true);
		  return n;
	  }
	  
	  public List<Expr> args() {
		  return arguments;
	  }
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("property");
		sb.append("(");
		boolean first = true;
		for (Expr e : arguments) {
			if (first) {
				first = false;
			}
			else {
				sb.append(", ");
			}
			sb.append(e);
		}
		sb.append(");");
		return sb.toString();
	}
	
	public Node typeCheck(ContextVisitor tc) throws SemanticException {
		TypeSystem ts = tc.typeSystem();
		Context ctx = tc.context();
		X10NodeFactory nf = (X10NodeFactory) tc.nodeFactory();
		Position pos = position();
		Job job = tc.job();
		if (! (ctx.inCode()) || ! (ctx.currentCode() instanceof X10ConstructorDef))
			throw new SemanticException("A property statement may only occur in the body of a constructor.",
					position());
		X10ConstructorDef thisConstructor = null;
		thisConstructor = (X10ConstructorDef) ctx.currentCode();
		// Now check that the types of each actual argument are subtypes of the corresponding
		// property for the class reachable through the constructor.
		List<FieldInstance> definedProperties = 
			((X10ParsedClassType) thisConstructor.asInstance().container()).definedProperties();
		int pSize = definedProperties.size();
		int aSize = arguments.size();
		if (aSize != pSize) {
		    throw new SemanticException("The property initializer must have the same number of arguments as properties for the class.",
		                                position());
		}
		
		 checkAssignments(tc, pos, thisConstructor, definedProperties);
		 
		 List<Stmt> s = new ArrayList<Stmt>(pSize);

		 for (int i=0; i < aSize; i++) {
		     //				 We fudge type checking of the generating code as follows.
		     // X10 Typechecking of the assignment statement is problematic since 	
		     // the type of the field may have references to other fields, hence may use this,
		     // But this doesn't exist yet. We will check all the properties simultaneously
		     // in AssignPropertyBody. So we do not need to check it here. 
		     Expr arg = arguments.get(i);
		     
		     Expr this_ = (Expr) nf.This(pos).del().disambiguate(tc).del().typeCheck(tc).del().checkConstants(tc);
		     FieldInstance fi = definedProperties.get(i);
		     FieldAssign as = nf.FieldAssign(pos, this_, nf.Id(pos, fi.name()), Assign.ASSIGN, arg);
		     // Do not type check the assignment!
		     as = (FieldAssign) as.type(arg.type());
		     as = as.fieldInstance(fi);
//		     as = (FieldAssign) this.visitChild(as, tc);
		     Stmt a = (Stmt) nf.Eval(pos, as);
		     s.add(a);
		 }

		 return nf.AssignPropertyBody(pos, s, thisConstructor, definedProperties).del().typeCheck(tc);
	}

	protected void checkAssignments(ContextVisitor tc, Position pos, X10ConstructorDef thisConstructor, List<FieldInstance> definedProperties)
		throws SemanticException {
	    X10TypeSystem ts = (X10TypeSystem) tc.typeSystem();
		    X10Context ctx = (X10Context) tc.context();
		    if (Types.get(thisConstructor.returnType()) instanceof UnknownType) {
		        throw new SemanticException();
		    }
		    
		    Type returnType = Types.get(thisConstructor.returnType());
		    
//		    XConstraint result = X10TypeMixin.xclause(returnType);
		    XConstraint result = X10TypeMixin.realX(returnType);
		    
		    if (result.valid())
		        result = null;
		    
		    if (result != null) {
			XConstraint known = Types.get(thisConstructor.supClause());
			known = (known==null ? new XConstraint_c() : known.copy());
			try {
		            known.addIn(Types.get(thisConstructor.guard()));

		            XRoot thisVar = thisConstructor.thisVar();
		            if (! XTypeTranslator.THIS_VAR)
		                thisVar = ts.xtypeTranslator().transThisWithoutTypeConstraint();
		            
		            for (int i = 0; i < arguments.size(); i++) {
		        	Expr initializer = arguments.get(i);
		        	Type initType = initializer.type();
		        	final FieldInstance fii = definedProperties.get(i);
		        	XVar prop = (XVar) ts.xtypeTranslator().trans(known, known.self(), fii);

		        	// Add in the real clause of the initializer with [self.prop/self]
		        	XConstraint c = X10TypeMixin.realX(initType);
		        	if (c != null)
		        	    known.addIn(c.substitute(prop, c.self()));
		        	
		        	
		        	    XTerm initVar = ts.xtypeTranslator().trans(known, initializer, (X10Context) ctx);
		        	    known.addBinding(prop, initVar);
		        	
		        	
		            }

		            // bind this==self; sup clause may constrain this.
		            if (thisVar != null) {
		                known.addSelfBinding(thisVar);
		                known.setThisVar(thisVar);
		            }

		            if (! known.entails(result, ctx.constraintProjection(known, result))) {
		        	    throw new SemanticException("Instances created by this constructor satisfy " + known 
		        	                                + "; this is not strong enough to entail the return constraint " + result,
		        	                                position());
		            }
		    }
		    catch (XFailure e) {
		            throw new SemanticException(e.getMessage());
		    } 
		    }
	}
	
	/** Visit the children of the statement. */
	
	    public Node visitChildren(NodeVisitor v) {
	        List<Expr> args = visitList(this.arguments, v);
		return args(args);
	    }
	    
	    Expr expr;
	    
	   
}

