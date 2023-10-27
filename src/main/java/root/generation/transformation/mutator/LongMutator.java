package root.generation.transformation.mutator;

import java.util.Random;

public class LongMutator extends AbstractInputMutator{

    private final Random random = new Random();

    @Override
    public Object getNextInput(Object oldValue) {
        this.addInputMutants(random.nextLong());
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }

}
