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

package x10.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.ClassMember;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.ast.FlagsNode;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Local;
import polyglot.ast.MethodDecl;
import polyglot.ast.MethodDecl_c;
import polyglot.ast.NamedVariable;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Return;
import polyglot.ast.Stmt;
import polyglot.ast.TypeCheckFragmentGoal;
import polyglot.ast.TypeNode;
import polyglot.frontend.Globals;
import polyglot.frontend.Job;
import polyglot.frontend.SetResolverGoal;
import polyglot.main.Report;
import polyglot.types.ClassDef;
import polyglot.types.ClassType;
import polyglot.types.ConstructorDef;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Def;
import polyglot.types.ErrorRef_c;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.LazyRef;
import polyglot.types.LocalDef;
import polyglot.types.MemberDef;
import polyglot.types.MemberInstance;
import polyglot.types.MethodDef;
import polyglot.types.MethodInstance;
import polyglot.types.Name;
import polyglot.types.Package;
import polyglot.types.QName;
import polyglot.types.Ref;
import polyglot.types.Ref_c;
import polyglot.types.SemanticException;
import polyglot.types.StructType;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.types.Types;
import polyglot.types.UnknownType;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.TypedList;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeCheckPreparer;
import polyglot.visit.TypeChecker;
import x10.constraint.XFailure;
import x10.constraint.XName;
import x10.constraint.XNameWrapper;
import x10.constraint.XRef;
import x10.constraint.XVar;
import x10.constraint.XTerm;
import x10.constraint.XTerms;
import x10.constraint.XVar;
import x10.errors.Errors;
import x10.extension.X10Del;
import x10.extension.X10Del_c;
import x10.types.ConstrainedType;
import x10.types.MacroType;
import x10.types.ParameterType;
import x10.types.X10ClassDef;
import x10.types.X10ClassType;
import x10.types.X10ConstructorDef;
import x10.types.X10Context;
import x10.types.X10Flags;
import x10.types.X10MemberDef;
import x10.types.X10MethodDef;
import x10.types.X10MethodInstance;
import x10.types.X10ProcedureDef;
import x10.types.X10TypeEnv_c;

import x10.types.X10TypeMixin;
import x10.types.X10TypeSystem;
import x10.types.XTypeTranslator;
import x10.types.checker.Checker;
import x10.types.checker.PlaceChecker;
import x10.types.checker.VarChecker;
import x10.types.constraints.CConstraint;
import x10.types.constraints.TypeConstraint;
import x10.types.constraints.XConstrainedTerm;
import x10.visit.X10TypeChecker;

/** A representation of a method declaration.
 * Includes an extra field to represent the guard
 * in the method definition.
 * 
 * @author vj
 *
 */
public class X10MethodDecl_c extends MethodDecl_c implements X10MethodDecl {
	// The representation of the  guard on the method definition
	DepParameterExpr guard;
	List<TypeParamNode> typeParameters;

	// set by createMethodDef.
	XTerm placeTerm;

	TypeNode offerType;
	TypeNode hasType;
	public X10MethodDecl_c(Position pos, FlagsNode flags, 
			TypeNode returnType, Id name,
			List<TypeParamNode> typeParams, List<Formal> formals, DepParameterExpr guard, List<TypeNode> throwTypes, TypeNode offerType, Block body) {
		super(pos, flags, returnType instanceof HasTypeNode_c ? ((X10NodeFactory) Globals.NF()).UnknownTypeNode(returnType.position()) : returnType, 
				name, formals, throwTypes, body);
		this.guard = guard;
		this.typeParameters = TypedList.copyAndCheck(typeParams, TypeParamNode.class, true);
		if (returnType instanceof HasTypeNode_c) 
			hasType = ((HasTypeNode_c) returnType).typeNode();
		this.offerType = offerType;

	}

	public TypeNode offerType() {
		return offerType;
	}
	protected X10MethodDecl_c hasType(TypeNode hasType) {
		if (this.hasType != hasType)  {
			X10MethodDecl_c n = (X10MethodDecl_c) copy();
			n.hasType = hasType;
			return n;
		}
		return this;
	}
	protected X10MethodDecl_c offerType(TypeNode offerType) {
		if (this.offerType != offerType)  {
			X10MethodDecl_c n = (X10MethodDecl_c) copy();
			n.offerType = offerType;
			return n;
		}
		return this;
	}

