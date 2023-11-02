package root.generation.transformation.mutator;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;

import java.util.Random;

public class BooleanMutator extends AbstractInputMutator {

    private final Random random = new Random();

    @Override
    public Object getNextInput(Object oldValue) {
        //now is random, but not always!
        BooleanLiteralExpr expr = new BooleanLiteralExpr();
        expr.setValue(random.nextBoolean());
        this.addInputMutants(expr);
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }
}
