/*
 *
 * (C) Copyright IBM Corporation 2006-2008.
 *
 *  This file is part of X10 Language.
 *
 */

package x10.lang;

import x10.compiler.Native;
import x10.io.Console;
import x10.util.Timer;
import x10.util.Pair;

public class System {

    private def this() {}
    
    public static def currentTimeMillis() = Timer.milliTime();
    public static def nanoTime() = Timer.nanoTime();

    @Native("java", "java.lang.System.exit(#1)")
    @Native("c++", "x10aux::system_utils::exit(#1)")
    public static native def exit(code: Int):void;

    public static def exit() = exit(-1);

    /* TODO: XTENLANG-180.  Provide full System properties API in straight X10 */
    @Native("java", "java.lang.System.setProperty(#1,#2)")
    @Native("c++", "printf(\"not setting %s\\n\", (#1)->c_str())") // FIXME: Trivial definition to allow XRX compilation to go through.
    public static native def setProperty(p:String,v:String):void;


    // FIXME: this ought to be in ValRail but @Native system does not allow this
    static public def copyTo[T] (src:ValRail[T], src_off:Int, dst:Rail[T], dst_off:Int, len:Int) {
        // could be further optimised to send only the part of the valrail needed
        at (dst.location) {
            //TODO: implement optimisation in backend so we can use: for ((i):Point(1) in 0..len-1) {
            for (var i:Int=0 ; i<len ; ++i) {
                dst(dst_off+i) = src(src_off+i);
            }
        }
    }

    // FIXME: this ought to be in ValRail but @Native system does not allow this
    static public def copyTo[T] (src:ValRail[T], src_off:Int,
                          dst_place:Place, dst_finder:()=>Pair[Rail[T]!,Int],
                          len:Int) {
        // could be further optimised to send only the part of the valrail needed
        at (dst_place) {
            val pair = dst_finder();
            val dst = pair.first;
            val dst_off = pair.second;
            //TODO: implement optimisation in backend so we can use: for ((i):Point(1) in 0..len-1) {
            for (var i:Int=0 ; i<len ; ++i) {
                dst(dst_off+i) = src(src_off+i);
            }
        }
    }

    // FIXME: this ought to be in Rail but @Native system does not allow this
    static public def copyTo[T] (src:Rail[T], src_off:Int, dst:Rail[T], dst_off:Int, len:Int) {
        // semantics allows an async per rail element inside a single finish
        // this version is optimised to use a single async for the whole rail
        // it could be further optimised to send only the part of the rail needed
        val to_serialize = src as ValRail[T];
        at (dst.location) {
            //TODO: implement optimisation in backend so we can use: for ((i):Point(1) in 0..len-1) {
            for (var i:Int=0 ; i<len ; ++i) {
                dst(dst_off+i) = to_serialize(src_off+i);
            }
        }
    }

    // FIXME: this ought to be in Rail but @Native system does not allow this
    static public def copyTo[T] (src:Rail[T], src_off:Int,
                          dst_place:Place, dst_finder:()=>Pair[Rail[T]!,Int],
                          len:Int) {
        // semantics allows an async per rail element inside a single finish
        // this version is optimised to use a single async for the whole rail
        // it could be further optimised to send only the part of the rail needed
        val to_serialize = src as ValRail[T];
        at (dst_place) {
            val pair = dst_finder();
            val dst = pair.first;
            val dst_off = pair.second;
            //TODO: implement optimisation in backend so we can use: for ((i):Point(1) in 0..len-1) {
            for (var i:Int=0 ; i<len ; ++i) {
                dst(dst_off+i) = to_serialize(src_off+i);
            }
        }
    }

    // FIXME: this ought to be in Rail but @Native system does not allow this
    static public def copyTo[T] (src:Rail[T], src_off:Int,
                          dst_place:Place, dst_finder:()=>Pair[Rail[T]!,Int],
                          len:Int, notifier:()=>Void) {
        // semantics allows an async per rail element inside a single finish
        // this version is optimised to use a single async for the whole rail
        // it could be further optimised to send only the part of the rail needed
        val to_serialize = src as ValRail[T];
        x10.runtime.NativeRuntime.runAt(dst_place.id, ()=>{
            val pair = dst_finder();
            val dst = pair.first;
            val dst_off = pair.second;
            //TODO: implement optimisation in backend so we can use: for ((i):Point(1) in 0..len-1) {
            for (var i:Int=0 ; i<len ; ++i) {
                dst(dst_off+i) = to_serialize(src_off+i);
            }
            notifier();
        });
    }


    // FIXME: this ought to be in Rail but @Native system does not allow this
    static public def copyFrom[T] (dst:Rail[T], dst_off:Int, src:Rail[T], src_off:Int, len:Int) {
        // semantics allows an async per rail element inside a single finish
        // this version is optimised to use a single async for the whole rail
        // it could be further optimised to send only the part of the rail needed
        at (src) {
            val to_serialize = src as ValRail[T];
            at (dst) {
                //TODO: implement optimisation in backend so we can use: for ((i):Point(1) in 0..len-1) {
                for (var i:Int=0 ; i<len ; ++i) {
                    dst(dst_off+i) = to_serialize(src_off+i);
                }
            }
        }
    }

    // FIXME: this ought to be in Rail but @Native system does not allow this
    static public def copyFrom[T] (dst:Rail[T], dst_off:Int,
                            src_place:Place, src_finder:()=>Pair[Rail[T]!,Int],
                            len:Int) {
        // semantics allows an async per rail element inside a single finish
        // this version is optimised to use a single async for the whole rail
        // it could be further optimised to send only the part of the rail needed
        at (src_place) {
            val pair = src_finder();
            val src = pair.first;
            val src_off = pair.second;
            val to_serialize = src as ValRail[T];
            at (dst.location) {
                //TODO: implement optimisation in backend so we can use: for ((i):Point(1) in 0..len-1) {
                for (var i:Int=0 ; i<len ; ++i) {
                    dst(dst_off+i) = to_serialize(src_off+i);
                }
            }
        }
    }

    // FIXME: this ought to be in Rail but @Native system does not allow this
    static public def copyFrom[T] (dst:Rail[T]!, dst_off:Int, src:ValRail[T], src_off:Int, len:Int) {
        // source is always local
        // semantics allows an async per rail element inside a single finish
        // this version is optimised to not use any asynchrony
        //TODO: implement optimisation in backend so we can use: for ((i):Point(1) in 0..len-1) {
        for (var i:Int=0 ; i<len ; ++i) {
            dst(dst_off+i) = src(src_off+i);
        }
    }

    // FIXME: this ought to be in Rail but @Native system does not allow this
/*  uncomment upon resolution of XTENLANG-533
    static def copyFrom[T] (dst:Rail[T], dst_off:Int,
                            src_place:Place, src_finder:()=>Pair[ValRail[T]!,Int],
                            len:Int) {
        // not necessarily local, so go and fetch the ValRail...
        // semantics allows an async per rail element inside a single finish
        // this version is optimised to use a single async for the whole rail
        // it could be further optimised to send only the part of the rail needed
        at (src_place) {
            val pair = src_finder();
            val src = pair.first;
            val src_off = pair.second;
            at (dst.location) {
                //TODO: implement optimisation in backend so we can use: for ((i):Point(1) in 0..len-1) {
                for (var i:Int=0 ; i<len ; ++i) {
                    dst(dst_off+i) = src(src_off+i);
                }
            }
        }
    }
*/
}