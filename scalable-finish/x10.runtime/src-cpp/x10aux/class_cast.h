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

#ifndef X10AUX_CLASS_CAST_H
#define X10AUX_CLASS_CAST_H

#include <x10aux/config.h>
#include <x10aux/throw.h>

#include <x10aux/RTT.h>
#include <x10aux/ref.h>
#include <x10aux/basic_functions.h>

#include <x10/lang/Reference.h>
#include <x10/lang/IBox.h>

namespace x10aux {

    /*
     * Throughout this file:
     *  T stands for "to"
     *  F stands for "from"
     */
    
    extern void throwClassCastException() X10_PRAGMA_NORETURN;
    
    template<typename T, typename F> GPUSAFE T class_cast(F obj);
    template<typename T, typename F> GPUSAFE T class_cast(F obj, bool checked);

    template<class T> static GPUSAFE ref<T> real_class_cast(ref<x10::lang::Reference> obj, bool checked) {
        if (obj == x10aux::null) {
            // NULL passes any class cast check and remains NULL
            _CAST_("Special case: null gets cast to "<<TYPENAME(ref<T>));
            return obj;
        }
        if (checked) {
            const RuntimeType *from = obj->_type();
            const RuntimeType *to = getRTT<ref<T> >();
            #ifndef NO_EXCEPTIONS
            _CAST_(from->name()<<" to "<<to->name());
            if (!from->subtypeOf(to)) {
                throwClassCastException();
            }
            #else
            (void) from; (void) to;
            _CAST_("UNCHECKED! "<<from->name()<<" to "<<to->name());
            #endif
        }
        return static_cast<ref<T> >(obj);
    }

    // ClassCastNotPrimitive
    template<class T, class F> struct ClassCastNotPrimitive { static GPUSAFE T _(F obj, bool checked) {
        // If we get here, then we are doing a ref==>struct or struct==>ref, which is not allowed in X10 2.0.
        throwClassCastException();
        return NULL;
    } };

    template<class T, class F> struct ClassCastNotPrimitive<ref<T>,ref<F> > {
        static GPUSAFE ref<T> _(ref<F> obj, bool checked) {
            _CAST_("Ref to ref cast "<<TYPENAME(F)<<" to "<<TYPENAME(T));
            return real_class_cast<T>(obj, checked);
        }
    };

    template<class T, class F> struct ClassCastNotPrimitive<ref<T>,F> {
        static GPUSAFE ref<T> _(F val, bool checked) {
            _CAST_("Struct to ref cast "<<TYPENAME(F)<<" to "<<TYPENAME(T));
            if (checked) {
                const RuntimeType *from = getRTT<F>();
                const RuntimeType *to = getRTT<ref<T> >();
                #ifndef NO_EXCEPTIONS
                _CAST_(from->name()<<" to "<<to->name());
                if (!from->subtypeOf(to)) {
                    throwClassCastException();
                }
                #else
                (void) from; (void) to;
                _CAST_("UNCHECKED! "<<from->name()<<" to "<<to->name());
                #endif
            }
            x10aux::ref<x10::lang::IBox<F> > obj = new (x10aux::alloc<x10::lang::IBox<F> >()) x10::lang::IBox<F>(val);
            return obj;
        }
    };
    
    template<class T, class F> struct ClassCastNotPrimitive<T,ref<F> > {
        static GPUSAFE T _(ref<F> val, bool checked) {
            _CAST_("Ref to struct cast "<<TYPENAME(F)<<" to "<<TYPENAME(T));
            if (val == x10aux::null) {
                // NULL cannot be cast to a struct.
                _CAST_("Special case: null cannot be cast to "<<TYPENAME(T));
                throwClassCastException();
            }
            if (checked) {
                x10aux::ref<x10::lang::Reference> asRef = val;
                const RuntimeType *from = asRef->_type();
                const RuntimeType *to = getRTT<T>();
                #ifndef NO_EXCEPTIONS
                _CAST_(from->name()<<" to "<<to->name());
                if (!from->subtypeOf(to)) {
                    throwClassCastException();
                }
                #else
                (void) from; (void) to;
                _CAST_("UNCHECKED! "<<from->name()<<" to "<<to->name());
                #endif
            }
            x10aux::ref<x10::lang::IBox<T> > ibox = val;
            return ibox->value; 
        }
    };
    
    // This is the second level that recognises primitive casts
    template<class T, class F> struct ClassCastPrimitive { static GPUSAFE T _(F obj, bool checked) {
        // if we get here it's not a primitive cast
        _CAST_("Not a primitive cast "<<TYPENAME(F)<<" to "<<TYPENAME(T));
        return ClassCastNotPrimitive<T,F>::_(obj, checked);
    } };

