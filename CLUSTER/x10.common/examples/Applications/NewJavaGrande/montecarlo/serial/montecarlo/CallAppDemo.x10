/**
 * X10 port of montecarlo benchmark from Section 2 of Java Grande Forum Benchmark Suite (Version 2.0)
 *
 * @author Vivek Sarkar (vsarkar@us.ibm.com)
 *
 * Porting issues identified:
 * 
 * 1) Add nullable type constructor to declaration of ap.  
 *    Note that no non-nullable conversion is needed prior to method invocation on ap.
 */
package montecarlo;
public class CallAppDemo  {
	protected int size;
	// protected int[] datasizes = {10000, 60000};
	protected int[] datasizes = {1000, 60000};
	int input[] = new int[2];
	nullable AppDemo ap = null;
	
	public void initialise () {
		
		input[0] = 1000;
		input[1] = datasizes[size];
		
		String dirName="Data";
		String filename="hitData";
		ap = new AppDemo(dirName, filename,(input[0]),(input[1]));
		ap.initSerial();
	}
	
	public void runiters () {
		ap.runSerial();
	}
	public void presults () {
		ap.processSerial();
	}
	
}