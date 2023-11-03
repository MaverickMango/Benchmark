package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.quality.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.generation.helper.Helper;
import root.generation.helper.MutatorHelper;
import root.generation.helper.TransformHelper;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class InputTransformer {

    private final Logger logger = LoggerFactory.getLogger(InputTransformer.class);

    Map<String, Skeleton> skeletons;//<absolutePath, Skeleton>

    public InputTransformer() {
        this.skeletons = new HashMap<>();
    }

    public List<Input> transformInput(Input oldInput, List<Object> values) {
        List<Input> inputs = new ArrayList<>();
        for (Object value :values) {
            Input newInput = transformInput(oldInput, value);
            inputs.add(newInput);
        }
        return inputs;
    }

    public Input transformInput(Input oldInput, Object value) {
        if (oldInput.getInputExpr().getParentNode().isEmpty()) {
            logger.error("Node " + oldInput.getInputExpr() + " has lost its parent node, can't process further!");
            throw new IllegalArgumentException(oldInput.toString());
        }
        logger.info("New input '" + value.toString() + "' has been transformed for " + oldInput);
        Input newInput = oldInput.clone();
        Expression basicExpr = oldInput.getBasicExpr();
        Expression newInputExpr = (Expression) value;//transform(basicExpr, value);
        newInput.setBasicExprTransformed(newInputExpr);
        return newInput;
    }

    @SuppressWarnings("deprecation")
    public Expression transform(@NotNull Expression basicExpr, Object value) {
        Class<? extends Expression> inputType;
        try {
            String qualifiedName = Helper.getType(basicExpr);
            inputType = MutatorHelper.INPUTS_BY_TYPE.get(qualifiedName).get(0);
        } catch (IllegalStateException e) {
            inputType = basicExpr.getClass();
        }
        Expression newInputExpr = null;
        try {
            if (inputType.equals(BooleanLiteralExpr.class)) {
                newInputExpr = new BooleanLiteralExpr();
                ((BooleanLiteralExpr) newInputExpr).setValue((Boolean) value);
            }
            if (inputType.equals(DoubleLiteralExpr.class)) {
                newInputExpr = new DoubleLiteralExpr();
                ((DoubleLiteralExpr) newInputExpr).setDouble((Double) value);
            }
            if (inputType.equals(IntegerLiteralExpr.class)) {
                newInputExpr = new IntegerLiteralExpr();
                ((IntegerLiteralExpr) newInputExpr).setInt((Integer) value);
            }
            if (inputType.equals(CharLiteralExpr.class)) {
                newInputExpr = new CharLiteralExpr();
                ((CharLiteralExpr) newInputExpr).setChar((Character) value);
            }
            if (inputType.equals(LongLiteralExpr.class)) {
                newInputExpr = new LongLiteralExpr();
                ((LongLiteralExpr) newInputExpr).setLong((Long) value);
            }
            if (inputType.equals(StringLiteralExpr.class)) {
                newInputExpr = new StringLiteralExpr();
                ((StringLiteralExpr) newInputExpr).setString((String) value);
            }
            if (inputType.equals(UnaryExpr.class)) {
                newInputExpr = (UnaryExpr) value;
            }
        } catch (ClassCastException e) {
            logger.error("Error casting while generate new input by argument '" + value + "'! " + e.getMessage());
            logger.info("No newInput was generated. OldInput return.");
        }
        if (newInputExpr == null) {
            throw new IllegalArgumentException("TBD. Unsupported type of Input has been given.");
        }
        return newInputExpr;
    }

    public Map<CompilationUnit, String[]> buildNewTestByInput(Skeleton skeleton, Input newInput) {
        String path = skeleton.getAbsolutePath();
        if (!this.skeletons.containsKey(path)) {
            this.skeletons.put(path, skeleton);
        }
        Map<CompilationUnit, String[]> compilationUnitMap = constructSkeleton(skeleton, newInput);
        return compilationUnitMap;
    }

    public Map<CompilationUnit, String[]> buildNewTestByInputs(Skeleton skeleton, List<Input> newInputs) {
        Map<CompilationUnit, String[]> map = constructSkeleton(skeleton, newInputs);
        return map;
    }
    public Map<CompilationUnit, String[]> constructSkeleton(Skeleton skeleton, List<Input> newInputs) {
        if (!skeleton.isSplit())
            skeleton.splitAssert();//删除原有的assert语句
        skeleton.setInputs(newInputs);
        skeleton.applyTransform(newInputs);
        Map<CompilationUnit, String[]> map = new HashMap<>();
        List<Input> collect = skeleton.getInputs().stream().filter(input -> !input.isCompleted()).collect(Collectors.toList());
        List<Input> collect1 = skeleton.getInputs().stream().filter(Input::isCompleted).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            Map<CompilationUnit, String[]> oracle = skeleton.getOracle(TransformHelper.bugRepository, newInputs);//需要oracle的语句则需要先执行一遍
            map.putAll(oracle);
        }
        if (!collect1.isEmpty()) {
            Map<MethodDeclaration, Input> methodDeclarationInputMap = skeleton.addStatementsAtLast(collect1);//对于不需要oracle的语句，直接根据input更新method
            CompilationUnit compilationUnit = skeleton.addMethods2CompilationUnit(methodDeclarationInputMap);
            List<String> names = new ArrayList<>();
            for (Map.Entry<MethodDeclaration, Input> entry: methodDeclarationInputMap.entrySet()) {
                String testNamePrefix = skeleton.getTestNamePrefix(compilationUnit, entry.getKey().getNameAsString());
                names.add(testNamePrefix);
            }
            map.put(compilationUnit, names.toArray(new String[0]));
        }
        return map;
    }

    public Map<CompilationUnit, String[]> constructSkeleton(Skeleton skeleton, Input newInput) {
        if (!skeleton.isSplit())
            skeleton.splitAssert();//删除原有的assert语句
        skeleton.addInput(newInput);
        skeleton.applyTransform(newInput);
        Map<CompilationUnit, String[]> newUnit;
        if (!newInput.isCompleted()) {
            newUnit = skeleton.getOracle(TransformHelper.bugRepository, newInput);//需要oracle的语句则需要先执行一遍
        } else {
            MethodCallExpr methodCallExpr = newInput.getMethodCallExpr();
            ExpressionStmt stmt = new ExpressionStmt(methodCallExpr);
            MethodDeclaration methodDeclaration = skeleton.addStatementAtLast(stmt);//对于不需要oracle的语句，直接根据input更新method
            CompilationUnit compilationUnit = skeleton.addMethod2CompilationUnit(methodDeclaration);//向原有类添加新的测试函数
            String testNamePrefix = skeleton.getTestNamePrefix(compilationUnit, methodDeclaration.getNameAsString());
            newUnit = new HashMap<>();
            newUnit.put(compilationUnit, new String[] {testNamePrefix});
        }
        return newUnit;
    }
}
