/*
 * Created on Sep 29, 2004
 */
package polyglot.ext.x10.ast;

import polyglot.ast.Stmt;
import polyglot.ast.Expr;
import polyglot.ast.Block;

/** The node constructed for the X10 construct atomic(P) {S}.
 * @author Christian Grothoff
 */
public interface Atomic extends Stmt {
    
    /** Set the Atomic's body */
    Atomic body(Stmt body);

    /** Get the body of the Atomic. */
    Stmt body();

    Expr place();
    
    Atomic place(Expr place);
}
