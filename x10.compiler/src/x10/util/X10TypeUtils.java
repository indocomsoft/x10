package x10.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.ast.Block;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.FlagsNode;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.LocalDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Receiver;
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.types.ClassDef;
import polyglot.types.ContainerType;
import polyglot.types.Context;
import polyglot.types.FieldDef;
import polyglot.types.Flags;
import polyglot.types.LocalDef;
import polyglot.types.Name;
import polyglot.types.QName;
import polyglot.types.Ref;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.Types;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import x10.ast.X10ClassDecl_c;
import x10.ast.X10LocalDecl_c;
import x10.ast.X10MethodDecl;
import x10.types.MethodInstance;
import x10.types.X10ClassDef;
import x10.types.X10CodeDef;
import x10.types.X10ConstructorDef_c;
import x10.types.X10ConstructorInstance;
import x10.types.X10FieldDef;
import x10.types.X10LocalDef;
import x10.types.X10LocalInstance;
import x10.types.X10MethodDef;
import x10.types.X10MethodDef_c;
import x10.types.X10ParsedClassType_c;

public class X10TypeUtils {
	
    public static Type lookUpType(TypeSystem ts, QName qname) {
    	try {
			return ts.systemResolver().findOne(qname);
		} catch (SemanticException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
	
	public static X10LocalDef  findLocalDef(ContextVisitor tc, Name id) {
		try {
			X10LocalInstance local = tc.context().findLocal(id);
			return local.x10Def();
		} catch (SemanticException e) {
			return null;
		}
	}
	
	public static X10FieldDef findFieldDef(ContextVisitor tc, Name id) {
		ClassDef type = tc.context().currentClassDef();
        ArrayList<FieldDef> l = new ArrayList<FieldDef>(type.fields());
        for (int i = 0; i < l.size(); i++) {
            FieldDef fi = (FieldDef) l.get(i);
            if(fi.name().equals(id)) {
            	return (X10FieldDef) fi;
            }
        }
        return null;
	}
	
	public static List<String> getAllFormalNames(X10CodeDef currentCode) {
    	List<String> formalNames = new LinkedList<String>();
    	
    	if(currentCode instanceof X10MethodDef_c) {
    		X10MethodDef_c methodDef = (X10MethodDef_c)currentCode;
    		for(LocalDef def: methodDef.formalNames()) {
    			formalNames.add(def.name().toString());
    		}
    	} else if (currentCode instanceof X10ConstructorDef_c) {
    		X10ConstructorDef_c constructorDef = (X10ConstructorDef_c)currentCode;
    		for(LocalDef def : constructorDef.formalNames()) {
    			formalNames.add(def.name().toString());
    		}
    	}
    	
    	return formalNames;
    }
	
	public static List<Type> getAllFormalTypes(X10CodeDef currentCode) {
		List<Type> formalTypes = new LinkedList<Type>();
		
		if(currentCode instanceof X10MethodDef_c) {
    		X10MethodDef_c methodDef = (X10MethodDef_c)currentCode;
    		for(LocalDef def: methodDef.formalNames()) {
    			formalTypes.add(def.type().get());
    		}
    	} else if (currentCode instanceof X10ConstructorDef_c) {
    		X10ConstructorDef_c constructorDef = (X10ConstructorDef_c)currentCode;
    		for(LocalDef def : constructorDef.formalNames()) {
    			formalTypes.add(def.type().get());
    		}
    	}
		
		return formalTypes;
	}
    
    public static FieldDecl createFieldDecl(NodeFactory nf, TypeSystem ts, Position pos,
    		Flags flags, Type fieldType, Name fieldName,
    		ContainerType containerType, Expr initExpr) {
		Id fieldId = nf.Id(pos, fieldName);
		TypeNode lockTypeNode = nf.CanonicalTypeNode(pos, fieldType);
		X10FieldDef fd = ts.fieldDef(pos, Types.ref(containerType), flags,
					Types.ref(fieldType), fieldName);	
		FieldDecl fDecl = nf.FieldDecl(pos, nf.FlagsNode(pos, flags),
					lockTypeNode, fieldId, initExpr).fieldDef(fd);
		return fDecl;
    }
	
    public static X10MethodDecl createX10MethodDecl(NodeFactory nf, TypeSystem ts, Position pos,
    		Flags flags, ContainerType containerType, Type retType,
    		Name methodName, List<Stmt> bodyStatements) {
		Id methodId = nf.Id(pos, methodName);
		TypeNode retTypeNode = nf.CanonicalTypeNode(pos, retType);
		X10MethodDef methodDef = ts.methodDef(pos, Types.ref(containerType),
		           flags, Types.ref(retType), methodName,
		            Collections.<Ref<? extends Type>>emptyList(),  null);
		Block body = nf.Block(pos, bodyStatements);
		X10MethodDecl methodDecl = nf.MethodDecl(pos, nf.FlagsNode(pos, flags),
					retTypeNode, methodId, Collections.<Formal>emptyList(), body).methodDef(methodDef);
		return methodDecl;
    }
    
    public static LocalDecl createLocalDecl(NodeFactory nf, TypeSystem ts, Position pos,
    		Flags flags, Type localType, Name localName, Expr init) {
    	final LocalDef li = ts.localDef(pos, flags, Types.ref(localType), localName);
        final Id varId = nf.Id(pos, localName);
        final LocalDecl ld = nf.LocalDecl(pos, nf.FlagsNode(pos, flags),
        		nf.CanonicalTypeNode(pos, localType), varId, init).
        		localDef(li);
        return ld;
    }
    
    public static New createNewObjectByDefaultConstructor(NodeFactory nf, TypeSystem ts, Context context,
    		Position pos, Type clazzType) {
        TypeNode typeNode = nf.CanonicalTypeNode(pos, clazzType);
        X10ConstructorInstance ci = findDefaultConstructor(ts, context, clazzType);
	    New newInstance = (New) nf.New(pos, typeNode,
			Collections.<Expr>emptyList()).constructorInstance(ci).type(clazzType);
	    return newInstance;
    }
    
    public static X10ConstructorInstance findDefaultConstructor(TypeSystem ts, Context context, Type t) {
    	try {
			return ts.findConstructor(t, ts.ConstructorMatcher(t, Collections.<Type>emptyList(),
					context));
		} catch (SemanticException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    
    public static MethodInstance findMethod(TypeSystem ts, Context context, Type container, Name name, List<Type> argTypes) {
    	try {
			Collection<MethodInstance> collections = ts.findMethods(container, ts.MethodMatcher(container, name, 
					argTypes, context));
			assert collections.size() == 1;
			
			List<MethodInstance> mis = new LinkedList<MethodInstance>();
			mis.addAll(collections);
			assert mis.size() == 1;
			
			return mis.get(0);
		} catch (SemanticException e) {
			throw new RuntimeException(e);
		}
    }
    
    public static boolean hasStaticAtomicField(X10ClassDef clazzDef) {
    	for(FieldDef fdef : clazzDef.getAtomicFields()) {
    		if(fdef.flags().contains(Flags.STATIC)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static boolean hasNonStaticAtomicField(X10ClassDef clazzDef) {
    	for(FieldDef fdef : clazzDef.getAtomicFields()) {
    		if(!fdef.flags().contains(Flags.STATIC)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static List<Ref<? extends Type>> typesToTypeRefs(List<Type> types) {
    	List<Ref<? extends Type>> refs = new LinkedList<Ref<? extends Type>>();
    	for(Type t : types) {
    		refs.add(Types.ref(t));
    	}
    	return refs;
    }
    
    public static boolean hasFlag(FlagsNode flags, Flags f) {
    	return flags != null && flags.flags().contains(f);
    }
    

    
    public static boolean isPublic(Flags flags) {
    	return flags.contains(Flags.PUBLIC);
    }
    
    public static boolean isAtomic(Flags flags) {
    	return flags.contains(Flags.ATOMIC);
    }
    
    public static boolean isAtomicPlus(Flags flags) {
    	return flags.contains(Flags.ATOMICPLUS);
    }
    
    public static boolean isCompilerGenerated(Node node) {
    	return node.position().isCompilerGenerated();
    }
    
    public static boolean hasAtomic(FlagsNode flags) {
    	return hasFlag(flags, Flags.ATOMIC); //flags != null && flags.flags().contains(Flags.ATOMIC);
    }
    
    public static boolean isAtomicLocalDecl(X10LocalDecl_c localDecl) {
    	return hasAtomic(localDecl.flags()); // localDecl.flags() != null && localDecl.flags().flags().contains(Flags.ATOMIC);
    }
    
    //XXX an ugly hack here
    public static boolean isX10ArrayClass(Type c) {
    	return c.toString().startsWith("x10.array.Array");
    }

    
    private static String[] SKIPPED_CLASS_PACKAGES = new String[]{"x10."};
    public static boolean skipProcessingClass(Type type) {
    	String clazzFullName = type.fullName().toString();
    	return skipClassByName(clazzFullName);
    }
    public static boolean skipClassByName(String clazzFullName) {
    	for(String skipPkg : SKIPPED_CLASS_PACKAGES) {
    		if(clazzFullName.startsWith(skipPkg)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static boolean isSkippedOrPrimitiveType(Type t) {
    	return skipProcessingClass(t) || isPrimitiveType(t);
    }
    
    private static Set<String> primitiveTypes = new HashSet<String>();
    static {
    	primitiveTypes.add("int");
    	primitiveTypes.add("float");
    	primitiveTypes.add("double");
    	primitiveTypes.add("char");
    	primitiveTypes.add("short");
    	primitiveTypes.add("long");
    	primitiveTypes.add("boolean");
    	primitiveTypes.add("byte");
    	
    	primitiveTypes.add("x10.lang.Int");
    	primitiveTypes.add("x10.lang.Float");
    	primitiveTypes.add("x10.lang.Double");
    	primitiveTypes.add("x10.lang.Character");
    	primitiveTypes.add("x10.lang.Short");
    	primitiveTypes.add("x10.lang.Long");
    	primitiveTypes.add("x10.lang.Boolean");
    	primitiveTypes.add("x10.lang.Byte");
    }
    public static boolean isPrimitiveType(Type t) {
    	String fullTypeName = t.fullName().toString();
    	return primitiveTypes.contains(fullTypeName);
    }
}