import java.util.Iterator;
/**
 * Testing the standard region iterator.
 */
public class RegionTestIterator {

	public boolean run() {
		region r = region.factory.region(0,100); // (low,high)
		region[] r2 = new region[] {r,r};
		region reg = region.factory.region(r2);
		
		int sum = 0;
		for (Iterator it = reg.iterator(); it.hasNext();) {
			point p = (point) it.next();
			sum  += p[0]-p[1];
		}
		// for(point [i,j]:reg) sum += i - j;
		return sum == 0;
	}
	
    public static void main(String[] args) {
        final boxedBoolean b=new boxedBoolean();
        try {
                finish async b.val=(new RegionTestIterator()).run();
        } catch (Throwable e) {
                e.printStackTrace();
                b.val=false;
        }
        System.out.println("++++++ "+(b.val?"Test succeeded.":"Test failed."));
        x10.lang.Runtime.setExitCode(b.val?0:1);
    }
    static class boxedBoolean {
        boolean val=false;
    }


}