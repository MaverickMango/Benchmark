package root.diff;

import com.github.javaparser.quality.NotNull;
import com.github.javaparser.quality.Nullable;
import gumtree.spoon.diff.operations.*;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import root.entities.benchmarks.Defects4JBug;
import root.generation.transformation.visitor.MinimalVisitor;
import root.util.GitTool;
import root.util.PatchHelper;
import spoon.reflect.declaration.CtElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import root.entities.Difference;
import root.entities.MultiFilesPatch;
import root.entities.Patch;
import root.entities.ci.BugRepository;
import root.generation.transformation.TransformHelper;
import root.util.ConfigurationProperties;

public class DiffExtractor {

    private static final Logger logger = LoggerFactory.getLogger(DiffExtractor.class);

    Map<String, CompilationUnit> parsedUnit = new HashMap<>();//todo 放到全局的parsedUnit里面

    private String getLabel(String absPath, String version) {
        return absPath + "#" + version;
    }

    private CompilationUnit getParsedUnit(String unitPath, String version) {
        String label = getLabel(unitPath, version);
        if (!parsedUnit.containsKey(label)) {
            CompilationUnit unit = TransformHelper.ASTExtractor.getCompilationUnit(unitPath);
            parsedUnit.put(label, unit);
        }
        return parsedUnit.get(label);
    }

    private CompilationUnit getParsedUnitFromCode(String unitPath, String code, String version) {
        String label = getLabel(unitPath, version);
        if (!parsedUnit.containsKey(label)) {
            CompilationUnit unit = TransformHelper.ASTExtractor.getCompilationUnitFromCode(code);
            parsedUnit.put(label, unit);
        }
        return parsedUnit.get(label);
    }

    public Difference getDifferenceForPatch(Patch patch) {
        Difference difference = new Difference(patch);
        String location = ConfigurationProperties.getProperty("location");
        List<Patch> singleFilePatches = new ArrayList<>();
        if (patch.isSingleFile())
            singleFilePatches.add(patch);
        else {
            singleFilePatches.addAll(((MultiFilesPatch) patch).getAllSingleFiles());
        }
        logger.debug("...Mapping buggy version to original version");
        List<String> fileInOrgMappingToBuggy = getFileInOrgMappingToBuggy(singleFilePatches);

        logger.info("...Difference for file " + patch.getName());
        for (Patch singleFilePatch :singleFilePatches) {
            String bugPath = singleFilePatch.getPatchAbsPath().replace(singleFilePatch.getPathFromRoot(), location);
            List<Pair<Node, Node>> buggyAndPatch = getDifferentPairs(bugPath,
                    singleFilePatch.getPatchAbsPath(),
                    null, null,
                    "buggy", "patch");
            if (buggyAndPatch == null || buggyAndPatch.isEmpty()) {
                logger.error("there is no different between patched file " + singleFilePatch.getName() + " and relevant buggy file!");
                continue;
            }
            difference.addDiffBetweenBugAndPatch(buggyAndPatch);//1

            List<Pair<Node, Node>> inducingAndOrg = getInducingRelevantDiffNodes(fileInOrgMappingToBuggy);
            difference.addDiffBetweenInducingAndOrg(inducingAndOrg);//2
        }
        return difference;
    }

    List<String> diffStatusBetweenBugAndOrg = new ArrayList<>();

    private List<String> getDiffStatusBetweenBugAndOrg() {
        if (diffStatusBetweenBugAndOrg.isEmpty()) {
            String location = ConfigurationProperties.getProperty("location");
            BugRepository bugRepository = TransformHelper.bugRepository;
            GitTool gitAccess = bugRepository.getGitAccess();
            String buggyCommit = ((Defects4JBug) bugRepository.getBug()).getD4JBuggy();
            String originalCommit = ((Defects4JBug) bugRepository.getBug()).getOriginalCommit();
            diffStatusBetweenBugAndOrg = gitAccess.getFileStatDiffBetweenCommits(location, buggyCommit, originalCommit);
        }
        return diffStatusBetweenBugAndOrg;
    }

    private List<String> getFileInOrgMappingToBuggy(List<Patch> singleFilePatches) {
        BugRepository bugRepository = TransformHelper.bugRepository;
        GitTool gitAccess = bugRepository.getGitAccess();
        List<String> bugPaths = singleFilePatches.stream().map(patch ->
                patch.getPatchAbsPath().substring(
                        patch.getPatchAbsPath().lastIndexOf(patch.getPathFromRoot())
                                + patch.getPathFromRoot().length() + 1
                )
        ).collect(Collectors.toList());
        List<String> fileStatDiffBetweenCommits = getDiffStatusBetweenBugAndOrg();
        List<String> mappedFiles = gitAccess.getRelevantFiles(fileStatDiffBetweenCommits, bugPaths);
        return mappedFiles;
    }

