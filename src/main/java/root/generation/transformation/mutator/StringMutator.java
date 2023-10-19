package root.generation.transformation.mutator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StringMutator extends AbstractInputMutator{

    private final Random random = new Random();
    private final String specialChars = "!@#$%^&*()+-=_[]{};:'\",./<>?|\\`~";
    List<String> usedStrings = new ArrayList<>();

    @Override
    protected Object getInput() {
        this.addInputMutants(randomStrMutate(""));
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }

    protected Object getInput(String root) {
        this.addInputMutants(randomStrMutate(root));
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }

    private Object randomStrMutate(String root) {
        String target = root;
        int idx;
        if (usedStrings.size() > 0) {
            idx = random.nextInt(usedStrings.size());
            target = usedStrings.get(idx);
        }

        if (random.nextInt(5) == 0 && target.length() > 0) {
            idx = random.nextInt(target.length());
            char randomChar = specialChars.charAt(random.nextInt(specialChars.length()));
            target = target.substring(0, idx) + randomChar + target.substring(idx);
        }
        return "\"" + target + "\"";
    }
}
