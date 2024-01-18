package root.analysis.groum.visualizer;

import com.github.javaparser.utils.Pair;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.groum.entity.*;
import root.analysis.groum.extractor.ExtractFromJavaParser;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Label.Justification.LEFT;
import static guru.nidi.graphviz.attribute.Label.Justification.MIDDLE;
import static guru.nidi.graphviz.model.Factory.*;

public class Graphvizer {

    final static Logger logger = LoggerFactory.getLogger(Graphvizer.class);
    int i = 0;

    public Pair<AbstractNode, Node> getNode(AbstractNode node, Map<AbstractNode, Node> map) {
        if (node == null) {
            return null;
        }
        if (map.containsKey(node)) {
            return new Pair<>(node, map.get(node));
        }
        String label = node.getLabel().replace("<", "&lt;").replace(">", "&gt;");
        Set<InvolvedVar> attributes = node.getAttributes();
        StringBuilder attrs = new StringBuilder();
        for (InvolvedVar attr : attributes) {
            attrs.append("<br/>").append(attr.toString().replace("<", "&lt;").replace(">", "&gt;"));
        }

        Node graphNode = node(String.valueOf(i++)).with(
//                Label.lines(MIDDLE, label, attrs.toString())
                Label.html("<b>" + label + "</b><br/>" + attrs)
        );
        if (node instanceof ControlNode) {
            graphNode = graphNode.with(Shape.DIAMOND);
        }
        map.put(node, graphNode);
        return new Pair<>(node, graphNode);
    }

    public void getGraph(IntraGroum groum) throws IOException {
        Graph g = graph("groum").directed();

        Map<AbstractNode, Node> map = new HashMap<>();
        for (AbstractNode node :groum.getNodes()) {
            Node graphNode = getNode(node, map).b;
            Set<AbstractNode> toEdges = node.getToEdges();
            List<Node> target = toEdges.stream().map(n -> getNode(n, map).b).collect(Collectors.toList());
            g = g.with(graphNode.link(target));
            if (node instanceof ControlNode) {
                List<AbstractNode> scope = ((ControlNode) node).getScope();
                List<Node> tmp = scope.stream().map(s -> getNode(s, map).b).collect(Collectors.toList());
                tmp.add(graphNode);
                g = g.with(graph(String.valueOf(i++)).cluster().with(tmp));
            }
        }
        String filePath = "example/groum.svg";
        Graphviz.fromGraph(g).render(Format.SVG).toFile(new File(filePath));
        logger.info("Graph has been stored in " + filePath);
    }
}