    /**
     * compare between two single java files.
     * @param srcPath source java file
     * @param dstPath target java file
     */
    public List<Pair<Node, Node>> getDifferentPairs(@NotNull String srcPath, @NotNull String dstPath,
                                                    @Nullable String srcContent, @Nullable String dstContent,
                                                    @NotNull String srcVersion, @NotNull String dstVersion) {
        try {
            AstComparator astComparator = new AstComparator();
            Diff compare;
            if (srcContent == null || dstContent == null) {
                logger.debug("getting compare from file path");
                compare = astComparator.compare(new File(srcPath), new File(dstPath));
            } else {
                logger.debug("getting compare from code");
                compare = astComparator.compare(srcContent, dstContent);//会导致没有sourceFragment
            }
            return getPairsFromDiff(compare, srcPath, dstPath,
                    srcContent, dstContent,
                    srcVersion, dstVersion);
        } catch (Exception e) {
            logger.error("Error occurred when getting diff from gumtree-spoon\n" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private List<Pair<Node, Node>> getPairsFromDiff(Diff compare,
                                                    String srcPath, String dstPath,
                                                    String srcContent, String dstContent,
                                                    String srcVersion, String dstVersion) {
        try {
            CompilationUnit srcUnit = srcContent == null ? getParsedUnit(srcPath, srcVersion) : getParsedUnitFromCode(srcPath, srcContent, srcVersion);
            CompilationUnit dstUnit = dstContent == null ? getParsedUnit(dstPath, dstVersion) : getParsedUnitFromCode(dstPath, dstContent, dstVersion);
            List<Pair<Node, Node>> pairs = new ArrayList<>();
            Map<CtElement, Node> usedNodes = new HashMap<>();
            List<Operation> rootOperations = compare.getRootOperations();
            for (int i = 0; i < rootOperations.size(); i ++) {
                Operation operation = rootOperations.get(i);
                Pair<Node, Node> nodsFromNodes = getNodsFromNodes(usedNodes, srcUnit, dstUnit, operation);
                if (nodsFromNodes == null)
                    continue;
                pairs.add(nodsFromNodes);
            }
            return pairs;
        } catch (Exception e) {
            logger.error("Error occurred when getting diff from gumtree-spoon\n" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private Pair<Node, Node> getNodsFromNodes(Map<CtElement, Node> usedNodes,
                                              CompilationUnit srcUnit, CompilationUnit dstUnit,
                                              Operation operation) {
        if (operation instanceof UpdateOperation) {
            CtElement srcNode = operation.getSrcNode();
            CtElement dstNode = operation.getDstNode();
            Node src = getNodeFromNode(usedNodes, srcNode, srcUnit);
            Node dst = getNodeFromNode(usedNodes, dstNode, dstUnit);
            return new Pair<>(src, dst);
        }
        if (operation instanceof InsertOperation) {
            CtElement srcNode = operation.getSrcNode();
            Node dst = getNodeFromNode(usedNodes, srcNode, dstUnit);
            return new Pair<>(null, dst);
        }
        if (operation instanceof DeleteOperation || operation instanceof MoveOperation) {//move的也算在里面，ab有空的如果对上，涉及的变量就可以删掉。
            CtElement srcNode = operation.getSrcNode();
            Node src = getNodeFromNode(usedNodes, srcNode, srcUnit);
            return new Pair<>(src, null);
        }
        return null;
    }

    private Node getNodeFromNode(Map<CtElement, Node> usedNodes, CtElement node, CompilationUnit unit) {
        if (!usedNodes.containsKey(node)) {
            int line = node.getPosition().getLine();//position available?
            int endLine = node.getPosition().getEndLine();
            String label = getSourceString(node);
            Node src = TransformHelper.ASTExtractor.extractExpressionByLabel(unit, label, line, endLine);
            usedNodes.put(node, src);
        }
        return usedNodes.get(node);
    }

    private String getSourceString(CtElement node) {
        try {
            String sourceCode = node.getOriginalSourceFragment().getSourceCode();
            return sourceCode.replaceAll("\\s", "").replaceAll(";", "");
        } catch (Exception e) {//如果通过字符串parse就没有sourceFragment
            logger.error("Can not find code element's source code:\n" + node.toString());
        }
        return node.toString();
    }

    private List<String> diffStatusBetweenOrgAndInd = new ArrayList<>();

    private List<String> getDiffStatusBetweenOrgAndInd() {
        if (diffStatusBetweenOrgAndInd.isEmpty()) {
            BugRepository bugRepository = TransformHelper.bugRepository;
            GitTool gitAccess = bugRepository.getGitAccess();
            String inducingCommit = bugRepository.getBug().getInducingCommit();
            String originalCommit = ConfigurationProperties.getProperty("originalCommit");
            String location = ConfigurationProperties.getProperty("location");
            diffStatusBetweenOrgAndInd = gitAccess.getFileStatDiffBetweenCommits(location, originalCommit, inducingCommit);
        }
        return diffStatusBetweenOrgAndInd;
    }

    public List<Pair<Node, Node>> getInducingRelevantDiffNodes(List<String> fileInOrgMappingToBuggy) {
//        Defects4JBug bug = (Defects4JBug) TransformHelper.bugRepository.getBug();
//        Defects4JBug pat = new Defects4JBug(bug.getProj(), bug.getId(), ConfigurationProperties.getProperty("patchDir"),
//                bug.getFixingCommit(), bug.getBuggyCommit(), bug.getInducingCommit(), bug.getOriginalCommit());
//        patchRepository = new BugRepository(pat, analyzer);
//        patchRepository.switchToBug();
        //todo 这里的文件提取改为从两个新的文件夹进行，不要改变原本的文件夹
        BugRepository bugRepository = TransformHelper.bugRepository;
        bugRepository.switchToInducing();
        String inducingDir = ((Defects4JBug) bugRepository.getBug()).getWorkingDir();
        BugRepository patchRepository = PatchHelper.patchRepository;
        patchRepository.switchToOrg();
        String orgDir = ((Defects4JBug) patchRepository.getBug()).getWorkingDir();

        GitTool gitAccess = bugRepository.getGitAccess();
        List<Pair<Node, Node>> diffList = new ArrayList<>();//可以是空，空就说明这个补丁修改的文件和历史引入bug的位置不相关。
        List<String> fileStatDiffBetweenCommits = getDiffStatusBetweenOrgAndInd();
        List<Pair<String, String>> mappedFiles = gitAccess.getMappedFiles(fileStatDiffBetweenCommits, fileInOrgMappingToBuggy);
        for (Pair<String, String> org2Inducing :mappedFiles) {
            if (org2Inducing.a == null || org2Inducing.b == null){
                logger.info("This bug might not be a intrinsic bug. Inducing changes maybe contain some new files.");
                continue;
            }
            String dstPath = orgDir + File.separator + org2Inducing.a;
            String srcPath = inducingDir + File.separator + org2Inducing.b;
            List<Pair<Node, Node>> diffPairs = getDifferentPairs(srcPath, dstPath,
                    null, null,
                    "original", "inducing");
            assert  diffPairs != null;
            diffList.addAll(diffPairs);
        }

        bugRepository.switchToBug();
        bugRepository.compile();
        patchRepository.switchToBug();
        patchRepository.compile();
//        RefactoringMiner miner = new RefactoringMiner();
//        Set<ASTDiff> astDiffs = miner.diffAtCommit(bugRepository.getRepository(), originalCommit, inducingCommit);
//        for (ASTDiff astDiff: astDiffs) {
//            String srcPath = astDiff.getSrcPath();
//            String dstPath = astDiff.getDstPath();
//            boolean flag = fileInOrgMappingToBuggy.stream().anyMatch(srcPath::endsWith);
//            if (!flag)
//                continue;
//            String srcContents = astDiff.getSrcContents();
//            String dstContents = astDiff.getDstContents();
//            String srcSavedPath = "." + File.separator + "tmp" + File.separator + srcPath;
//            String dstSavedPath = "." + File.separator + "tmp" + File.separator + dstPath;
//            FileUtils.writeToFile(srcContents, srcSavedPath, false);
//            FileUtils.writeToFile(dstContents, dstSavedPath, false);
//            List<Pair<Node, Node>> diffPairs = getDifferentPairs(srcSavedPath, dstSavedPath,
//                    null, null,
//                    "original", "inducing");
//            assert  diffPairs != null;
//            FileUtils.removeFile(srcSavedPath);
//            FileUtils.removeFile(dstSavedPath);
//            diffList.addAll(diffPairs);
//        }
        return diffList;
    }

    public static Pair<Set<Node>, Set<Node>> getMinimalDiffNodes(Set<Node> leftNodes, Set<Node> rightNodes) {
        leftNodes = leftNodes.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        rightNodes = rightNodes.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Node> minimalLeft = AminusB(leftNodes, rightNodes);
        Set<Node> minimalRight = AminusB(rightNodes, leftNodes);
        return new Pair<>(minimalLeft, minimalRight);
    }

    private static Set<Node> AminusB(Set<Node> A, Set<Node> B) {
        Set<Node> left = new HashSet<>();
        for (Node n :A) {
            List<Node> subNode = minimal(n, B);
            if (subNode != null)
                left.addAll(subNode);
        }
        return left;
    }

    private static List<Node> minimal(Node root, Set<Node> target) {//返回root或者null，target会不断减少或者不变
        Set<Node> minuends = new HashSet<>(target);
        MinimalVisitor visitor = new MinimalVisitor(root);
        List<Node> subNode = null;
        for (Node t :minuends) {
            //如果minuend中有root节点的一部分，就把对应的minuend从target中删掉
            List<Node> left = visitor.minimalByTarget(t);
            subNode = visitor.getSubNode();
            if (!left.contains(t)) {
                target.remove(t);
                target.addAll(left);
                break;
            }
        }
        return subNode;
    }

    public static void filterChildNode(Set<Node> nodes) {
        Set<Node> current = new HashSet<>(nodes);
        for (Node node :current) {
            if (node == null)
                continue;
            boolean anyMatch = nodes.stream().anyMatch(n -> n != null && n.getParentNode().isPresent() &&
                    n.getParentNode().get().equals(node));
            if (anyMatch) {
                nodes.remove(node);
            }
        }
    }
}
