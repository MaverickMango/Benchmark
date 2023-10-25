package root.generation.transformation.mutator;

import java.util.Random;

public class CharMutator extends AbstractInputMutator{

    private final Random random = new Random();
    private final String specialChars = "!@#$%^&*()+-=_[]{};:'\",./<>?|\\`~";
    private final String vocabulary = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public Object getNextInput() {
        this.addInputMutants(randomCharMutate());
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }

    private Character randomCharMutate() {
        int idx = random.nextInt();
        String s = specialChars + vocabulary;
        return s.charAt(idx);
    }
}
