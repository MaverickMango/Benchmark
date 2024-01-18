package root.analysis.groum.extractor;

import root.analysis.groum.entity.AbstractNode;
import root.analysis.groum.entity.IntraGroum;
import root.analysis.groum.entity.InvolvedVar;
import root.analysis.groum.vector.Exaser;
import root.analysis.groum.vector.Feature;
import root.util.FileUtils;

import java.util.*;

public class GraphMerger {

    Exaser exaser = new Exaser();

    /**
     * parallel merge(X V Y) will get a new groum that contains all nodes and edges of X and Y, and there is no edge between any nodes of X and Y.
     * @param X: one groum
     * @param Y: another groum
     * @return
     */
    public IntraGroum parallelMerge(IntraGroum X, IntraGroum Y) {
        if (X == Y || X == null) {
            return Y;
        }
        if (Y != null) {
            if (X.getNodes().size() == 1) {
                newNode(X, X.getNodes().get(0));
            }
            Y.getNodes().forEach(n -> newNode(X, n));
        }
        return X;
    }

    /**
     * sequential merge(X => Y) will get a new groum like parallel merge with additional edges from
     * each sink node from X to each source node of Y, which edges represent the temporal usage order.
     * @param X: a head groum
     * @param Y: a tail groum
     * @return
     */
    public IntraGroum sequentialMerge(IntraGroum X, IntraGroum Y) {
        if (X == Y || X == null) {
            return Y;
        }
        if (Y != null) {
            List<AbstractNode> sinkNodes = X.getSinkNodes();
            List<AbstractNode> sourceNodes = Y.getSourceNodes();
            for (AbstractNode head : sinkNodes) {
                for (AbstractNode tail : sourceNodes) {
                    linkNodesWithDataDependency(X, head, tail);
                }
            }
            X.extendNodes(Y.getNodes());
        }
        return X;
    }

    private void newNode(IntraGroum groum, AbstractNode node) {
        groum.addNode(node);
        exaser.newNode(node);
    }

    public void linkNodesWithDataDependency(IntraGroum groum, AbstractNode head, AbstractNode tail) {
        if (head.equals(tail)) {
            //todo:使用 + 压缩，而不是直接省略
            return;
        }
        if (head.isTerminal()) {
            return;
        }
        if (!groum.getNodes().contains(head)) {
            newNode(groum, head);
        }
        exaser.incrVector(groum, head, tail);//提取vector
        //顺联结构的头尾节点应该具有数据依赖
        head.addOutgoingEdges(tail);
        tail.addIncomingEdges(head);
    }

    public void buildlFinalGroum(IntraGroum temporary) {
//        Map<String, List<AbstractNode>> varMap = new HashMap<>();
//        for (AbstractNode node: temporary.getNodes()) {
//            Set<InvolvedVar> attributes = node.getAttributes();
//            for (InvolvedVar attr :attributes) {
//                if (!varMap.containsKey(attr.toString())) {
//                    varMap.put(attr.toString(), new ArrayList<>());
//                }
//                varMap.get(attr.toString()).add(node);
//            }
//        }
//        for (Map.Entry<String, List<AbstractNode>> entry :varMap.entrySet()) {
//            
//        }=
        List<AbstractNode> nodes = temporary.getNodes();
        List<AbstractNode> terminals = temporary.getTerminals();
        for (int i = 0; i < nodes.size(); i++) {
            AbstractNode one = nodes.get(i);
            Set<InvolvedVar> oneSet = one.getAttributes();
            if (terminals.contains(one) || oneSet.isEmpty())
                continue;
            for (int j = i + 1; j < nodes.size(); j++) {
                AbstractNode another = nodes.get(j);
                Set<InvolvedVar> anotherSet = another.getAttributes();
                if (anotherSet.isEmpty())
                    continue;
                // 如果head和tail具有相同的involved变量，则两者具有数据依赖
                Collection<?> intersection = FileUtils.intersection(oneSet, anotherSet);
                if (!intersection.isEmpty()) {
                    linkNodesWithDataDependency(temporary, one, another);
                }
            }
        }
    }

    public List<Integer> getVector() {
        return exaser.getVector();
    }

}