	protected MethodDef createMethodDef(TypeSystem ts, ClassDef ct, Flags flags) {
		X10MethodDef mi = (X10MethodDef) ((X10TypeSystem) ts).methodDef(position(), Types.ref(ct.asType()), flags, returnType.typeRef(), name.id(),
				Collections.<Ref<? extends Type>>emptyList(), Collections.<Ref<? extends Type>>emptyList(), 
				offerType == null ? null : offerType.typeRef());

		mi.setThisVar(((X10ClassDef) ct).thisVar());
		this.placeTerm = PlaceChecker.methodPT(flags, ct);
		return mi;
	}

	@Override
	public Node buildTypesOverride(TypeBuilder tb) throws SemanticException {
		X10MethodDecl_c n = (X10MethodDecl_c) super.buildTypesOverride(tb);

		X10MethodDef mi = (X10MethodDef) n.methodDef();

		n = (X10MethodDecl_c) X10Del_c.visitAnnotations(n, tb);

		List<AnnotationNode> as = ((X10Del) n.del()).annotations();
		if (as != null) {
			List<Ref<? extends Type>> ats = new ArrayList<Ref<? extends Type>>(as.size());
			for (AnnotationNode an : as) {
				ats.add(an.annotationType().typeRef());
			}
			mi.setDefAnnotations(ats);
		}

		// Enable return type inference for this method declaration.
		if (n.returnType() instanceof UnknownTypeNode) {
			mi.inferReturnType(true);
		}

		if (n.guard() != null) {
			mi.setGuard(n.guard().valueConstraint());
			mi.setTypeGuard(n.guard().typeConstraint());
		}

		List<Ref<? extends Type>> typeParameters = new ArrayList<Ref<? extends Type>>(n.typeParameters().size());
		for (TypeParamNode tpn : n.typeParameters()) {
			typeParameters.add(Types.ref(tpn.type()));
		}
		mi.setTypeParameters(typeParameters);

		List<LocalDef> formalNames = new ArrayList<LocalDef>(n.formals().size());
		for (Formal f : n.formals()) {
			formalNames.add(f.localDef());
		}
		mi.setFormalNames(formalNames);

		if (n.returnType() instanceof UnknownTypeNode && n.body() == null) {
			Errors.issue(tb.job(),
			             new SemanticException("Cannot infer method return type; method has no body.",
			                                   position()));
			NodeFactory nf = tb.nodeFactory();
			TypeSystem ts = tb.typeSystem();
			Position rtpos = n.returnType().position();
			n = (X10MethodDecl_c) n.returnType(nf.CanonicalTypeNode(rtpos, ts.unknownType(rtpos)));
		}

		X10Flags xf = X10Flags.toX10Flags(mi.flags());
		if (xf.isProperty()) {
			final LazyRef<XTerm> bodyRef = Types.lazyRef(null);
			bodyRef.setResolver(new SetResolverGoal(tb.job()));
			mi.body(bodyRef);
		}

		// property implies public, final
		if (xf.isProperty()) {
			if (xf.isAbstract())
				xf = X10Flags.toX10Flags(xf.Public());
			else
				xf = X10Flags.toX10Flags(xf.Public().Final());

			mi.setFlags(xf);
			n = (X10MethodDecl_c) n.flags(n.flags().flags(xf));
		}


		return n;
	}

	@Override
	public void addDecls(Context c) {


	}
	public void setResolver(Node parent, final TypeCheckPreparer v) {
		X10MethodDef mi = (X10MethodDef) this.mi;
		if (mi.body() instanceof LazyRef) {
			LazyRef<XTerm> r = (LazyRef<XTerm>) mi.body();
			TypeChecker tc = new X10TypeChecker(v.job(), v.typeSystem(), v.nodeFactory(), v.getMemo());
			tc = (TypeChecker) tc.context(v.context().freeze());
			r.setResolver(new TypeCheckFragmentGoal(parent, this, tc, r, false));
		}
	}

	/** Visit the children of the method. */
	public Node visitSignature(NodeVisitor v) {
		X10MethodDecl_c result = (X10MethodDecl_c) super.visitSignature(v);
		List<TypeParamNode> typeParams = (List<TypeParamNode>) visitList(result.typeParameters, v);
		if (! CollectionUtil.allEqual(typeParams, result.typeParameters))
			result = (X10MethodDecl_c) result.typeParameters(typeParams);
		DepParameterExpr guard = (DepParameterExpr) visitChild(result.guard, v);
		if (guard != result.guard)
			result = (X10MethodDecl_c) result.guard(guard);
		TypeNode ht = (TypeNode) visitChild(result.hasType, v);
		result = result.hasType(ht);
		if (offerType != null) {
			TypeNode ot = (TypeNode) visitChild(result.offerType, v);
			result = result.offerType(ot);
		}

		return result;
	}


