package root.analysis;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import root.util.FileUtils;
import root.util.GitAccess;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ASTManipulator implements GitAccess {

    private ASTParser parser = null;

    public ASTManipulator(int complianceLevel) {
        getParser(true, complianceLevel);
    }

    public ASTParser getParser(boolean newOne, int complianceLevel) {
        if (parser == null || newOne) {
            if (complianceLevel == 8)
                parser = ASTParser.newParser(AST.JLS8);
            else
                parser = ASTParser.newParser(AST.getJLSLatest());
        }
        return parser;
    }

    public MethodDeclaration extractTest(char[] fileSource, String methodName
            , List<?> targetImports, List<MethodDeclaration> dependencies) {
        parser.setSource(fileSource);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        final MethodDeclaration[] method = new MethodDeclaration[1];
        ASTVisitor astVisitor = new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                if (node.getParent() instanceof TypeDeclaration) {
                    if (node.getName().toString().equals(methodName)) {
                        method[0] = node;
                    }
                    if (!node.getName().toString().startsWith("test")) {
                        dependencies.add(node);
                    }
                }
                return super.visit(node);
            }
        };
        compilationUnit.accept(astVisitor);
        targetImports.addAll(compilationUnit.imports());
        return method[0];
    }

    public String insertTest(char[] fileSource, ASTNode targetMethod, String mappingFile
            , List<?> targetImports, List<MethodDeclaration> dependencies) {
        //todo: checkout if it needs annotation "@Test"
        Document document = new Document(String.valueOf(fileSource));
        parser.setSource(document.get().toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        ASTRewrite rewriter = ASTRewrite.create(compilationUnit.getAST());
        List<String> collect = (List<String>) compilationUnit.imports().stream().map(o -> ((ImportDeclaration)o).getName().toString()).collect(Collectors.toList());
        List<?> importAdded= targetImports.stream().filter(o -> !collect.contains(((ImportDeclaration) o).getName().toString())).collect(Collectors.toList());
        ListRewrite importRewrite = rewriter.getListRewrite(compilationUnit, CompilationUnit.IMPORTS_PROPERTY);
        List<String> mappings = FileUtils.readEachLine(mappingFile);
        for (Object obj :importAdded) {
            ImportDeclaration aImport = (ImportDeclaration) obj;
            String packageName = aImport.getName().toString().replaceAll("[.]", File.separator);
            List<String> mapping = mappings.stream().filter(o -> o.contains(packageName)).collect(Collectors.toList());
            String[] tmp;
            if (!mapping.isEmpty() && (tmp = mapping.get(0).split("\t"))[0].startsWith("R")) {
                String qualifiedName = tmp[2].substring(tmp[1].indexOf(packageName), tmp[2].lastIndexOf("[.]")).replaceAll(File.separator, ".");
                //todo: java.lang.IllegalArgumentException
                aImport.setName(compilationUnit.getAST().newName(qualifiedName));
            }
            importRewrite.insertLast(aImport, null);
        }
        List<?> types = compilationUnit.types();
        for (Object obj :types) {
            TypeDeclaration typeDeclaration = (TypeDeclaration) obj;
            ListRewrite listRewrite = rewriter.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
            List<MethodDeclaration> methods = Arrays.stream(typeDeclaration.getMethods()).filter(o -> o.getName().toString().equals(((MethodDeclaration)targetMethod).getName().toString())).collect(Collectors.toList());
            if (methods.isEmpty())
                listRewrite.insertLast(targetMethod, null);
            else {
                listRewrite.replace(methods.get(0), targetMethod, null);
            }
        }
        try {
            TextEdit edit = rewriter.rewriteAST(document, null);
            UndoEdit undo = edit.apply(document);
        } catch(MalformedTreeException | BadLocationException e) {
            e.printStackTrace();
        }
        String editedFile = document.get().toString();
        return editedFile;
    }
}
