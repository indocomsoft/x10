#include <x10aux/config.h>
#include <x10aux/alloc.h>
#include <x10aux/RTT.h>

#include <x10/lang/PlaceLocalHandle_Impl.h>

using namespace x10aux;
using namespace x10::lang;

namespace x10 {
    namespace lang {

        x10aux::RuntimeType PlaceLocalHandle_Impl<void>::rtt;

        void
        _initRTTHelper_PlaceLocalHandle_Impl(RuntimeType *location, const RuntimeType *rtt) {
            const RuntimeType* params[1] = { rtt };
            RuntimeType::Variance variances[1] = { RuntimeType::invariant };
            const RuntimeType *canonical = x10aux::getRTT<PlaceLocalHandle_Impl<void> >();
            const char *name = alloc_printf("x10.lang.PlaceLocalHandle_Impl[+%s]",rtt->name());
            location->init(canonical, name, 0, NULL, 1, params, variances);
        }
    }
}

