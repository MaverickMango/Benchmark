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
import root.analysis.groum.entity.InvolvedVar;

import java.util.List;
import java.util.Set;

public class AttributeVisitor extends VoidVisitorAdapter<Set<InvolvedVar>> {

    ExtractFromJavaParser extractFromJavaParser = new ExtractFromJavaParser();

    @Override
    public void visit(NameExpr n, Set<InvolvedVar> arg) {
        try {
            ResolvedValueDeclaration resolve = n.resolve();
            if (resolve instanceof JavaParserFieldDeclaration ||
                resolve instanceof JavaParserParameterDeclaration ||
                resolve instanceof JavaParserVariableDeclaration) {
                arg.add(extractFromJavaParser.extractVar(n));
            }
        } catch (Exception e) {
        }
    }
}
