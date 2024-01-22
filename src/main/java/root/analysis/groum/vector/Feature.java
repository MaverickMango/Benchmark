package root.analysis.groum.vector;

import root.analysis.groum.entity.AbstractNode;
import root.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class Feature {
    AbstractNode root;
    List<Object> labelComponents;
    boolean isNode;//如果是node，则labelComponents的大小恒等于3

    public Feature(AbstractNode root) {//path
        this.isNode = false;
        labelComponents = new ArrayList<>();
        this.root = root;
    }

    public Feature(AbstractNode root, int incomingSize, int outgoingSize) {//node
        this.isNode = true;
        this.root = root;
        labelComponents = new ArrayList<>();
        labelComponents.add(String.valueOf(incomingSize));
        labelComponents.add(String.valueOf(outgoingSize));
    }

    public AbstractNode getRoot() {
        return root;
    }

    public void setRoot(AbstractNode root) {
        this.root = root;
    }

    public boolean isNode() {
        return isNode;
    }

    /**
     * 对于Node feature才有 replaceLabel操作
     * @param newComponent 新名字
     * @param idx 被替换的component的位置，1为incoming size， 2为outgoing size，其他数值无效。
     */
    public void replaceLabel(String newComponent, int idx) {
        if (!isNode)
            return;
        if (idx != 1 && idx != 2)//i和2是标签的位置
            return;
        labelComponents.remove(idx - 1);
        labelComponents.add(idx - 1, newComponent);
    }

    public List<Object> getPathNode() {
        if (isNode)
            return null;
        List<Object> list = new ArrayList<>();
        list.add(root);
        list.addAll(labelComponents);
        return list;
    }

    public List<Object> getLabelComponents() {
        return labelComponents;
    }

    public void addComponent(Object newComponent) {
        labelComponents.add(newComponent);
    }

    public void addComponents(List<Object> newComponents) {
        labelComponents.addAll(newComponents);
    }

    public int getLength() {
        if (isNode)
            return -1;
        return labelComponents.size() + 1;
    }

    public boolean startNodeIs(Object labelComponent) {
        if (isNode)
            return false;
        return root.toString().equals(labelComponent.toString());
    }

    public boolean endNodeIs(Object labelComponent) {
        if (isNode)
            return false;
        if (labelComponents.size() < 1)
            return root.toString().equals(labelComponent.toString());
        return labelComponents.get(labelComponents.size() - 1).toString().equals(labelComponent.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Feature) {
            Feature that = (Feature) obj;
            if (this.isNode != that.isNode)
                return false;
            if (!isNode) {
                if (this.getLabelComponents().size() != that.getLabelComponents().size())
                    return false;
            } else {
                if (!this.root.equals(that.root))
                    return false;
            }
            if (this.toString().equals(that.toString()))
                return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.toString().hashCode();
        return result;
    }

    @Override
    public String toString() {
        String separator = "-";
        StringBuilder components = FileUtils.getStrOfIterable(labelComponents, separator);
        String str = root.toString();
        if (components.length() > 0) {
            components.deleteCharAt(components.length() - 1);
            str += separator + components;
        }
        return str;
    }
}
