package root.generation.helper;

import com.github.javaparser.ast.expr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entity.Input;
import root.generation.transformation.mutator.*;

import java.util.*;

public class MutatorHelper {

    private static final Logger logger = LoggerFactory.getLogger(MutatorHelper.class);

    public static Map<Class<? extends Expression>, AbstractInputMutator> MUTATORS;
    public static Map<String, List<Class<? extends Expression>>> INPUTS_BY_TYPE;

    public static void initialize() {
        //todo: whether to collect all values of different types in test file?
        MUTATORS = new HashMap<>();
        MUTATORS.put(BooleanLiteralExpr.class, new BooleanMutator());
        MUTATORS.put(DoubleLiteralExpr.class, new DoubleMutator());
        MUTATORS.put(IntegerLiteralExpr.class, new IntegerMutator());
        MUTATORS.put(LongLiteralExpr.class, new LongMutator());
        MUTATORS.put(StringLiteralExpr.class, new StringMutator());
        MUTATORS.put(CharLiteralExpr.class, new CharMutator());
        MUTATORS.put(UnaryExpr.class, new UnaryMutator());

        INPUTS_BY_TYPE = new HashMap<>();
        INPUTS_BY_TYPE.put("boolean", Collections.singletonList(BooleanLiteralExpr.class));
        INPUTS_BY_TYPE.put("java.util.Boolean", Collections.singletonList(BooleanLiteralExpr.class));
        INPUTS_BY_TYPE.put("int", Collections.singletonList(IntegerLiteralExpr.class));
        INPUTS_BY_TYPE.put("java.lang.Integer", Collections.singletonList(IntegerLiteralExpr.class));
        INPUTS_BY_TYPE.put("double", Collections.singletonList(DoubleLiteralExpr.class));
        INPUTS_BY_TYPE.put("java.lang.Double", Collections.singletonList(DoubleLiteralExpr.class));
        INPUTS_BY_TYPE.put("long", Collections.singletonList(LongLiteralExpr.class));
        INPUTS_BY_TYPE.put("java.lang.Long", Collections.singletonList(DoubleLiteralExpr.class));
        INPUTS_BY_TYPE.put("java.lang.String", Collections.singletonList(StringLiteralExpr.class));
        INPUTS_BY_TYPE.put("Unary", Collections.singletonList(UnaryExpr.class));
        List<Class<? extends Expression>> list = Arrays.asList(
                BooleanLiteralExpr.class, IntegerLiteralExpr.class,
                DoubleLiteralExpr.class, StringLiteralExpr.class);
        INPUTS_BY_TYPE.put("Object", list);
        INPUTS_BY_TYPE.put("java.lang.Object", list);
    }

    public static Object getInputMutant(Input oldInput) {
        List<Object> inputMutants = getInputMutants(oldInput, 1);
        Random random = new Random();
        return inputMutants.get(random.nextInt(inputMutants.size()));
    }

    public static List<Object> getInputMutants(Input oldInput, int num){
        // check whether type is known class,and apply its mutator to get its mutants.
        String qualifiedName = Helper.getType(oldInput.getBasicExpr());
        AbstractInputMutator mutator;
        if (MutatorHelper.isKnownType(qualifiedName)) {
            mutator = getKnownMutator(qualifiedName);
            List<Object> nextInputs = mutator.getNextInputs(oldInput.getBasicExpr(), num);
            return nextInputs;
        } else {
            mutator = getUnknownMutator(oldInput);
            List<Object> nextInputs = mutator.getNextInputs(oldInput.getBasicExpr(), num);
            return nextInputs;
        }
    }

    public static boolean isKnownType(String className) {
        return INPUTS_BY_TYPE.containsKey(className);
    }

    public static AbstractInputMutator getKnownMutator(String type) {
        if (MutatorHelper.INPUTS_BY_TYPE.containsKey(type)) {
            List<Class<? extends Expression>> classes = MutatorHelper.INPUTS_BY_TYPE.get(type);
            //Now, here is a random getter for mutators.
            Random random = new Random();
            int idx = random.nextInt(classes.size());
            Class<? extends Expression> target = classes.get(idx);
            return MutatorHelper.MUTATORS.get(target);
        } else {
            logger.error("Unsupported type of mutator!");
            throw new IllegalArgumentException("Illegal argument: " + type);
        }
    }

    public static AbstractInputMutator getUnknownMutator(Input input) {
        //?
        return MutatorHelper.MUTATORS.get(Expression.class);
    }
}
