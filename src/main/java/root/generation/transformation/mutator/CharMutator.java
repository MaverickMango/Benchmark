package root.generation.transformation.mutator;

import com.github.javaparser.ast.expr.CharLiteralExpr;

import java.util.Random;

public class CharMutator extends AbstractInputMutator{

    private final Random random = new Random();
    private final String specialChars = "!@#$%^&*()+-=_[]{};:'\",./<>?|\\`~";
    private final String vocabulary = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public Object getNextInput(Object oldValue) {
        CharLiteralExpr expr = new CharLiteralExpr();
        expr.setChar(randomCharMutate());
        this.addInputMutants(expr);
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }

    private Character randomCharMutate() {
        int idx = random.nextInt();
        String s = specialChars + vocabulary;
        return s.charAt(idx);
    }
}
