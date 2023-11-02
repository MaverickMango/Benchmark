package root.generation.transformation.mutator;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import root.generation.helper.Helper;
import root.generation.helper.MutatorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UnaryMutator extends AbstractInputMutator {
    private final Random random = new Random();
    List<UnaryExpr.Operator> cans;

    public UnaryMutator() {
        cans = new ArrayList<>();
        cans.add(UnaryExpr.Operator.PLUS);
        cans.add(UnaryExpr.Operator.MINUS);
    }

    @Override
    public Object getNextInput(Object oldValue) {
        UnaryExpr expr = (UnaryExpr) randomMutate((UnaryExpr) oldValue);
        this.addInputMutants(expr);
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }

    private Object randomMutate(UnaryExpr oldValue) {
        UnaryExpr newValue = oldValue.clone();
        Expression expression = newValue.getExpression();
        Expression nextInput = (Expression) MutatorHelper.getKnownMutator(Helper.getType(expression)).getNextInput(expression);
        newValue.setExpression(nextInput);
        UnaryExpr.Operator operator = newValue.getOperator();
        if (cans.contains(operator)) {
            int idx = random.nextInt(2);
            operator = cans.get(idx);
            newValue.setOperator(operator);
        }
        return newValue;
    }
}
