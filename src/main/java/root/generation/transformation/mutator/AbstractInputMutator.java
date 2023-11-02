package root.generation.transformation.mutator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractInputMutator {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInputMutator.class);

    Set<Object> inputs = new HashSet<>();
    int num;

    public abstract Object getNextInput(Object oldValue);

    public List<Object> getNextInputs(Object oldValue, int num) {
        this.num = num;
        while (getInputs().size() < num) {
            this.getNextInput(oldValue);
        }
        return getInputs();
    }

    public List<Object> getInputs() {
        return Arrays.asList(this.inputs.toArray());
    }

    protected void addInputMutants(Object value) {
        inputs.add(value);
    }
}
