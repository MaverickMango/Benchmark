package root.generation.transformation;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.generation.helper.MutatorHelper;

import java.util.*;

public class InputTransformer {

    private final Logger logger = LoggerFactory.getLogger(InputTransformer.class);

    Map<MethodDeclaration, List<Skeleton>> skeletons;//?应该是个什么列表

    public InputTransformer() {
        this.skeletons = new HashMap<>();
    }

    public InputTransformer(MethodDeclaration methodDeclaration) {
        this.skeletons = new HashMap<>();
        skeletons.put(methodDeclaration, new ArrayList<>());
    }

    public void addMethodToProcess(MethodDeclaration methodDeclaration) {
        if (skeletons.containsKey(methodDeclaration)) {
            List<Skeleton> list = skeletons.get(methodDeclaration);
            //todo compare prefix equality.
        }
    }

    public Input transformInput(Input oldInput, Object value) {
        if (oldInput.getInputExpr().getParentNode().isEmpty()) {
            logger.error("Node " + oldInput.getInputExpr() + " has lost its parent node, can't process further!");
            throw new IllegalArgumentException(oldInput.toString());
        }
        logger.info("New input '" + value.toString() + "' transforming for " + oldInput);
        Expression inputExpr = oldInput.getInputExpr();//todo: generate surround old expression value?
        Expression newInputExpr = null;
        try {
            if (oldInput.isPrimitive()) {
                Class<? extends Expression> inputType = MutatorHelper.INPUTS_BY_TYPE.get(oldInput.getType()).get(0);
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
                if (inputType.equals(LongLiteralExpr.class)) {
                    newInputExpr = new LongLiteralExpr();
                    ((LongLiteralExpr) newInputExpr).setLong((Long) value);
                }
                if (inputType.equals(StringLiteralExpr.class)) {
                    newInputExpr = new StringLiteralExpr();
                    ((StringLiteralExpr) newInputExpr).setString((String) value);
                }
            } else {
                //todo: add support to Object.
                throw new IllegalArgumentException("TBD. Unsupported type of Input has been given.");
            }
            return new Input(oldInput.getMethodCallExpr(),
                    newInputExpr, oldInput.getType(), oldInput.getArgIdx());
        } catch (ClassCastException e) {
            logger.error("Error casting while generate new input by argument '" + value + "'! " + e.getMessage());
            logger.info("No newInput was generated. OldInput return.");
            return oldInput;
        }
    }

    public void buildNewTestByInput(Skeleton skeleton, Input newInput) {
        MethodDeclaration methodDeclaration = skeleton.getOriginalMethod();
        skeleton.constructSkeleton(newInput);
        if (this.skeletons.containsKey(methodDeclaration)) {
            this.skeletons.get(methodDeclaration).add(skeleton);
        } else {
            List<Skeleton> list = new ArrayList<>();
            list.add(skeleton);
            this.skeletons.put(methodDeclaration, list);
        }
    }

    public void buildNewTestByInputs(Skeleton skeleton, List<Input> newInputs) {
        for (Input input :newInputs) {
            buildNewTestByInput(skeleton, input);
        }
    }

    public static void getOracle(List<Input> inputs) {
        //todo:把需要获取oracle的函数放回到original版本运行

    }
}
