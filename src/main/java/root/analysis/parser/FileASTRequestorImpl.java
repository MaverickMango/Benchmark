package root.analysis.parser;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IBinding;

import java.util.Map;

public class FileASTRequestorImpl extends FileASTRequestor {

    Map<String, Object> asts;

    public FileASTRequestorImpl(Map<String, Object> asts) {
        this.asts = asts;
    }

    @Override
    public void acceptAST(String sourceFilePath, CompilationUnit ast) {
        asts.put(sourceFilePath, ast);
        //todo: implement a visitor here to collection some subnodes?
    }

    @Override
    public void acceptBinding(String bindingKey, IBinding binding) {
        super.acceptBinding(bindingKey, binding);
    }
}
