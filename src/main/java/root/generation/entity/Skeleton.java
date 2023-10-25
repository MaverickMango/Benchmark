package root.generation.entity;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.quality.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.helper.Helper;
import root.generation.transformation.InputTransformer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * a method declaration of test, the last statement is the one before trigger assert.
 * if all statements before are $ASSERT, Prefix will have an empty body.
 */
public class Skeleton {

    private static final Logger logger = LoggerFactory.getLogger(Skeleton.class);
    CompilationUnit clazz;
    String clazzName;
    MethodDeclaration originalMethod;
    boolean isSplit;//是否完成对多余断言的删除
    List<Input> inputs;
    List<MethodDeclaration> methodDeclarations;
    int generatedTestsIdx;

    public Skeleton(@NotNull CompilationUnit clazz, @NotNull MethodDeclaration originalMethod) {
        this.clazz = clazz;
        this.originalMethod = originalMethod;
        if (originalMethod.getParentNode().isEmpty()) {
            throw new IllegalArgumentException("originalMethod " + originalMethod.getNameAsString() + " doesn't have parent node!");
        }
        if (!(originalMethod.getParentNode().get() instanceof ClassOrInterfaceDeclaration)) {
            throw new IllegalArgumentException("Unsupported method type. " +
                    "May be originalMethod " + originalMethod.getNameAsString() + " is anonymous!");
        }
        this.clazzName = ((ClassOrInterfaceDeclaration) originalMethod.getParentNode().get()).getNameAsString();
        this.isSplit = false;
        this.inputs = new ArrayList<>();
        this.methodDeclarations = new ArrayList<>();
        this.generatedTestsIdx = 0;
    }

    public CompilationUnit getClazz() {
        return clazz;
    }

    public String getClazzName() {
        return clazzName;
    }

    public MethodDeclaration getOriginalMethod() {
        return originalMethod;
    }

    public void addInput(Input input) {
        this.inputs.add(input);
    }

    public void setInputs(List<Input> inputs) {
        this.inputs.addAll(inputs);
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void constructSkeleton(List<Input> newInputs) {
        if (!this.isSplit)
            splitAssert();//删除原有的assert语句
        setInputs(newInputs);

        List<Input> collect = inputs.stream().filter(input -> !input.isCompleted()).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            for (Input newInput :collect) {
                newInput.getOracle();//需要oracle的语句则需要先执行一遍
            }
        }

        collect = inputs.stream().filter(Input::isCompleted).collect(Collectors.toList());
        addStatementsAtLast(collect);//对于不需要oracle的语句，直接根据input更新method

        addMethodsAtCompilationUnit();//向原有类添加新的测试函数
    }

    public void constructSkeleton(Input newInput) {
        if (!this.isSplit)
            splitAssert();//删除原有的assert语句
        addInput(newInput);
        if (!newInput.isCompleted())
            newInput.getOracle();//需要oracle的语句则需要先执行一遍
        MethodDeclaration methodDeclaration = addStatementAtLast(newInput);//对于不需要oracle的语句，直接根据input更新method
        addMethodAtCompilationUnit(methodDeclaration);//向原有类添加新的测试函数
    }
    public void splitAssert() {
        MethodDeclaration clone = this.getOriginalMethod().clone();
        (clone.getBody().get()).findAll(ExpressionStmt.class).forEach((stmt) -> {
            if (Helper.isAssertion(stmt)) {
                stmt.remove();
            }
        });
        this.originalMethod = clone;
        this.isSplit = true;
    }

    public MethodDeclaration addStatementAtLast(Input newInput) {
        ExpressionStmt stmt = new ExpressionStmt();
        stmt.setExpression(newInput.getMethodCallExpr());
        MethodDeclaration clone = this.getOriginalMethod().clone();
        clone.setName(getNewMethodName(clone.getNameAsString()));
        Optional<BlockStmt> body = clone.getBody();
        if (body.isPresent()) {
            BlockStmt blockStmt = body.get();
            NodeList<Statement> statements = blockStmt.getStatements();
            statements.addLast(stmt);
        } else {
            logger.error("methodDeclaration in skeleton has been initialized with error. Empty skeleton will be create.");
            BlockStmt blockStmt = new BlockStmt(new NodeList<>());
            blockStmt.getStatements().addLast(stmt);
        }
        methodDeclarations.add(clone);
        return clone;
    }

    public void addStatementsAtLast(List<Input> inputs) {
        for (Input newInput : inputs) {
            addStatementAtLast(newInput);
        }
    }

    public void addMethodAtCompilationUnit(MethodDeclaration methodDeclaration) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = clazz.getClassByName(clazzName).get();
        List<String> collect = classOrInterfaceDeclaration.getMethods().stream()
                .map(NodeWithSimpleName::getNameAsString).collect(Collectors.toList());
        if (!collect.contains(methodDeclaration.getNameAsString()))
            classOrInterfaceDeclaration.addMember(methodDeclaration);
    }

    public void addMethodsAtCompilationUnit() {
        for (MethodDeclaration method :methodDeclarations) {
            addMethodAtCompilationUnit(method);
        }
    }

    private String getNewMethodName(String oldName) {
        return oldName + "_generatedTest" + generatedTestsIdx ++;
    }
}
