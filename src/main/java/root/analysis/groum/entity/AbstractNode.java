package root.analysis.groum.entity;

import com.github.javaparser.ast.Node;
import root.util.FileUtils;

import java.util.*;

public abstract class AbstractNode {
    String label;
    Set<InvolvedVar> attributes;//包含所有涉及到的变量
    Node originalNode;
    Set<AbstractNode> toEdges;//each edge means a (temporal) usage order and a data dependency, the list stores the sink nodes.
    Set<AbstractNode> fromEdges;//the list stores the source nodes

    public AbstractNode(Node originalNode) {
        this.toEdges = new HashSet<>();
        this.fromEdges = new HashSet<>();
        this.attributes = new HashSet<>();
        this.originalNode = originalNode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<AbstractNode> getToEdges() {
        return toEdges;
    }

    public void addToEdges(AbstractNode tailNode) {
        this.toEdges.add(tailNode);
    }

    public Set<AbstractNode> getFromEdges() {
        return fromEdges;
    }

    public void addFromEdges(AbstractNode headNode) {
        this.fromEdges.add(headNode);
    }

    public boolean isSinkNode() {
        return toEdges.isEmpty();
    }

    public boolean isSourceNode() {
        return fromEdges.isEmpty();
    }

    public Set<InvolvedVar> getAttributes() {
        return attributes;
    }

    public void addAttribute(InvolvedVar attribute) {
        this.attributes.add(attribute);
    }

    public void addAttributes(Set<InvolvedVar> attributes) {
        if (attributes == null)
            return;
        this.attributes.addAll(attributes);
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractNode) {
            if (!this.getLabel().equals(((AbstractNode) obj).getLabel()))
                return false;
            Collection<?> difference = FileUtils.difference(this.getAttributes(), ((AbstractNode) obj).getAttributes());
            if (difference.isEmpty())
                return true;
        }
        return super.equals(obj);
    }
}
