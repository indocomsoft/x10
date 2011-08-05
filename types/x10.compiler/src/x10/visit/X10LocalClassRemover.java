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

package x10.visit;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ConstructorCall;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Local;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.frontend.Job;
import polyglot.types.ClassDef;
import polyglot.types.ClassType;
import polyglot.types.CodeDef;
import polyglot.types.ConstructorDef;
import polyglot.types.Context;
import polyglot.types.FieldDef;
import polyglot.types.LocalInstance;
import polyglot.types.MethodDef;
import polyglot.types.Name;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.Types;
import polyglot.types.VarDef;
import polyglot.types.VarInstance;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SubtypeSet;
import polyglot.visit.ContextVisitor;
import polyglot.visit.InnerClassRemover;
import polyglot.visit.LocalClassRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeBuilder;
import x10.ast.TypeParamNode;
import x10.ast.X10ClassDecl;
import x10.ast.X10MethodDecl;
import x10.ast.X10New;
import x10.types.AsyncDef;
import x10.types.AtDef;
import x10.types.EnvironmentCapture;
import x10.types.ParameterType;
import x10.types.TypeParamSubst;
import x10.types.X10ClassDef;
import x10.types.X10ClassType;
import x10.types.X10CodeDef;
import x10.types.X10ConstructorInstance;
import x10.types.X10Context_c;
import x10.types.X10MethodDef;

import polyglot.types.TypeSystem;

public class X10LocalClassRemover extends LocalClassRemover {

    /**
     * The type to be extended when translating an anonymous class that
     * implements an interface.
     */
    protected TypeNode defaultSuperType(Position pos) {
        TypeSystem ts = (TypeSystem) this.ts;
        return nf.CanonicalTypeNode(pos, ts.Object());
    }

    protected class X10ConstructorCallRewriter extends ConstructorCallRewriter {
        public X10ConstructorCallRewriter(List<FieldDef> fields, ClassDef ct) {
            super(fields, ct);
        }
            
        public Node leave(Node old, Node n, NodeVisitor v) {
            Node n_ = super.leave(old, n, v);
            
            if (n_ instanceof X10New) {
                X10New neu = (X10New) n_;
                X10ConstructorInstance ci = (X10ConstructorInstance) neu.constructorInstance();
                ConstructorDef nci = ci.def();
                X10ClassType container = (X10ClassType) Types.get(nci.container());
                
                if (container.def() == theLocalClass) {
                    X10ClassType type = (X10ClassType) Types.baseType(neu.objectType().type());
                    List<Type> ta = type.typeArguments();
                    List<TypeNode> nta = neu.typeArguments();
                    assert (ta == null || ta.size() == nta.size());
                    List<ParameterType> params = type.x10Def().typeParameters();
                    if (!params.isEmpty() && (ta == null || ta.size() != params.size())) {
                        assert (context().currentCode() instanceof X10MethodDef);
                        X10MethodDef md = (X10MethodDef) context().currentCode();
                        if (ta == null) {
                            ta = new ArrayList<Type>();
                            nta = new ArrayList<TypeNode>();
                        } else if (!md.typeParameters().isEmpty()) {
                            ta = new ArrayList<Type>(ta);
                            nta = new ArrayList<TypeNode>(nta);
                        }
                        ta.addAll(md.typeParameters());
                        for (Type pt : md.typeParameters()) {
                            nta.add(nf.CanonicalTypeNode(neu.objectType().position(), pt));
                        }
                        assert (ta.size() == nta.size());
                        assert (ta.size() == params.size());
                    }
                    TypeParamSubst subst = new TypeParamSubst((TypeSystem) ts, ta, params);
                    X10ConstructorInstance xci = (X10ConstructorInstance) subst.reinstantiate(ci);
                    neu = neu.constructorInstance(xci);
                    neu = neu.objectType(nf.CanonicalTypeNode(neu.objectType().position(), subst.reinstantiate(type)));
                    neu = neu.typeArguments(nta);
                    neu = (X10New) neu.type(subst.reinstantiate(neu.type()));
                    // FIX:XTENLANG-949 (for mismatch between neu.argument and neu.ci.formalTypes)
                    if (neu.arguments().size() > ci.formalTypes().size()) {
                        assert (false) : "This should not happen";
                        List<Type> newFormalTypes = new ArrayList<Type>();
                        for (Expr arg : neu.arguments()) {
                            newFormalTypes.add(arg.type());
                        }
                        neu = neu.constructorInstance(ci.formalTypes(newFormalTypes));
                    }
                }
                
                return neu;
            }
            
            return n_;
        }
    }

