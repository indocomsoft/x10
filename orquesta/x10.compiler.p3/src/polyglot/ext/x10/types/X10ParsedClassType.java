/*
 *
 * (C) Copyright IBM Corporation 2006
 *
 *  This file is part of X10 Language.
 *
 */
/*
 * Created on Nov 30, 2004
 */
package polyglot.ext.x10.types;

import java.util.List;

import polyglot.types.ParsedClassType;
import polyglot.types.Type;

/**
 * @author vj
 *
 *
 */
public interface X10ParsedClassType extends ParsedClassType, X10ClassType, X10NamedType {

    X10ParsedClassType typeArguments(List<Type> typeArgs);
    
    /** Returns true iff superClassRoot() equals ts.Object().
     * @return
     */
    boolean isJavaType();
}
