#include <sstream>

#include <x10aux/ref.h>
#include <x10aux/alloc.h>

#include <x10/lang/Closure.h>
#include <x10/lang/String.h>
#include <x10/lang/Place.h>

using namespace x10::lang;
using namespace x10aux;

void Closure::_serialize(x10aux::ref<Closure> this_,
                         x10aux::serialization_buffer &buf) 
{
    x10aux::serialization_id_t id = this_->_get_serialization_id();
    _S_("Serializing a "<<ANSI_SER<<ANSI_BOLD<<"value id "<<id<<ANSI_RESET<<" to buf: "<<&buf);
    buf.write(id);
    _S_("Serializing the "<<ANSI_SER<<"value body"<<ANSI_RESET<<" to buf: "<<&buf);
    this_->_serialize_body(buf);
}           

x10_boolean Closure::at(x10aux::ref<x10::lang::Ref> o) {
    return location == o->location;
}

Place Closure::home() {
    return x10::lang::Place_methods::_make(location);
}

x10aux::ref<x10::lang::String> x10::lang::Closure::toString() {
    return String::Lit("Closure without toString defined.");
}

x10aux::ref<x10::lang::String> x10::lang::Closure::typeName() {
    return x10::lang::String::Lit(_type()->name());
}


// vim:tabstop=4:shiftwidth=4:expandtab