    public X10LocalClassRemover(X10InnerClassRemover icrv) {
        super(icrv);
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v) {
        Node res = super.leaveCall(old, n, v);
        if (res instanceof X10MethodDecl) {
            X10MethodDecl decl = (X10MethodDecl) res;
            Type rt = decl.returnType().type();
            if (!rt.isClass())
                return decl;
            X10ClassType type = (X10ClassType) Types.baseType(rt.toClass());
            List<Type> ta = type.typeArguments();
            List<ParameterType> params = type.x10Def().typeParameters();
            if (!params.isEmpty() && (ta == null || ta.size() != params.size())) {
                X10MethodDef md = decl.methodDef();
                if ((ta == null || ta.equals(params)) && !md.typeParameters().isEmpty())
                    ta = new ArrayList<Type>();
                ta.addAll(md.typeParameters());
                assert (ta.size() == params.size());
            }
            TypeParamSubst subst = new TypeParamSubst((TypeSystem) ts, ta, params);
            res = decl.returnType(decl.returnType().typeRef(Types.ref(subst.reinstantiate(rt))));
        }
        return res;
    }

    @Override
    protected New adjustObjectType(New neu, ClassType ct) {
        X10New r = (X10New) super.adjustObjectType(neu, ct);
        assert (r.body() != null);
        Position pos = r.objectType().position();
        List<Type> ta = ((X10ClassType) ct).typeArguments();
        List<TypeNode> typeArgs = new ArrayList<TypeNode>();
        if (ta != null) {
            for (Type t : ta) {
                typeArgs.add(nf.CanonicalTypeNode(pos, t));
            }
        }
        r = r.typeArguments(typeArgs);
        return r;
    }

    @Override
    protected X10ConstructorInstance computeConstructorInstance(ConstructorDef cd) {
        ClassDef container = ((X10ClassType) Types.get(cd.container())).def();
        return (X10ConstructorInstance) cd.asInstance().container(computeConstructedType(container, context().currentCode()));
    }

    @Override
    protected X10ClassType computeConstructedType(ClassDef cd, CodeDef currentCode) {
        X10ClassDef def = (X10ClassDef) cd;
        X10CodeDef md = (X10CodeDef) currentCode;
        X10ClassType t = ((X10ClassType)def.asType()).typeArguments(new ArrayList<Type>(md.typeParameters()));
        return t;
    }

