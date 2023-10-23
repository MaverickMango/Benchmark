package root.generation.transformation.mutator;

import java.util.Random;

public class DoubleMutator extends AbstractInputMutator{

    private final Random random = new Random();

    @Override
    public Object getNextInput() {
        this.addInputMutants(random.nextDouble());
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }
}
