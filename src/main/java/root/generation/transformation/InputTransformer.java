package root.generation.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entity.Input;
import root.generation.helper.MutatorHelper;
import root.generation.parser.AbstractASTParser;
import root.generation.transformation.extractor.InputExtractor;

public class InputTransformer {

    private static final Logger logger = LoggerFactory.getLogger(InputExtractor.class);

    AbstractASTParser parser;

    public InputTransformer(AbstractASTParser parser) {
        this.parser = parser;
    }

    public void replaceInput(Input oldInput) {
        if (!oldInput.getInputExpr().getParentNode().isPresent()) {
            logger.error("Node " + oldInput.getInputExpr() + " has lost its parent node, can't process further!");
            return;
        }
// todo: check whether type is known class,and apply its mutator!
//        if (MutatorHelper.isKnownClass(oldInput.getType()))
    }
}
