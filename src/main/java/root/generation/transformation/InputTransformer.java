package root.generation.transformation;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entity.Input;
import root.generation.entity.Prefix;
import root.generation.helper.MutatorHelper;
import root.generation.parser.AbstractASTParser;
import root.generation.transformation.extractor.InputExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputTransformer {

    private final Logger logger = LoggerFactory.getLogger(InputTransformer.class);

    Map<MethodDeclaration, List<Prefix>> prefixes;//?应该是个什么列表

    public InputTransformer(MethodDeclaration methodDeclaration, int lineNumber) {
        this.prefixes = new HashMap<>();
        prefixes.put(methodDeclaration, new ArrayList<>());
        prefixes.get(methodDeclaration).add(new Prefix(methodDeclaration, lineNumber));
    }

    public void addMethodToProcess(MethodDeclaration methodDeclaration, int lineNumber) {
        if (prefixes.containsKey(methodDeclaration)) {
            List<Prefix> list = prefixes.get(methodDeclaration);
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
            return new Input(oldInput.getMethodCallExpr(), newInputExpr, oldInput.getType());
        } catch (ClassCastException e) {
            logger.error("Error casting while generate new input by argument '" + value + "'! " + e.getMessage());
            logger.info("No newInput was generated. OldInput return.");
            return oldInput;
        }
    }

    public void buildNewTestByInput(Input newInput) {
        //todo first: 根据新生成的输入构建新的测试，还原回prefix里，有断言的执行一遍后再收取结果，没断言的直接完成一整个类的构建。

    }
}
