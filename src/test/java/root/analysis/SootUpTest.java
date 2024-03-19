package root.analysis;

import org.junit.Test;
import root.analysis.groum.Graphvizer;
import root.analysis.soot.SootUpAnalyzer;
import sootup.callgraph.*;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.*;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;


public class SootUpTest {
    protected JavaIdentifierFactory identifierFactory;
    protected JavaClassType mainClassSignature;
    protected MethodSignature mainMethodSignature;
    private String algorithmName;

    protected AbstractCallGraphAlgorithm createAlgorithm(JavaView view) {
        if (algorithmName.equals("RTA")) {
            return new RapidTypeAnalysisAlgorithm(view);
        } else {
            return new ClassHierarchyAnalysisAlgorithm(view);
        }
    }

    private JavaView createViewForClassPath(List<String> classPaths) {
        List<AnalysisInputLocation> inputLocations = new ArrayList<>();
//        inputLocations.add(new DefaultRTJarAnalysisInputLocation());
        classPaths.forEach(cp -> inputLocations.add(new JavaClassPathAnalysisInputLocation(cp)));

        JavaView javaView = new JavaView(inputLocations);
        identifierFactory = javaView.getIdentifierFactory();
        return javaView;
    }

    CallGraph loadCallGraph() {
//        double version = Double.parseDouble(System.getProperty("java.specification.version"));
//        if (version > 1.8) {
//            fail("The rt.jar is not available after Java 8. You are using version " + version);
//        }

        List<String> classPaths = new ArrayList<>();
        classPaths.add("build/classes/java/test/");
        classPaths.add("build/classes/java/main/");

        // JavaView view = viewToClassPath.computeIfAbsent(classPath, this::createViewForClassPath);
        JavaView view = createViewForClassPath(classPaths);

        mainClassSignature = identifierFactory.getClassType("root.analysis.groum.vector.ExaserTest");
        SootClass sc = view.getClass(mainClassSignature).orElse(null);
        assertNotNull(sc);
        mainMethodSignature =
                identifierFactory.getMethodSignature(
                        mainClassSignature, "getExampleA",
                        "root.analysis.groum.entity.IntraGroum",
                        new ArrayList<>());

        SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
        assertNotNull(mainMethodSignature + " not found in classloader", m);

        AbstractCallGraphAlgorithm algorithm = createAlgorithm(view);
        CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

        assertNotNull(cg);
        assertTrue(
                mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
        System.out.println(cg);
        return cg;
    }

    @Test
    public void testCHA() throws IOException {
        algorithmName = "CHA";
        CallGraph cg = loadCallGraph();
        Graphvizer graphvizer = new Graphvizer();
        graphvizer.outputGraph(cg.exportAsDot(), "example/callgraph.svg");

        MethodSignature inner =
                identifierFactory.getMethodSignature(
                        identifierFactory.getClassType("root.analysis.groum.extractor.GraphMerger"),
                        "linkNodesWithDataDependency",
                        "void",
                        List.of(new String[]{"root.analysis.groum.entity.IntraGroum",
                                "root.analysis.groum.entity.AbstractNode",
                                "root.analysis.groum.entity.AbstractNode"}));

        assertTrue(cg.containsCall(mainMethodSignature, inner));
    }

//    @Test
//    public void testPath() throws IOException {
//        algorithmName = "CHA";
//        CallGraph cg = loadCallGraph();
//
//        MethodSignature outer =
//                identifierFactory.getMethodSignature(
//                        identifierFactory.getClassType("root.analysis.groum.vector.Exaser"),
//                        "incrVector",
//                        "void",
//                        List.of(new String[]{"root.analysis.groum.entity.IntraGroum",
//                                "root.analysis.groum.entity.AbstractNode",
//                                "root.analysis.groum.entity.AbstractNode"}));
//        List<List<MethodSignature>> entryToOut = new ArrayList<>();
//        List<MethodSignature> path = new ArrayList<>();
//        SootUpAnalyzer.backForwardDfs(cg, mainMethodSignature, outer, path, entryToOut, new HashSet<>());
//        assertEquals(4, entryToOut.size());
//        assertFalse(cg.containsCall(mainMethodSignature, outer));
//    }
}
