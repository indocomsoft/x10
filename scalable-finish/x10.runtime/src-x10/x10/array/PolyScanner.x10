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

package x10.array;

import x10.io.*;


/**
 * Here's the general scheme for the information used in scanning,
 * illustrated for a region of rank r=4. Each axis Xi is bounded by
 * two sets of halfspaces, min[i] and max[i], obtained from the
 * region halfspaces by FME (resulting in the 0 coefficients as
 * shown). Computing the bounds for Xi requires substituting X0 up to
 * Xi-1 into each halfspace (as shown for the min[i]s) and taking
 * mins and maxes.
 *
 *           0   1   2   r-1
 * 
 * min[0]
 *           A0  0   0   0   B   X0 bounded by B / A0
 *           A0  0   0   0   B   X0 bounded by B / A0
 *           ...                 ...
 * 
 * min[1]
 *           A0  A1  0   0   B   X1 bounded by (B+A0*X0) / A1
 *           A0  A1  0   0   B   X1 bounded by (B+A0*X0) / A1
 *           ...                 ...
 * 
 * min[2]
 *           A0  A1  A2  0   B   X2 bounded by (B+A0*X0+A1*X1) / A2
 *           A0  A1  A2  0   B   X2 bounded by (B+A0*X0+A1*X1) / A2
 *           ...                 ...
 * 
 * min[3]
 *           A0  A1  A2  A3  B   X3 bounded by (B+A0*X0+A1*X1+A2*X2) / A3
 *           A0  A1  A2  A3  B   X3 bounded by (B+A0*X0+A1*X1+A2*X2) / A3
 *           ...                 ...
 *
 * In the innermost loop the bounds for X3 could be computed by
 * substituting the known values of X0 through X2 into each
 * halfspace. However, part of that computation can be pulled out of
 * the inner loop by keeping track for each halfspace in in min[k]
 * and each constraint in max[k] a set of partial sums of the form
 *
 *     minSum[0] = B
 *     minSum[1] = B+A0*X0
 *     ...
 *     minSum[k] = B+A0*X0+A1*X1+...+Ak-1*Xk-1
 *
 * (and similiarly for maxSum) and updating each partial sum
 * minSum[i+1] (and similarly for maxSum[i+1]) every time Xi changes
 * by
 *
 *     minSum[i+1] := sum[i] + Ai*Xi
 *
 * The loop bounds for Xk are then obtained by computing mins and
 * maxes over the sum[k]/Ak for the halfspaces in elim[k].
 */
final public class PolyScanner(rank:Int)/*(C:PolyMat)*/ implements Region.Scanner {

     public val C: PolyMat;

  //  global public val rank: int;

    private val myMin: Rail[VarMat]!;
    private val myMax: Rail[VarMat]!;
    private val minSum: Rail[VarMat]!;
    private val maxSum: Rail[VarMat]!;

    private val parFlags: Rail[boolean]!;
    private val min2: Rail[Rail[PolyRow]!]!;
    private val max2: Rail[Rail[PolyRow]!]!;

    public static def make(pm:PolyMat):PolyScanner!{self.rank==pm.rank} {
	val x = new PolyScanner(pm);
        x.init();
	return x;
    }

    private def this(pm: PolyMat):PolyScanner!{self.rank==pm.rank} {
	property(pm.rank);
        var pm0:PolyMat = pm.simplifyAll();
       
        this.C = pm;
        val r = pm0.rank;
        val n = Rail.make[VarMat](r);
        myMin = n;
        val x = Rail.make[VarMat](r);
        myMax = x;
        val nSum = Rail.make[VarMat](r);
        minSum = nSum;
        val xSum = Rail.make[VarMat](r);
        maxSum = xSum;
        val n2 = Rail.make[Rail[PolyRow]!](r);
        min2 = n2;
        val x2 = Rail.make[Rail[PolyRow]!](r);
        max2 = x2;
        //printInfo(Console.OUT);

        parFlags = Rail.make[boolean](r);
    }

    private def init() {
	var pm:PolyMat =C;
        init(pm, rank-1);
        for (var k: int = rank-2; k>=0; k--) {
            pm = pm.eliminate(k+1, true);
            init(pm, k);
        }

    }

    final private def init(pm: PolyMat, axis: int) {

        //pm.printInfo(Console.OUT, "pm for axis " + axis);

        // count
        var imin: int = 0;
        var imax: int = 0;
        for (r:PolyRow in pm) {
            if (r(axis)<0) imin++;
            if (r(axis)>0) imax++;
        }

        // complain if unbounded
        if (imin==0 || imax==0) {
            val m = imin==0? "minimum" : "maximum";
            val msg = "axis " + axis + " has no " + m;
            throw new UnboundedRegionException(msg);
        }

        // allocate
        myMin(axis) = new VarMat(imin, axis+1);
        myMax(axis) = new VarMat(imax, axis+1);
        minSum(axis) = new VarMat(imin, axis+1);
        maxSum(axis) = new VarMat(imax, axis+1);
        min2(axis) = Rail.make[PolyRow](imin);
        max2(axis) = Rail.make[PolyRow](imax);

        // fill in
        imin=0; imax=0;
        for (r:PolyRow in pm) {
            if (r(axis)<0) {
                for (var i: int = 0; i<=axis; i++)
                    myMin(axis)(imin)(i) = r(i);
                minSum(axis)(imin)(0) = r(rank);
                min2(axis)(imin) = r;
                imin++;
            }
            if (r(axis)>0) {
                for (var i: int = 0; i<=axis; i++)
                    myMax(axis)(imax)(i) = r(i);
                maxSum(axis)(imax)(0) = r(rank);
                max2(axis)(imax) = r;
                imax++;
            }
        }

        /*
        min(axis).printInfo(Console.OUT, "min for axis " + axis);
        Console.OUT.println("min2 for axis " + axis);
        for (var i:int=0; i<min2(axis).length; i++)
            min2(axis)(i).printInfo(Console.OUT, i);
        */
    }

