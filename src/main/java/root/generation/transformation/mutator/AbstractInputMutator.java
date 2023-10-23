package root.generation.transformation.mutator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInputMutator {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInputMutator.class);

    List<Object> inputs = new ArrayList<>();

    public abstract Object getNextInput();

    public List<Object> getInputs() {
        return this.inputs;
    }

    protected void addInputMutants(Object value) {
        inputs.add(value);
    }
}
