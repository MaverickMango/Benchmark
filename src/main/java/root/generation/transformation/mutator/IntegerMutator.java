package root.generation.transformation.mutator;

import com.github.javaparser.ast.expr.IntegerLiteralExpr;

import java.util.Random;

public class IntegerMutator extends AbstractInputMutator{

    private final Random random = new Random();

    @Override
    public Object getNextInput(Object oldValue) {
        IntegerLiteralExpr expr = new IntegerLiteralExpr();
        expr.setInt(random.nextInt());
        this.addInputMutants(expr);
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }

}
