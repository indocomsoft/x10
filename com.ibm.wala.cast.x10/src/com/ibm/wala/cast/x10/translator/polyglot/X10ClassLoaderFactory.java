/*
 * Created on Apr 28, 2006
 */
package com.ibm.domo.ast.x10.translator.polyglot;

import java.io.IOException;

import com.ibm.domo.ast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.domo.ast.java.translator.polyglot.IRTranslatorExtension;
import com.ibm.domo.ast.java.translator.polyglot.PolyglotClassLoaderFactory;
import com.ibm.domo.ast.java.translator.polyglot.PolyglotSourceLoaderImpl;
import com.ibm.domo.classLoader.ClassLoaderImpl;
import com.ibm.domo.classLoader.IClassLoader;
import com.ibm.domo.ipa.callgraph.AnalysisScope;
import com.ibm.domo.ipa.callgraph.impl.SetOfClasses;
import com.ibm.domo.ipa.cha.ClassHierarchy;
import com.ibm.domo.types.ClassLoaderReference;
import com.ibm.domo.util.warnings.WarningSet;

public class X10ClassLoaderFactory extends PolyglotClassLoaderFactory {

    public X10ClassLoaderFactory(SetOfClasses exclusions, WarningSet warnings, IRTranslatorExtension extInfo) {
	super(exclusions, warnings, extInfo);
    }

    protected IClassLoader makeNewClassLoader(ClassLoaderReference classLoaderReference, ClassHierarchy cha, IClassLoader parent, AnalysisScope scope) throws IOException {
 	if (classLoaderReference.equals(JavaSourceAnalysisScope.SOURCE_REF)) {
	    ClassLoaderImpl cl = new X10SourceLoaderImpl(classLoaderReference, parent, getExclusions(), cha, getWarnings(), fExtInfo);
	    cl.init( scope.getModules( classLoaderReference ));
	    return cl;
	} else {
	    return super.makeNewClassLoader(classLoaderReference, cha, parent, scope);
	}
    }
}
