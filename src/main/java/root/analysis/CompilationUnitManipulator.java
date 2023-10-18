package root.analysis;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import root.util.FileUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CompilationUnitManipulator extends ASTManipulator {
    private final int kind = ASTParser.K_COMPILATION_UNIT;

    public CompilationUnitManipulator(int complianceLevel) {
        getParser(true, complianceLevel);
    }
    public ASTParser getParser() {
        return parser;
    }

    public Set<MethodDeclaration> extractMethodByPos(char[] fileSource, Set<Integer> positions, boolean isLineNumber) {
        parser.setSource(fileSource);
        parser.setKind(kind);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        List<?> types = compilationUnit.types();
        Set<MethodDeclaration> methodDeclarations = new HashSet<>();
        for (Object obj :types) {
            TypeDeclaration typeDeclaration = (TypeDeclaration) obj;
            List list = typeDeclaration.bodyDeclarations();
            recursiveExtract(methodDeclarations, compilationUnit, positions, isLineNumber, list);
        }
        return methodDeclarations;
    }

    private void recursiveExtract(Set<MethodDeclaration> methodDeclarations, CompilationUnit compilationUnit, Set<Integer> positions, boolean isLineNumber, List list) {
        for (Object dcls :list) {
            if (dcls instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) dcls;
                if (isThisMethodDecl(compilationUnit, methodDeclaration, positions, isLineNumber))
                    methodDeclarations.add(methodDeclaration);
            }
            if (dcls instanceof TypeDeclaration) {
                TypeDeclaration typeDeclaration1 = (TypeDeclaration) dcls;
                recursiveExtract(methodDeclarations, compilationUnit, positions, isLineNumber, typeDeclaration1.bodyDeclarations());
            }
        }
    }

    private boolean isThisMethodDecl(CompilationUnit compilationUnit, MethodDeclaration methodDeclaration, Set<Integer> positions, boolean isLineNumber) {
        int startPosition = methodDeclaration.getStartPosition();
        int endPosition = methodDeclaration.getLength() + startPosition;
//                  ASTNode node = NodeFinder.perform(compilationUnit, startPosition, methodDeclaration.getLength());
        if (isLineNumber) {
            int start = compilationUnit.getLineNumber(startPosition);
            int end = compilationUnit.getLineNumber(endPosition);
            if (inThisFunction(positions, start, end)) {
                return true;
            }
        } else if (inThisFunction(positions,
                startPosition, endPosition)) {
            return true;
        }
        return false;
    }

    private boolean inThisFunction(Set<Integer> pos, int start, int end) {
        if (pos == null || pos.isEmpty())
            return false;
        List<Integer> positions = pos.stream().collect(Collectors.toList());
        positions = positions.stream().sorted().collect(Collectors.toList());
        if (positions.get(0) >= start && positions.get(0) <= end)
            return true;
        if (positions.get(positions.size() - 1) >= start && positions.get(positions.size() - 1) <= end)
            return true;
        int idx = positions.size() / 2;
        while(idx > 0 && idx < positions.size() - 1) {
            int last = positions.get(idx - 1);
            int mid = positions.get(idx);
            int next = positions.get(idx + 1);
            if (mid >= start && mid <= end) {
                return true;
            } else if (last < start && next > end) {
                return false;
            } else if (mid > end) {
                idx --;
            } else {
                idx ++;
            }
        }
        return false;
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
        Document document = new Document(String.valueOf(fileSource));
        parser.setSource(document.get().toCharArray());
        parser.setKind(kind);
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
            List<MethodDeclaration> junitType = Arrays.stream(typeDeclaration.getMethods()).filter(o -> o.toString().startsWith("@Test")).collect(Collectors.toList());
            if (junitType.isEmpty()) {
                //checkout if it needs annotation "@Test"
                boolean junit4 = targetMethod.toString().startsWith("@Test");
                if (junit4) {
                    List<IExtendedModifier> modifiers = ((MethodDeclaration) targetMethod).modifiers();
                    modifiers = modifiers.stream().filter(m ->m.isAnnotation()).collect(Collectors.toList());
                    ASTRewrite rewriter2 = ASTRewrite.create(targetMethod.getAST());
                    ListRewrite listRewrite2 = rewriter2.getListRewrite(targetMethod, MethodDeclaration.MODIFIERS2_PROPERTY);
                    for (IExtendedModifier m: modifiers) {
                        if (((Annotation) m).getTypeName().getFullyQualifiedName().startsWith("Test")) {
                            listRewrite2.remove((ASTNode) m, null);
                            SimpleName oldName = ((MethodDeclaration) targetMethod).getName();
                            if (!oldName.toString().startsWith("test")) {
                                Name newName = targetMethod.getAST().newName("test" + oldName.toString());
                                rewriter2.replace(oldName, newName, null);
                            }
                        }
                    }
                    //todo apply the changes for rewriter2
                }
            }
            List<MethodDeclaration> methods = Arrays.stream(typeDeclaration.getMethods()).filter(o -> o.getName().toString().equals(((MethodDeclaration)targetMethod).getName().toString())).collect(Collectors.toList());
            if (methods.isEmpty()) {
                listRewrite.insertLast(targetMethod, null);
                break;
            } else {
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
