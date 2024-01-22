package root.analysis.groum.vector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import root.AbstractMain;
import root.analysis.groum.Graphvizer;
import root.analysis.groum.entity.ActionNode;
import root.analysis.groum.entity.IntraGroum;
import root.analysis.groum.entity.InvolvedVar;
import root.analysis.groum.extractor.GraphMerger;
import root.analysis.groum.extractor.PreOrderVisitorInMth;
import root.generation.ProjectPreparation;
import root.parser.ASTJavaParser;
import root.util.CommandSummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ExaserTest {
    GraphMerger graphMerger = new GraphMerger();

    IntraGroum getExampleA() {
        ActionNode node1 = new ActionNode(null, "in", "");
        InvolvedVar var = new InvolvedVar("tmp", "");
        node1.addAttribute(var);
        IntraGroum groum = new IntraGroum(node1);

        ActionNode node2 = new ActionNode(null, "in", "");
        groum = graphMerger.parallelMerge(groum, new IntraGroum(node2));

        ActionNode node6 = new ActionNode(null, "mul", "");
        groum = graphMerger.sequentialMerge(groum, new IntraGroum(node6));

        ActionNode node5 = new ActionNode(null, "gain", "");
        graphMerger.linkNodesWithDataDependency(groum, node1, node5);
        groum.addNode(node5);

        ActionNode node9 = new ActionNode(null, "sum", "");
        groum = graphMerger.sequentialMerge(groum, new IntraGroum(node9));
        return groum;
    }

    private ProjectPreparation setting() {
        CommandSummary cs = new CommandSummary();
        ProjectPreparation projectPreparation;
        String filePath;// = "/home/liumengjiao/Desktop/CI/bugs/Closure_10_bug/test/com/google/javascript/jscomp/PeepholeFoldConstantsTest.java";
        String methodName;// = "testIssue821";
        int lineNumber;// = 582;
        cs.append("-location", "./");
        cs.append("-srcJavaDir", "src/main/java");
        cs.append("-srcTestDir", "src/test/java");
        cs.append("-binJavaDir", "build/classes/java/main");
        cs.append("-binTestDir", "build/classes/java/test");
        cs.append("-complianceLevel", "1.8");
        AbstractMain main = new AbstractMain();
        projectPreparation = main.initialize(cs.flat());
        return projectPreparation;
    }

    private LinkedHashMap<Feature, Integer> getExampleRead(boolean isFinal) throws IOException {
        ASTJavaParser parser = (ASTJavaParser) setting().parser;
        parser.parseASTs("src/test/java/FileRead.java");
        Map<String, Object> asts = parser.getASTs();
        PreOrderVisitorInMth visitor = new PreOrderVisitorInMth();
        for (String key :asts.keySet()) {
            CompilationUnit o = (CompilationUnit) asts.get(key);
            List<BodyDeclaration<?>> collect = o.getType(0).getMembers().stream().filter(b -> b instanceof MethodDeclaration).collect(Collectors.toList());
            ArrayList<IntraGroum> arg = new ArrayList<>();
            visitor.buildGraph(collect.get(0), arg, isFinal);
            assert arg.size() == 1;
            if (arg.get(0).getNodes().size() > 3) {
                String filePath = "example/example.svg";
                Graphvizer er = new Graphvizer();
                er.getGraph(arg.get(0), filePath);
                return visitor.getFeatures();
            }
        }
        return null;
    }

    @Test
    public void testGraph() throws IOException {
        IntraGroum groum = getExampleA();
        String filePath = "example/exas.svg";
        Graphvizer er = new Graphvizer();
        er.getGraph(groum, filePath);
    }

    @Test
    public void testExas() {
        IntraGroum groum = getExampleA();
        List<Integer> vector = graphMerger.getVector();
        List<Integer> expected = List.of(2, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1);
        Assertions.assertEquals(expected.size(), vector.size());
        Assertions.assertTrue(vector.containsAll(expected));
    }

    @Test
    public void testRead() throws IOException {
        LinkedHashMap<Feature, Integer> exampleRead = getExampleRead(false);
        List<Feature> collect = exampleRead.keySet().stream().filter(f -> f.isNode).collect(Collectors.toList());
        Assertions.assertEquals(13, collect.size());
        for (int i = 0; i < 13; i ++) {
            int finalI = i;
            collect = exampleRead.keySet().stream().filter(f -> !f.isNode && f.getLength() == (1 + finalI)).collect(Collectors.toList());
            Assertions.assertEquals(13 - i, collect.size(), "the size of " + (1 + i) + " path should be " + (13 - i));
        }
    }

    @Test
    public void testReadFinalGraph() throws IOException {
        LinkedHashMap<Feature, Integer> exampleRead = getExampleRead(true);
        List<Feature> collect = exampleRead.keySet().stream().filter(f -> f.isNode).collect(Collectors.toList());
        for (int i = 1; i < 2; i ++) {
            int finalI = i;
            collect = exampleRead.keySet().stream().filter(f -> !f.isNode && f.getLength() == (1 + finalI)).collect(Collectors.toList());
            int expected = 12;
            switch (i) {
                case 1: expected += 9;
            }
            Assertions.assertEquals(expected, collect.size(), "the size of " + (1 + i) + " path should be " + expected);
        }
    }

}