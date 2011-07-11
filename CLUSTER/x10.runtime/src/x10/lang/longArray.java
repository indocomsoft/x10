package x10.lang;

import java.io.Serializable;

import x10.lang.x10Array.pointwiseOpTag;


/** The base class for all (value or reference) multidimensional,
 * distributed long arrays in X10.  Is a subclass-only mutable class
 * (has no mutable state, and all methods are value methods).
 * Specialized from array by replacing the type parameter with long.
 
 * Handtranslated from the X10 code in x10/lang/longArray.x10
 * 
 * @author vj 12/24/2004
 */

abstract public class longArray extends x10Array {
	protected longArray( dist D) {
		super(D);
	}
	
	public static interface binaryOp extends Serializable {
		long apply(long r, long s);
	}
	
	public static final binaryOp sub = new binaryOp() { public long apply(long r, long s) { return r-s;}};
	public static final binaryOp add = new binaryOp() { public long apply(long r, long s) { return r+s;}};
	public static final binaryOp mul = new binaryOp() { public long apply(long r, long s) { return r*s;}};
	public static final binaryOp div = new binaryOp() { public long apply(long r, long s) { return r/s;}};
	public static final binaryOp max = new binaryOp() { public long apply(long r, long s) { return Math.max(r,s);}};
	
	public static interface pointwiseOp/*(region r)*/  extends pointwiseOpTag, Serializable {
		long apply(point/*(r)*/ p);
	}
    
    public static interface unaryOp {
        long apply(long r);
    }
	
    public static final unaryOp abs = new unaryOp() { public long apply(long r) { return Math.abs(r);}};
	
    //called from high level array operations
	public long get(int d0, boolean chkPl) {return get(d0); }
	public long get(int d0, int d1, boolean chkPl) {return get(d0, d1); }
	public long get(point pos, boolean chkPl) {return get(pos); }
	
	abstract public static /*value*/ class factory {
		
		/** Return the unique long value array initialized with 0 
		 * and defined over the distribution 0..k-1 -> here.
		 * vj: Note that in this implementation, returns longArray rather than
		 * longValueArray.
		 * @param k
		 * @return
		 */
		public longArray longValueArray( /*nat*/ int k) {
			return longValueArray(k, 0);
		}
		/** Return the unique long value array initialized with initVal 
		 * and defined over the distribution 0..k-1 -> here.
		 * 
		 * @param k
		 * @return
		 */
		public longArray/*(:rank=1)*/  longValueArray(/*nat*/ int k, long initVal) { 
			return longValueArray(x10.lang.dist.factory.local(k), initVal);
		}
		/** Return the unique long value array initialized with init 
		 * and defined over the distribution 0..k-1 -> here.
		 * 
		 * @param k
		 * @return
		 */
		public longArray/*(:rank=1)*/ longValueArray(/*nat*/ int k, pointwiseOp init) {
			return longValueArray( x10.lang.dist.factory.local(k), init);
		}
		
		abstract public 
		/*(distribution D)*/ longArray/*(D)*/ longValueArray(dist D, long init);
		abstract public 
		/*(distribution D)*/ longArray/*(D)*/ longValueArray(dist D, pointwiseOp/*(D.region)*/ init);
		
		
		/** Return the unique long value array initialized with 0 
		 * and defined over the distribution 0..k-1 -> here.
		 * 
		 * @param k
		 * @return
		 */
		public LongReferenceArray LongReferenceArray( /*nat*/ int k) {
			return LongReferenceArray(k, 0);
		}
		/** Return the unique long value array initialized with initVal 
		 * and defined over the distribution 0..k-1 -> here.
		 * 
		 * @param k
		 * @return
		 */
		public LongReferenceArray/*(:rank=1)*/  LongReferenceArray(/*nat*/ int k, long initVal) { 
			return LongReferenceArray(x10.lang.dist.factory.local(k), initVal);
		}
		/** Return the unique long value array initialized with init 
		 * and defined over the distribution 0..k-1 -> here.
		 * 
		 * @param k
		 * @return
		 */
		public LongReferenceArray/*(:rank=1)*/ LongReferenceArray(/*nat*/ int k, pointwiseOp init) {
			return LongReferenceArray( x10.lang.dist.factory.local(k), init);
		}
		
		
		abstract public 
		/*(distribution D)*/ LongReferenceArray/*(D)*/ LongReferenceArray(dist D, long init);
		abstract public 
		/*(distribution D)*/ LongReferenceArray/*(D)*/ LongReferenceArray(dist D, 
				pointwiseOp/*(D.region)*/ init);
		
	}
	
	public static final factory factory = Runtime.factory.getLongArrayFactory();
	
