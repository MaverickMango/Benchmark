package root.analysis;

import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
//import soot.G;

//import soot.Scene;
//import soot.SootClass;
//import soot.SootMethod;
//import soot.options.Options;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.Project;
import sootup.core.cache.provider.LRUCacheProvider;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.ReferenceType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.*;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaModuleView;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SootAnalysisTest {

    @Test
    public void createByteCodeProject() {
        AnalysisInputLocation<JavaSootClass> inputLocation =
                new JavaClassPathAnalysisInputLocation("build/classes/java/test");//用sourceCode会出错

        JavaLanguage language = new JavaLanguage(8);

        Project<JavaSootClass, JavaView> project =
                JavaProject.builder(language).
//                        addInputLocation(new JavaClassPathAnalysisInputLocation(System.getProperty("java.home") + "/lib/jrt-fs.jar")).
                        addInputLocation(inputLocation).build();

        JavaView view = project.createView(new LRUCacheProvider<>(50));//最多存最近的50个（least recently used）

        Assertions.assertNotNull(view);

        // Retrieve class
        ClassType classType = view.getIdentifierFactory().getClassType("FileRead");

        // Create a signature for the method we want to analyze
        MethodSignature methodSignature =
                view.getIdentifierFactory()
                        .getMethodSignature(
                                classType, "read", "void", Collections.singletonList("java.io.File"));

        // Assert that class is present
        Assertions.assertNotNull(classType);

        SootClass<JavaSootClassSource> sootClass = view.getClass(classType).get();
//        sootClass.getMethods();

        // Retrieve method
        Optional<? extends SootMethod> method = view.getMethod(methodSignature);

        // Alternatively:
        Assertions.assertTrue(method.isPresent());
        SootMethod sootMethod = method.get();

        // Read jimple code of method
        System.out.println(sootMethod.getBody());

        // Assert that Hello world print is present
        Assertions.assertTrue(sootMethod.getBody().getStmts().stream()
                .anyMatch(
                        stmt ->
                                stmt instanceof JInvokeStmt
                                        && stmt.getInvokeExpr() instanceof JVirtualInvokeExpr
                                        && stmt.getInvokeExpr()
                                        .getArg(0) != null));
    }

    @Test
    public void callGraphTest() {
        AnalysisInputLocation<JavaSootClass> inputLocation =
                new JavaClassPathAnalysisInputLocation("build/classes/java");//用sourceCode会出错
//        AnalysisInputLocation<JavaSootClass> cp =
//                new JavaClassPathAnalysisInputLocation("build/classes/java/test");//用sourceCode会出错

        JavaLanguage language = new JavaLanguage(11);

        Project<JavaSootClass, JavaView> project =
                JavaProject.builder(language).
                        addInputLocation(new JrtFileSystemAnalysisInputLocation()).
//                        addInputLocation(cp).
//                        addInputLocation(new JavaClassPathAnalysisInputLocation(System.getProperty("java.home"))).
                        addInputLocation(inputLocation).build();
        JavaView view = project.createView(new LRUCacheProvider<>(50));//最多存最近的50个（least recently used）
        // Retrieve class
        ClassType classType = JavaModuleIdentifierFactory.getInstance().getClassType("test.root.analysis.groum.vector.ExaserTest");
//        ClassType classType1 = view.getIdentifierFactory().getClassType("java.lang.Object");
//        JavaClassType classType1 = JavaModuleIdentifierFactory.getInstance().getClassType("java.base.java.lang.Object");
//        view.getClass(classType1).get();

        // Create a signature for the method we want to analyze
        MethodSignature entryMethodSignature =
                JavaIdentifierFactory.getInstance()
                        .getMethodSignature(
                                classType,
                                JavaIdentifierFactory.getInstance()
                                        .getMethodSignature(classType
                                                , "getExampleRead"
                                                , "java.util.LinkedHashMap"
                                                , List.of(
                                                        new String[]{"boolean"})
                                        ).getSubSignature());
//                                        .getMethodSubSignature(
//                                                "initialize"
//                                                , new JavaClassType("ProjectPreparation"
//                                                        , new PackageName("root.generation"))
//                                                , new ArrayList<>()));

        SootClass<JavaSootClassSource> sootClass = view.getClass(classType).get();
        Optional<? extends SootMethod> method = view.getMethod(entryMethodSignature);
        Assertions.assertTrue(method.isPresent());
        SootMethod sootMethod = method.get();

        //        CallGraphAlgorithm cha =
//                new RapidTypeAnalysisAlgorithm(view);//仅考虑实例化了的接口的实现，ClassHierarchyAnalysisAlgorithm更完善
        CallGraphAlgorithm cha =
                new ClassHierarchyAnalysisAlgorithm(view);

        CallGraph cg =
                cha.initialize(Collections.singletonList(entryMethodSignature));

        System.out.println(cg);
    }

//    @Test
//    public void sootTest() {
//        G.reset();
//        String userdir = System.getProperty("user.dir");
//        String sootCp = System.getProperty("java.home") +File.separator+ "lib"+File.separator+"jrt-fs.jar"
//                        + File.pathSeparator
//                        + userdir
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