    /**
     * Rewrites the class L as follows:
     * <pre>
     * class X[A,B]{g} {
     *     def m[C,D](){h} {
     *         class L[E,F]{c} extends S[A,B,C,D,E,F] {
     *             body
     *         }
     *         val v = new L[P,Q]();
     *     }
     * }
     * </pre>
     * to
     * <pre>
     * class X[A,B]{g} {
     *     class L'[E,F,C',D']{c[C'/C,D'/D]&&h[C'/C,D'/D]} extends S[A,B,C',D',E,F] {
     *         body[C'/C,D'/D]
     *     }
     *     def m[C,D]() {
     *         val v = new L[P,Q,C,D]();
     *     }
     * }
     * </pre>
     */
    @Override
    protected ClassDecl rewriteLocalClass(ClassDecl n, List<FieldDef> newFields) {
        assert (n instanceof X10ClassDecl && n.classDef().isMember());
        X10ClassDecl cd = (X10ClassDecl) n;
        X10ClassDef def = cd.classDef();
        X10ClassDef outer = (X10ClassDef) Types.get(def.outer());
        X10CodeDef method = (X10CodeDef) context.currentCode();
        assert outer != null;
        assert method != null;

        List<TypeParamNode> params = new ArrayList<TypeParamNode>();
        List<ParameterType> typeParameters = new ArrayList<ParameterType>();
        List<ParameterType.Variance> variances = new ArrayList<ParameterType.Variance>();

        params.addAll(cd.typeParameters());
        typeParameters.addAll(method.typeParameters());
        for (ParameterType pt : method.typeParameters()) {
            // methods cannot have variant type parameters
            variances.add(ParameterType.Variance.INVARIANT);
        }

        List<ParameterType> origTypeParams = def.typeParameters();
        for (int i = 0; i < typeParameters.size(); i++) {
            ParameterType p = typeParameters.get(i);
            ParameterType.Variance v = variances.get(i);

            NodeFactory xnf = (NodeFactory) nf;
            TypeParamNode pn = xnf.TypeParamNode(n.position(), xnf.Id(n.position(), Name.makeFresh(p.name())), v);
            TypeBuilder tb = new X10TypeBuilder(job, ts, nf);
            try {
                tb = tb.pushClass(outer);
                tb = tb.pushCode(method);
                tb = tb.pushClass(def);
                pn = (TypeParamNode) pn.del().buildTypes(tb);
                def.addTypeParameter(pn.type(), v);
            }
            catch (SemanticException e) {
                throw new InternalCompilerError(e);
            }
            params.add(pn);
        }

        if (params.size() != cd.typeParameters().size()) {
            cd = cd.typeParameters(params);
            typeParameters.addAll(0, origTypeParams);
            TypeParamSubst subst = new TypeParamSubst((TypeSystem) ts, def.typeParameters(), typeParameters);
            def.superType(subst.reinstantiate(def.superType()));
            def.setInterfaces(subst.reinstantiate(def.interfaces()));
            cd = rewriteTypeParams(subst, cd);
        }

        n = cd.body((ClassBody) rewriteConstructorCalls(cd.body(), def, newFields));

        return icrv.addFieldsToClass(n, newFields, ts, nf, false);
    }

    private X10ClassDecl rewriteTypeParams(final TypeParamSubst subst, X10ClassDecl cd) {
        return (X10ClassDecl) cd.visit(new NodeTransformingVisitor(job, ts, nf, new TypeParamSubstTransformer(subst)).context(context));
    }

    protected NodeVisitor localBoxer() {
        return new X10LocalBoxer().context(context().freeze());
    }

    protected class X10LocalBoxer extends LocalBoxer {
        public X10LocalBoxer() {
        }

        @Override
        protected Node leaveCall(Node old, Node n, NodeVisitor v) {
            Node r = super.leaveCall(old, n, v);
            if (n instanceof Local && r instanceof Field) {
                Local l = (Local) n;
                Field f = (Field) r;
                Context c = context.findEnclosingCapturingScope();
                if (c != null) {
                    EnvironmentCapture ec = (EnvironmentCapture) c.currentCode();
                    List<VarInstance<? extends VarDef>> env =
                        new ArrayList<VarInstance<? extends VarDef>>(ec.capturedEnvironment());
                    env.remove(l.localInstance());
                    env.add(f.fieldInstance());
                    ec.setCapturedEnvironment(env);
                }
            }
            return r;
        }
    }
    
    @Override
    protected boolean isLocal(Context c, Name name) {
        X10Context_c xcon = (X10Context_c)c;
        CodeDef ci = xcon.definingCodeDef(name);
        if (ci == null) return false;
        while (c != null) {
            CodeDef curr = c.currentCode();
            if (curr == ci) return true;
            // Allow closures, asyncs
            if (curr instanceof AsyncDef || curr instanceof AtDef)
                ;
            else {
                // FIX:XTENLANG-1159
                return xcon.isValInScopeInClass(name);
            }
            c = c.pop();
        }
        // FIX:XTENLANG-1159
        return xcon.isValInScopeInClass(name);
    }
    
    @Override
    protected Node rewriteConstructorCalls(Node s, final ClassDef ct, final List<FieldDef> fields) {
        Node r = s.visit(new X10ConstructorCallRewriter(fields, ct));
        return r;
    }
}