/*
 *
 * (C) Copyright IBM Corporation 2006
 *
 *  This file is part of X10 Test.
 *
 */
import harness.x10Test;;

/**
 * Test for arrays, regions and dists.
 * Based on original arraycopy3 by vj.
 *
 * @author kemal 1/2005
 */
public class ArrayCopy3 extends x10Test {

	/**
	 * Returns true iff point x is not in the domain of
	 * dist D
	 */
	static def outOfRange(val D: dist, val x: point): boolean = {
		var gotException: boolean = false;
		try {
			async(D(x)) {}; // dummy op just to use D[x]
		} catch (var e: Throwable) {
			gotException = true;
		}
		return gotException;
	}

	/**
	 * Does not throw an error iff A[i] == B[i] for all points i.
	 */
	public def arrayEqual(val A: Array[int], val B: Array[int]): void = {
		val D: dist = A.dist;
		val E: dist = B.dist;
		// Spawn an activity for each index to
		// fetch the B[i] value
		// Then compare it to the A[i] value
		finish
			ateach (val p: point in D) {
			val f = (future(E(p)){B(p)}).force();
			chk(A(p) == f);
			}
	}

	/**
	 * Set A[i] = B[i] for all points i.
	 * A and B can have different dists whose
	 * regions are equal.
	 * Throws an error iff some assertion failed.
	 */
	public def arrayCopy(val A: Array[int], val B: Array[int]{rank==A.rank}): void = {
		val D: dist{rank==A.rank} = A.dist;
		val E: dist{rank==A.rank} = B.dist;
		// Allows message aggregation

		val D_1: dist = dist.makeUnique(D.places());
		// number of times elems of A are accessed
		val accessed_a: Array[int] = Array.make[int](D);
		// number of times elems of B are accessed
		val accessed_b: Array[int] = Array.make[int](E);

		finish
			ateach (val x: point in D_1) {
				val px: place = D_1(x);
				chk(here == px);
				val LocalD: region{rank==A.rank} = (D | px).region;
				for (val py: place in (E | LocalD).places()) {
					val RemoteE: region{rank==A.rank} = (E | py).region;
					val Common: region{rank==A.rank} = LocalD && RemoteE;
					val D_common: dist{rank==A.rank} = D | Common;
					// the future's can be aggregated
					for (val i: point in D_common) {
						async(py) atomic accessed_b(i) += 1;
						val temp: int = (future(py){B(i)}).force();
						// the following may need to be bracketed in
						// atomic, unless the disambiguator
						// knows about dists
						A(i) = temp;
						atomic accessed_a(i) += 1;
					}
					// check if dist ops are working
					val D_notCommon: dist{rank==A.rank} = D - D_common;
					chk((D_common || D_notCommon).equals(D));
					val E_common: dist{rank==A.rank} = E | Common;
					val E_notCommon: dist{rank==A.rank} = E - E_common;

					chk((E_common || E_notCommon).equals(E));
					for (val k: point in D_common) {
						chk(D_common(k) == px);
						chk(outOfRange(D_notCommon, k));
						chk(E_common(k) == py);
						chk(outOfRange(E_notCommon, k));
						chk(D(k) == px && E(k) == py);
					}

					for (val k: point in D_notCommon) {
						chk(outOfRange(D_common, k));
						chk(!outOfRange(D_notCommon, k));
						chk(outOfRange(E_common, k));
						chk(!outOfRange(E_notCommon, k));
						chk(!(D(k) == px && E(k) == py));
					}
				}
			}
		// ensure each A[i] was accessed exactly once
		finish ateach (val i: point in D) chk(accessed_a(i) == 1);
		// ensure each B[i] was accessed exactly once
		finish ateach (val i: point in E) chk(accessed_b(i) == 1);
	}

	public const N: int = 3;

	/**
	 * For all combinations of dists of arrays B and A,
	 * do an array copy from B to A, and verify.
	 */
	public def run(): boolean = {
		val R: region{rank==4} = [0..N-1, 0..N-1, 0..N-1, 0..N-1];
		val TestDists: region = [0..dist2.N_DIST_TYPES-1, 0..dist2.N_DIST_TYPES-1];

		for (val distP: point[dX,dY] in TestDists) {
			val D: dist{rank==4} = dist2.getDist(dX, R);
			val E: dist{rank==4} = dist2.getDist(dY, R);
			chk(D.region.equals(E.region) && D.region.equals(R));
			val A: Array[int]{rank==4} = Array.make[int](D);
			val B: Array[int]{rank==A.rank} = Array.make[int](E, 
			(var p(i,j,k,l): point) => { var x: int = ((i*N+j)*N+k)*N+l; x*x+1 });
			arrayCopy(A, B);
			arrayEqual(A, B);
		}
		return true;
	}

	public static def main(var args: Rail[String]): void = {
		new ArrayCopy3().execute();
	}

	/**
	 * utility for creating a dist from a
	 * a dist type int value and a region
	 */
	static class dist2 {
		const BLOCK: int = 0;
		const CYCLIC: int = 1;
		const BLOCKCYCLIC: int = 2;
		const CONSTANT: int = 3;
		const RANDOM: int = 4;
		const ARBITRARY: int = 5;
		const N_DIST_TYPES: int = 6;

		/**
		 * Return a dist with region r, of type disttype
		 */
		public static def getDist(distType: Int, r: Region): Dist(r) = {
			switch(distType) {
				case BLOCK: return dist.makeBlock(r);
				case CYCLIC: return dist.makeCyclic(r);
				case BLOCKCYCLIC: return dist.makeBlockCyclic(r, 3);
				case CONSTANT: return r->here;
				case RANDOM: return dist.makeRandom(r);
				case ARBITRARY:return dist.makeArbitrary(r);
				default: throw new Error();
			}
		}
	}
}