	public List<TypeParamNode> typeParameters() {
		return typeParameters;
	}

	public X10MethodDecl typeParameters(List<TypeParamNode> typeParams) {
		X10MethodDecl_c n = (X10MethodDecl_c) copy();
		n.typeParameters=TypedList.copyAndCheck(typeParams, TypeParamNode.class, true);
		return n;
	}

	public DepParameterExpr guard() { return guard; }
	public X10MethodDecl guard(DepParameterExpr e) {
		X10MethodDecl_c n = (X10MethodDecl_c) copy();
		n.guard = e;
		return n;
	}


	@Override
	public Context enterChildScope(Node child, Context c) {
		// We should have entered the method scope already.
		assert c.currentCode() == this.methodDef();

		if (child != body()) {
			// Push formals so they're in scope in the types of the other formals.
			c = c.pushBlock();
			for (TypeParamNode f : typeParameters) {
				f.addDecls(c);
			}
			for (Formal f : formals) {
				f.addDecls(c);
			}
		}

		// Ensure that the place constraint is set appropriately when
		// entering the body of the method, the return type and the throw type.


		if (child == body || child == returnType || child == hasType || child == throwTypes || child == offerType || (formals != null && formals.contains(child))) {
			if (placeTerm != null)
				c = ((X10Context) c).pushPlace( XConstrainedTerm.make(placeTerm));
			// 	PlaceChecker.pushHereTerm(methodDef(), (X10Context) c);
		}

		// Add the method guard into the environment.
		if (guard != null) {
			Ref<CConstraint> vc = guard.valueConstraint();
			Ref<TypeConstraint> tc = guard.typeConstraint(); // todo: tc is ignored

			if (vc != null || tc != null) {
				c = c.pushBlock();
				try {
					if (vc.known())
						c= ((X10Context) c).pushAdditionalConstraint(vc.get());
					// TODO: Add type constraint.

				} catch (SemanticException z) {
					throw 
					new InternalCompilerError("Unexpected inconsistent guard" + z);
				}
				//        ((X10Context) c).setCurrentConstraint(vc.get());
				//        ((X10Context) c).setCurrentTypeConstraint(tc.get());
			}            
		}

		return super.enterChildScope(child, c);
	}

	public void translate(CodeWriter w, Translator tr) {
		Context c = tr.context();
		Flags flags = flags().flags();

		if (c.currentClass().flags().isInterface()) {
			flags = flags.clearPublic();
			flags = flags.clearAbstract();
		}

		// Hack to ensure that X10Flags are not printed out .. javac will
		// not know what to do with them.

		FlagsNode oldFlags = this.flags;
		try {
			this.flags = this.flags.flags(X10Flags.toX10Flags(flags));
			super.translate(w, tr);
		}
		finally {
			this.flags = oldFlags;
		}
	}

	@Override
	public Node setResolverOverride(Node parent, TypeCheckPreparer v) {
		if (returnType() instanceof UnknownTypeNode && body != null) {
			UnknownTypeNode tn = (UnknownTypeNode) returnType();

			NodeVisitor childv = v.enter(parent, this);
			childv = childv.enter(this, returnType());

			if (childv instanceof TypeCheckPreparer) {
				TypeCheckPreparer tcp = (TypeCheckPreparer) childv;
				final LazyRef<Type> r = (LazyRef<Type>) tn.typeRef();
				TypeChecker tc = new X10TypeChecker(v.job(), v.typeSystem(), v.nodeFactory(), v.getMemo(), true);
				tc = (TypeChecker) tc.context(tcp.context().freeze());
				r.setResolver(new TypeCheckReturnTypeGoal(this, body, tc, r, true));
			}
		}
		return super.setResolverOverride(parent, v);
	}

	@Override
	protected void checkFlags(ContextVisitor tc, Flags flags) throws SemanticException {
		X10Flags xf = X10Flags.toX10Flags(flags);

		if (xf.isExtern() && body != null) {
			throw new SemanticException("An extern method cannot have a body.", position());
		}

		if (xf.isProto() && xf.isStatic()) {
			throw new SemanticException("Only instance method may have a proto flag.", 
					position());
		}

		// Set the native flag if incomplete or extern so super.checkFlags doesn't complain.
		if (xf.isIncomplete() || xf.isExtern())
			super.checkFlags(tc, xf.Native());
		else
			super.checkFlags(tc, xf);

		if (xf.isProperty() && ! xf.isAbstract() && ! xf.isFinal()) {
			throw new SemanticException("A non-abstract property method must be final.", position());
		}
		if (xf.isProperty() && xf.isStatic()) {
			throw new SemanticException("A property method cannot be static.", position());
		}
	}

