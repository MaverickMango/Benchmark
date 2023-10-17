package root.analysis;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;
import java.util.stream.Collectors;

public class MethodManipulator extends ASTManipulator{

    public String getFunctionSig(MethodDeclaration methodDeclaration) {
        String sig = "";
        CompilationUnit compilationUnit = (CompilationUnit) methodDeclaration.getRoot();
        String packageName = compilationUnit.getPackage().getName().toString();
        ASTNode innerType = methodDeclaration.getParent();
        if (innerType instanceof TypeDeclaration) {
            List types = compilationUnit.types();
            sig = packageName + "." + getInnerType(types, ((TypeDeclaration) innerType).getName().toString())
                    + ":" + methodDeclaration.getName().toString();
        } else {
            sig = methodDeclaration.getName().toString();
        }
        sig += ":" + methodDeclaration.parameters();
        sig += ":" + methodDeclaration.getReturnType2();
        return sig;
    }

    private String getInnerType(List types, String typeName) {
        for (Object obj: types) {
            TypeDeclaration type = (TypeDeclaration) obj;
            if (typeName.equals(type.getName().toString())) {
                return type.getName().toString();
            }
            List collect = (List) type.bodyDeclarations().stream().filter(o -> o instanceof TypeDeclaration).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                String innerType = getInnerType(collect, typeName);
                if (innerType != null) {
                    return type.getName().toString() + "\\$" + innerType;
                }
            }
        }
        return null;
    }
}
