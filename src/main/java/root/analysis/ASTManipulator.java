package root.analysis;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public abstract class ASTManipulator {

    protected ASTParser parser = null;

    public ASTParser getParser(boolean newOne, int complianceLevel) {
        if (parser == null || newOne) {
            if (complianceLevel == 8)
                parser = ASTParser.newParser(AST.JLS8);
            else
                parser = ASTParser.newParser(AST.getJLSLatest());
        }
        return parser;
    }

}
