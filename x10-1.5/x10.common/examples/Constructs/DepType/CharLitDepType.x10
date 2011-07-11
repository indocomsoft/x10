/*
 *
 * (C) Copyright IBM Corporation 2006
 *
 *  This file is part of X10 Test.
 *
 */
import harness.x10Test;

/**
 * Check that a float literal can be cast to float.
 */
public class CharLitDepType extends x10Test {
	public boolean run() {
		char(:self=='a') f =  'a';
		return true;
	}

	public static void main(String[] args) {
		new CharLitDepType().execute();
	}


}
