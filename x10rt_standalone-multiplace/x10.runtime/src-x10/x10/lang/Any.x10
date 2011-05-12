package x10.lang;

import x10.compiler.Native;
import x10.compiler.NativeRep;
/**
 * The top of the type hierarchy.
 * @author vj 12/14/09
 * 
 */
@NativeRep("java", "x10.core.Any", null, null)
@NativeRep("c++", "x10aux::ref<x10::lang::Any>", "x10::lang::Any", null)
public interface Any {
	
    @Native("java", "x10.lang.Place.place(x10.core.Ref.home(#0))")
    @Native("c++", "(#0)->home")
    property def home():Place;
	
    @Native("java", "x10.core.Ref.at(#0, #1)")
    @Native("c++", "((#0)->home == (#1)->home)")
    property def at(p:Object):Boolean;
	
    @Native("java", "x10.core.Ref.at(#0, #1.id)")
    @Native("c++", "((#0)->location == (#1)->FMGL(id))")
    property def at(p:Place):Boolean;
	
    @Native("java", "#0.toString()")
    @Native("c++", "x10aux::to_string(#0)")
    global safe def toString():String;
	
    @Native("java", "x10.core.Ref.typeName(#0)")
    @Native("c++", "x10aux::type_name(#0)")
    global safe def typeName():String;
}