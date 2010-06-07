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

import x10.io.Console;

public class NQueensPar {

    var nSolutions:int = 0;

    public static val expectedSolutions =
        [0, 1, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712, 365596, 2279184, 14772512];

    val N:Int, P:Int;

    def this(N:Int, P:Int) { this.N=N; this.P=P;}

    def start() {
        new Board().search();
    }

    /**
     * Return an array of P regions, which together block divide the 1-D region R.
     */
    public static def block(R: Region(1), P: Int): ValRail[Region(1)](P) = {
        assert P >= 0;
        val low = R.min()(0), high = R.max()(0), count = high-low+1;
        val baseSize = count/P, extra = count - baseSize*P;
        ValRail.make[Region(1)](P, (i:int):Region(1) => {
            val start = low+i*baseSize+ (i < extra? i:extra);
            start..start+baseSize+(i < extra?0:-1)
        })
    }

    class Board {

        val q: Rail[Int]{self.at(this)};

        def this() {
            q = Rail.make[Int](0, (int)=>0);
        }

        def this(old: Rail[Int]!, newItem:Int) {
            val n = old.length;
            q = Rail.make[Int](n+1, (i:int)=> (i < n? old(i) : newItem));
        }

        def safe(j: int) {
            val n = q.length;
            for ((k) in 0..n-1) {
                if (j == q(k) || Math.abs(n-k) == Math.abs(j-q(k)))
                    return false;
            }
            return true;
        }

        /** Search for all solutions in parallel, on finding
         * a solution update nSolutions.
         */
        def search2(R: Region(1)){
            for ((k) in R)
                if (safe(k))
                    new Board(q, k).search();
        }

        def search() {
            if (q.length == N) {
                atomic nSolutions++;
                return;
            }

            if (q.length == 0) {
                val R = block(0..N-1, P);
                foreach ((q) in 0..P-1) {
                //  search(R(q));
				
val R3 = block(0..N-1, P);
search2(R3(0));
				}

            } else search2(0..N-1);
        }
    }

    public static def main(args: Rail[String]!)  {
        val n = args.length > 0 ? Int.parse(args(0)) : 8;
        println("N=" + n);
        //warmup
        //finish new NQueensPar(12, 1).start();
        val ps = [1,2,4];
        for (var i:Int = 0; i < ps.length; i++) {
            println("starting " + ps(i) + " threads");
            val nq = new NQueensPar(n,ps(i));
            var start:Long = -System.nanoTime();
            finish nq.start();
            val result = nq.nSolutions==expectedSolutions(nq.N);
            start += System.nanoTime();
            start /= 1000000;
            println("NQueensPar " + nq.N + "(P=" + ps(i) +
                    ") has " + nq.nSolutions + " solutions" +
                    (result? " (ok)." : " (wrong).") + "time=" + start + "ms");
        }
    }

    static def println(s:String) = Console.OUT.println(s);
}