	@Override
	public Node typeCheck(ContextVisitor tc) throws SemanticException {
		X10MethodDecl_c n = this;
		NodeFactory nf = tc.nodeFactory();
		X10TypeSystem ts = (X10TypeSystem) tc.typeSystem();
		if (((X10TypeSystem) tc.typeSystem()).isStructType(mi.container().get())) {
			Flags xf = X10Flags.toX10Flags(mi.flags()).Global().Final();
			mi.setFlags(xf);
			n = (X10MethodDecl_c) n.flags(n.flags().flags(xf));
		}

		for (TypeNode type : n.throwTypes()) {
			CConstraint rc = X10TypeMixin.xclause(type.type());
			if (rc != null && ! rc.valid())
				throw new SemanticException("Cannot throw a dependent type.", type.position());
		}

		X10Flags xf = X10Flags.toX10Flags(mi.flags());

		// Check these flags here rather than in checkFlags since we need the body.
		if (xf.isIncomplete() && body != null) {
			throw new SemanticException("An incomplete method cannot have a body.", position());
		}

		//            if (xf.isProperty() && body == null) {
		//        	throw new SemanticException("A property method must have a body.", position());
		//            }

		if (xf.isIncomplete()) {
			n.mi.setFlags(xf.clearIncomplete());
			Flags oldFlags = n.flags().flags();
			X10Flags newFlags = X10Flags.toX10Flags(oldFlags).clearIncomplete();
			n = (X10MethodDecl_c) n.flags(n.flags().flags(newFlags));
			Type rtx = ts.RuntimeException();

			CanonicalTypeNode rtxNode = nf.CanonicalTypeNode(position(), Types.ref(rtx));
			Expr msg = nf.StringLit(position(), "Incomplete method.");
			New newRtx = nf.New(position(), rtxNode, Collections.singletonList(msg));
			Block b = nf.Block(position(), nf.Throw(position(), newRtx));
			b = (Block) b.visit(tc);
			n = (X10MethodDecl_c) n.body(b);
		}

		if (xf.isProperty()) {
			boolean ok = false;
			if (xf.isAbstract()) {
				ok = true;
			}
			if (n.body != null && n.body.statements().size() == 1) {
				Stmt s = n.body.statements().get(0);
				if (s instanceof Return) {
					Return r = (Return) s;
					if (r.expr() != null) {
						XTerm v = ts.xtypeTranslator().trans((CConstraint) null, r.expr(), (X10Context) tc.context());
						ok = true;
						X10MethodDef mi = (X10MethodDef) this.mi;
						if (mi.body() instanceof LazyRef) {
							LazyRef<XTerm> bodyRef = (LazyRef<XTerm>) mi.body();
							bodyRef.update(v);
						}
					}
				}
			}
			if (! ok)
				throw new SemanticException("Property method body must be a constraint expression.", position());
		}

		n = (X10MethodDecl_c) n.superTypeCheck(tc);

		dupFormalCheck(typeParameters, formals);

		X10TypeMixin.protoTypeCheck(formals(),  returnType().type(), position(),
				true);
		X10TypeMixin.checkMissingParameters(n.returnType());
		return n;
	}



	public static void dupFormalCheck(List<TypeParamNode> typeParams, List<Formal> formals) throws SemanticException {
		Set<Name> pnames = new HashSet<Name>();
		for (TypeParamNode p : typeParams) {
			Name name = p.name().id();
			if (pnames.contains(name))
				throw new SemanticException("Type parameter \"" + name + "\" multiply defined.", p.position());
			pnames.add(name);
		}

		// Check for duplicate formals. This isn't caught in Formal_c
		// because we add all the formals into the scope before visiting a
		// formal, so the lookup of a duplicate formal returns itself rather
		// than the previous formal.
		Set<Name> names = new HashSet<Name>();
		LinkedList<Formal> q = new LinkedList<Formal>();
		q.addAll(formals);
		while (! q.isEmpty()) {
			Formal f = q.removeFirst();
			Name name = f.name().id();
			if (! name.equals(Name.make(""))) {
				if (names.contains(name))
					throw new SemanticException("Local variable \"" + name + "\" multiply defined.", f.position());
				names.add(name);
			}
			if (f instanceof X10Formal) {
				X10Formal ff = (X10Formal) f;
				q.addAll(ff.vars());
			}
		}
	}

