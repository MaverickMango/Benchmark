package root.generation.entity;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
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
    ClassOrInterfaceDeclaration clazz;
    MethodDeclaration originalMethod;
    boolean isSplit;//是否完成对多余断言的删除
    List<Input> inputs;
    List<MethodDeclaration> methodDeclarations;

    public Skeleton(@NotNull ClassOrInterfaceDeclaration clazz, @NotNull MethodDeclaration originalMethod) {
        this.clazz = clazz;
        this.originalMethod = originalMethod;
        this.isSplit = false;
        this.inputs = new ArrayList<>();
        this.methodDeclarations = new ArrayList<>();
    }

    public Skeleton(@NotNull ClassOrInterfaceDeclaration clazz, @NotNull MethodDeclaration originalMethod, Input input) {
        this.clazz = clazz;
        this.originalMethod = originalMethod;
        this.isSplit = false;
        this.inputs = new ArrayList<>();
        addInput(input);
        this.methodDeclarations = new ArrayList<>();
    }

    public ClassOrInterfaceDeclaration getClazz() {
        return clazz;
    }

    public void setOriginalMethod(MethodDeclaration originalMethod) {
        this.originalMethod = originalMethod;
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
        MethodDeclaration methodDeclaration = getOriginalMethod();
        setInputs(newInputs);
        splitAssert();//删除原有的assert语句
        List<Input> collect = inputs.stream().filter(input -> !input.isCompleted()).collect(Collectors.toList());
        InputTransformer.getOracle(collect);//需要oracle的语句则需要先执行一遍
        collect = inputs.stream().filter(Input::isCompleted).collect(Collectors.toList());
        addStatementAtLast(collect);//对于不需要oracle的语句，直接根据input更新method
        addMethodAtCompilationUnit();//向原有类添加新的测试函数
    }

    public void constructSkeleton(Input newInput) {//todo 怎么处理单个newInput?
        MethodDeclaration methodDeclaration = getOriginalMethod();
        splitAssert();//删除原有的assert语句
        List<Input> collect = inputs.stream().filter(Input::isCompleted).collect(Collectors.toList());
        addStatementAtLast(collect);//对于不需要oracle的语句，直接根据input更新method
        addMethodAtCompilationUnit();//向原有类添加新的测试函数
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

    public void addStatementAtLast(List<Input> inputs) {
        for (int i = 0; i < inputs.size(); i++) {
            Input newInput = inputs.get(i);
            ExpressionStmt stmt = new ExpressionStmt();
            stmt.setExpression(newInput.getMethodCallExpr());
            MethodDeclaration clone = this.getOriginalMethod().clone();
            clone.setName(clone.getNameAsString() + "_" + i);
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
        }
    }

    public void addMethodAtCompilationUnit() {
        List<MethodDeclaration> methods = clazz.getMethods();
        methods.addAll(methodDeclarations);//todo：不能直接这么修改类的函数
    }
}
