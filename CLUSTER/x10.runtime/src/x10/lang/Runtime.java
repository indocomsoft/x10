package x10.lang;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.io.PrintStream;
import java.util.StringTokenizer;

import x10.cluster.ClusterConfig;
import x10.cluster.ClusterRunner;
import x10.cluster.ClusterRuntime;
import x10.runtime.Activity;
import x10.runtime.Configuration;
import x10.runtime.DefaultRuntime_c;
import x10.runtime.Place;
import x10.runtime.PoolRunner;
import x10.runtime.Report;

import x10.runtime.util.ConfigurationError;
import x10.runtime.util.ShowUsageNotification;

/**
 * This is the central entrypoint to the X10 Runtime for the
 * compiler. There is exactly one Runtime per JVM running X10.
 * 
 * The Runtime is NOT an X10Object! In fact, it cannot be since
 * X10Object's constructor requires already an existing and working
 * Runtime.  Conceptually Runtime is an Interface (!) for the X10
 * world (just like Clock/Region/Distribution/Array/Range/Activity).
 * It is implemented as an abstract class since we need to put some
 * code here, notably the static  "getRuntime()"
 * implementation.  Sadly, Java does not allow statics code in
 * interfaces, otherwise this would be an interface. 
 * 
 * @author Christian Grothoff, Christoph von Praun
 * @see Place
 * @see Activity
 * 
 * @author Raj Barik, Vivek Sarkar
 * 3/6/2006: add runBootAsync() method, which is like runAsync() except that it is only used for the boot activity
 */
public abstract class Runtime {
	
	public static Runtime runtime;
	
	/**
	 * This instance should be used only in the implementation of 
	 * the x10.runtime. 
	 */
	public static JavaRuntime javaRuntime;
	
	private static boolean done_;
	public static void init() {
		assert !done_;
		done_ = true;
		String rt = System.getProperty("x10.runtime");
		Runtime r = null;
		try {
			if (rt != null)
				r = (Runtime) Class.forName(rt).newInstance();
			else
				r = new DefaultRuntime_c();
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Runtime::<clinit> did not find runtime " + rt);
			throw new Error(cnfe);
		} catch (IllegalAccessException iae) {
			System.err.println("Runtime::<clinit> could not access runtime " + rt);
			throw new Error(iae);
		} catch (InstantiationException ie) {
			System.err.println("Runtime::<clinit> could not create runtime " + rt);
			throw new Error(ie);
		} catch (Throwable t) {
			System.err.println("Runtime::<clinit> unknown exception during creation of runtime " + rt);
			t.printStackTrace();
			throw new Error(t);
		} finally {        
			assert (r != null);
			runtime = r;
			javaRuntime = new JavaRuntime();
			factory = runtime.getFactory();
			// ArrayFactory.init(r);
			r.initialize();  //eagerly 
			/*dist.initFactory();
			 intArray.initFactory();
			 genericArray.initFactory();
			 point.initFactory();
			 charArray.initFactory();
			 doubleArray.initFactory();
			 */
		}
	}
	public static Factory factory; 
	
	protected abstract void initialize();
	
	public static void main(String[] args) {
		try {
			String[] args_stripped = Configuration.parseCommandLine(args);
			init();
			runtime.run(args_stripped);
		}/* catch (ShowUsageNotification e) {
			usage(System.out, null);
		} catch (ConfigurationError e) {
			usage(System.err, e);
		}*/ catch (Exception e) {
			Runtime.javaRuntime.error("Unexpected Exception in X10 Runtime.", e);
		}
		// vj: The return from this method does not signal termination of the VM
		// because a separate non-daemon thread has been spawned to execute Boot Activity.
	}

	/**
	 * The name of the language this runtime represents.
	 */
	public static final String LANGUAGE = "x10";

	/* The following code was borrowed from Polyglot's Options.java */
    /**
     * The maximum width of a line when printing usage information. Used
     * by <code>usageForFlag</code> and <code>usageSubsection</code>.
     */
    protected static final int USAGE_SCREEN_WIDTH = 76;

    /**
     * The number of spaces from the left that the descriptions for flags will
	 * be displayed. Used by <code>usageForFlag</code>.
     */
    private static final int USAGE_FLAG_WIDTH = 34;

    private static final int USAGE_FLAG_INDENT = 4;

