/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package x10.ast;

import java.util.*;

import polyglot.types.SemanticException;
import polyglot.util.*;
import polyglot.visit.ContextVisitor;
import polyglot.visit.PrettyPrinter;

/** 
 * A <code>StringLit</code> represents an immutable instance of a 
 * <code>String</code> which corresponds to a literal string in Java code.
 */
public class StringLit_c extends Lit_c implements StringLit
{
    protected String value;

    public StringLit_c(Position pos, String value) {
	super(pos);
	assert(value != null);
	this.value = value;
    }

    /** Get the value of the expression. */
    public String value() {
	return this.value;
    }

    /** Set the value of the expression. */
    public StringLit value(String value) {
	StringLit_c n = (StringLit_c) copy();
	n.value = value;
	return n;
    }

    /** Type check the expression. */
    public Node typeCheck(ContextVisitor tc) throws SemanticException {
        return type(tc.typeSystem().String());
    }

    public String toString() {
        if (StringUtil.unicodeEscape(value).length() > 11) {
            return "\"" + StringUtil.unicodeEscape(value).substring(0,8) + "...\"";
        }
                
	return "\"" + StringUtil.unicodeEscape(value) + "\"";
    }

    protected int MAX_LENGTH = 60;
 
    /** Write the expression to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        List<String> l = breakupString();

        // If we break up the string, parenthesize it to avoid precedence bugs.
        if (l.size() > 1) {
            w.write("(");
        }

        w.begin(0);

        for (Iterator<String> i = l.iterator(); i.hasNext(); ) {
            String s = i.next();

            w.write("\"");
            w.write(StringUtil.escape(s));
            w.write("\"");

            if (i.hasNext()) {
                w.write(" +");
                w.allowBreak(0, " ");
            }
        }

        w.end();

        if (l.size() > 1) {
            w.write(")");
        }
    }

    /**
     * Break a long string literal into a concatenation of small string
     * literals.  This avoids messing up the pretty printer and editors. 
     */
    protected List<String> breakupString() {
        List<String> result = new LinkedList<String>();
        int n = value.length();
        int i = 0;

        while (i < n) {
            int j;

            // Compensate for the unicode transformation by computing
            // the length of the encoded string.
            int len = 0;

            for (j = i; j < n; j++) {
                char c = value.charAt(j);
                int k = StringUtil.unicodeEscape(c).length();
                if (len + k > MAX_LENGTH) break;
                len += k;
            }

            result.add(value.substring(i, j));

            i = j;
        }

        if (result.isEmpty()) {
            // This should only happen when value == "".
            if (! value.equals("")) {
                throw new InternalCompilerError("breakupString failed");
            }
            result.add(value);
        }

        return result;
    }
    
    public Object constantValue() {
	return value;
    }
    

}
