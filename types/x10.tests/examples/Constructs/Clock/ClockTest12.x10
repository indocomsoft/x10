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

import harness.x10Test;

/**
 * Testing if c.resume() declares quiescence of current activity on c.
 * c.resume() without next can advance clock c.
 * next waits until all clocks have advanced to their next phase.
 *
 * @author kemal 4/2005
 * @author vj moved to X10 1.7 09/13/08
 */
public class ClockTest12 extends x10Test {

	var phase: int = 0;

	public def run(): boolean = {
		finish async {
		    val c  = Clock.make();
		    async clocked(c) taskA(1, c);
		    async clocked(c) taskA(2, c);
		    async clocked(c) taskB(c);
		}
		return true;
	}

	def taskA(var id: int, val c: Clock): void = {
		var tmp: int;
		System.sleep(1000);
		atomic tmp = phase;
		x10.io.Console.OUT.println(""+id+" observed current phase = "+tmp);
		chk(tmp == 0);
		c.resume(); //  1st next advances in activity #2
		System.sleep(1000);
		c.resume(); // not an error, still in phase 0
		when (phase > 0) {
			x10.io.Console.OUT.println(""+id+" observed future phase = "+phase);
			chk(phase == 1);
			System.sleep(5000);
			chk(phase == 1); // cannot go beyond next phase
		}
		next;
		System.sleep(1000);
		atomic tmp = phase;
		x10.io.Console.OUT.println(""+id+" observed current phase = "+tmp);
		chk(tmp == 1);
		c.resume(); // 2nd next advances in activity #2
		c.resume(); // not an error still in phase 1
		c.resume();
		when (phase>1) {
			x10.io.Console.OUT.println(""+id+" observed future phase = "+phase);
			chk(phase == 2);
			System.sleep(5000);
			chk(phase == 2); // cannot go beyond next phase
		}
		next;
		next;
	}

	def taskB(val c: Clock): void = {
		var tmp: int;
		atomic tmp = phase;
		x10.io.Console.OUT.println("now in phase "+tmp);
		c.resume();
		next;
		atomic phase++;
		atomic tmp = phase;
		x10.io.Console.OUT.println("now in phase "+tmp);
		c.resume();
		next;
		atomic phase++;
		atomic tmp = phase;
		x10.io.Console.OUT.println("now in phase "+tmp);
		c.resume();
		next;
	}

	public static def main(var args: Array[String](1)): void = {
		new ClockTest12().execute();
	}
}