    /**
     * Utility method to print a number of spaces to a PrintStream.
     * @param out output PrintStream
     * @param n number of spaces to print.
     */
    protected static void printSpaces(PrintStream out, int n) {
        while (n-- > 0) {
            out.print(' ');
        }
    }

    /**
     * Output a flag and a description of its usage in a nice format.
     *
     * @param out output PrintStream
     * @param flag
     * @param description description of the flag.
     */
    protected static void usageForFlag(PrintStream out, String flag, String description) {
		printSpaces(out, USAGE_FLAG_INDENT);
        out.print(flag);
        // cur is where the cursor is on the screen.
        int cur = flag.length() + USAGE_FLAG_INDENT;

        // print space to get up to indentation level
        if (cur < USAGE_FLAG_WIDTH) {
            printSpaces(out, USAGE_FLAG_WIDTH - cur);
        }
        else {
            // the flag is long. Get a new line before printing the
            // description.
            out.println();
            printSpaces(out, USAGE_FLAG_WIDTH);
        }
        cur = USAGE_FLAG_WIDTH;

        // break up the description.
        StringTokenizer st = new StringTokenizer(description);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (cur + s.length() > USAGE_SCREEN_WIDTH) {
                out.println();
                printSpaces(out, USAGE_FLAG_WIDTH);
                cur = USAGE_FLAG_WIDTH;
            }
            out.print(s);
            cur += s.length();
            if (st.hasMoreTokens()) {
                if (cur + 1 > USAGE_SCREEN_WIDTH) {
                    out.println();
                    printSpaces(out, USAGE_FLAG_WIDTH);
                    cur = USAGE_FLAG_WIDTH;
                }
                else {
                    out.print(" ");
                    cur++;
                }
            }
        }
        out.println();
    }

	static int exitCode;

	public static void setExitCode(int code) {
		exitCode = code;
	}

	public static void x10Exit() {
		if (Report.should_report("activity", 3)) {
			Thread t = Thread.currentThread();
			Report.report(3, t + "@" + System.currentTimeMillis()
					+ " The XVM is now terminating.");
		}
		System.exit(exitCode);
	}

	public static void exit(int code) {
		setExitCode(code);
		x10Exit();
	}

	/**
	 * Sleep for the specified number of milliseconds.
	 * [IP] NOTE: Unlike Java, x10 sleep() simply exits when interrupted.
	 * @param millis the number of milliseconds to sleep
	 * @return true if completed normally, false if interrupted
	 */
	public static boolean sleep(long millis) {
		try {
			Thread.sleep(millis);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	protected Runtime() {
	}

    public static abstract class Factory {
    	public abstract region.factory getRegionFactory();
    	public abstract dist.factory getDistributionFactory();
    	public abstract point.factory getPointFactory();
    	public abstract clock.factory getClockFactory();
        public abstract booleanArray.factory getBooleanArrayFactory();
        public abstract charArray.factory getCharArrayFactory();
        public abstract byteArray.factory getByteArrayFactory();
        public abstract shortArray.factory getShortArrayFactory();
    	public abstract intArray.factory getIntArrayFactory();
    	public abstract longArray.factory getLongArrayFactory();
        public abstract floatArray.factory getFloatArrayFactory();
        public abstract doubleArray.factory getDoubleArrayFactory();
        public abstract genericArray.factory getGenericArrayFactory();
        public abstract structureArray.factory getStructureArrayFactory();
        public abstract complex4Array.factory getComplex4ArrayFactory();
    	public abstract place.factory getPlaceFactory();
    }

	public abstract Factory getFactory();

	/**
	 * Run the X10 application.
	 */
	protected abstract void run(String[] args) throws Exception;

	public abstract void setCurrentPlace(place p);

	public abstract Place currentPlace();

	public abstract Activity currentActivity();

	public abstract Place[] getPlaces();

	/**
	 * @return The place of the thread executing the current activity. 
	 * ('here' in X10).
	 */
	public static Place here() {
		place p = runtime.currentPlace();
		
		assert  p != null;
		return (Place) p;
	}

	/* this is called from inside the array library */
	public static void hereCheckPlace(place p) {  
		Thread cur = Thread.currentThread();
		if(cur instanceof PoolRunner) {
			if (p != ((PoolRunner)cur).getPlace()) {
				//cluster situation
				//if(ClusterConfig.multi && ClusterRuntime.isLocal(p)) ; //good
				//else 
				throw new BadPlaceException(p, here());
			}
		} else if(cur instanceof ClusterRunner) {
			if(! ClusterRuntime.isLocal(p))
				throw new BadPlaceException(p, here());
		} else {
			throw new Error("Runtime: hereCheckPlace error");
		}
	}

	/* this is called from the code snippet for field and array access */
	public static java.lang.Object hereCheck(java.lang.Object o) {
        if (Configuration.BAD_PLACE_RUNTIME_CHECK &&
                o != null &&
                o instanceof x10.lang.Object &&
                ! (o instanceof ValueType)) {
            hereCheckPlace(((x10.lang.Object) o).getLocation());
        }
        return o;
    }

	/**
	 * Method used to do dynamic nullcheck when nullable is casted away.
	 */
	public static java.lang.Object nullCheck(java.lang.Object o) {
		if (o == null)
			throw new ClassCastException("Cast of value 'null' to non-nullable type failed.");
		return o;
	}

	/**
	 * Method used to do dynamic nullcheck when nullable is casted away.
	 */
	public static java.lang.Object placeCheck(java.lang.Object o,
			x10.lang.place p) {
		if (o == null)
			throw new ClassCastException("Place-cast of value 'null' failed.");
		if (!(o instanceof x10.lang.Object))
			throw new Error(
					"Place-cast currently not available for object of type "
							+ o.getClass().getName());
		x10.lang.Object xo = (x10.lang.Object) o;
		if (!xo.getLocation().equals(p))
			throw new BadPlaceException(xo, here());
		return o;
	}

	public static Activity getCurrentActivity() {
		return runtime.currentActivity();
	}

	public static Place[] places() {
		Place[] pl = runtime.getPlaces();
		Place[] ret = new Place[pl.length];
		System.arraycopy(pl, 0, ret, 0, pl.length);
		return ret;
	}

	public static void runAsync(Activity a) {
		runtime.getPlaces()[0].runAsync(a);
	}

	/**
	 * runBootAsync is like runAsync, except that it is only used for the boot activity
	 */
	public static void runBootAsync(Activity a) {
		runtime.getPlaces()[0].runAsync(a); //runBootAsync(a);
	}

	public static Future runFuture(Activity.Expr a) {
		return runtime.getPlaces()[0].runFuture(a);
	}

	/**
	 * Implementation of "==" for value types.
	 * TODO: vj, implement for value arrays.
	 * @param o1
	 * @param o2
	 * @return true iff the values are value-equals (all fields have
	 *    the same value)
	 */
	public static boolean equalsequals(java.lang.Object o1, java.lang.Object o2) {
		if (o1 == o2)
			return true;
		if ((o1 == null) || (o2 == null))
			return false;
		Class c = o1.getClass();
		if ((o1 instanceof Indexable) && (o2 instanceof Indexable)) {
			Indexable i1 = (Indexable) o1;
			Indexable i2 = (Indexable) o2;
			if (!(i1.isValue() && i2.isValue()))
				return false;
			return i1.valueEquals(i2);
		}
		if (c != o2.getClass())
			return false;
		if (!(o1 instanceof ValueType))
			return false;
		try {
			while (c != null) {
				Field[] fs = c.getDeclaredFields();
				for (int i = fs.length - 1; i >= 0; i--) {
					Field f = fs[i];
					if (Modifier.isStatic(f.getModifiers()))
						continue;
					f.setAccessible(true);
					if (f.getType().isPrimitive()) {
						if (!f.get(o1).equals(f.get(o2)))
							return false;
					} else {
						// I assume here that value types are immutable
						// and can thus not contain mutually recursive
						// structures.  If that is wrong, we would have to do
						// more work here to avoid dying with a StackOverflow.
						if (!equalsequals(f.get(o1), f.get(o2)))
							return false;
					}
				}
				c = c.getSuperclass();
				if ((c == java.lang.Object.class)
						|| (c == x10.lang.Object.class))
					break; // otherwise we get problems with fields like 'place' in X10Object
			}
		} catch (IllegalAccessException iae) {
			throw new Error(iae); // fatal, should never happen
		}
		return true;
	}

} // end of Runtime