package root.generation.transformation.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.entities.PathFlow;
import root.generation.helper.Helper;
import root.generation.transformation.MutateHelper;
import root.util.FileUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PathVisitor extends VoidVisitorAdapter<PathFlow> {
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
    public void visit(DoStmt n, PathFlow arg) {
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
    public void visit(ExpressionStmt n, PathFlow arg) {
        n.getExpression().accept(this, arg);
    }

    @Override
    public void visit(VariableDeclarationExpr n, PathFlow arg) {
        VariableDeclarationExpr cloneVD = new VariableDeclarationExpr();
        cloneVD.setModifiers(n.getModifiers());
        n.getVariables().forEach(p -> {
            p.accept(this, arg);
            if (arg.getVariables().contains(p.getName().toString())) {
                NodeList<VariableDeclarator> list = new NodeList<>();
                VariableDeclarator clone = p.clone();
                clone.setInitializer((Expression) null);
                list.add(clone);
                cloneVD.setVariables(list);
                arg.addDeclarator(cloneVD.toString());
            }
        });
    }

    @Override
    public void visit(VariableDeclarator n, PathFlow arg) {
        if (arg.getVariables().contains(n.getName().toString())) {
            arg.addDataFlow(n.toString());
            n.getInitializer().ifPresent(l -> {
                l.accept(variableVisitor, arg);
            });
        }
    }

    @Override
    public void visit(UnaryExpr n, PathFlow arg) {
        switch (n.getOperator()) {
            case POSTFIX_DECREMENT:
            case POSTFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case PREFIX_INCREMENT:
                if (arg.getVariables().contains(n.getExpression().toString())) {
                    arg.addDataFlow(n.toString());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(AssignExpr n, PathFlow arg) {
        if (arg.getVariables().contains(n.getTarget().toString())) {
            arg.addDataFlow(n.toString());
            n.getValue().accept(variableVisitor, arg);
            if (Helper.isReferenceType(n)) {
                n.getValue().accept(this, arg);
            }
        }
    }

    private void mappingReturn(PathFlow arg, String invocation, String receiver) {
        Set<String> mappings = arg.getMappingVar(invocation);
        if (mappings == null)
            return;
        HashSet<String> mappingVar = new HashSet<>(mappings);
        mappingVar.forEach(v -> {
            String[] split = v.split("#");
            if (split.length == 2 && "RETURN".equals(split[0])) {
                arg.addDataFlow(receiver + " = " + split[1]);
                arg.removeMappingVar(invocation, v);
            }
        });
    }
    private void mappingArgsAndPars(PathFlow arg, String invocation, String idx, String argName) {
        Set<String> mappings = arg.getMappingVar(invocation);
        if (mappings == null)
            return;
        HashSet<String> mappingVar = new HashSet<>(mappings);
        mappingVar.forEach(v -> {
            String[] split = v.split("#");
            if (split.length == 3 && "PAR".equals(split[0]) && idx.equals(split[1])) {
                arg.addDataFlow(split[2] + " = " + argName);
                arg.removeMappingVar(invocation, v);
            }
        });
    }

    @Override
    public void visit(MethodCallExpr n, PathFlow arg) {
        String invocation = n.getName().toString();
        n.getParentNode().ifPresent(par -> {
            //父节点是asg则建立函数返回值和target的关系
            if (par instanceof AssignExpr) {
                Expression target = ((AssignExpr) par).getTarget();
                mappingReturn(arg, invocation, target.toString());
            } else if (par instanceof VariableDeclarator) {
                SimpleName name = ((VariableDeclarator) par).getName();
                mappingReturn(arg, invocation, name.toString());
            } else if (par instanceof MethodCallExpr) {
                mappingReturn(arg, ((MethodCallExpr) par).getNameAsString(), n.toString());
            } else {
                mappingReturn(arg, invocation, n.toString());
            }
        });
        for (int i = 0; i < n.getArguments().size(); i++) {
            Expression a = n.getArguments().get(i);
            //建立函数形参和实参的关系
            mappingArgsAndPars(arg, invocation, String.valueOf(i), a.toString());//todo 数组类型的没法解析！
            a.accept(this, arg);
        }
        //增加scope进variable
        n.getScope().ifPresent(l -> l.accept(variableVisitor, arg));
//        n.getName().accept(this, arg);
//        n.getTypeArguments().ifPresent(l -> l.forEach(v -> v.accept(this, arg)));
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, PathFlow arg) {
        n.getArguments().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(ForEachStmt n, PathFlow arg) {
        Expression iterable = n.getIterable();
        VariableDeclarationExpr variable = n.getVariable();
        for (VariableDeclarator v: variable.getVariables()) {
            if (arg.getVariables().contains(v.getName().toString())) {
                iterable.accept(variableVisitor, arg);
            }
        }
    }

    @Override
    public void visit(ForStmt n, PathFlow arg) {
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
    public void visit(IfStmt n, PathFlow arg) {
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
    public void visit(ReturnStmt n, PathFlow arg) {
        //判断上一条语句是否是函数调用赋值或者对象类型的函数调用参数，如果是的话，把返回值和赋值语句的target或者受影响的形参和实参对应起来
        Optional<Expression> expression = n.getExpression();
        expression.ifPresent(l -> {
            MethodDeclaration declaration = getDeclarationName(n);
            if (declaration == null) {
                return;
            }
            n.getBegin().ifPresent(b -> {
                if (lineno.contains(b.line)) {
                    arg.addMappingVars(declaration.getNameAsString(),
                            "RETURN#" + expression.get());
                }
            });
            l.accept(this, arg);
        });
    }
    private MethodDeclaration getDeclarationName(Node n) {
        Optional<Node> parentNode = n.getParentNode();
        while (parentNode.isPresent()) {
            if (parentNode.get() instanceof MethodDeclaration) {
                return (MethodDeclaration) parentNode.get();
            }
            parentNode = parentNode.get().getParentNode();
        }
        return null;
    }

    @Override
    public void visit(SwitchStmt n, PathFlow arg) {
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
    public void visit(WhileStmt n, PathFlow arg) {
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
    public void visit(NameExpr n, PathFlow arg) {
//        if (arg.getVariables().contains(n.toString())) {
//            arg.addDataFlow(n.toString());
//        }
    }
}