	protected X10MethodDecl_c superTypeCheck(ContextVisitor tc) throws SemanticException {
		return (X10MethodDecl_c) super.typeCheck(tc);
	}

	@Override
	public Node conformanceCheck(ContextVisitor tc) throws SemanticException {
		checkVariance(tc);

		MethodDef mi = this.methodDef();
		X10TypeSystem xts = (X10TypeSystem) tc.typeSystem();

		for (TypeNode type : throwTypes()) {
			CConstraint rc = X10TypeMixin.xclause(type.type());
			if (rc != null && ! rc.valid())
				throw new SemanticException("Cannot throw a dependent type.", type.position());
		}

		if (X10Flags.toX10Flags(mi.flags()).isProperty()) {
			X10MethodInstance xmi = (X10MethodInstance) mi.asInstance();
			if (xmi.guard() != null && ! xmi.guard().valid())
				throw new SemanticException("A property method cannot have a guard.", guard != null ? guard.position() : position());
		}

		if (X10Flags.toX10Flags(mi.flags()).isExtern()) {
			if (!mi.returnType().get().isPrimitive())
				throw new SemanticException("Return type " + mi.returnType() + " of extern method must be a primitive type.", this.position());

			for (Formal parameter : this.formals()) {
				Type declType = parameter.declType();
				boolean isOk = true;
				if (!declType.isPrimitive()) {
					isOk = false;
				}
				else if (declType.isArray()) {
					isOk = false;
				}
				else if (declType.isClass()) {
					isOk = xts.isRail(declType) || xts.isValRail(declType);
				}
				if (!isOk)
					throw new SemanticException("Parameters to extern calls must be either X10 arrays or primitives.", parameter.position());
			}
		}

		checkVisibility(tc.typeSystem(), tc.context(), this);

		// Need to ensure that method overriding is checked in a context in which here=this.home
		// has been asserted.
		Context childtc = enterChildScope(returnType(), tc.context());
		ContextVisitor childVisitor = tc.context(childtc);
		try {
		    return super.conformanceCheck(childVisitor);
		} catch (SemanticException e) {
		    Errors.issue(tc.job(), e, this);
		    return this;
		}
	}

	final static boolean CHECK_VISIBILITY = false;

