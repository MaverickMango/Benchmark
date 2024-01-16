package root.analysis.groum.extractor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserVariableDeclaration;

import java.util.List;

public class AttributeVisitor extends VoidVisitorAdapter<List<NameExpr>> {

    @Override
    public void visit(NameExpr n, List<NameExpr> arg) {
        try {
            ResolvedValueDeclaration resolve = n.resolve();
            if (resolve instanceof JavaParserFieldDeclaration ||
                resolve instanceof JavaParserParameterDeclaration ||
                resolve instanceof JavaParserVariableDeclaration) {
                arg.add(n);
            }
        } catch (Exception e) {
            arg.add(n);
        }
    }
}
