/*
 *
 * (C) Copyright IBM Corporation 2006-2008.
 *
 *  This file is part of X10 Language.
 *
 */

package x10.lang;

import x10.compiler.Native;
import x10.compiler.NativeRep;

@NativeRep("java", "x10.core.Settable<#1, #2>", null, null)
public interface Settable[-I,V] {
    @Native("java", "(#0).set(#1, #2)")
    def set(v: V, i: I): V;
}