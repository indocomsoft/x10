import harness.x10Test;

public class XTENLANG_3140_MustFailCompile /*extends x10Test*/ {

    public static def g() throws java.io.IOException { }

    public static def f() {
        g();
    }

/*
    public static def run() : Boolean = true;

    public static def main(args:Rail[String]) { }
*/
}

// vim: shiftwidth=4:tabstop=4:expandtab

