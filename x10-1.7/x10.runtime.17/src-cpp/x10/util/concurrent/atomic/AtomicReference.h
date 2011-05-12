#ifndef X10_UTIL_CONCURRENT_ATOMIC_ATOMICREFERENCE_H
#define X10_UTIL_CONCURRENT_ATOMIC_ATOMICREFERENCE_H

#include <x10aux/config.h>
#include <x10aux/ref.h>
#include <x10aux/RTT.h>
#include <x10aux/string_utils.h>
#include <x10aux/atomic_ops.h>

#include <x10/lang/Ref.h>

namespace x10 {
    namespace lang {
        class String;
        class Ref;
    }

    namespace util {
        namespace concurrent {
            namespace atomic {

                extern const x10aux::RuntimeType* _initRTTHelper_AtomicReference(const x10aux::RuntimeType **location, const x10aux::RuntimeType *rtt);

                /*
                 * Implementation note: Extreme care must be taken when casting up/down
                 * from ref<T> to volatile S* that it is done in enough steps that any
                 * this pointer adjustment required by the C++ object model is still done.
                 * In particular, the only usage of a hard C style cast should be between
                 * S* and volatile S*.  Moving between S and T where S != T needs to be done
                 * with the C++ type system so that object model operations are performed.
                 */
                template<class T> class AtomicReference : public x10::lang::Ref {
                public:
                    static const x10aux::RuntimeType* rtt;
                    static const x10aux::RuntimeType* getRTT() { return NULL == rtt ? _initRTT() : rtt; }
                    static const x10aux::RuntimeType* _initRTT();
                    virtual const x10aux::RuntimeType *_type() const { return getRTT(); }

                    static x10aux::ref<AtomicReference<T > > _make();
                    static x10aux::ref<AtomicReference<T > > _make(T val);

                private:
                    volatile x10::lang::Ref* _data;

                public:
                    T get();

                    void set(T val);

                    T compareAndSet(T oldVal, T newVal);
                    
                    T weakCompareAndSet(T oldVal, T newVal);

                    T getAndSet(T val);

                    virtual x10aux::ref<x10::lang::String> toString();

                    static void _static_init() {
                        x10::lang::Ref::_static_init();
                    }
                };

                template<class T> x10aux::ref<AtomicReference<T > > AtomicReference<T >::_make() {
                    x10aux::ref<AtomicReference<T > > result = (new (x10aux::alloc<AtomicReference<T > >())AtomicReference<T >());
                    result->_data = NULL;
                    return result;
                }

                template<class T> x10aux::ref<AtomicReference<T > > AtomicReference<T >::_make(T val) {
                    x10aux::ref<AtomicReference<T > > result = (new (x10aux::alloc<AtomicReference<T > >())AtomicReference<T >());
                    x10::lang::Ref *tmp = val.get(); /* Does two things: gets backing S* ptr from ref<S> and upcasts S* to Ref* */
                    result->_data = tmp;
                    return result;
                }
                
                template<class T> T AtomicReference<T>::get() {
                    x10::lang::Ref *tmp = (x10::lang::Ref*)_data; /* drops volatile */
                    x10aux::ref<x10::lang::Ref> tmp2 = tmp; /* boxes to ref */
                    T tmp3 = tmp2;  /* downcast from ref<Ref> to T (ref<S>) */
                    return tmp3;
                }

                template<class T> void AtomicReference<T>::set(T val) {
                    x10::lang::Ref* tmp = val.get();
                    _data = tmp;
                }

                template<class T> T AtomicReference<T>::compareAndSet(T oldVal, T newVal) {
                    x10::lang::Ref *oldValPtr = oldVal.get(); /* Does two things: gets backing S* ptr from ref<S> and upcasts S* to Ref* */
                    x10::lang::Ref *newValPtr = newVal.get(); /* Does two things: gets backing S* ptr from ref<S> and upcasts S* to Ref* */
                    x10::lang::Ref *res = (x10::lang::Ref *)x10aux::atomic_ops::compareAndSet_ptr((volatile void**)&_data, (void*)oldValPtr, (void*)newValPtr);
                    x10aux::ref<x10::lang::Ref> res2 = res; /* boxes to ref */
                    T res3 = res2; /* downcast from ref<Ref> to T (ref<S>) */
                    return res3;
                }

                template<class T> T AtomicReference<T>::weakCompareAndSet(T oldVal, T newVal) {
                    // TODO: for minor optimization on ppc we could add a weakCompareAndSet_ptr in atomic_ops and use that here
                    return compareAndSet(oldVal, newVal);
                }
                
                template<class T> T AtomicReference<T>::getAndSet(T val) {
                    T res = get();
                    set(val);
                    return res;
                }

                template<class T> x10aux::ref<x10::lang::String> AtomicReference<T >::toString() {
                    x10::lang::Ref* tmp = (x10::lang::Ref*)_data; /* drops volatile */
                    x10aux::ref<x10::lang::Ref> tmp2 = tmp; /* boxes to ref */
                    T tmp3 = tmp2; /* downcast from ref<Ref> to T (ref<S) */
                    return x10aux::safe_to_string(tmp3);
                }
                
                template<class T> const x10aux::RuntimeType* AtomicReference<T >::_initRTT() {
                    return x10::util::concurrent::atomic::_initRTTHelper_AtomicReference(&rtt, x10aux::getRTT<T>());
                }

                template<class T> const x10aux::RuntimeType *AtomicReference<T >::rtt = NULL;
            }
        }
    }
}


#endif
// vim:tabstop=4:shiftwidth=4:expandtab