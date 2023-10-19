package root.generation.transformation.mutator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInputMutator {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInputMutator.class);

    List<Object> inputs = new ArrayList<>();

    abstract Object getInput();

    List<Object> getInputs() {
        return this.inputs;
    }

    protected void addInputMutants(Object value) {
        inputs.add(value);
    }
}
