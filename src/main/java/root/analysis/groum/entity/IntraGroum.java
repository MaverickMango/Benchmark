package root.analysis.groum.entity;

import root.util.FileUtils;

import java.util.*;
import java.util.stream.Collectors;

public class IntraGroum {
    List<AbstractNode> nodes;

    public IntraGroum() {
        this.nodes = new ArrayList<>();
    }

    public IntraGroum(AbstractNode node) {
        this.nodes = new ArrayList<>();
        addNode(node);
    }

    public List<AbstractNode> getNodes() {
        return nodes;
    }

    public void addNode(AbstractNode node) {
        this.nodes.add(node);
    }

    public void extendNodes(List<AbstractNode> node) {
        this.nodes.addAll(node);
    }

    public List<AbstractNode> getSinkNodes() {
        List<AbstractNode> sinkNodes = new ArrayList<>();
        for (AbstractNode node :nodes) {
            if (node.isSinkNode()) {
                sinkNodes.add(node);
            }
        }
        return sinkNodes;
    }

    public List<AbstractNode> getSourceNodes() {
        List<AbstractNode> sourceNodes = new ArrayList<>();
        for (AbstractNode node :nodes) {
            if (node.isSourceNode()) {
                sourceNodes.add(node);
            }
        }
        return sourceNodes;
    }

    public List<AbstractNode> getTerminals() {
        List<AbstractNode> nodes = getNodes();
        List<AbstractNode> returns = nodes.stream().filter(AbstractNode::isTerminal).collect(Collectors.toList());
        List<AbstractNode> scopes = new ArrayList<>();
        returns.forEach(n -> {
            List<AbstractNode> tmp = ((ControlNode) n).getScope();
            scopes.addAll(tmp);
        });
        returns.addAll(scopes);
        return returns;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntraGroum) {
            IntraGroum that = (IntraGroum) obj;
            if (this.getNodes().size() != that.getNodes().size())
                return false;
            Collection<?> difference = FileUtils.difference(this.getNodes(), that.getNodes());
            if (difference.isEmpty())
                return true;
        }
        return super.equals(obj);
    }
}