	protected static void checkVisibility(final TypeSystem ts, Context c, final ClassMember mem) throws SemanticException {
		// This doesn't work since we've already translated away expressions into constraints.
		if (! CHECK_VISIBILITY)
			return;

		final SemanticException[] ex = new SemanticException[1];

		// Check if all fields, methods, etc accessed from the signature of mem are in scope wherever mem can be accessed.
		// Assumes the fields, methods, etc are accessible from mem, at least.

		mem.visitChildren(new NodeVisitor() {
			boolean on = false;

			@Override
			public Node override(Node parent, Node n) {
				if (! on) {
					if (n instanceof TypeNode) {
						try {
							on = true;
							return this.visitEdgeNoOverride(parent, n);
						}
						finally {
							on = false;
						}
					}
					else {
						return this.visitEdgeNoOverride(parent, n);

					}
				}

				if (n instanceof Stmt) {
					return n;
				}

				if (parent instanceof FieldDecl && n == ((FieldDecl) parent).init()) {
					return n;
				}

				if (n instanceof Field) {
					FieldInstance fi = (((Field) n).fieldInstance());
					if (! hasSameScope(fi, mem.memberDef())) {
						ex[0] = new SemanticException("Field " + fi.name() + " cannot be used in this signature; not accessible from all contexts in which the member is accessible.", n.position());
					}
				}
				if (n instanceof Call) {
					MethodInstance mi = (((Call) n).methodInstance());
					if (! hasSameScope(mi, mem.memberDef())) {
						ex[0] = new SemanticException("Method " + mi.signature() + " cannot be used in this signature; not accessible from all contexts in which the member is accessible.", n.position());
					}
				}
				if (n instanceof ClosureCall) {
					MethodInstance mi = (((ClosureCall) n).closureInstance());
					if (! hasSameScope(mi, mem.memberDef())) {
						ex[0] = new SemanticException("Method " + mi.signature() + " cannot be used in this signature; not accessible from all contexts in which the member is accessible.", n.position());
					}
				}
				if (n instanceof TypeNode) {
					TypeNode tn = (TypeNode) n;
					Type t = tn.type();
					t = X10TypeMixin.baseType(t);
					if (t instanceof X10ClassType) {
						X10ClassType ct = (X10ClassType) t;
						if (! hasSameScope(ct, mem.memberDef())) {
							ex[0] = new SemanticException("Class " + ct.fullName() + " cannot be used in this signature; not accessible from all contexts in which the member is accessible.", n.position());
						}
					}
				}
				if (n instanceof New) {
					ConstructorInstance ci = (((New) n).constructorInstance());
					if (! hasSameScope(ci, mem.memberDef())) {
						ex[0] = new SemanticException("Constructor " + ci.signature() + " cannot be used in this signature; not accessible from all contexts in which the member is accessible.", n.position());
					}
				}

				return null;
			}

			Flags getSignatureFlags(MemberDef def) {
				Flags sigFlags = def.flags();
				if (def instanceof ClassDef) {
					ClassDef cd = (ClassDef) def;
					if (cd.isTopLevel()) {
						return sigFlags;
					}
					if (cd.isMember()) {
						ClassDef outer = Types.get(cd.outer());
						Flags outerFlags = getSignatureFlags(outer);
						return combineFlagsWithContainerFlags(sigFlags, outerFlags);
					}
					return Flags.PRIVATE;
				}
				else {
					Type t = Types.get(def.container());
					t = X10TypeMixin.baseType(t);
					if (t instanceof ClassType) {
						ClassType ct = (ClassType) t;
						Flags outerFlags = getSignatureFlags(ct.def());
						return combineFlagsWithContainerFlags(sigFlags, outerFlags);
					}
				}
				return sigFlags;
			}

			private Flags combineFlagsWithContainerFlags(Flags sigFlags, Flags outerFlags) {
				if (outerFlags.isPrivate() || sigFlags.isPrivate())
					return Flags.PRIVATE;
				if (outerFlags.isPackage() || sigFlags.isPackage())
					return Flags.NONE;
				if (outerFlags.isProtected())
					return Flags.NONE;
				if (sigFlags.isProtected())
					return Flags.PROTECTED;
				return Flags.PUBLIC;
			}

			private <T extends Def> boolean hasSameScope(MemberInstance<T> mi, MemberDef signature) {
				Flags sigFlags = getSignatureFlags(signature);
				if (sigFlags.isPrivate()) {
					return true;
				}
				if (sigFlags.isPackage()) {
					if (mi.flags().isPublic())
						return true;
					if (mi.flags().isProtected() || mi.flags().isPackage()) {
						return hasSamePackage(mi.def(), signature);
					}
					return false;
				}
				if (sigFlags.isProtected()) {
					if (mi.flags().isPublic())
						return true;
					if (mi.flags().isProtected()) {
						return hasSameClass(mi.def(), signature);
					}
					return false;
				}
				if (sigFlags.isPublic()) {
					if (mi.flags().isPublic())
						return true;
					return false;
				}
				return false;
			}

			private ClassDef getClass(Def def) {
				if (def instanceof ClassDef) {
					return (ClassDef) def;
				}
				if (def instanceof MemberDef) {
					MemberDef md = (MemberDef) def;
					Type container = Types.get(md.container());
					if (container != null) {
						container = X10TypeMixin.baseType(container);
						if (container instanceof ClassType) {
							return ((ClassType) container).def();
						}
					}
				}
				return null;
			}

			private boolean hasSameClass(Def def, MemberDef accessor) {
				ClassDef c1 = getClass(def);
				ClassDef c2 = getClass(accessor);
				if (c1 == null || c2 == null)
					return false;
				return c1.equals(c2);
			}

			private boolean hasSamePackage(Def def, MemberDef accessor) {
				ClassDef c1 = getClass(def);
				ClassDef c2 = getClass(accessor);
				if (c1 == null || c2 == null)
					return false;
				Package p1 = Types.get(c1.package_());
				Package p2 = Types.get(c2.package_());
				if (p1 == null && p2 == null)
					return true;
				if (p1 == null || p2 == null)
					return false;
				return p1.equals(p2);
			}
		});

		if (ex[0] != null)
			throw ex[0];
	}


