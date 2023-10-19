package root.generation.helper;

import com.github.javaparser.ast.expr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.transformation.mutator.*;

import java.util.*;

public class MutatorHelper {

    private static final Logger logger = LoggerFactory.getLogger(MutatorHelper.class);

    public static Map<Class<? extends Expression>, AbstractInputMutator> MUTATORS;
    public static Map<String, List<Class<? extends Expression>>> INPUTS_BY_TYPE;

    public static void initialize() {
        //todo: collect all values of different types in test file?
        MUTATORS = new HashMap<>();
        MUTATORS.put(BooleanLiteralExpr.class, new BooleanMutator());
        MUTATORS.put(DoubleLiteralExpr.class, new DoubleMutator());
        MUTATORS.put(IntegerLiteralExpr.class, new IntegerMutator());
        MUTATORS.put(LongLiteralExpr.class, new LongMutator());
        MUTATORS.put(StringLiteralExpr.class, new StringMutator());

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
        List<Class<? extends Expression>> list = Arrays.asList(
                BooleanLiteralExpr.class, IntegerLiteralExpr.class,
                DoubleLiteralExpr.class, StringLiteralExpr.class);
        INPUTS_BY_TYPE.put("Object", list);
        INPUTS_BY_TYPE.put("java.lang.Object", list);
    }

    public static boolean isKnownClass(String className) {
        return INPUTS_BY_TYPE.containsKey(className);
    }
}
