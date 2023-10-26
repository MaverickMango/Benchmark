package root.generation.transformation.extractor;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entity.BasicInput;
import root.generation.entity.Input;
import root.generation.entity.ObjectInput;
import root.generation.helper.Helper;
import root.generation.parser.AbstractASTParser;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InputExtractor {

    private static final Logger logger = LoggerFactory.getLogger(InputExtractor.class);

    AbstractASTParser parser;

    public InputExtractor(AbstractASTParser parser) {
        this.parser = parser;
    }

    public CompilationUnit getCompilationUnit(String classNormalAbsPath) {
        Map<String, Object> asts = parser.getASTs();
        CompilationUnit compilationUnit = (CompilationUnit) asts.get(classNormalAbsPath);
        if (compilationUnit == null) {
            logger.error("File " + classNormalAbsPath + " is not in the initialized directory! CompilationUnit is Null");
            throw new IllegalArgumentException("Illegal Argument : " + classNormalAbsPath);
        }
        return compilationUnit;
    }

    public MethodDeclaration extractMethodByName(String classNormalAbsPath, String methodName,
                                               List<ImportDeclaration> targetImports) {
        CompilationUnit compilationUnit = getCompilationUnit(classNormalAbsPath);
        targetImports.addAll(compilationUnit.getImports());
        List<MethodDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class)
                .stream().filter(m -> m.getName().toString().equals(methodName)).collect(Collectors.toList());
        if (methodDeclarations.size() >= 1) {
            return methodDeclarations.get(0);
        } else {
            logger.error("Method " + methodName + " is not in the class file!");
            throw new IllegalArgumentException("Illegal Argument : " + methodName);
        }
    }

    public MethodCallExpr extractMethodCallByLine(MethodDeclaration methodDeclaration, int lineNumber) {
        if (methodDeclaration == null) {
            logger.error("Extracted method declaration is null! Process Interrupted.");
            throw new IllegalArgumentException("Illegal Argument: " + methodDeclaration);
        }
        List<Statement> collect = methodDeclaration.findAll(Statement.class).stream().filter(stmt ->
                stmt.getRange().isPresent() && stmt.getRange().get().begin.line == lineNumber
        ).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            Statement statement = collect.get(0);
            if (statement.isExpressionStmt()) {
                Expression expression = statement.asExpressionStmt().getExpression();
                if (expression.isMethodCallExpr()) {
                    return expression.asMethodCallExpr();
                }
            }
        }
        logger.error("LineNumber " + lineNumber + " does not in the method " + methodDeclaration.getName() + " or it is not a method call statement!");
        throw new IllegalArgumentException("Illegal Argument: " + lineNumber);
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
