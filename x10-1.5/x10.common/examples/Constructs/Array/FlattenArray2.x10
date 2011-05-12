/*
 *
 * (C) Copyright IBM Corporation 2006
 *
 *  This file is part of X10 Test.
 *
 */
import harness.x10Test;

/**
 * Test for array reference flattening. Checks that after flattening
 the variable x and y can still be referenced, i.e. are not 
 declared within local blocks.
  
  To check that this test does what it was intended to, examine
  the output Java file. It should have a series of local variables
  pulling out the subters of m(a[1,1]).
  
  Checks that array references can occur deep in an expression.
 */
 
public class FlattenArray2 extends x10Test {
    int[.] a;
    public FlattenArray2() {
      a = new int[[1:10,1:10]] (point [i,j]) { return i+j;};
    }
    int m(int x) {
      return x;
    }
	public boolean run() {
	    int x = m(3) + m(a[1,1]); // being called in a method to force flattening.
	    int y = m(4) + m(a[2,2]);
	    return true;
	}

	public static void main(String[] args) {
		new FlattenArray2().execute();
	}
	
}