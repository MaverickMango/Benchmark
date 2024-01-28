package root.entities;

import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class Difference {
    private final Patch patch;
    private final List<Pair<Node, Node>> diffBetweenBugAndPatch;//a=buggy node, b=patched node
    private final List<Pair<Node, Node>> diffBetweenInducingAndOrg;//a=inducing node, b=org node

    public Difference(Patch patch) {
        this.patch = patch;
        this.diffBetweenBugAndPatch = new ArrayList<>();
        this.diffBetweenInducingAndOrg = new ArrayList<>();
    }

    public void addDiffBetweenBugAndPatch(List<Pair<Node, Node>> diffBetweenBugAndPatch) {
        this.diffBetweenBugAndPatch.addAll(diffBetweenBugAndPatch);
    }

    public void addDiffBetweenInducingAndOrg(List<Pair<Node, Node>> diffBetweenInducingAndOrg) {
        this.diffBetweenInducingAndOrg.addAll(diffBetweenInducingAndOrg);
    }

    public void addDiffBetweenBugAndPatch(Node buggyNode, Node patchedNode) {
        this.diffBetweenBugAndPatch.add(new Pair<>(buggyNode, patchedNode));
    }

    public void addDiffBetweenBugAndPatch(Pair<Node, Node> pair) {
        this.diffBetweenBugAndPatch.add(pair);
    }

    public void addDiffBetweenInducingAndOrg(Pair<Node, Node> pair) {
        this.diffBetweenInducingAndOrg.add(pair);
    }

    public List<Pair<Node, Node>> getDiffBetweenBugAndPatch() {
        return diffBetweenBugAndPatch;
    }

    public List<Pair<Node, Node>> getDiffBetweenInducingAndOrg() {
        return diffBetweenInducingAndOrg;
    }

    public Patch getPatch() {
        return patch;
    }

}
