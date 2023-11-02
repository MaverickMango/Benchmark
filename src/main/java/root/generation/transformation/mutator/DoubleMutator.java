package root.generation.transformation.mutator;

import com.github.javaparser.ast.expr.DoubleLiteralExpr;

import java.util.Random;

public class DoubleMutator extends AbstractInputMutator{

    private final Random random = new Random();

    @Override
    public Object getNextInput(Object oldValue) {
        DoubleLiteralExpr expr = new DoubleLiteralExpr();
        expr.setDouble(random.nextDouble());
        this.addInputMutants(expr);
        return expr;
    }
}
