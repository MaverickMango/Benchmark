package root.generation.transformation;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.quality.Nullable;
import com.github.javaparser.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.entities.Difference;
import root.generation.entities.Input;
import root.generation.helper.Helper;
import root.generation.transformation.mutator.*;

import java.util.*;

public class MutateHelper {

    private static final Logger logger = LoggerFactory.getLogger(MutateHelper.class);

    private static int MAX_MUTANTS_NUM;
    public static Map<Class<? extends Expression>, AbstractInputMutator> MUTATORS;
    public static Map<String, List<Class<? extends Expression>>> INPUTS_BY_TYPE;

    public static void initialize() {
        MAX_MUTANTS_NUM = 300;
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
        INPUTS_BY_TYPE.put("java.lang.Boolean", Collections.singletonList(BooleanLiteralExpr.class));
        INPUTS_BY_TYPE.put("int", Collections.singletonList(IntegerLiteralExpr.class));
        INPUTS_BY_TYPE.put("java.lang.Integer", Collections.singletonList(IntegerLiteralExpr.class));
        INPUTS_BY_TYPE.put("double", Collections.singletonList(DoubleLiteralExpr.class));
        INPUTS_BY_TYPE.put("java.lang.Double", Collections.singletonList(DoubleLiteralExpr.class));
        INPUTS_BY_TYPE.put("long", Collections.singletonList(LongLiteralExpr.class));
        INPUTS_BY_TYPE.put("java.lang.Long", Collections.singletonList(DoubleLiteralExpr.class));
        //todo
        INPUTS_BY_TYPE.put("char", Collections.singletonList(null));
        INPUTS_BY_TYPE.put("java.lang.Character", Collections.singletonList(null));
        INPUTS_BY_TYPE.put("java.lang.String", Collections.singletonList(StringLiteralExpr.class));
        List<Class<? extends Expression>> list = Arrays.asList(
                BooleanLiteralExpr.class, IntegerLiteralExpr.class,
                DoubleLiteralExpr.class, StringLiteralExpr.class);
        INPUTS_BY_TYPE.put("Object", list);
        INPUTS_BY_TYPE.put("java.lang.Object", list);
    }

    public static Pair<Expression, Object> getInputMutant(Input oldInput) {
        Random random = new Random(new Date().getTime());
        MAX_MUTANTS_NUM = 1;
        List<Pair<Expression, Object>> inputMutants = getInputMutants(oldInput, null);
        int idx = random.nextInt(inputMutants.size());
        return inputMutants.get(idx);
    }

    public static List<Pair<Expression, Object>> getInputMutants(Input oldInput, @Nullable List<Difference> differences){
        Random random = new Random(new Date().getTime());
        List<Expression> basicExprs = oldInput.getBasicExpr();//todo 重新写变异规则！
        Set<Pair<Expression, Object>> mutants = new HashSet<>();
        AbstractInputMutator mutator;
        while (mutants.size() < MAX_MUTANTS_NUM) {
            int idx = random.nextInt(basicExprs.size());
            Expression basicExpr = basicExprs.get(idx);
            // check whether type is known class,and apply its mutator to get its mutants.
            String qualifiedName = Helper.getType(basicExpr);
            if (MutateHelper.isKnownType(qualifiedName)) {
                mutator = getKnownMutator(qualifiedName);
                Object nextInput = mutator.getNextInputWithRange(basicExpr);
                mutants.add(new Pair<>(basicExpr, nextInput));
            } else {
                mutator = getUnknownMutator(oldInput);
                Object nextInput = mutator.getNextInputWithRange(basicExpr);
                mutants.add(new Pair<>(basicExpr, nextInput));
            }
        }
        return new ArrayList<>(mutants);
    }

    public static boolean isKnownType(String className) {
        return INPUTS_BY_TYPE.containsKey(className);
    }

    public static AbstractInputMutator getKnownMutator(String type) {
        if (MutateHelper.INPUTS_BY_TYPE.containsKey(type)) {
            List<Class<? extends Expression>> classes = MutateHelper.INPUTS_BY_TYPE.get(type);
            //Now, here is a random getter for mutators.
            Random random = new Random();
            int idx = random.nextInt(classes.size());
            Class<? extends Expression> target = classes.get(idx);
            return MutateHelper.MUTATORS.get(target);
        } else {
            logger.error("Unsupported type of mutator!");
            throw new IllegalArgumentException("Illegal argument: " + type);
        }
    }

    public static AbstractInputMutator getUnknownMutator(Input input) {
        //?
        return MutateHelper.MUTATORS.get(Expression.class);
    }

    public static Expression getUnsatisfiedCondition(Expression condition) {
        UnaryExpr unsatisfied = new UnaryExpr();
        unsatisfied.setOperator(UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        unsatisfied.setExpression(new EnclosedExpr(condition));
        return unsatisfied;
    }
}
