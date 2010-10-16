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

import polyglot.types.SemanticException;
import x10.ast.Expr;

/**
 * 
 * Interface for any synthesizers that can generate a statement
 *
 */
public interface IExprSynth {
    Expr genExpr() throws SemanticException;
}
