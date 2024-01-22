package root.analysis.groum.entity;

import com.github.javaparser.ast.Node;
import root.util.FileUtils;

import java.util.*;

public abstract class AbstractNode {
    String label;
    Set<InvolvedVar> attributes;//包含所有涉及到的变量
    Node originalNode;
    Set<AbstractNode> outgoingEdges;//each edge means a (temporal) usage order and a data dependency, the list stores the sink nodes.
    Set<AbstractNode> incomingEdges;//the list stores the source nodes

    public AbstractNode(Node originalNode) {
        this.outgoingEdges = new HashSet<>();
        this.incomingEdges = new HashSet<>();
        this.attributes = new HashSet<>();
        this.originalNode = originalNode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<AbstractNode> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void addOutgoingEdges(AbstractNode tailNode) {
        this.outgoingEdges.add(tailNode);
    }

    public Set<AbstractNode> getIncomingEdges() {
        return incomingEdges;
    }

    public void addIncomingEdges(AbstractNode headNode) {
        this.incomingEdges.add(headNode);
    }

    public boolean isTerminal() {
        if (this instanceof ControlNode) {
            ControlNode controlNode = (ControlNode) this;
            return controlNode.type.equals(ControlNode.Type.RETURN) || controlNode.type.equals(ControlNode.Type.Throw);
        }
        return false;
    }

    public boolean isSinkNode() {
        return outgoingEdges.isEmpty();
    }

    public boolean isSourceNode() {
        return incomingEdges.isEmpty();
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

//    public String getPQNodeLabel() {
//        return label + "-" + incomingEdges.size() + "-" + outgoingEdges.size();
//    }
//    public String getPQNodeLabel(int incomingSize, int outgoingSize) {
//        return label + "-" + incomingSize + "-" + outgoingSize;
//    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getLabel().hashCode();
        result = prime * result + this.getAttributes().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractNode) {
            AbstractNode that = (AbstractNode) obj;
            if (!this.getLabel().equals(that.getLabel()))
                return false;
            if (this.getAttributes().size() != that.getAttributes().size())
                return false;
            Collection<?> difference = FileUtils.difference(this.getAttributes(), that.getAttributes());
            return difference.isEmpty();
        }
        return false;
    }
}
