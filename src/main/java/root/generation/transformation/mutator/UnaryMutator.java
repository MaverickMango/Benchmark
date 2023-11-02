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
        return expr;
    }

    private Object randomMutate(UnaryExpr oldValue) {
        UnaryExpr newValue = oldValue.clone();
        Expression expression = newValue.getExpression();
//        String type = Helper.getType(expression);//todo type must be resolved under a compilationUnit, now the oldValue is an orphan.
//        Expression nextInput = (Expression) MutatorHelper.getKnownMutator(type).getNextInput(expression);
//        newValue.setExpression(nextInput);
        UnaryExpr.Operator operator = newValue.getOperator();
        if (cans.contains(operator)) {
            int idx = random.nextInt(2);
            operator = cans.get(idx);
            newValue.setOperator(operator);
        }
        return newValue;
    }
}
