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

package x10cpp.extension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import polyglot.frontend.ExtensionInfo;
import polyglot.types.ClassType;
import polyglot.types.MethodDef;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import x10.ast.ClassBody_c;
import x10.ast.ClassMember;
import x10.ast.Formal_c;
import x10.ast.Id;
import x10.ast.MethodDecl;
import x10.ast.MethodDecl_c;
import x10.ast.Node;
import x10.ast.NodeFactory;
import x10.extension.X10Ext_c;
import x10.types.X10Flags;
import x10.types.X10TypeSystem;


/**
 * Implementation of extern (previously known as native) calls.
 * Check class bodies for 'extern' keyword (aka native)
 * and generate approriate wrappers and stubs to support
 * the simplified JNI-like interface to native code from X10.
 *
 * @author donawa
 * @author igor
 */
public class X10ClassBodyExt_c extends X10Ext_c {

	private BufferedWriter wrapperFile;
	private X10TypeSystem typeSystem;

	private static final String EXTERN_STUB_SUFFIX = "_x10stub.c";
	String[] wrapperPrologue = {
			"/*Automatically generated -- DO NOT EDIT THIS FILE */\n",
			"#include <sys/types.h>\n",
			"#ifdef __cplusplus\n", "extern \"C\" {\n", "#endif\n", "" };

	String[] wrapperEpilogue = { "\n", "#ifdef __cplusplus\n", "}\n",
			"#endif\n" };

