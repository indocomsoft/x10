/*
 *
 * (C) Copyright IBM Corporation 2006
 *
 *  This file is part of X10 Language.
 *
 */
/**
 * 
 */
package polyglot.ext.x10.ast;

import polyglot.ast.Instanceof;

/**
 * @author vj
 *
 */
public interface DepInstanceof extends Instanceof {
	DepParameterExpr dep();
}