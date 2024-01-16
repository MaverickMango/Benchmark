package root.analysis.groum.entity;

import root.util.FileUtils;

import java.util.*;

public class Groum {
    List<AbstractNode> nodes;

    public Groum() {
        this.nodes = new ArrayList<>();
    }

    public Groum(AbstractNode node) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Groum) {
            Groum that = (Groum) obj;
            if (this.getNodes().size() != that.getNodes().size())
                return false;
            Collection<?> difference = FileUtils.difference(this.getNodes(), that.getNodes());
            if (difference.isEmpty())
                return true;
        }
        return super.equals(obj);
    }
}
