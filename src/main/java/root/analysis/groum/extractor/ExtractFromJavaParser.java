package root.analysis.groum.extractor;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.groum.entity.AbstractNode;
import root.analysis.groum.entity.ActionNode;
import root.analysis.groum.entity.InvolvedVar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractFromJavaParser {

    final static Logger logger = LoggerFactory.getLogger(ExtractFromJavaParser.class);

    Map<String, InvolvedVar> varNames = new HashMap<>();

    public InvolvedVar extractVar(Parameter n) {
        String type = n.getTypeAsString();
        String name = n.getNameAsString();
        InvolvedVar involvedVar = new InvolvedVar(type, name);
        if (!varNames.containsKey(involvedVar.toString())) {
            varNames.put(involvedVar.toString(), involvedVar);
        }
        return varNames.get(involvedVar.toString());
    }

    public InvolvedVar extractVar(NameExpr n) {
        String type = "Unresolved";
        String name = n.getNameAsString();
        try {
            type = n.calculateResolvedType().describe();
        } catch (Exception e) {
            logger.debug("Can't resolve the type of variable " + name);
        }
        InvolvedVar involvedVar = new InvolvedVar(type, name);
        if (!varNames.containsKey(involvedVar.toString())) {
            varNames.put(involvedVar.toString(), involvedVar);
        }
        return varNames.get(involvedVar.toString());
    }

    public InvolvedVar extractVar(VariableDeclarator n) {
        String type = n.getTypeAsString();
        String name = n.getNameAsString();
        InvolvedVar involvedVar = new InvolvedVar(type, name);
        if (!varNames.containsKey(involvedVar.toString())) {
            varNames.put(involvedVar.toString(), involvedVar);
        }
        return varNames.get(involvedVar.toString());
    }

    public AbstractNode extract(MethodCallExpr n) {
        String name = n.getNameAsString();
        String className = "Unresolved";
        try {
            className = n.resolve().getClassName();
        } catch (Exception e) {
            logger.debug("Can't resolve the class name of method " + n.toString());
        }
//        n.getArguments()
        return new ActionNode(n, className, name);
    }

    public AbstractNode extract(ObjectCreationExpr n) {
//        n.getType().accept(this, arg);
//        n.getTypeArguments().ifPresent(l -> l.forEach(v -> v.accept(this, arg)));
        String name = n.getTypeAsString();
        StringBuilder stringBuilder = new StringBuilder();
        n.getTypeArguments().ifPresent(l -> {
            stringBuilder.append("<");
            l.forEach(t -> stringBuilder.append(t.toDescriptor()).append(","));
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), ">");
        });
        return new ActionNode(n, name + stringBuilder, "<init>");
    }

    public AbstractNode extract(ArrayCreationExpr n) {
//        n.getElementType().accept(this, arg);
        String name = n.getElementType().asString();
        return new ActionNode(n, name, "<init>");
    }

    public AbstractNode extract(FieldAccessExpr n) {
        String name = n.getNameAsString();
        String className = "Unresolved";
        try {
            className = n.calculateResolvedType().describe();
        } catch (Exception e) {
            logger.debug("Can't resolve the class name of method " + n.toString());
        }
        return new ActionNode(n, className, name);
    }

    public AbstractNode extract(AssignExpr n) {
        String name = n.getTarget().toString();
        String className = "Unresolved";
        try {
            className = n.getTarget().calculateResolvedType().describe();
        } catch (Exception e) {
            logger.debug("Can't resolve the class name of method " + n.toString());
        }
        return new ActionNode(n, className, name);
    }

    public AbstractNode extract(UnaryExpr n) {
        String name = n.getExpression().toString();
        String className = "Unresolved";
        try {
            className = n.getExpression().calculateResolvedType().describe();
        } catch (Exception e) {
            logger.debug("Can't resolve the class name of method " + n.toString());
        }
        return new ActionNode(n, className, name);
    }

    public AbstractNode extract(ReturnStmt n) {
        String className = "Void";
        if (n.getExpression().isPresent()) {
            Expression expression = n.getExpression().get();
//            String name = expression.toString() + "<return>";
            try {
                className = expression.calculateResolvedType().describe();
            } catch (Exception e) {
                logger.debug("Can't resolve the class name of method " + n.toString());
            }
//            return new ActionNode(n, className, name);
        }
        return new ActionNode(n, className, "<return>");
    }

    public AbstractNode extract(CastExpr n) {
        String name = "<cast>";
        String type = n.getTypeAsString();
        return new ActionNode(n, type, name);
    }

    public AbstractNode extract(MethodDeclaration n) {
        String name = n.getNameAsString() + "#<init>";
        String className = "Unresolved";
        try {
            className = n.resolve().getClassName();
        } catch (Exception e) {
            logger.debug("Can't resolve the class name of method " + n.toString());
        }
        return new ActionNode(n, className, name);
    }

    public AbstractNode extract(ConstructorDeclaration n) {
        String name = n.getNameAsString() + "#<init>";
        String className = "Unresolved";
        try {
            className = n.resolve().getClassName();
        } catch (Exception e) {
            logger.debug("Can't resolve the class name of method " + n.toString());
        }
        return new ActionNode(n, className, name);
    }
}