	protected void checkVariance(ContextVisitor tc) throws SemanticException {
		if (methodDef().flags().isStatic())
			return;

		X10ClassDef cd = (X10ClassDef) tc.context().currentClassDef();
		final Map<Name,ParameterType.Variance> vars = new HashMap<Name, ParameterType.Variance>();
		for (int i = 0; i < cd.typeParameters().size(); i++) {
			ParameterType pt = cd.typeParameters().get(i);
			ParameterType.Variance v = cd.variances().get(i);
			vars.put(pt.name(), v);
		}

		Checker.checkVariancesOfType(returnType.position(), returnType.type(), ParameterType.Variance.COVARIANT, "as a method return type", vars, tc);
		for (Formal f : formals) {
			Checker.checkVariancesOfType(f.type().position(), f.declType(), ParameterType.Variance.CONTRAVARIANT, "as a method parameter type", vars, tc);
		}
	}

	public Node typeCheckOverride(Node parent, ContextVisitor tc) {
		X10MethodDecl nn = this;
		X10MethodDecl old = nn;

		X10TypeSystem xts = (X10TypeSystem) tc.typeSystem();

		// Step I.a.  Check the formals.
		TypeChecker childtc = (TypeChecker) tc.enter(parent, nn);

		// First, record the final status of each of the type params and formals.
		List<TypeParamNode> processedTypeParams = nn.visitList(nn.typeParameters(), childtc);
		nn = (X10MethodDecl) nn.typeParameters(processedTypeParams);
		List<Formal> processedFormals = nn.visitList(nn.formals(), childtc);
		nn = (X10MethodDecl) nn.formals(processedFormals);

		nn = (X10MethodDecl) X10Del_c.visitAnnotations(nn, childtc);

		// [NN]: Don't do this here, do it on lookup of the formal.  We don't want spurious self constraints in the signature.
		//            for (Formal n : processedFormals) {
		//        		Ref<Type> ref = (Ref<Type>) n.type().typeRef();
		//        		Type newType = ref.get();
		//        		
		//        		if (n.localDef().flags().isFinal()) {
		//            			XConstraint c = X10TypeMixin.xclause(newType);
		//            			if (c == null)
		//					c = new XConstraint_c();
		//				else
		//					c = c.copy();
		//            			try {
		//        				c.addSelfBinding(xts.xtypeTranslator().trans(n.localDef().asInstance()));
		//        			}
		//        			catch (XFailure e) {
		//        				throw new SemanticException(e.getMessage(), position());
		//        			}
		//            			newType = X10TypeMixin.xclause(newType, c);
		//        		}
		//        		
		//        		ref.update(newType);
		//            }

		// Step I.b.  Check the guard.
		if (nn.guard() != null) {
			DepParameterExpr processedWhere = (DepParameterExpr) nn.visitChild(nn.guard(), childtc);
			nn = (X10MethodDecl) nn.guard(processedWhere);

			VarChecker ac = new VarChecker(childtc.job(), Globals.TS(), Globals.NF());
			ac = (VarChecker) ac.context(childtc.context());
			processedWhere.visit(ac);

			if (ac.error != null) {
				Errors.issue(ac.job(), ac.error, this);
			}

			// Now build the new formal arg list.
			// TODO: Add a marker to the TypeChecker which records
			// whether in fact it changed the type of any formal.
			List<Formal> formals = processedFormals;

			//List newFormals = new ArrayList(formals.size());
			X10ProcedureDef pi = (X10ProcedureDef) nn.memberDef();
			CConstraint c = pi.guard().get();
			try {
				if (c != null) {
					c = c.copy();

					for (Formal n : formals) {
						Ref<Type> ref = (Ref<Type>) n.type().typeRef();
						Type newType =  ref.get();

						// Fold the formal's constraint into the guard.
						XVar var = xts.xtypeTranslator().trans(n.localDef().asInstance());
						CConstraint dep = X10TypeMixin.xclause(newType);
						if (dep != null) {
							dep = dep.copy();
							dep = dep.substitute(var, c.self());
							/*
                            XPromise p = dep.intern(var);
                            dep = dep.substitute(p.term(), c.self());
							 */
							c.addIn(dep);
						}

						ref.update(newType);
					}
				}

				// Report.report(1, "X10MethodDecl_c: typeoverride mi= " + nn.methodInstance());

				// Fold this's constraint (the class invariant) into the guard.
				{
					Type t =  tc.context().currentClass();
					CConstraint dep = X10TypeMixin.xclause(t);
					if (c != null && dep != null) {
						XVar thisVar = ((X10MemberDef) methodDef()).thisVar();
						if (thisVar != null)
							dep = dep.substitute(thisVar, c.self());
						//                                  dep = dep.copy();
						//                                  XPromise p = dep.intern(xts.xtypeTranslator().transThis(t));
						//                                  dep = dep.substitute(p.term(), c.self());
						c.addIn(dep);
					}
				}
			}
			catch (XFailure e) {
				tc.errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), position());
				c = null;
			}

