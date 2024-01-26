package root.parser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public abstract class AbstractASTParser {

    final Logger logger = LoggerFactory.getLogger(AbstractASTParser.class);
    Map<String, Object> asts;

    public AbstractASTParser(String srcJavaDir, String srcTestDir,
                             Set<String> dependencies) {
    }

    public abstract void parseASTs(String fileDir) throws IOException;

    public abstract CompilationUnit getAST(String filePath);

    public abstract Map<String, Object> getASTs();

}
