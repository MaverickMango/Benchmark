package root.generation.transformation.mutator;

import com.github.javaparser.ast.expr.IntegerLiteralExpr;

import java.util.Random;

public class IntegerMutator extends AbstractInputMutator{

    private final Random random = new Random();

    @Override
    public Object getNextInput(Object oldValue) {
        int old = Integer.parseInt(oldValue.toString());
        IntegerLiteralExpr expr = new IntegerLiteralExpr();
        expr.setInt(old - num + random.nextInt(old + num) + 1);
        this.addInputMutants(expr);
        return expr;
    }

}
