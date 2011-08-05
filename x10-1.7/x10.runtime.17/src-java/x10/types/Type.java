/*
 *
 * (C) Copyright IBM Corporation 2006-2008.
 *
 *  This file is part of X10 Language.
 *
 */

package x10.types;

import x10.core.fun.Fun_0_1;
import x10.core.fun.Fun_0_2;
import x10.constraint.XConstraint;

public interface Type<T> {
    boolean instanceof$(Object o);
    
    boolean equals(Object o);
    boolean isSubtype(Type<?> o);
    XConstraint getConstraint();
    
    /** Type-specific operators.  Will throw UnsupportedOperationException if the type does not support the given operator. */
    Fun_0_1<T,T>   absOperator();

    Fun_0_2<T,T,T> maxOperator();
    Fun_0_2<T,T,T> minOperator();

    Fun_0_1<T,T>   posOperator();
    Fun_0_1<T,T>   negOperator();
    Fun_0_2<T,T,T> addOperator();
    Fun_0_2<T,T,T> subOperator();
    Fun_0_2<T,T,T> mulOperator();
    Fun_0_2<T,T,T> divOperator();
    Fun_0_2<T,T,T> modOperator();
    Fun_0_1<T,T>   scaleOperator(int k);

    Fun_0_1<T,T>   notOperator();
    Fun_0_1<T,T>   invOperator();
    Fun_0_2<T,T,T> andOperator();
    Fun_0_2<T,T,T> orOperator();
    Fun_0_2<T,T,T> xorOperator();

    T minValue();
    T maxValue();

    T zeroValue();
    T unitValue();

    Object makeArray(int length);
    T setArray(Object array, int i, T v);
    T getArray(Object array, int i);
    int arrayLength(Object array);

    Class<?> getJavaClass();
}