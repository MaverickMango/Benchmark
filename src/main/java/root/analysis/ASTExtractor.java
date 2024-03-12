package root.analysis;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entities.BasicInput;
import root.generation.entities.Input;
import root.generation.entities.ObjectInput;
import root.generation.helper.Helper;
import root.generation.transformation.visitor.EqualVisitor;
import root.analysis.parser.AbstractASTParser;
import root.util.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ASTExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ASTExtractor.class);

    public AbstractASTParser parser;

    public ASTExtractor(AbstractASTParser parser) {
        this.parser = parser;
    }

    public AbstractASTParser getParser() {
        return parser;
    }

    public CompilationUnit getCompilationUnitFromCode(String str) {
        CompilationUnit compilationUnit = (CompilationUnit) parser.parseASTFromCode(str);
        if (compilationUnit == null) {
            throw new IllegalArgumentException("Illegal Argument : " + str);
        }
        return compilationUnit;
    }

    public CompilationUnit getCompilationUnit(String classNormalAbsPath) {
        if (FileUtils.notExists(classNormalAbsPath)) {
            logger.error("File " + classNormalAbsPath + " does not exist!");
            throw new IllegalArgumentException("Illegal Argument : " + classNormalAbsPath);
        }
        CompilationUnit compilationUnit = (CompilationUnit) parser.getAST(classNormalAbsPath);
        if (compilationUnit == null) {
            throw new IllegalArgumentException("Illegal Argument : " + classNormalAbsPath);
        }
        return compilationUnit;
    }

    public CallableDeclaration extractMethodByName(CompilationUnit compilationUnit, String mthQuailifiedName) {
        List<CallableDeclaration> methods = compilationUnit.findAll(CallableDeclaration.class)
                .stream().filter(m -> m.getName().toString().equals(mthQuailifiedName)).collect(Collectors.toList());
        if (!methods.isEmpty()) {
            return methods.get(0);
        } else {
            logger.error("Method " + mthQuailifiedName + " is not in the class file!");
            return null;
        }
    }

    public CallableDeclaration extractMethodByLine(CompilationUnit compilationUnit, int lineNumber) {
        List<CallableDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class)
                .stream().filter(m -> m.getBegin().isPresent() && m.getBegin().get().line <= lineNumber
                        && m.getEnd().isPresent() && m.getEnd().get().line >= lineNumber
                        ).collect(Collectors.toList());
        if (!methodDeclarations.isEmpty()) {
            return methodDeclarations.get(0);
        } else {
            methodDeclarations = compilationUnit.findAll(ConstructorDeclaration.class)
                    .stream().filter(m -> m.getBegin().isPresent() && m.getBegin().get().line <= lineNumber
                            && m.getEnd().isPresent() && m.getEnd().get().line >= lineNumber
                    ).collect(Collectors.toList());
            if (!methodDeclarations.isEmpty())
                return methodDeclarations.get(0);
            logger.error("Line " + lineNumber + " is not a methodDeclaration in the class file !");
            return null;
        }
    }

    public CallableDeclaration extractMethodByLine(CompilationUnit compilationUnit, int start, int end, String mthQualifiedName) {
        List<CallableDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class)
                .stream().filter(m -> (m.getBegin().isPresent() && m.getBegin().get().line <= start)
                        && (m.getEnd().isPresent() && m.getEnd().get().line >= end)
                ).collect(Collectors.toList());
        if (!methodDeclarations.isEmpty()) {
            return methodDeclarations.get(0);
        } else {
            methodDeclarations = compilationUnit.findAll(ConstructorDeclaration.class)
                    .stream().filter(m -> m.getBegin().isPresent() && m.getBegin().get().line <= start
                            && m.getEnd().isPresent() && m.getEnd().get().line >= end
                    ).collect(Collectors.toList());
            if (!methodDeclarations.isEmpty())
                return methodDeclarations.get(0);
            return extractMethodByName(compilationUnit, mthQualifiedName);
        }
    }
    
    public Node extractExpressionByLabel(CompilationUnit compilationUnit, String label,
                                               int start, int end) {
        CallableDeclaration methodDeclaration = extractMethodByLine(compilationUnit, start, end, null);
        EqualVisitor visitor = new EqualVisitor(label);
        List<Node> nodes = new ArrayList<>();
        methodDeclaration.accept(visitor, nodes);
        if (nodes.isEmpty()) {
            return null;
        } else {
            AtomicReference<Node> node = new AtomicReference<>(nodes.get(0));
            if (nodes.size() > 1) {
                nodes.forEach(n -> {
                    int beginLine = n.getBegin().isPresent() ? n.getBegin().get().line : - 1;
                    int endLine = n.getEnd().isPresent() ? n.getEnd().get().line : -1;
                    if (beginLine == start && endLine == end)
                        node.set(n);
                });
            }
            return node.get();
        }
    }

    public MethodCallExpr extractAssertByLine(CompilationUnit compilationUnit, String mthQualifiedName, int lineNumber) {
        CallableDeclaration methodDeclaration = extractMethodByName(compilationUnit, mthQualifiedName);
        if (methodDeclaration == null)
            return null;
        return extractAssertByLine(methodDeclaration, lineNumber);
    }

    public MethodCallExpr extractAssertByLine(CallableDeclaration methodDeclaration, int lineNumber) {
        if (methodDeclaration == null) {
            logger.error("Extracted method declaration is null! Process Interrupted.");
            throw new IllegalArgumentException("Illegal Argument: " + methodDeclaration);
        }
        Statement statement = null;
        if (lineNumber == 0) {//如果没有line Number就提取第一个assert？
            List<Statement> collect = methodDeclaration.findAll(Statement.class).stream().filter(Helper::isAssertion).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                statement = collect.get(0);
            }
        } else {
            List<Statement> collect = methodDeclaration.findAll(Statement.class).stream().filter(stmt ->
                    stmt.getRange().isPresent() && stmt.getRange().get().begin.line == lineNumber
            ).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                statement = collect.get(0);
            }
        }
        if (statement != null && statement.isExpressionStmt()) {
            Expression expression = statement.asExpressionStmt().getExpression();
            if (expression.isMethodCallExpr()) {
                return expression.asMethodCallExpr();
            }
        }
        logger.error("LineNumber " + lineNumber + " does not in the method " + methodDeclaration.getName() + " or it is not a method call statement!");
        throw new IllegalArgumentException("Illegal Argument: " + lineNumber);
    }

    public Input extractInput(CompilationUnit compilationUnit, String mthQualifiedName, int lineNumber) {
        MethodCallExpr methodCallExpr = (MethodCallExpr) extractAssertByLine(compilationUnit, mthQualifiedName, lineNumber);
        return extractInput(methodCallExpr);
    }

    public Input extractInput(MethodCallExpr methodCallExpr) {
        if (methodCallExpr == null) {
            logger.error("Extracted method call expression is null! Process Interrupted.");
            throw new IllegalArgumentException("Illegal argument: Null");
        }
//        Optional<MethodDeclaration> ancestor = methodCallExpr.findAncestor(MethodDeclaration.class);
//        if (ancestor.isEmpty()) {
//            logger.error("Node " + methodCallExpr + " has lost its parent node, can't process further!");
//            throw new IllegalArgumentException("Illegal argument: " + methodCallExpr.getNameAsString());
//        }
//        MethodDeclaration methodDeclaration = ancestor.get();
        NodeList<Expression> arguments = methodCallExpr.getArguments();
        if (arguments.size() == 0) {
            logger.error("Wrong method to extract arguments! There is no arguments here.");
            throw new IllegalArgumentException("Illegal argument: " + methodCallExpr.getNameAsString());
        }
        Expression actual;
        int argIdx = 0;
        String qualifiedName;
        //如果只有一个参数，并且不是assert语句，就直接提取参数进行变异，变异后塞回去就行。
        if (arguments.size() >= 2) {
            //todo: 存在多个参数的情况时实际是第二个条件吗？
            //如果是两个参数，一般第二个参数是实际值，提取后需要到original版本获取期望值。
            actual = arguments.get(1);
            argIdx = 1;
        } else {
            actual = arguments.get(0);
        }
        qualifiedName = Helper.getType(actual);
        List<LiteralExpr> all = actual.findAll(LiteralExpr.class);
        if (!all.isEmpty()) {
            return new BasicInput(methodCallExpr, actual, qualifiedName, argIdx);
        } else {
            return new ObjectInput(methodCallExpr, actual, qualifiedName, argIdx);
        }
    }
}