	/** Return the value of the array at the given point in the
	 * region.
	 */
	abstract /*value*/ public long get(point/*(region)*/ p);
	//TODO: interim support for multi-index access.
	abstract /*value*/ public long get(int p);
	abstract /*value*/ public long get(int p, int q);
	abstract /*value*/ public long get(int p, int q, int r);
	abstract /*value*/ public long get(int p, int q, int r, int s);
	abstract public long get(int[] p);
    
  
    
    /** Convenience method for returning the sum of the array.
     * @return sum of the array.
     */
	public long sum() {
		return reduce(add, 0);
	}
	/**
	 * Convenience method for returning the max of the array.
	 * @return
	 */
	public long max() {
		return reduce(max, 0);
	}
	/**
	 * Convenience method for returning the max of the array after applying the given fun.
	 * @param fun
	 * @return
	 */
	public long max(unaryOp fun) {
		return lift(fun).reduce(max, 0);
	}
	/**
	 * Convenience method for subtracting another array pointwise.
	 * @return
	 */
	public LongReferenceArray sub( longArray s) {
		return lift(sub, s);
	}
	/**
	 * Convenience method for subtracting another array pointwise.
	 * @return
	 */
	public LongReferenceArray add( longArray s) {
		return lift(add, s);
	}
	/**
	 * Convenience method for subtracting another array pointwise.
	 * @return
	 */
	public LongReferenceArray mul( longArray s) {
		return lift(mul, s);
	}
	/**
	 * Convenience method for subtracting another array pointwise.
	 * @return
	 */
	public LongReferenceArray div( longArray s) {
		return lift(div, s);
	}
	
	/**
	 * Convenience method for applying abs to each element in the array.
	 * @return
	 */
	public longArray abs() {
		return lift(abs);
	}
	/**
	 * Convenience method for applying max after applying abs.
	 * @return
	 */
	public long maxAbs() {
		return max(abs);
	}
	
	/** Return the value obtained by reducing the given array with the
	 function fun, which is assumed to be associative and
	 commutative. unit should satisfy fun(unit,x)=x=fun(x,unit).
	 */
	abstract /*value*/ public long reduce(binaryOp fun, long unit);
	
	/** Return an IntArray with the same distribution as this, by 
	 scanning this with the function fun, and unit unit.
	 */
	abstract /*value*/ public LongReferenceArray/*(distribution)*/ scan(binaryOp fun, long unit);
	
	/** Return an array of B@P defined on the intersection of the
	 region underlying the array and the parameter region R.
	 */
	abstract /*value*/ public /*(region(rank) R)*/
	LongReferenceArray/*(distribution.restriction(R)())*/  restriction(region R);
	
	/** Return an array of B@P defined on the intersection of 
	 the region underlying this and the parametric distribution.
	 */    
	abstract /*value*/ public  /*(distribution(:rank=this.rank) D)*/ 
	LongReferenceArray/*(distribution.restriction(D.region)())*/ restriction(dist D);
	
	public LongReferenceArray restriction(place P) {
		return restriction(distribution.restriction(P));
	}
	
	/** Take as parameter a distribution D of the same rank as *
	 * this, and defined over a disjoint region. Take as argument an *
	 * array other over D. Return an array whose distribution is the
	 * union of this and D and which takes on the value
	 * this.atValue(p) for p in this.region and other.atValue(p) for p
	 * in other.region.
	 */
	abstract /*value*/ public /*(distribution(:region.disjoint(this.region) &&
	rank=this.rank) D)*/
	LongReferenceArray/*(distribution.union(D))*/ union( longArray/*(D)*/ other);
	
	/** Return the array obtained by overlaying the array other on top of this.
	 The method takes as parameter a distribution D over the
	 same rank. It returns an array over the distribution
	 dist.asymmetricUnion(D).
	 */
	abstract /*value*/ public /*(distribution(:rank=this.rank) D)*/
	LongReferenceArray/*(distribution.asymmetricUnion(D))*/ overlay( longArray/*(D)*/ other);
	
	/** Update this array in place by overlaying the array other on top of this. The distribution
	 * of the input array must be a subdistribution of D.
	 * TODO: update the description of the parametric type.
	 */
    abstract public void update( longArray/*(D)*/ other);
	
	
	
	/** Assume given a longArray a over the given distribution.
	 * Assume given a function f: long -> long -> long.
	 * Return an longArray with distribution dist 
	 * containing fun(this.atValue(p),a.atValue(p)) for each p in
	 * dist.region.
	 */
	abstract /*value*/ public 
	LongReferenceArray/*(distribution)*/ lift(binaryOp fun, longArray/*(distribution)*/ a);
	abstract public 
	LongReferenceArray/*(distribution)*/ lift(unaryOp fun);
	
	/**
	 * Return an immutable copy of this array. Note: The implementation actually returns a copy
	 * at the representation of the X10 type x10.lang.longValueArray, which is longArray.
	 * @return an immutable version of this array.
	 */
	abstract public longArray toValueArray();
}