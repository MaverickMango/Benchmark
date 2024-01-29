package root.entities;

import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;
import root.diff.DiffExtractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Pair<Set<Node>, Set<Node>> getDiffExprInBuggy() {
        List<Pair<Node, Node>> bugAndPatch = getDiffBetweenBugAndPatch();
        Set<Node> bug = bugAndPatch.stream().map(n -> n.a).collect(Collectors.toSet());
        DiffExtractor.filterChildNode(bug);
        Set<Node> pat = bugAndPatch.stream().map(n -> n.b).collect(Collectors.toSet());
        DiffExtractor.filterChildNode(pat);
        Pair<Set<Node>, Set<Node>> minBugAndPat = DiffExtractor.getMinimalDiffNodes(bug, pat);
        return minBugAndPat;
    }

    public Pair<Set<Node>, Set<Node>> getDiffExprInOrg() {
        List<Pair<Node, Node>> inducingAndOrg = getDiffBetweenInducingAndOrg();
        Set<Node> inducing = inducingAndOrg.stream().map(n -> n.a).collect(Collectors.toSet());
        DiffExtractor.filterChildNode(inducing);
        Set<Node> org = inducingAndOrg.stream().map(n -> n.b).collect(Collectors.toSet());
        DiffExtractor.filterChildNode(org);
        Pair<Set<Node>, Set<Node>> minInducingOrg = DiffExtractor.getMinimalDiffNodes(inducing, org);
        return minInducingOrg;
    }
}
