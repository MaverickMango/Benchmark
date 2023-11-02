package root.generation.transformation.mutator;

import com.github.javaparser.ast.expr.LongLiteralExpr;

import java.util.Random;

public class LongMutator extends AbstractInputMutator{

    private final Random random = new Random();

    @Override
    public Object getNextInput(Object oldValue) {
        LongLiteralExpr expr = new LongLiteralExpr();
        expr.setLong(random.nextLong());
        this.addInputMutants(expr);
        return expr;
    }

}
