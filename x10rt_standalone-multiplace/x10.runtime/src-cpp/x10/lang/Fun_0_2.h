#ifndef X10_LANG_FUN_0_2_H
#define X10_LANG_FUN_0_2_H

#include <x10aux/config.h>
#include <x10aux/RTT.h>
#include <x10aux/fun_utils.h>

#include <x10/lang/Any.h>

namespace x10 {
    namespace lang {

        void _initRTTHelper_Fun_0_2(x10aux::RuntimeType *location,
                                    const x10aux::RuntimeType *rtt0,
                                    const x10aux::RuntimeType *rtt1,
                                    const x10aux::RuntimeType *rtt2);

        template<class P1, class P2, class R> class Fun_0_2 : public x10aux::AnyFun {
            public:
            RTT_H_DECLS_INTERFACE

            template <class I> struct itable {
                itable(R(I::*apply)(P1,P2),
                       x10_boolean (I::*_m0__at)(x10aux::ref<x10::lang::Ref>),
                       x10_boolean (I::*_m1__at)(x10::lang::Place),
                       x10::lang::Place (I::*home)(),
                       x10aux::ref<x10::lang::String> (I::*toString)(),
                       x10aux::ref<x10::lang::String> (I::*typeName)()
                    ) : apply(apply), _m0__at(_m0__at), _m1__at(_m1__at), home(home), toString(toString), typeName(typeName) {}
                R (I::*apply)(P1,P2);
                x10_boolean (I::*_m0__at)(x10aux::ref<x10::lang::Ref>);
                x10_boolean (I::*_m1__at)(x10::lang::Place);
                x10::lang::Place (I::*home)();
                x10aux::ref<x10::lang::String> (I::*toString)();
                x10aux::ref<x10::lang::String> (I::*typeName)();
            };
        };

        template<class P1, class P2, class R> void Fun_0_2<P1,P2,R>::_initRTT() {
            rtt.canonical = &rtt;
            x10::lang::_initRTTHelper_Fun_0_2(&rtt, x10aux::getRTT<P1>(), x10aux::getRTT<P2>(),
                                                    x10aux::getRTT<R>());
        }

        template<class P1, class P2, class R> x10aux::RuntimeType Fun_0_2<P1,P2,R>::rtt;

        template<> class Fun_0_2<void, void, void> {
        public:
            static x10aux::RuntimeType rtt;
            static const x10aux::RuntimeType* getRTT() { return &rtt; }
        };
    }
}
#endif
// vim:tabstop=4:shiftwidth=4:expandtab