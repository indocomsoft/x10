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

//LIMITATION:
//This test case will not meet expectations. It is a limitation of the current release.
import harness.x10Test;
import x10.array.Region;

/**
 *Tests upper and lower triangular, and banded regions.
 *
 * @author kemal 4/2005
 */
public class RegionTriangular extends x10Test {

	public def run(): boolean = {
		val Universe: Region = [0..7, 0..7];
		var upperT: Region = Region.makeUpperTriangular(8);
		pr("upperT", upperT);
		for (val (i,j): Point in Universe) chk(iff(i <= j, upperT.contains([i, j])));
		var lowerT: Region = Region.makeLowerTriangular(8);
		pr("lowerT", lowerT);
		for (val (i,j): Point in Universe) chk(iff(i >= j, lowerT.contains([i, j])));
		return true;
	}

	static def iff(var x: boolean, var y: boolean): boolean = {
		return (x == y);
	}

	static def pr(var s: String, var r: Region): void = {
		x10.io.Console.OUT.println();
		x10.io.Console.OUT.println("printing region "+s);
		var k: int = 0;
		val N: int = 8;
		for (val (i,j): Point in [0..N-1, 0..N-1]) {
			x10.io.Console.OUT.print(" "+(r.contains([i, j]) ? "+" : "."));
			if ((++k) % N == 0) x10.io.Console.OUT.println();
		}
	}

	public static def main(var args: Rail[String]): void = {
		new RegionTriangular().execute();
	}
}
