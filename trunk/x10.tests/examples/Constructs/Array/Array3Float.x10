/*
 *
 * (C) Copyright IBM Corporation 2006
 *
 *  This file is part of X10 Test.
 *
 */
import harness.x10Test;;

/**
 * Ensures float arrays are implemented.
 */
public class Array3Float extends x10Test {

	public def run(): boolean = {
		val r = Region.make([1..10, 1..10]);
	    var ia: Array[Float](2) = Array.makeFromRegion[Float](r, (x:Point)=>0.0F);
		ia(1, 1) = 42.0F;
		return (42.0F == ia(1, 1));
	}

	public static def main(var args: Rail[String]): void = {
		new Array3Float().execute();
	}
}
