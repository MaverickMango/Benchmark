package root.analysis.slicer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.entities.Difference;
import root.entities.ExecutionPathInMth;
import root.entities.PathFlow;
import root.entities.Stats;
import root.generation.helper.Helper;
import root.generation.transformation.MutateHelper;
import root.generation.transformation.TransformHelper;
import root.generation.transformation.visitor.ParameterVisitor;
import root.generation.transformation.visitor.PathVisitor;
import root.generation.transformation.visitor.VariableVisitor;
import root.util.FileUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Slicer {
    private static final Logger logger = LoggerFactory.getLogger(Slicer.class);
    private final MODE mode;

    public enum MODE {
        //all-保留所有trace;fault-已知出错行,从出错行切片;diff-考虑修改变量的切片
        ALL, FAULT, DIFF
    }

    private final boolean slim = true;//true-删除一个函数内路径中重复的部分.
    private final String sliceLog;
    private final String srcTestDir;
    private final String srcJavaDir;

    public Slicer(String srcJavaDir, String srcTestDir, String sliceLog, MODE mode) {
        this.sliceLog = sliceLog;
        this.srcJavaDir = srcJavaDir;
        this.srcTestDir = srcTestDir;
        this.mode = mode;
    }

    public List<ExecutionPathInMth> traceParser() {
        List<String> trace = FileUtils.readEachLine(sliceLog);
        List<ExecutionPathInMth> paths = new ArrayList<>();
        String lastMth = "";
        ExecutionPathInMth lastPath = null;
        for (String line : trace) {
            if (line.startsWith("---------")) {
                continue;
            }
            String[] split = line.split("#");
            String clz = split[0];
            clz = clz.replace(".", File.separator);
            if (clz.contains("$")) {
                clz = clz.substring(0, clz.indexOf("$"));
            }
            String clzPath = srcJavaDir + File.separator + clz + ".java";
            if (clz.contains("Test")) {
                clzPath = srcTestDir + File.separator + clz + ".java";
            }
            int lineno = Integer.parseInt(split[split.length - 1]);
            if (lineno < 0)
                continue;
            //这里得到的executionPath是buggy的！
            try {
                CompilationUnit compilationUnit = TransformHelper.ASTExtractor.getCompilationUnit(clzPath);
                CallableDeclaration methodDeclaration = TransformHelper.ASTExtractor.extractMethodByLine(compilationUnit, lineno);
                if (methodDeclaration == null)
                    continue;
                if (!lastMth.equals(methodDeclaration.toString())) {
                    lastMth = methodDeclaration.toString();
                    ExecutionPathInMth executionPathInMth = new ExecutionPathInMth(methodDeclaration);
                    lastPath = executionPathInMth;
                    paths.add(lastPath);
                }
                if (slim && lastPath.getLineno().contains(lineno)) {
                    continue;
                }
                lastPath.addLine(lineno);
            } catch (Exception e) {
                //should not be accessed. but access at case closure_133
                logger.error("compilationUnit does not exist or other error occurred!");
            }
        }
        return paths;
    }

    public List<PathFlow> dependencyAnalysis(List<Difference> differences, List<ExecutionPathInMth> path) {
        List<PathFlow> pathFlows = new ArrayList<>();
        switch (mode) {
            case DIFF:
                for (Difference difference: differences) {
                    PathFlow pathFlow = analysis4EachPatch(difference.getDiffExprInBuggy(), path);
                    Stats.getCurrentStats().addPatchStat(difference.getPatch().getName(), Stats.Patch.PATH_FLOW, pathFlow);
                    pathFlows.add(pathFlow);
                }
                break;
            case FAULT:
                //todo
                //break;
            default:
                PathFlow pathFlow = analysis4All(path);
                Stats.getCurrentStats().addPatchStat("all_path_flow", Stats.Patch.PATH_FLOW, pathFlow);
                pathFlows.add(pathFlow);
                break;
        }
        return pathFlows;
    }

    private PathFlow analysis4EachPatch(Pair<Set<Node>, Set<Node>> diffExprInBuggy, List<ExecutionPathInMth> path) {
        PathFlow pathFlow = new PathFlow();
        Set<Node> nodeInPat = diffExprInBuggy.b;
        nodeInPat.forEach(n -> {
            n.accept(new VariableVisitor(), pathFlow);
            if (Helper.isCondition(n)) {//如果pat修改的是条件就应该再加上pat的修改位置
                pathFlow.addCondition(MutateHelper.getUnsatisfiedCondition((Expression) n).toString());
            }//todo 如果是变量声明就加上变量声明， 如果是函数？
        });//初始化变量为pat修改涉及的变量

        ParameterVisitor parameterVisitor = new ParameterVisitor();
        PathVisitor visitor = new PathVisitor();
        //找到修改位置作为切片入口
        int startMth = findEntry(diffExprInBuggy.a, path, pathFlow);
        //倒着向前直到测试函数
        addDependencies(startMth, path, pathFlow, parameterVisitor, visitor);
        return pathFlow;
    }

    public PathFlow analysis4All(List<ExecutionPathInMth> path) {
        PathFlow pathFlow = new PathFlow();
        ParameterVisitor parameterVisitor = new ParameterVisitor();
        parameterVisitor.setContainsAll(true);
        PathVisitor visitor = new PathVisitor();
        visitor.setContainsAllVar(true);
        int startMth = path.size();
        //倒着向前直到测试函数
        addDependencies(startMth, path, pathFlow, parameterVisitor, visitor);
        return pathFlow;
    }

    private void addDependencies(int startMth, List<ExecutionPathInMth> path, PathFlow pathFlow,
                                 ParameterVisitor parameterVisitor, PathVisitor visitor) {
        for (int i = startMth - 1; i >= 0; i --) {
            ExecutionPathInMth pathInMth = path.get(i);
            List<Node> nodes = pathInMth.getNodes();
            visitor.setLineno(pathInMth.getLineno());
            for (int j = nodes.size() - 1; j >= 0; j --) {
                Node n = nodes.get(j);
                n.accept(visitor, pathFlow);
            }
            pathInMth.getMth().accept(parameterVisitor, pathFlow);
        }
    }

    private int findEntry(Set<Node> nodesInBuggy, List<ExecutionPathInMth> path, PathFlow pathFlow) {
        //从最后一个执行到的函数往前找，<s>切片入口是修改位置最早出现的地方。</s>切片入口是修改位置所在函数的最后一行（trace中第一次出现所在函数的那一行）。
        PathVisitor visitor = new PathVisitor();
        ParameterVisitor parameterVisitor = new ParameterVisitor();
        int startMth = path.size() - 1;
        while (startMth >= 0) {
            ExecutionPathInMth pathEntry = path.get(startMth);
            int startLineIdx = containsInLines(pathEntry.getLineno(), nodesInBuggy);
            if (startLineIdx == -1) {
                startLineIdx = containsNodes(pathEntry, nodesInBuggy, pathFlow);
            }
            if (startLineIdx != -1) {
                List<Node> nodes = pathEntry.getNodes();
                visitor.setLineno(pathEntry.getLineno().subList(0, startLineIdx + 1));//不是所有的node，只有lineno在修改位置之前的才开始
                for (int j = startLineIdx; j >= 0; j --) {
                    Node n = nodes.get(j);
                    n.accept(visitor, pathFlow);
                }
                pathEntry.getMth().accept(parameterVisitor, pathFlow);
                break;
            }
            startMth --;
        }
        return startMth;
    }

    private int containsNodes(ExecutionPathInMth pathEntry, Set<Node> nodesInBuggy, PathFlow pathFlow) {
        AtomicInteger begin = new AtomicInteger(-1);
        AtomicInteger end = new AtomicInteger(-1);
        if (pathEntry.getMth() instanceof ConstructorDeclaration) {
            ((ConstructorDeclaration) pathEntry.getMth()).getBegin().ifPresent(b -> begin.set(b.line));
            ((ConstructorDeclaration) pathEntry.getMth()).getEnd().ifPresent(e -> end.set(e.line));
        }
        if (pathEntry.getMth() instanceof MethodDeclaration) {
            ((MethodDeclaration) pathEntry.getMth()).getBegin().ifPresent(b -> begin.set(b.line));
            ((MethodDeclaration) pathEntry.getMth()).getEnd().ifPresent(e -> end.set(e.line));
        }
        AtomicInteger conditionLine = new AtomicInteger(-1);
        nodesInBuggy.forEach(
                n -> {
                    if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
                        int line = n.getBegin().get().line;
                        if (line >= begin.get() &&
                                n.getEnd().get().line <= end.get()) {//有修改节点在该函数中
                            Optional<Node> parentNode = n.getParentNode();
                            while (parentNode.isPresent()) {
                                Node node = parentNode.get();
                                if (node instanceof IfStmt || node instanceof ForStmt ||
                                        node instanceof SwitchStmt || node instanceof DoStmt ||
                                        node instanceof WhileStmt) {//寻找其对应的控制语句作为切片入口,添加条件为可满足,更新变量集
                                    conditionLine.set(line);
                                    PathVisitor visitor = new PathVisitor();
                                    visitor.setLineno(Collections.singletonList(line));
                                    node.accept(visitor, pathFlow);
                                    break;
                                }
                                parentNode = node.getParentNode();
                            }
                        }
                    }
                }
        );
        if (conditionLine.get() == -1) {
            return -1;
        }
        int res = pathEntry.getLineno().indexOf(conditionLine.get());
        if (res == -1) {
            //should not be accessed.
            res = 0;
            for(; res < pathEntry.getLineno().size(); res ++) {
                if (pathEntry.getLineno().get(res) > conditionLine.get()) {
                    break;
                }
            }
        }
        return res - 1;
    }

    private int containsInLines(List<Integer> lineno, Set<Node> nodes) {
        AtomicInteger max = new AtomicInteger(-1);
        nodes.forEach(n -> {
            if (n.getBegin().isPresent()) {
                int line = n.getBegin().get().line;
                if (lineno.contains(line)) {
                    max.set(Math.max(max.get(), lineno.indexOf(line)));
                }
            }
        });
        return max.get();
    }

}