    #define PRIMITIVE_CAST(T,F) \
    template<> struct ClassCastPrimitive<T,F> { \
        static GPUSAFE T _ (F obj, bool checked) { \
            _CAST_(TYPENAME(F) <<" converted to "<<TYPENAME(T)); \
            return static_cast<T>(obj); \
        } \
    }

    // make reflexive
    #define PRIMITIVE_CAST2(T,F) PRIMITIVE_CAST(T,F) ; PRIMITIVE_CAST(F,T)

    // boolean can't be cast to anything except itself (handled below)
    // everything else is totally connected

    PRIMITIVE_CAST2(x10_byte,x10_short);
    PRIMITIVE_CAST2(x10_byte,x10_int);
    PRIMITIVE_CAST2(x10_byte,x10_long);
    PRIMITIVE_CAST2(x10_byte,x10_float);
    PRIMITIVE_CAST2(x10_byte,x10_double);
    PRIMITIVE_CAST2(x10_byte,x10_ubyte);
    PRIMITIVE_CAST2(x10_byte,x10_ushort);
    PRIMITIVE_CAST2(x10_byte,x10_uint);
    PRIMITIVE_CAST2(x10_byte,x10_ulong);

    PRIMITIVE_CAST2(x10_short,x10_int);
    PRIMITIVE_CAST2(x10_short,x10_long);
    PRIMITIVE_CAST2(x10_short,x10_float);
    PRIMITIVE_CAST2(x10_short,x10_double);
    PRIMITIVE_CAST2(x10_short,x10_ubyte);
    PRIMITIVE_CAST2(x10_short,x10_ushort);
    PRIMITIVE_CAST2(x10_short,x10_uint);
    PRIMITIVE_CAST2(x10_short,x10_ulong);

    PRIMITIVE_CAST2(x10_int,x10_long);
    PRIMITIVE_CAST2(x10_int,x10_float);
    PRIMITIVE_CAST2(x10_int,x10_double);
    PRIMITIVE_CAST2(x10_int,x10_ubyte);
    PRIMITIVE_CAST2(x10_int,x10_ushort);
    PRIMITIVE_CAST2(x10_int,x10_uint);
    PRIMITIVE_CAST2(x10_int,x10_ulong);

    PRIMITIVE_CAST2(x10_long,x10_float);
    PRIMITIVE_CAST2(x10_long,x10_double);
    PRIMITIVE_CAST2(x10_long,x10_ubyte);
    PRIMITIVE_CAST2(x10_long,x10_ushort);
    PRIMITIVE_CAST2(x10_long,x10_uint);
    PRIMITIVE_CAST2(x10_long,x10_ulong);

    PRIMITIVE_CAST2(x10_float,x10_double);
    PRIMITIVE_CAST2(x10_float,x10_ubyte);
    PRIMITIVE_CAST2(x10_float,x10_ushort);
    PRIMITIVE_CAST2(x10_float,x10_uint);
    PRIMITIVE_CAST2(x10_float,x10_ulong);

    PRIMITIVE_CAST2(x10_double,x10_ubyte);
    PRIMITIVE_CAST2(x10_double,x10_ushort);
    PRIMITIVE_CAST2(x10_double,x10_uint);
    PRIMITIVE_CAST2(x10_double,x10_ulong);

    PRIMITIVE_CAST2(x10_ubyte,x10_ushort);
    PRIMITIVE_CAST2(x10_ubyte,x10_uint);
    PRIMITIVE_CAST2(x10_ubyte,x10_ulong);

    PRIMITIVE_CAST2(x10_ushort,x10_uint);
    PRIMITIVE_CAST2(x10_ushort,x10_ulong);

    PRIMITIVE_CAST2(x10_uint,x10_ulong);


    // first level of template specialisation that recognises <T,T>
    // (needed because generic classes can be instantiated in ways that make casts redundant)
    template<class T, class F> struct ClassCast { static GPUSAFE T _ (F obj, bool checked) {
        return ClassCastPrimitive<T,F>::_(obj, checked);
    } };
    template<class T> struct ClassCast<T,T> { static GPUSAFE T _ (T obj, bool checked) {
        // nothing to do (until we have constraints)
        _CAST_("Identity cast to/from "<<TYPENAME(T));
        return obj;
    } };

    template<typename T, typename F> GPUSAFE T class_cast(F obj) {
        return ClassCast<T,F>::_(obj, true);
    }

    template<typename T, typename F> GPUSAFE T class_cast(F obj, bool checked) {
        return ClassCast<T,F>::_(obj, checked);
    }

    template<typename T, typename F> GPUSAFE T class_cast_unchecked(F obj) {
        return ClassCast<T,F>::_(obj, false);
    }
}

    

#endif
// vim:tabstop=4:shiftwidth=4:expandtab