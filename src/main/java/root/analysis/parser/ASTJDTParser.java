package root.analysis.parser;

import com.github.javaparser.ast.CompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import root.util.ConfigurationProperties;
import root.util.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ASTJDTParser extends AbstractASTParser {
    ASTParser parser;
    FileASTRequestor requestor;
    
    public ASTJDTParser(String srcJavaDir, String srcTestDir, Set<String> dependencies,
                        Map<String, Object> asts) {
        super(srcJavaDir, srcTestDir, dependencies);
        String complianceLevel = ConfigurationProperties.getProperty("complianceLevel");
        parser = ASTParser.newParser(getASTLevel(complianceLevel));
        String[] classpathEntries = null;
        if (dependencies != null) {
            classpathEntries = dependencies.toArray(new String[dependencies.size()]);
        }
        parser.setEnvironment(classpathEntries, new String[]{srcJavaDir, srcTestDir}, null, true);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(getComplianceLevel(complianceLevel), options);
        parser.setCompilerOptions(options);

        this.asts = asts;
        requestor = new FileASTRequestorImpl(asts);
    }

    @Override
    public void parseASTs(String fileDir) throws IOException {
        List<String> allFiles = FileUtils.findAllFilePaths(fileDir, ".java");
        parser.createASTs(allFiles.toArray(new String[allFiles.size()]), null,
                new String[]{"UTF-8"}, requestor, null);
    }

    @Override
    public CompilationUnit getAST(String filePath) {
        parser.createASTs(new String[]{filePath}, null,
                new String[]{"UTF-8"}, requestor, null);
        return (CompilationUnit) asts.get(filePath);
    }

    @Override
    public Object parseASTFromCode(String code) {
        return null;
    }

    @Override
    public Map<String, Object> getASTs() {
        return asts;
    }

    private int getASTLevel(String complianceLevel) {
        int level;
        switch (complianceLevel) {
            case "1.4":
            case "4":
                level = AST.JLS4;
                break;
            case "1.3":
            case "3":
                level = AST.JLS3;
                break;
            case "1.2":
            case "2":
                level = AST.JLS2;
                break;
            case "1.9":
            case "9":
                level = AST.JLS9;
                break;
            case "10":
                level = AST.JLS10;
                break;
//            case "11":
//                level = AST.JLS11;
            case "1.8":
            case "8":
            case "1.7":
            case "7":
            case "1.6":
            case "6":
            case "1.5":
            case "5":
            default:
                level = AST.JLS8;
        }
        return level;
    }

    private String getComplianceLevel(String complianceLevel) {
        String level;
        switch (complianceLevel) {
            case "1.4":
            case "4":
                level = JavaCore.VERSION_1_4;
                break;
            case "1.3":
            case "3":
                level = JavaCore.VERSION_1_3;
                break;
            case "1.2":
            case "2":
                level = JavaCore.VERSION_1_2;
                break;
            case "1.9":
            case "9":
                level = JavaCore.VERSION_9;
                break;
            case "10":
                level = JavaCore.VERSION_10;
                break;
//            case "11":
//                level = JavaCore.VERSION_11;
//                break;
            case "1.7":
            case "7":
                level = JavaCore.VERSION_1_7;
                break;
            case "1.6":
            case "6":
                level = JavaCore.VERSION_1_6;
                break;
            case "1.5":
            case "5":
                level = JavaCore.VERSION_1_5;
                break;
            case "1.8":
            case "8":
            default:
                level = JavaCore.VERSION_1_8;
        }
        return level;
    }
}
