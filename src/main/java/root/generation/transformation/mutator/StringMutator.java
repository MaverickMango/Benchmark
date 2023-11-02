package root.generation.transformation.mutator;

import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StringMutator extends AbstractInputMutator{

    private final Random random = new Random();
    private final String specialChars = "!@#$%^&*()+-=_[]{};:'\",./<>?|\\`~";
    List<String> usedStrings = new ArrayList<>();

    @Override
    public Object getNextInput(Object oldValue) {
        StringLiteralExpr expr = new StringLiteralExpr();
        expr.setString((String) randomStrMutate(oldValue.toString()));
        this.addInputMutants(expr);
        return this.inputs.get(random.nextInt(this.inputs.size()));
    }

    private Object randomStrMutate(String root) {
        String target = root;
        int idx;
//        if (usedStrings.size() > 0) {
//            idx = random.nextInt(usedStrings.size());
//            target = usedStrings.get(idx);
//        }
        usedStrings.add(root);
        while (target.length() > 0 && usedStrings.contains(target)) {
            idx = random.nextInt(target.length());
            char randomChar = specialChars.charAt(random.nextInt(specialChars.length()));
            target = target.substring(0, idx) + randomChar + target.substring(idx);
        }
        usedStrings.add(target);
        return "\"" + target + "\"";
    }
}