    final public def set(axis: int, v: int): void {
        for (var k: int = axis+1; k<rank; k++)
            for (var l: int = 0; l<minSum(k).rows; l++)
                minSum(k)(l)(axis+1) = myMin(k)(l)(axis)*v + minSum(k)(l)(axis);
        for (var k: int = axis+1; k<rank; k++)
            for (var l: int = 0; l<maxSum(k).rows; l++)
                maxSum(k)(l)(axis+1) = myMax(k)(l)(axis)*v + maxSum(k)(l)(axis);
    }

    final public def min(axis: int): int {
        var result: int = Int.MIN_VALUE;
        for (var k: int = 0; k<myMin(axis).rows; k++) {
            val a = myMin(axis)(k)(axis);
            var b: int = minSum(axis)(k)(axis);
            // ax+b<=0 where a<0 => x>=ceil(-b/a)
            val m = b>0? (-b+a+1)/a : -b/a;
            if (m > result) result = m;
        }
        return result;
    }

    final public def max(axis: int): int {
        var result: int = Int.MAX_VALUE;
        for (var k: int = 0; k<myMax(axis).rows; k++) {
            val a = myMax(axis)(k)(axis);
            val b = maxSum(axis)(k)(axis);
            // ax+b<=0 where a>0 => x<=floor(-b/a)
            val m = b>0? (-b-a+1)/a : -b/a;
            if (m < result) result = m;
        }
        return result;
    }


    /**
     * odometer-style iterator for this scanner
     *
     * hasNext() computes the k that is the axis to be bumped:
     *
     * axis    0    1         k        k+1
     * now   x[0] x[1] ...  x[k]   max[k+1] ...  max[rank-1]
     * next  x[0] x[1] ...  x[k]+1 min[k+1] ...  min[rank-1]
     *
     * i.e. bump k, reset k+1 through rank-1
     * finished if k<0
     *
     * next() does the bumping and resetting
     *
     * assumes no degenerate axes, which is guaranteeed by Scanner API
     */


    final private class RailIt implements Iterator[Rail[int]] {
        proto def outerThis() = PolyScanner.this as PolyScanner!;
        private val rank: int = outerThis().rank;
        private val s = outerThis();

        private val x = Rail.make[int](rank);
        private val myMin = Rail.make[int](rank);
        private val myMax = Rail.make[int](rank);

        private var k: int;
        def this() {}
        def init() {
            myMin(0) = s.min(0);
            myMax(0) = s.max(0);
            x(0) = s.min(0);
            for (k=1; k<rank; k++) {
                s.set(k-1, x(k-1));
                val m = s.min(k);
                x(k) = m;
                myMin(k) = m;
                myMax(k) = s.max(k);
            }
            x(rank-1)--;
        }

        final public def hasNext(): boolean {
            k = rank-1;
            while (x(k)>=myMax(k))
                if (--k<0)
                    return false;
            return true;
        }

        final public def next() {
            x(k)++;
            for (k=k+1; k<rank; k++) {
                s.set(k-1, x(k-1));
                val m = s.min(k);
                x(k) = m;
                myMin(k) = m;
                myMax(k) = s.max(k);
            }
            return x;
        }

        public def remove() {}
    }

    /**
     * required by API, but less efficient b/c of allocation
     * XXX figure out how to expose
     *   1. Any/Var/ValPoint?
     *   2. hide inside iterator(body:(Point)=>void)?
     */

    final private class PointIt 
        implements Iterator[Point(PolyScanner.this.rank)] {

        val it: RailIt!;

        def this() {
            it = new RailIt();
        }

        public final def hasNext() = it.hasNext();
        public final def next(): Point(rank) = it.next() as Point(rank);
        public final def remove() = it.remove();
    }

    public def iterator(): Iterator[Point(rank)]! {
        val it = new PointIt();
        it.it.init();
        return it;
    }


    //
    // debugging info
    //

    // XXX doesn't handle multi-body case
    public def printInfo(ps: Printer) {
        ps.println("PolyScanner");
        C.printInfo(ps, "  C");
    }

    public def printInfo2(ps: Printer): void {
        for (var k: int = 0; k<myMin.length; k++) {
            ps.println("axis "+k);
            ps.println("  min");
            for (var l: int = 0; l<myMin(k).rows; l++) {
                ps.print("  ");
                for (var m: int = 0; m<myMin(k)(l).cols; m++)
                    ps.print(" "+myMin(k)(l)(m));
                ps.print("  sum");
                for (var m: int = 0; m<minSum(k)(l).cols; m++)
                    ps.print(" "+minSum(k)(l)(m));
                ps.print("\n");
            }
            ps.printf("  max\n");
            for (var l: int = 0; l<myMax(k).rows; l++) {
                ps.print("  ");
                for (var m: int = 0; m<myMax(k)(l).cols; m++)
                    ps.print(" "+myMax(k)(l)(m));
                ps.print("  sum");
                for (var m: int = 0; m<maxSum(k)(l).cols; m++)
                    ps.print(" "+maxSum(k)(l)(m));
                ps.println();
            }
        }
    }
}