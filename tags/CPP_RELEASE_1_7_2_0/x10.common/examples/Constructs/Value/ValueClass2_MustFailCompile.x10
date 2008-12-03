/*
 *
 * (C) Copyright IBM Corporation 2006
 *
 *  This file is part of X10 Test.
 *
 */
import harness.x10Test;

/**
 * Value class test:
 *
 * Testing that an assignment to a value class field
 * causes a compiler error.
 *
 * @author kemal 4/2005
 */
public class ValueClass2_MustFailCompile extends x10Test {

	public boolean run() {
		dist d = [0:9]->here;
		final foo f = new foo();
		myval x = new myval(1, new complex(2,3), f, new int value[d]);
		myval y = new myval(1, new complex(2,3), f, new int value[d]);
		//==== > compiler error should occur here
		x.intval += 1;
		return (x.intval == y.intval+1);
	}

	public static void main(String[] args) {
		new ValueClass2_MustFailCompile().execute();
	}

	static final value class myval {
		int intval;
		complex cval;
		foo refval;
		int value[.] arrayval;
		myval(int intval, complex cval, foo refval, int value[.] arrayval) {
			this.intval = intval;
			this.cval = cval;
			this.refval = refval;
			this.arrayval = arrayval;
		}
	}

	static class foo {
		int w = 19;
	}

	static final value complex {
		int re;
		int im;
		complex(int re, int im) {
			this.re = re;
			this.im = im;
		}
		complex add (complex other) {
			return new complex(this.re+other.re, this.im+other.im);
		}
	}
}

