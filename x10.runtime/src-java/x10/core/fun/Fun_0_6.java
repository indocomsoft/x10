/*
 *
 * (C) Copyright IBM Corporation 2006-2008.
 *
 *  This file is part of X10 Language.
 *
 */

package x10.core.fun;

import x10.types.RuntimeType;
import x10.types.Type;

public interface Fun_0_6<T1,T2,T3,T4,T5,T6,U> {
    U apply(T1 o1, T2 o2, T3 o3, T4 o4, T5 o5, T6 o6);
    Type<?> rtt_x10$lang$Fun_0_6_Z1();
    Type<?> rtt_x10$lang$Fun_0_6_Z2();
    Type<?> rtt_x10$lang$Fun_0_6_Z3();
    Type<?> rtt_x10$lang$Fun_0_6_Z4();
    Type<?> rtt_x10$lang$Fun_0_6_Z5();
    Type<?> rtt_x10$lang$Fun_0_6_Z6();
    Type<?> rtt_x10$lang$Fun_0_6_U();

    public static class RTT extends RuntimeType<Fun_0_6<?,?,?,?,?,?,?>>{
        Type<?> T1;
        Type<?> T2;
        Type<?> T3;
        Type<?> T4;
        Type<?> T5;
        Type<?> T6;
        Type<?> U;

        public RTT(Type<?> T1, Type<?> T2, Type<?> T3, Type<?> T4, Type<?> T5, Type<?> T6, Type<?> U) {
            super(Fun_0_6.class);
            this.T1 = T1;
            this.T2 = T2;
            this.T3 = T3;
            this.T4 = T4;
            this.T5 = T5;
            this.T6 = T6;
            this.U = U;
        }

        @Override
        public boolean instanceof$(Object o) {
            if (o instanceof Fun_0_6) {
                Fun_0_6<?,?,?,?,?,?,?> v = (Fun_0_6<?,?,?,?,?,?,?>) o;
                if (! v.rtt_x10$lang$Fun_0_6_U().isSubtype(U)) return false; // covariant
                if (! T1.isSubtype(v.rtt_x10$lang$Fun_0_6_Z1())) return false; // contravariant
                if (! T2.isSubtype(v.rtt_x10$lang$Fun_0_6_Z2())) return false; // contravariant
                if (! T3.isSubtype(v.rtt_x10$lang$Fun_0_6_Z3())) return false; // contravariant
                if (! T4.isSubtype(v.rtt_x10$lang$Fun_0_6_Z4())) return false; // contravariant
                if (! T5.isSubtype(v.rtt_x10$lang$Fun_0_6_Z5())) return false; // contravariant
                if (! T6.isSubtype(v.rtt_x10$lang$Fun_0_6_Z6())) return false; // contravariant
                return true;
            }
            return false;
        }

        @Override
        public boolean isSubtype(Type<?> o) {
            if (! super.isSubtype(o))
                return false;
            if (o instanceof Fun_0_6.RTT) {
                Fun_0_6.RTT t = (RTT) o;
                return U.isSubtype(t.U)
                        && t.T1.isSubtype(T1)
                        && t.T2.isSubtype(T2)
                        && t.T3.isSubtype(T3)
                        && t.T4.isSubtype(T4)
                        && t.T5.isSubtype(T5)
                        && t.T6.isSubtype(T6);
            }
            return false;
        }
    }
}
