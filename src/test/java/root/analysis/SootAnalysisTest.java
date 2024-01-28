package root.analysis;

import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//import soot.G;
//import soot.Scene;
//import soot.SootClass;
//import soot.SootMethod;
//import soot.options.Options;
public class SootAnalysisTest {

//    @Test
//    public void sootTest() {
//        G.reset();
//        String userdir = System.getProperty("user.dir");
//        String sootCp = //System.getProperty("java.home") +File.separator+ "lib"+File.separator+"jrt-fs.jar" +File.pathSeparator +
//                        userdir
//                        + File.separator
//                        + "build/classes/java/test";
////                        + File.pathSeparator
////                        + "/home/liumengjiao/Desktop/CI/Benchmark/libs/soot-4.4.1-jar-with-dependencies.jar";
//
//        Options.v().set_soot_classpath(sootCp);
//        Options.v().set_whole_program(true);
//        Options.v().setPhaseOption("cg.cha", "on");
//        Options.v().setPhaseOption("cg", "all-reachable:true");
//        Options.v().set_no_bodies_for_excluded(true);
//        Options.v().set_allow_phantom_refs(true);
//        Options.v().setPhaseOption("jb", "use-original-names:true");
//        Options.v().set_prepend_classpath(false);
//
////        Scene.v().addBasicClass("java.lang.StringBuilder");
//        SootClass c =
//                Scene.v().forceResolve("FileRead", SootClass.BODIES);
//        if (c != null) {
//            c.setApplicationClass();
//        }
//        Scene.v().loadNecessaryClasses();
//
//        SootMethod method = null;
//        for (SootClass tmp : Scene.v().getApplicationClasses()) {
//            if(tmp.getName().equals("FileRead")){
//                for (SootMethod m : tmp.getMethods()) {
////                    if (!m.hasActiveBody()) {
////                        continue;
////                    }
//                    if (m.getName().equals("read")) {
//                        method = m;
//                        break;
//                    }
//                }
//            }
//        }
//        Assertions.assertNotNull(method);
//    }

    @Ignore
    public void testCallGraph() {

//        method.getActiveBody().getUnits();
//        String targetTestClassName = target.exercise1.Hierarchy.class.getName();
//        G.reset();
//        String userdir = System.getProperty("user.dir");
//        String sootCp = userdir + File.separator + "target" + File.separator + "test-classes"+ File.pathSeparator + "lib"+File.separator+"rt.jar";
//        Options.v().set_whole_program(true);
//        Options.v().set_soot_classpath(sootCp);
//        Options.v().set_no_bodies_for_excluded(true);
//        Options.v().process_dir();
//        Options.v().set_allow_phantom_refs(true);
//        Options.v().setPhaseOption("jb", "use-original-names:true");
//        Options.v().set_prepend_classpath(false);
//        SootClass c = Scene.v().forceResolve(targetTestClassName, SootClass.BODIES);
//        if (c != null)
//            c.setApplicationClass();
//        Scene.v().loadNecessaryClasses();
//
//        Hierarchy hierarchy = new Hierarchy();
    }
}
