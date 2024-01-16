package root.analysis.groum.extractor;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.analysis.groum.entity.*;
import root.util.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PreOrderVisitorInMth extends VoidVisitorAdapter<List<Groum>> {

    ExtractFromJavaParser extractFromJavaParser = new ExtractFromJavaParser();
    AttributeVisitor attributeVisitor = new AttributeVisitor();

    @Override
    public void visit(MethodCallExpr n, List<Groum> arg) {
        //函数调用本身
        AbstractNode extract = extractFromJavaParser.extract(n);
        Groum tail = new Groum(extract);

        //级联调用的前半部分
        n.getScope().ifPresent(l -> {
            if (l instanceof NameExpr) {
                InvolvedVar var = extractFromJavaParser.extractVar((NameExpr) l);
                extract.addAttribute(var);
            } else {
                l.accept(this, arg);
            }
        });
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //函数参数部分
        AtomicReference<Groum> head = new AtomicReference<>();
        n.getArguments().forEach(p -> {
            p.accept(this, arg);
            Groum tmp = arg.isEmpty() ? null : arg.get(0);
            if (tmp == null) {
                List<NameExpr> attrs = new ArrayList<>();
                p.accept(attributeVisitor, attrs);
                for (NameExpr attr :attrs) {
                    extract.addAttribute(extractFromJavaParser.extractVar(attr));
                }
            }
            head.set(MergeHelper.parallelMerge(head.get(), tmp));//参数平行连接
            arg.clear();
        });
        Groum merged = MergeHelper.sequentialMerge(head.get(), tail);//连接参数和函数调用节点

        merged = MergeHelper.sequentialMerge(head0, merged);//连接级联调用和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ObjectCreationExpr n, List<Groum> arg) {
        //函数调用本身
        AbstractNode extract = extractFromJavaParser.extract(n);
        Groum tail = new Groum(extract);

        //级联调用的前半部分
        n.getScope().ifPresent(l -> {
            if (l instanceof NameExpr) {
                extract.addAttribute(extractFromJavaParser.extractVar((NameExpr) l));
            } else {
                l.accept(this, arg);
            }
        });
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //函数参数部分
        AtomicReference<Groum> head = new AtomicReference<>();
        n.getArguments().forEach(p -> {
            p.accept(this, arg);
            Groum tmp = arg.isEmpty() ? null : arg.get(0);
            if (tmp == null) {
                List<NameExpr> attrs = new ArrayList<>();
                p.accept(attributeVisitor, attrs);
                for (NameExpr attr :attrs) {
                    extract.addAttribute(extractFromJavaParser.extractVar(attr));
                }
            }
            head.set(MergeHelper.parallelMerge(head.get(), tmp));//参数平行连接
            arg.clear();
        });
        //匿名body
        n.getAnonymousClassBody().ifPresent(l -> l.forEach(v -> v.accept(this, arg)));
        Groum head1 = arg.isEmpty() ? null : arg.get(0);
        head.set(MergeHelper.parallelMerge(head.get(), head1));//平行？连接参数和匿名body

        Groum merged = MergeHelper.sequentialMerge(head.get(), tail);//连接参数和函数调用节点

        merged = MergeHelper.sequentialMerge(head0, merged);//连接级联调用和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ArrayCreationExpr n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //层级
        AtomicReference<Groum> head = new AtomicReference<>();
        n.getLevels().forEach(p -> {
            p.accept(this, arg);
            Groum tmp = arg.isEmpty() ? null : arg.get(0);
            head.set(MergeHelper.parallelMerge(head.get(), tmp));//参数平行连接
            arg.clear();
        });
        //初始化
        n.getInitializer().ifPresent(l -> l.accept(this, arg));
        Groum tail = arg.isEmpty() ? null : arg.get(0);
        head.set(MergeHelper.parallelMerge(head.get(), tail));//平行？连接参数和匿名body

        //函数调用本身
        AbstractNode extract = extractFromJavaParser.extract(n);
        tail = new Groum(extract);
        Groum merged = MergeHelper.sequentialMerge(head.get(), tail);//连接参数和函数调用节点

        merged = MergeHelper.sequentialMerge(head0, merged);//连接级联调用和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(VariableDeclarator n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //当前节点
        n.getInitializer().ifPresent(l -> l.accept(this, arg));
        Groum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail != null) {
            List<AbstractNode> sinkNodes = tail.getSinkNodes();
            assert sinkNodes.size() == 1 && sinkNodes.get(0) instanceof ActionNode;
            sinkNodes.get(0).addAttribute(extractFromJavaParser.extractVar(n));//涉及的变量
        }

        Groum merged = MergeHelper.sequentialMerge(head0, tail);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(IntersectionType n, List<Groum> arg) {//type
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassExpr n, List<Groum> arg) {//var
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldAccessExpr n, List<Groum> arg) {
        n.getScope().accept(this, arg);
        Groum head = arg.isEmpty() ? null : arg.get(0);
        AbstractNode extract = extractFromJavaParser.extract(n);
        Groum tail = new Groum(extract);
        Groum merged = MergeHelper.sequentialMerge(head, tail);
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(NameExpr n, List<Groum> arg) {//？只把变量加到尾部节点里？
        Groum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail != null) {
            List<AbstractNode> sinkNodes = tail.getSinkNodes();
            assert sinkNodes.size() == 1 && sinkNodes.get(0) instanceof ActionNode;
            sinkNodes.get(0).addAttribute(extractFromJavaParser.extractVar(n));//涉及的变量
        }
    }

    @Override
    public void visit(AssignExpr n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //赋值右侧为输入或者没有
        n.getValue().accept(this, arg);
        Groum head = arg.isEmpty() ? null : arg.get(0);

        //当前节点
        arg.clear();
        n.getTarget().accept(this, arg);
        Groum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail == null) {
            AbstractNode extract = extractFromJavaParser.extract(n);
            tail = new Groum(extract);
        }
        assert !tail.getNodes().isEmpty();
        List<AbstractNode> sinkNodes = tail.getSinkNodes();
        assert sinkNodes.size() == 1 && sinkNodes.get(0) instanceof ActionNode;
        List<NameExpr> attrs = new ArrayList<>();
        n.accept(attributeVisitor, attrs);
        for (NameExpr attr :attrs) {
            sinkNodes.get(0).addAttribute(extractFromJavaParser.extractVar(attr));//涉及的变量
        }

        tail = MergeHelper.sequentialMerge(head, tail);//连接左右
        Groum merged = MergeHelper.sequentialMerge(head0, tail);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }


    @Override
    public void visit(UnaryExpr n, List<Groum> arg) {
        switch (n.getOperator()) {
            case POSTFIX_DECREMENT:
            case POSTFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case PREFIX_INCREMENT:
                //父节点
                Groum head0 = arg.isEmpty() ? null : arg.get(0);
                arg.clear();

                //赋值为变量或者没有
                n.getExpression().accept(this, arg);
                Groum tail = arg.isEmpty() ? null : arg.get(0);
                if (tail == null) {
                    AbstractNode extract = extractFromJavaParser.extract(n);
                    List<NameExpr> attrs = new ArrayList<>();
                    n.accept(attributeVisitor, attrs);
                    for (NameExpr attr :attrs) {
                        extract.addAttribute(extractFromJavaParser.extractVar(attr));//涉及的变量
                    }
                    tail = new Groum(extract);
                }

                Groum merged = MergeHelper.sequentialMerge(head0, tail);//连接父节点和当前节点
                arg.clear();
                arg.add(merged);
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(BinaryExpr n, List<Groum> arg) {
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        n.getLeft().accept(this, arg);
        Groum head = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        n.getRight().accept(this, arg);
        Groum tail = arg.isEmpty() ? null : arg.get(0);
        Groum merged = MergeHelper.parallelMerge(head, tail);

        merged = MergeHelper.sequentialMerge(head0, merged);
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(InstanceOfExpr n, List<Groum> arg) {
        super.visit(n, arg);///////
    }

    @Override
    public void visit(BreakStmt n, List<Groum> arg) {
        super.visit(n, arg);//todo？？？
    }

    @Override
    public void visit(ContinueStmt n, List<Groum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(CastExpr n, List<Groum> arg) {
        n.getExpression().accept(this, arg);
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //cast应该有一个顺连结构？
        Groum tail = new Groum(extractFromJavaParser.extract(n));
        Groum merged = MergeHelper.sequentialMerge(head0, tail);
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(TryStmt n, List<Groum> arg) {
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        Groum head = null;
        //java8会有resource
        String complianceLevel = ConfigurationProperties.getProperty("complianceLevel");
        if (complianceLevel.contains("8")) {
            n.getResources().accept(this, arg);
            head = arg.isEmpty() ? null : arg.get(0);
            arg.clear();
        }

        n.getTryBlock().accept(this, arg);
        Groum tail = arg.isEmpty() ? null : arg.get(0);
        head = MergeHelper.sequentialMerge(head, tail);//try的resource到try-block

        arg.clear();
        n.getCatchClauses().accept(this, arg);
        tail = arg.isEmpty() ? null : arg.get(0);
        head = MergeHelper.parallelMerge(head, tail);//try和catch为平行?

        arg.clear();
        n.getFinallyBlock().ifPresent(blockStmt -> {
            blockStmt.accept(this, arg);
        });
        tail = arg.isEmpty() ? null : arg.get(0);
        tail = MergeHelper.sequentialMerge(head, tail);//连接try和finally

        Groum merged = MergeHelper.sequentialMerge(head0, tail);
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ConditionalExpr n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.IF);

        //if条件部分
        n.getCondition().accept(this, arg);
        Groum head1 = arg.isEmpty() ? null : arg.get(0);
        if (head1 == null) {
            List<NameExpr> attrs = new ArrayList<>();
            n.getCondition().accept(attributeVisitor, attrs);
            for (NameExpr attr :attrs) {
                controlNode.addAttribute(extractFromJavaParser.extractVar(attr));
            }
        } else {
            List<AbstractNode> nodes = head1.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head1 = MergeHelper.sequentialMerge(head1, new Groum(controlNode));//连接条件和控制节点

        arg.clear();
        n.getThenExpr().accept(this, arg);
        Groum head = arg.isEmpty() ? null : arg.get(0);//then节点
        List<AbstractNode> nodes = head == null ? null : head.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        arg.clear();
        n.getElseExpr().accept(this, arg);
        Groum tail = arg.isEmpty() ? null : arg.get(0);//else节点
        nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        tail = MergeHelper.parallelMerge(head, tail);//连接then和else
        Groum merged = MergeHelper.sequentialMerge(head1, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = MergeHelper.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(IfStmt n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.IF);

        //if条件部分
        n.getCondition().accept(this, arg);
        Groum head1 = arg.isEmpty() ? null : arg.get(0);
        if (head1 == null) {
            List<NameExpr> attrs = new ArrayList<>();
            n.getCondition().accept(attributeVisitor, attrs);
            for (NameExpr attr :attrs) {
                controlNode.addAttribute(extractFromJavaParser.extractVar(attr));
            }
        } else {
            List<AbstractNode> nodes = head1.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head1 = MergeHelper.sequentialMerge(head1, new Groum(controlNode));//连接条件和控制节点

        arg.clear();
        n.getThenStmt().accept(this, arg);
        Groum head = arg.isEmpty() ? null : arg.get(0);//then节点
        List<AbstractNode> nodes = head == null ? null : head.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        arg.clear();
        n.getElseStmt().ifPresent(l -> l.accept(this, arg));
        Groum tail = arg.isEmpty() ? null : arg.get(0);//else节点
        nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        tail = MergeHelper.parallelMerge(head, tail);//连接then和else
        Groum merged = MergeHelper.sequentialMerge(head1, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = MergeHelper.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(DoStmt n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.WHILE);

        n.getBody().accept(this, arg);
        Groum head = arg.isEmpty() ? null : arg.get(0);//body节点
        List<AbstractNode> nodes = head == null ? null : head.getNodes();
        controlNode.addScope(nodes);//添加范围节点
        head = MergeHelper.sequentialMerge(head, new Groum(controlNode));//连接条件和控制节点

        //do条件部分
        arg.clear();
        n.getCondition().accept(this, arg);
        Groum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail == null) {
            List<NameExpr> attrs = new ArrayList<>();
            n.getCondition().accept(attributeVisitor, attrs);
            for (NameExpr attr :attrs) {
                controlNode.addAttribute(extractFromJavaParser.extractVar(attr));
            }
        } else {
            nodes = tail.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }

        Groum merged = MergeHelper.sequentialMerge(head, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = MergeHelper.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(WhileStmt n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.WHILE);

        //条件部分
        n.getCondition().accept(this, arg);
        Groum head = arg.isEmpty() ? null : arg.get(0);
        if (head == null) {
            List<NameExpr> attrs = new ArrayList<>();
            n.getCondition().accept(attributeVisitor, attrs);
            for (NameExpr attr :attrs) {
                controlNode.addAttribute(extractFromJavaParser.extractVar(attr));
            }
        } else {
            List<AbstractNode> nodes = head.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head = MergeHelper.sequentialMerge(head, new Groum(controlNode));//连接条件和控制节点

        arg.clear();
        n.getBody().accept(this, arg);
        Groum tail = arg.isEmpty() ? null : arg.get(0);//body节点
        List<AbstractNode> nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        Groum merged = MergeHelper.sequentialMerge(head, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = MergeHelper.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ForEachStmt n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.FOR);

        //迭代部分
        n.getIterable().accept(this, arg);
        Groum head = arg.isEmpty() ? null : arg.get(0);
        if (head == null) {
            List<NameExpr> attrs = new ArrayList<>();
            n.getIterable().accept(attributeVisitor, attrs);
            for (NameExpr attr :attrs) {
                controlNode.addAttribute(extractFromJavaParser.extractVar(attr));
            }
        } else {
            List<AbstractNode> nodes = head.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head = MergeHelper.sequentialMerge(head, new Groum(controlNode));//连接迭代变量和控制节点

        arg.clear();
        n.getBody().accept(this, arg);
        Groum tail = arg.isEmpty() ? null : arg.get(0);//body节点
        List<AbstractNode> nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点
        if (nodes != null) {
            for (AbstractNode node :nodes) {
                node.addAttribute(extractFromJavaParser.extractVar(n.getVariableDeclarator()));
            }
        }

        Groum merged = MergeHelper.sequentialMerge(head, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = MergeHelper.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ForStmt n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.IF);

        //for变量初始化
        n.getInitialization().forEach(p -> p.accept(this, arg));
        Groum head = arg.isEmpty() ? null : arg.get(0);
        List<AbstractNode> nodes = head == null ? null : head.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        //条件部分
        arg.clear();
        n.getCompare().ifPresent(l -> l.accept(this, arg));
        Groum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail == null) {
            List<NameExpr> attrs = new ArrayList<>();
            n.getCompare().ifPresent(c -> c.accept(attributeVisitor, attrs));
            for (NameExpr attr :attrs) {
                controlNode.addAttribute(extractFromJavaParser.extractVar(attr));
            }
        } else {
            nodes = tail.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head = MergeHelper.sequentialMerge(head, tail);//连接变量和条件

        head = MergeHelper.sequentialMerge(head, new Groum(controlNode));//连接条件和控制节点

        //body部分
        arg.clear();
        n.getBody().accept(this, arg);
        tail = arg.isEmpty() ? null : arg.get(0);
        nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点
        head = MergeHelper.sequentialMerge(head, tail);//连接条件和body

        //update部分
        arg.clear();
        n.getUpdate().forEach(p -> p.accept(this, arg));
        tail = arg.isEmpty() ? null : arg.get(0);//else节点
        nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点
        tail = MergeHelper.sequentialMerge(head, tail);//连接body和update
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        Groum merged = MergeHelper.sequentialMerge(head0, tail);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }


    @Override
    public void visit(ReturnStmt n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //当前节点
        n.getExpression().ifPresent(l -> {
            l.accept(this, arg);
        });
        Groum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail == null) {
            AbstractNode extract = extractFromJavaParser.extract(n);
            List<NameExpr> attrs = new ArrayList<>();
            n.accept(attributeVisitor, attrs);
            for (NameExpr attr :attrs) {
                extract.addAttribute(extractFromJavaParser.extractVar(attr));
            }
            tail = new Groum(extract);
        }

        Groum merged = MergeHelper.sequentialMerge(head0, tail);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(SwitchStmt n, List<Groum> arg) {
        //父节点
        Groum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.IF);

        //当前节点
        n.getSelector().accept(this, arg);
        Groum head = arg.isEmpty() ? null : arg.get(0);
        if (head == null) {
            List<NameExpr> attrs = new ArrayList<>();
            n.getSelector().accept(attributeVisitor, attrs);
            for (NameExpr attr :attrs) {
                controlNode.addAttribute(extractFromJavaParser.extractVar(attr));
            }
        } else {
            List<AbstractNode> nodes = head.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head = MergeHelper.sequentialMerge(head, new Groum(controlNode));//连接条件和控制节点

        //body部分
        AtomicReference<Groum> tail = new AtomicReference<>();
        n.getEntries().forEach(p -> {
            arg.clear();
            p.accept(this, arg);
            Groum tmp = arg.isEmpty() ? null : arg.get(0);
            tail.set(MergeHelper.parallelMerge(tail.get(), tmp));//每个entry平行连接
        });
        tail.set(MergeHelper.sequentialMerge(head, tail.get()));

        Groum merged = MergeHelper.sequentialMerge(head0, tail.get());//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ThrowStmt n, List<Groum> arg) {
        super.visit(n, arg);//?
    }

    @Override
    public void visit(LambdaExpr n, List<Groum> arg) {
        super.visit(n, arg);//?
    }

    @Override
    public void visit(MethodReferenceExpr n, List<Groum> arg) {
        super.visit(n, arg);//?
    }
}