	private void generateWrapperPrologue() {
		try {
			for (int i = 0; i < wrapperPrologue.length; ++i) {
				wrapperFile.write(wrapperPrologue[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error("Problems writing to " + wrapperFile);
		}
	}

	private void generateWrapperEpilogue() {
		try {
			for (int i = 0; i < wrapperEpilogue.length; ++i) {
				wrapperFile.write(wrapperEpilogue[i]);
			}
			wrapperFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error("Problems with " + wrapperFile );
		}
	}

	/**
	 * Create a text file with a given name, one for each outermost class which
	 * contains x10 extern methods
	 * @param fileName
	 */
	private void createWrapperFile(String fileName, File output_dir) {
		Date timeStamp = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat();

		try {
			wrapperFile = new BufferedWriter(new FileWriter(new File(output_dir, fileName)));
			wrapperFile.write("/*\n * Filename:"+fileName +
					"\n * Generated: "+formatter.format(timeStamp)+" */\n");

		} catch (IOException e) {
			e.printStackTrace();
			throw new Error("Problems writing to "+wrapperFile);
		}
	}
/*
	private String typeToCType(Type theType) {
		if (theType.isPrimitive()) {
			return typeToCString(theType);
		}
		else if (theType.isArray()){
			return typeToCType(theType.toArray().base())+"*";
		}
		else // theType.isClass()
		{
			if (typeSystem.isX10Array(theType)) {
				Type base = typeSystem.baseType(theType);
				if (!base.isPrimitive())
					return "void**";
				return typeToCString(base)+"*";
			}
			return "void*";
		}
	}
	*/
	
	private String typeToCString(Type theType) {
		if (theType.isPrimitive()) {
			//System.out.println(theType.toString() + " is primitive");
			if (theType.isInt())
				return "signed int";
			if (theType.isChar())
				return "signed short";
			if (theType.isBoolean())
				return "unsigned char";
			if (theType.isByte())
				return "signed char";
			if (theType.isShort())
				return "signed short";
			if (theType.isLong())
				return "int64_t"; // best to use jni.h's defn
			if (theType.isFloat())
				return "float";
			if (theType.isDouble())
				return "double";
			if (theType.isVoid())
				return "void";
			throw new Error("Unexpected type" + theType.toString());
		}
		else {
			if(!theType.isArray()) throw new Error("Unexpected type"+theType.toString());
			Type baseType = theType.toArray().base();
			if(!baseType.isPrimitive()) throw new Error("Only primitive arrays are supported, not "+theType.toString());
			return typeToCString(baseType)+"Array";
			
		}
	}

	private String typeToJavaSigString(Type theType) {

		if (theType.isPrimitive()) {
			//System.out.println(theType.toString() + " is primitive");
		
			if (theType.isInt())
				return "I";
			if (theType.isChar())
				return "J";
			if (theType.isBoolean())
				return "Z";
			if (theType.isByte())
				return "B";
			if (theType.isShort())
				return "S";
			if (theType.isLong())
				return "J";
			if (theType.isFloat())
				return "F";
			if (theType.isDouble())
				return "D";
			if (theType.isVoid())
				return "V";
			throw new Error("Unexpected type" + theType.toString());
		} else {
			if (!theType.isArray())
				throw new Error("Only java arrays are supported, not "+theType.toString());
			Type baseType = theType.toArray().base();
			if (!baseType.isPrimitive())
				throw new Error("Only primitive arrays are supported, not "+theType.toString()); 
			return "["+typeToJavaSigString(baseType);
			
		}
	}

	/*
	private String generateJavaSignature(MethodDecl_c method) {
		String signature = "";
		for (ListIterator i = method.formals().listIterator(); i.hasNext();) {
			Formal_c parameter = (Formal_c) i.next(); 
			Type type = parameter.declType();
			if (type.isPrimitive() || type.isArray())
				signature += typeToJavaSigString(type);
			else {
				// assume this is an X10 array object.  Determine backing array type and add
				// descriptor signature
				Type base = typeSystem.baseType(type);
				signature += typeToJavaSigString(typeSystem.arrayOf(parameter.position(), base));
				signature += typeToJavaSigString(typeSystem.arrayOf(parameter.position(), typeSystem.Int()));
				
			}
		}
		return signature;
	}
	*/

	private static final String zeros = "0000";
	/**
	 * replace '_' with '_1'
	 *         '<unicode>' with '_0<unicode>'
	 *         ';' with '_2'
	 *         '[' with '_3'
	 * and strip out anything within "/*" and "*\/"
	 * assume '*' and '/' are illegal characters
	 * @param inName
	 * @return
	 */
	private static String JNImangle(String inName) {
		StringBuffer buffer = new StringBuffer(inName.length());
		boolean inCommentMode=false;
		char lastChar='a';
		for (int i = 0; i < inName.length(); ++i) {
			char ch = inName.charAt(i);

			if(inCommentMode){
				if (ch == '/' && lastChar == '*')
					inCommentMode =false;
			}
			else
				switch (ch) {
				case '/': /* do not record */
					break;
				case '*':
					if(lastChar == '/')
						inCommentMode=true;
					break;
				case '_':
					buffer.append("_1");
					break;
				case ';':
					buffer.append("_2");
					break;
				case '[':
					buffer.append("_3");
					break;
				case '.':
					buffer.append("_");
					break;
				default:
					if (Character.isLetterOrDigit(ch))
						buffer.append(ch);
					else {
						String hex = Integer.toHexString((int)ch);
						hex = zeros.substring(hex.length()) + hex;
						buffer.append("_0").append(hex);
					}
					break;
				}
			lastChar=ch;
		}
		if (false) System.out.println("convert from "+inName+" to "+buffer.toString());
		return buffer.toString();
	}

	/**
	 * Convert the input type to a canonical fully-qualified string,
	 * with '.'s separating packages, and '$'s separating nested classes.
	 * @param t the type (must be a class type)
	 */
	private static String canonicalTypeString(Type t) {
		String s = "";
		ClassType cl = t.toClass();
		for (; cl.isNested(); cl = cl.outer()) {
			if (cl.isAnonymous())
				throw new RuntimeException("Anonymous inner classes not supported yet");
			s = "$" + cl.name() + s;
		}
		return cl.fullName() + s;
	}

	public static String generateX10NativeName(MethodDecl_c method) {
		return JNImangle(canonicalTypeString(method.methodDef().container().get())) + "_" + method.name();
	}

	/**
	 * Create C stub that user will later compile into a dynamic library
	 * contains JNI signature C code which calls the expected X10 routine
	 * @param nativeMethod
	 * @param isOverloaded
	 */
	/*
	private void generateStub(MethodDecl_c nativeMethod, boolean isOverloaded) {

		String _newName = generateX10NativeName(nativeMethod);
		if (isOverloaded)
			_newName += "__" + JNImangle(generateJavaSignature(nativeMethod));

		String wrapperDecl = "extern " + typeToCType(nativeMethod.methodDef().returnType().get()) + " ";
		wrapperDecl += _newName + "(";

		for (ListIterator i = nativeMethod.formals().listIterator(); i.hasNext(); ) {
			Formal_c parameter = (Formal_c) i.next();
			Type type = parameter.declType();
			if (type.isPrimitive()) { 
				wrapperDecl += typeToCType(type);
			} else if (type.isArray()) {
				wrapperDecl += typeToCType(type.toArray().base()) + "*";
			} else { // X10 array
				Type base = typeSystem.baseType(type);
				Type aType = typeSystem.arrayOf(parameter.position(), base);
				wrapperDecl += typeToCType(aType);
				// if we see an array type there must be a descriptor right after
				wrapperDecl += ", int*";  
			}
			if (i.hasNext())
				wrapperDecl += ", ";
		}
		wrapperDecl += ");\n";

		try {
			wrapperFile.write("\n"+wrapperDecl);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error("Problems writing file");
		}
	}
	*/

	private static int containingClassDepth = 0; // use to create stub file for outermost class w/ natives

	/**
	 * Identify native (aka extern) x10 methods and create a wrapper with
	 * the same name.  The wrapper makes a JNI call to a routine which
	 * then calls the expected X10 native call.
	 * e.g.
	 * <code>
	 * class C {
	 *   static int extern foo(int x);
	 * }
	 * </code>
	 * would result in java code
	 * <code>
	 * class C {
	 *   static int native C_foo(int x);
	 *   static int foo(int x) { return C_foo(x); }
	 * }
	 * </code>
	 *
	 * <code>C_foo</code> is a native C call, which would end up looking like
	 * <code>int Java_C_C_1foo(int x) { return C_foo(x); }</code>
	 * Stub files for each containing class are generated containing
	 * the C wrappers.  It is up to the user to compile these, along
	 * with the actual native implementation of <code>C_foo(int)</code>, into
	 * a dynamic library, and ensure that the X10 program can find them
	 */
	/*
	public Node rewrite(X10TypeSystem ts, NodeFactory nf, ExtensionInfo info) {
		typeSystem = ts;
		boolean seenNativeMethodDecl = false;

		ClassBody_c cb = (ClassBody_c) node();
		List members = cb.members();
		Map methodHash = null;
		for (ListIterator i = members.listIterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof MethodDecl) {
				MethodDecl_c md = (MethodDecl_c) o;
				MethodDef mi = md.methodDef();

				if (!mi.flags().isNative()) {
					continue;
				}

				if (!seenNativeMethodDecl) {
					// JNI signature changes depends on whether the method
					// is overloaded or not.  Determine that by scanning
					// through and hashing all native method names
					methodHash = buildNativeMethodHash(members);
					if (0 == containingClassDepth++) {
						createWrapperFile(wrapperFileName(mi.container()),
										  info.getOptions().output_directory);
						generateWrapperPrologue();
					}
					seenNativeMethodDecl = true;
				}

				boolean isOverLoaded = (null != methodHash.get(md.name()));

				generateStub(md, isOverLoaded);
			}
		}

		if (seenNativeMethodDecl) {
			--containingClassDepth;
			if (0 == containingClassDepth)
				generateWrapperEpilogue();
		}

		return cb;
	}
	*/

	/**
	 * Create a stub file name for the type with suffix _x10stub.c
	 * @param type
	 */
	public static String wrapperFileName(ReferenceType type) {
		return JNImangle(canonicalTypeString(type)) + EXTERN_STUB_SUFFIX;
	}

	private Map<Id, MethodDecl> buildNativeMethodHash(List<ClassMember> members) {
		Map<Id, MethodDecl> methodHash = new HashMap<Id, MethodDecl>();
		for (ListIterator<ClassMember> j = members.listIterator(); j.hasNext();) {
			Object theObj = j.next();
			if (!(theObj instanceof MethodDecl))
				continue;
			MethodDecl methodDecl = (MethodDecl) theObj;
			if (!X10Flags.isExtern(methodDecl.methodDef().flags()))
				continue;

			if (methodHash.containsKey(methodDecl.name())) {
				methodHash.put(methodDecl.name(), methodDecl); // more than one instance
			} else {
				methodHash.put(methodDecl.name(), null);
			}
		}
		return methodHash;
	}
}

