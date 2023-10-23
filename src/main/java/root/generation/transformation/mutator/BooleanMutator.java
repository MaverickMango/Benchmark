package root.generation.transformation.mutator;

import java.util.Random;

public class BooleanMutator extends AbstractInputMutator {

    private final Random random = new Random();

    @Override
    public Object getNextInput() {
        //now is random, but not always!
        this.addInputMutants(random.nextBoolean());
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }
}
