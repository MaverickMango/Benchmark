package root.generation.parser;

import org.eclipse.jdt.core.dom.ASTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public abstract class AbstractASTParser {

    final Logger logger = LoggerFactory.getLogger(AbstractASTParser.class);
    Map<String, Object> asts;

    public AbstractASTParser(String srcJavaDir, String srcTestDir,
                             Set<String> dependencies) {
    }

    public abstract void parseASTs(String fileDir);

    public abstract Map<String, Object> getASTs();

}