			// Check if the guard is consistent.
			if (c != null && ! c.consistent()) {
				Errors.issue(tc.job(),
						new Errors.DependentClauseIsInconsistent("method", guard),
						this);
				// FIXME: [IP] mark constraint clause as invalid
			}
		}


		// Step II. Check the return type. 
		// Now visit the returntype to ensure that its depclause, if any is processed.
		// Visit the formals so that they get added to the scope .. the return type
		// may reference them.
		//TypeChecker tc1 = (TypeChecker) tc.copy();
		// childtc will have a "wrong" mi pushed in, but that doesnt matter.
		// we simply need to push in a non-null mi here.
		TypeChecker childtc1 = (TypeChecker) tc.enter(parent, nn);
		if (childtc1.context() == tc.context())
			childtc1 = (TypeChecker) childtc1.context((Context) tc.context().copy());
		// Add the type params and formals to the context.
		nn.visitList(nn.typeParameters(),childtc1);
		nn.visitList(nn.formals(),childtc1);
		(( X10Context ) childtc1.context()).setVarWhoseTypeIsBeingElaborated(null);
		{ 
			final TypeNode r = (TypeNode) nn.visitChild(nn.returnType(), childtc1);
			nn = (X10MethodDecl) nn.returnType(r);
			Type type = PlaceChecker.ReplaceHereByPlaceTerm(r.type(), ( X10Context ) childtc1.context());
			((Ref<Type>) nn.methodDef().returnType()).update(r.type());

			if (hasType != null) {
				final TypeNode h = (TypeNode) nn.visitChild(((X10MethodDecl_c) nn).hasType, childtc1);
				Type hasType = PlaceChecker.ReplaceHereByPlaceTerm(h.type(), ( X10Context ) childtc1.context());
				nn = (X10MethodDecl) ((X10MethodDecl_c) nn).hasType(h);
				if (! Globals.TS().isSubtype(type, hasType, tc.context())) {
					Errors.issue(tc.job(),
							new Errors.TypeIsNotASubtypeOfTypeBound(type, hasType, position()));
				}
			}
			// Check the offer type
			if (offerType != null) {
				final TypeNode o = (TypeNode) nn.visitChild(((X10MethodDecl_c) nn).offerType, childtc1);
				nn = (X10MethodDecl) ((X10MethodDecl_c) nn).offerType(o);		
				((X10MethodDef) nn.methodDef()).setOfferType(Types.ref(o.type()));
			} 
		}


		// Report.report(1, "X10MethodDecl_c: typeoverride mi= " + nn.methodInstance());
		// Step III. Check the body. 
		// We must do it with the correct mi -- the return type will be
		// checked by return e; statements in the body, and the offerType by offer e; statements in the body.

		TypeChecker childtc2 = (TypeChecker) tc.enter(parent, nn); // this will push in the right mi.
		// Add the type params and formals to the context.
		nn.visitList(nn.typeParameters(),childtc2);
		nn.visitList(nn.formals(),childtc2);
		//Report.report(1, "X10MethodDecl_c: after visiting formals " + childtc2.context());
		// Now process the body.
		nn = (X10MethodDecl) nn.body((Block) nn.visitChild(nn.body(), childtc2));
		nn = (X10MethodDecl) childtc2.leave(parent, old, nn, childtc2);

		if (nn.returnType() instanceof UnknownTypeNode) {
			NodeFactory nf = tc.nodeFactory();
			TypeSystem ts = tc.typeSystem();
			// Body had no return statement.  Set to void.
			Type t;
			if (! (((Ref<Type>) nn.returnType().typeRef()).getCached() instanceof UnknownType)) {
				t = ((Ref<Type>) nn.returnType().typeRef()).getCached();
			}
			else {
				t = ts.Void();
			}
			((Ref<Type>) nn.returnType().typeRef()).update(t);
			nn = (X10MethodDecl) nn.returnType(nf.CanonicalTypeNode(nn.returnType().position(), t));
		}

		return nn;
	}

	private static final Collection<String> TOPICS = 
		CollectionUtil.list(Report.types, Report.context);

	public Node visitChildren(NodeVisitor v) {
		X10MethodDecl_c n = (X10MethodDecl_c) super.visitChildren(v);
		TypeNode ht  = (TypeNode) n.visitChild(this.hasType, v);
		n = n.hasType(ht);
		TypeNode ot  = (TypeNode) n.visitChild(this.offerType, v);
		n = n.offerType(ot);
		return n;
	}
}