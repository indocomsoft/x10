package x10.emitter;

import java.util.Iterator;
import java.util.List;

import polyglot.ext.x10.visit.X10PrettyPrinterVisitor;
import polyglot.visit.Translator;

/**
 * Expand a given template in a loop with the given set of arguments.
 * For the loop body, pass in an array of Lists of identical length
 * (each list representing all instances of a given argument),
 * which will be translated into array-length repetitions of the
 * loop body template.
 * If the template has only one argument, a single list can be used.
 */
public class Loop extends Expander {
	/**
	 * 
	 */

	private final String id;
	//private final String template;
	private final List[] lists;
	private final int N;
	public Loop(Emitter er, String id, List arg) {
		this(er, id, new List[] { arg });
	}
	public Loop(Emitter er, String id, List arg1, List arg2) {
		this(er, id, new List[] { arg1, arg2 });
	}
	public Loop(Emitter er, String id, List arg1, List arg2, List arg3) {
		this(er, id, new List[] { arg1, arg2, arg3 });
	}
	public Loop(Emitter er, String id, List arg1, List arg2, List arg3, List arg4) {
		this(er, id, new List[] { arg1, arg2, arg3, arg4 });
	}
	public Loop(Emitter er, String id, List[] components) {
		super(er);
	
		this.id = id;
		//this.template = translate(id);
		this.lists = components;
		// Make sure we have at least one parameter
		assert(lists.length > 0);
		int n = -1;
		int i = 0;
		for (; i < lists.length && n == -1; i++)
			n = lists[i].size();
		// Make sure the lists are all of the same size or circular
		for (; i < lists.length; i++)
			assert(lists[i].size() == n || lists[i].size() == -1);
		this.N = n;
	}
	public void expand(Translator tr) {
		er.w.write("/* Loop: { */");
		Object[] args = new Object[lists.length];
		Iterator[] iters = new Iterator[lists.length];
		// Parallel iterators over all argument lists
		for (int j = 0; j < lists.length; j++)
			iters[j] = lists[j].iterator();
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < args.length; j++)
				args[j] = iters[j].next();
			er.dump(id, args, tr);
		}
		er.w.write("/* } */");
	}
}