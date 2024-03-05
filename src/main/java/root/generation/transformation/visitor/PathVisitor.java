package root.generation.transformation.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.entities.PathCondition;
import root.generation.transformation.MutateHelper;
import root.util.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PathVisitor extends VoidVisitorAdapter<PathCondition> {
    boolean isEntry;
    List<Integer> lineno = null;
    VariableVisitor variableVisitor;

    public PathVisitor() {
        isEntry = false;
        variableVisitor = new VariableVisitor();
    }
    public void setEntry(boolean isEntry) {
        this.isEntry = isEntry;
    }

    public void setLineno(List<Integer> lineno) {//每次换methodDeclaration的时候都应该重新设置lineno list
        this.lineno = lineno;
    }

    private boolean isSatisfied(Node n) {
        if (n instanceof BlockStmt) {
            return ((BlockStmt) n).getStatements().stream().anyMatch(this::isSatisfied);
        }
        return lineno.stream().anyMatch(l -> n.getBegin().get().line <= l && n.getEnd().get().line >= l);
    }

    @Override
    public void visit(DoStmt n, PathCondition arg) {
        Expression condition = n.getCondition();
        if (!isSatisfied(n.getBody())) {
            Expression unsatisfiedCondition = MutateHelper.getUnsatisfiedCondition(condition);
            arg.addCondition(unsatisfiedCondition.toString());
        } else {
            arg.addCondition(condition.toString());
        }
        condition.accept(variableVisitor, arg);
    }

    @Override
    public void visit(ExpressionStmt n, PathCondition arg) {
        n.getExpression().accept(this, arg);
    }

    @Override
    public void visit(VariableDeclarator n, PathCondition arg) {
        n.getName().accept(this, arg);
        n.getInitializer().ifPresent(l -> l.accept(this, arg));
    }

    @Override
    public void visit(AssignExpr n, PathCondition arg) {
        if (arg.getVariables().contains(n.getTarget().toString())) {
            n.getValue().accept(variableVisitor, arg);
            n.getTarget().ifTypeExpr(typeExpr -> {
                if (typeExpr.getType().isPrimitiveType()) {
                    arg.addDataFlow(n.toString());
                } else {
                    n.getValue().accept(this, arg);//如果是对象类型的则说明覆盖了其他的函数，那么下一条语句
                }
            });
        }
    }

    boolean lastIsMethodCall = false;

    @Override
    public void visit(MethodCallExpr n, PathCondition arg) {
        n.getParentNode().ifPresent(par -> {
            if (par instanceof AssignExpr) {
                return;
            }
            n.getArguments().forEach(a -> {
                a.ifTypeExpr(typeExpr -> {
                    if (!typeExpr.getType().isPrimitiveType()) {
                        if (arg.getVariables().contains(a.toString())) {
                            //todo
                        }
                    }
                });
            });
        });
//        n.getName().accept(this, arg);
//        n.getScope().ifPresent(l -> l.accept(this, arg));
//        n.getTypeArguments().ifPresent(l -> l.forEach(v -> v.accept(this, arg)));
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, PathCondition arg) {
        n.getArguments().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(ForEachStmt n, PathCondition arg) {
        Expression iterable = n.getIterable();
        VariableDeclarationExpr variable = n.getVariable();
        for (VariableDeclarator v: variable.getVariables()) {
            if (arg.getVariables().contains(v.getName().toString())) {
                iterable.accept(variableVisitor, arg);
            }
        }
    }

    @Override
    public void visit(ForStmt n, PathCondition arg) {
        Expression condition = n.getCompare().orElse(null);
        if (condition != null) {
            if (!isSatisfied(n.getBody())) {
                Expression unsatisfiedCondition = MutateHelper.getUnsatisfiedCondition(condition);
                arg.addCondition(unsatisfiedCondition.toString());
            } else {
                arg.addCondition(condition.toString());
            }
            condition.accept(variableVisitor, arg);
        }
//        n.getInitialization().forEach(p -> p.accept(this, arg));
//        n.getUpdate().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(IfStmt n, PathCondition arg) {
        Expression condition = n.getCondition();
        if (!isSatisfied(n.getThenStmt())) {
            Expression unsatisfiedCondition = MutateHelper.getUnsatisfiedCondition(condition);
            arg.addCondition(unsatisfiedCondition.toString());
        } else {
            arg.addCondition(condition.toString());
        }
        condition.accept(variableVisitor, arg);
    }

    @Override
    public void visit(ReturnStmt n, PathCondition arg) {
        //todo 判断上一条语句是否是函数调用赋值或者对象类型的函数调用参数，如果是的话，把返回值和赋值语句的target或者受影响的形参和实参对应起来
        n.getExpression().ifPresent(l -> l.accept(variableVisitor, arg));
    }

    @Override
    public void visit(SwitchStmt n, PathCondition arg) {
        String condition = n.getSelector().toString();
        NodeList<SwitchEntry> entries = n.getEntries();
        List<String> subCons = new ArrayList<>();
        for (int i = entries.size() - 1; i >= 0; i--) {
            SwitchEntry p = entries.get(i);
            List<String> labels = p.getLabels().stream().map(Node::toString).collect(Collectors.toList());
            if (isSatisfied(p)) {
                //condition = selector == label1 [|| selector == label2 || ...]
                labels.forEach(l -> subCons.add(condition + " == " + l));
            }
        }
        StringBuilder strOfIterable = FileUtils.getStrOfIterable(subCons, " || ");
        strOfIterable.replace(strOfIterable.lastIndexOf(" || "), strOfIterable.length(), "");
        arg.addCondition(strOfIterable.toString());
    }

    @Override
    public void visit(WhileStmt n, PathCondition arg) {
        Expression condition = n.getCondition();
        if (!isSatisfied(n.getBody())) {
            Expression unsatisfiedCondition = MutateHelper.getUnsatisfiedCondition(condition);
            arg.addCondition(unsatisfiedCondition.toString());
        } else {
            arg.addCondition(condition.toString());
        }
        condition.accept(variableVisitor, arg);
    }

    @Override
    public void visit(NameExpr n, PathCondition arg) {
    }
}
