package root.entities;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.utils.Pair;
import root.analysis.groum.entity.IntraGroum;
import root.analysis.groum.extractor.PreOrderVisitorInMth;
import root.analysis.groum.vector.Feature;
import root.analysis.soot.SootUpAnalyzer;
import root.diff.DiffExtractor;
import root.generation.transformation.TransformHelper;
import sootup.core.signatures.MethodSignature;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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

    public List<List<List<MethodSignature>>> getPathFromTestToChange(String testNamePrefix) {
        SootUpAnalyzer analyzer = TransformHelper.bugRepository.analyzer;
        String[] split = testNamePrefix.split("::");
        assert split.length == 2;
        List<List<List<MethodSignature>>> paths = new ArrayList<>();
        MethodSignature test = analyzer.getMethodSignature(split[0], split[1], "void", new ArrayList<>());
        Pair<Set<Node>, Set<Node>> diffExprInBuggy = getDiffExprInBuggy();
        if (!diffExprInBuggy.b.isEmpty()) {
            Set<MethodDeclaration> changed = diffExprInBuggy.b.stream().map(this::getMethodDeclaration).collect(Collectors.toSet());
            for (MethodDeclaration mth :changed) {
                IntraGroum groum = innerAnalysis(mth);
                List<List<MethodSignature>> pathFromEntryToOut = getPath(test, mth, analyzer);
                //todo 这个依赖怎么联系起来阿aaa
                paths.add(pathFromEntryToOut);
            }
        }
        return paths;
    }

    private IntraGroum innerAnalysis(MethodDeclaration mth) {
        PreOrderVisitorInMth visitor = new PreOrderVisitorInMth(false);
        ArrayList<IntraGroum> arg = new ArrayList<>();
        visitor.buildGraph(mth, arg, true);
        assert arg.size() == 1;
        IntraGroum groum = arg.get(0);
        return groum;
    }

    private List<List<MethodSignature>> getPath(MethodSignature entryPoint, MethodDeclaration outerMth, SootUpAnalyzer analyzer) {
        String qualifiedClassName = getQualifiedClassName(outerMth);
        String mthName = outerMth.getNameAsString();
        String typeName = getTypeName(outerMth);
        NodeList<Parameter> parameters = outerMth.getParameters();
        List<String> parTypes = parameters.stream().map(p -> resolveDescriptor(p.getType().toDescriptor())).collect(Collectors.toList());
        MethodSignature methodSignature = analyzer.getMethodSignature(qualifiedClassName, mthName, typeName, parTypes);
        List<List<MethodSignature>> pathFromEntryToOut = analyzer.getPathFromEntryToOut(entryPoint, methodSignature);
        List<List<MethodSignature>> noVoidMths = pathFromEntryToOut.stream().map(path ->
                path.stream().filter(mthsig ->
                        !mthsig.getType().toString().equals("void")
                ).collect(Collectors.toList())
        ).collect(Collectors.toList());
        return pathFromEntryToOut;
    }

    public String resolveDescriptor(String descriptor) {
        assert descriptor.startsWith("L");
        String substring = descriptor.substring(1, descriptor.length() - 1);
        return substring.replaceAll(File.separator, ".");
    }

    private String getTypeName(MethodDeclaration mth) {
        String descriptor = mth.getType().toDescriptor();
        return resolveDescriptor(descriptor);
    }

    private String getQualifiedClassName(MethodDeclaration mth) {
        String className;
        try {
            className = mth.resolve().getClassName();
        } catch (Exception e) {
            Optional<Node> parentNode = mth.getParentNode();
            while (parentNode.isPresent() && !(parentNode.get() instanceof ClassOrInterfaceDeclaration)) {
                parentNode = parentNode.get().getParentNode();
            }
            if (parentNode.isEmpty())
                return null;
            className = ((ClassOrInterfaceDeclaration) parentNode.get()).getNameAsString();
        }
        CompilationUnit unit = patch.getUnit();
        Optional<PackageDeclaration> packageDeclaration = unit.getPackageDeclaration();
        AtomicReference<String> pack = new AtomicReference<>("");
        packageDeclaration.ifPresent(p -> pack.set(p.getNameAsString()));
        return pack.get() + "." + className;
    }

    private MethodDeclaration getMethodDeclaration(Node childNode) {
        if (childNode instanceof MethodDeclaration)
            return (MethodDeclaration) childNode;
        Optional<Node> parentNode = childNode.getParentNode();
        while (parentNode.isPresent() && !(parentNode.get() instanceof MethodDeclaration)) {
            parentNode = parentNode.get().getParentNode();
        }
        return (MethodDeclaration) parentNode.orElse(null);
    }
}
