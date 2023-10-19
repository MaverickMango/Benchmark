package root.generation.transformation.extractor;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entity.Input;
import root.generation.parser.AbstractASTParser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InputExtractor {

    private static final Logger logger = LoggerFactory.getLogger(InputExtractor.class);

    AbstractASTParser parser;

    public InputExtractor(AbstractASTParser parser) {
        this.parser = parser;
    }

    public MethodDeclaration extractMethodByName(String classNormalAbsPath, String methodName,
                                               List<ImportDeclaration> targetImports) {
        Map<String, Object> asts = parser.getASTs();
        CompilationUnit compilationUnit = (CompilationUnit) asts.get(classNormalAbsPath);
        if (compilationUnit == null) {
            logger.error("File " + classNormalAbsPath + " is not in the initialized directory! CompilationUnit is Null");
            return null;
        }
        targetImports.addAll(compilationUnit.getImports());
        List<MethodDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class)
                .stream().filter(m -> m.getName().toString().equals(methodName)).collect(Collectors.toList());
        if (methodDeclarations.size() >= 1) {
            return methodDeclarations.get(0);
        }
        return null;
    }

    public MethodCallExpr extractMethodCallByLine(MethodDeclaration methodDeclaration, int lineNumber) {
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
        return null;
    }

    public Input extractInput(MethodCallExpr methodCallExpr) {
        NodeList<Expression> arguments = methodCallExpr.getArguments();
        if (arguments.size() == 0) {
            logger.error("Wrong method to extract arguments! There is no arguments here.");
            return null;
        }
        Expression actual = arguments.get(0);
        if (arguments.size() == 1) {
            //如果只有一个参数，并且不是assert语句，就直接提取参数进行变异，变异后塞回去就行。
            actual = arguments.get(0);
        } else if (arguments.size() == 2){
            //如果是两个参数，一般第二个参数是实际值，提取后需要到original版本获取期望值。
            actual = arguments.get(1);
        } else {
            //todo: 存在多个参数的情况时第几个是实际条件?
        }
        return new Input(actual.getClass(), actual);
    }
}
