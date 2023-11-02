package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.quality.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.bean.BugRepository;
import root.bean.ci.CIBug;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.generation.helper.Helper;
import root.generation.helper.MutatorHelper;
import root.generation.helper.TransformHelper;

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
        //todo test Input实现深拷贝
        Input newInput = oldInput.clone();
        Expression basicExpr = oldInput.getBasicExpr();
        Expression newInputExpr = (Expression) value;//transform(basicExpr, value);
        newInput.setBasicExprTransformed(newInputExpr);
        return newInput;
    }

    @SuppressWarnings("deprecation")
    private Expression transform(@NotNull Expression basicExpr, Object value) {
        String qualifiedName = Helper.getType(basicExpr);
        Class<? extends Expression> inputType = MutatorHelper.INPUTS_BY_TYPE.get(qualifiedName).get(0);
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

    public CompilationUnit buildNewTestByInput(Skeleton skeleton, Input newInput) {
        String path = skeleton.getAbsolutePath();
        if (!this.skeletons.containsKey(path)) {
            this.skeletons.put(path, skeleton);
        }
        CompilationUnit compilationUnit = constructSkeleton(skeleton, newInput);
        return compilationUnit;
    }

    public void buildNewTestByInputs(Skeleton skeleton, List<Input> newInputs) {
        for (Input input :newInputs) {
            buildNewTestByInput(skeleton, input);
        }
    }
    public void constructSkeleton(Skeleton skeleton, List<Input> newInputs) {
        if (!skeleton.isSplit())
            skeleton.splitAssert();//删除原有的assert语句
        skeleton.setInputs(newInputs);
        skeleton.applyTransform(newInputs);

        List<Input> collect = skeleton.getInputs().stream().filter(input -> !input.isCompleted()).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            for (Input newInput :collect) {
                skeleton.getOracle(TransformHelper.bugRepository, newInput);//需要oracle的语句则需要先执行一遍
            }
        }

        collect = skeleton.getInputs().stream().filter(Input::isCompleted).collect(Collectors.toList());
        skeleton.addStatementsAtLast(collect);//对于不需要oracle的语句，直接根据input更新method

        skeleton.addMethodsAtCompilationUnit();//向原有类添加新的测试函数
    }

    public CompilationUnit constructSkeleton(Skeleton skeleton, Input newInput) {
        if (!skeleton.isSplit())
            skeleton.splitAssert();//删除原有的assert语句
        skeleton.addInput(newInput);
        skeleton.applyTransform(newInput);
        CompilationUnit newUnit;
        if (!newInput.isCompleted()) {
            newUnit = skeleton.getOracle(TransformHelper.bugRepository, newInput);//需要oracle的语句则需要先执行一遍
        } else {
            MethodDeclaration methodDeclaration = skeleton.addStatementAtLast(newInput.getMethodCallExpr());//对于不需要oracle的语句，直接根据input更新method
            newUnit = skeleton.getTransformedCompilationUnit(methodDeclaration);//向原有类添加新的测试函数
        }
        return newUnit;
    }

}
