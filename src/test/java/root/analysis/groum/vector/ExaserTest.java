package root.analysis.groum.vector;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import root.analysis.groum.Graphvizer;
import root.analysis.groum.entity.ActionNode;
import root.analysis.groum.entity.IntraGroum;
import root.analysis.groum.entity.InvolvedVar;
import root.analysis.groum.extractor.GraphMerger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

}