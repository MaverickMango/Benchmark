package root.analysis.slicer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.utils.Pair;
import root.entities.ExecutionPathInMth;
import root.entities.PathFlow;
import root.entities.benchmarks.Defects4JBug;
import root.entities.ci.BugRepository;
import root.entities.ci.BugWithHistory;
import root.generation.helper.Helper;
import root.generation.transformation.MutateHelper;
import root.generation.transformation.TransformHelper;
import root.generation.transformation.visitor.ParameterVisitor;
import root.generation.transformation.visitor.PathVisitor;
import root.generation.transformation.visitor.VariableVisitor;
import root.util.ConfigurationProperties;
import root.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Slicer {
    private String sliceLog;
    private String srcTestDir;
    private String srcJavaDir;

    public Slicer(String srcJavaDir, String srcTestDir, String sliceLog) {
        this.sliceLog = sliceLog;
        this.srcJavaDir = srcJavaDir;
        this.srcTestDir = srcTestDir;
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
            String clz = line.split("#")[0];
            clz = clz.replace(".", File.separator);
            if (clz.contains("$")) {
                clz = clz.substring(0, clz.indexOf("$"));
            }
            String clzPath = (clz.contains("Test") ? srcTestDir : srcJavaDir) + File.separator + clz + ".java";
            int lineno = Integer.parseInt(line.split("#")[1]);
            if (lineno < 0)
                continue;
            //这里得到的executionPath是buggy的！
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
            lastPath.addLine(lineno);
        }
        return paths;
    }

    public PathFlow dependencyAnalysis(Pair<Set<Node>, Set<Node>> diffExprInBuggy, List<ExecutionPathInMth> path) {
        PathVisitor visitor = new PathVisitor();
        ParameterVisitor parameterVisitor = new ParameterVisitor();
        PathFlow pathFlow = new PathFlow();
        if (!diffExprInBuggy.b.isEmpty()) {
            Set<Node> nodeInPat = diffExprInBuggy.b;
            nodeInPat.forEach(n -> {
                n.accept(new VariableVisitor(), pathFlow);
                if (Helper.isCondition(n)) {//如果pat修改的是条件就应该再加上pat的修改位置
                    pathFlow.addCondition(MutateHelper.getUnsatisfiedCondition((Expression) n).toString());
                }//todo 如果是变量声明就加上变量声明
            });//初始化变量为pat修改涉及的变量

            //找到修改位置作为切片入口
            int startMth = findEntry(diffExprInBuggy.a, path, pathFlow);
            //倒着向前直到测试函数
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
            return pathFlow;
        }
        return null;
    }

    private int findEntry(Set<Node> nodesInBuggy, List<ExecutionPathInMth> path, PathFlow pathFlow) {
        //从最后一个执行到的函数往前找，切片入口是修改位置最早出现的地方。
        PathVisitor visitor = new PathVisitor();
        ParameterVisitor parameterVisitor = new ParameterVisitor();
        int startMth = path.size() - 1;
        while (startMth >= 0) {
            ExecutionPathInMth pathEntry = path.get(startMth);
            int startLineIdx = containsInLines(pathEntry.getLineno(), nodesInBuggy);
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
