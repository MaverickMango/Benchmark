package root.analysis.groum.extractor;

import root.analysis.groum.entity.AbstractNode;
import root.analysis.groum.entity.Groum;
import root.analysis.groum.entity.InvolvedVar;
import root.util.FileUtils;

import java.util.Collection;
import java.util.Set;

public class MergeHelper {

    /**
     * parallel merge(X V Y) will get a new groum that contains all nodes and edges of X and Y, and there is no edge between any nodes of X and Y.
     * @param X: one groum
     * @param Y: another groum
     * @return
     */
    public static Groum parallelMerge(Groum X, Groum Y) {
        if (X == Y || X == null) {
            return Y;
        }
        if (Y != null) {
            X.extendNodes(Y.getNodes());
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
    public static Groum sequentialMerge(Groum X, Groum Y) {
        if (X == Y || X == null) {
            return Y;
        }
        if (Y != null) {
            for (AbstractNode head : X.getNodes()) {
                for (AbstractNode tail : Y.getNodes()) {
                    linkNodesWithDataDependency(head, tail);
                }
            }
        }
        return parallelMerge(X, Y);
    }

    public static void linkNodesWithDataDependency(AbstractNode head, AbstractNode tail) {
        if (head.equals(tail)) {
            //todo:使用 + 压缩，而不是直接省略
            return;
        }
        Set<InvolvedVar> x = head.getAttributes();
        Set<InvolvedVar> y = tail.getAttributes();
        Collection<?> difference = FileUtils.difference(x, y);
        //顺联结构的头尾节点应该具有数据依赖
        // 如果head和tail具有相同的involved变量，则两者具有数据依赖
        if (head.isSinkNode() && tail.isSourceNode() ||
                !difference.isEmpty()) {
            head.addToEdges(tail);
            tail.addFromEdges(head);
        }
    }

}
