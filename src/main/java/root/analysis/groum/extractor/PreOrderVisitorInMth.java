package root.analysis.groum.extractor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.analysis.groum.entity.*;
import root.analysis.groum.vector.Feature;
import root.util.ConfigurationProperties;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PreOrderVisitorInMth extends VoidVisitorAdapter<List<IntraGroum>> {

    ExtractFromJavaParser extractFromJavaParser;
    AttributeVisitor attributeVisitor;
    GraphMerger graphMerger;

    public PreOrderVisitorInMth(boolean incrVector) {
        this.extractFromJavaParser = new ExtractFromJavaParser();
        this.attributeVisitor = new AttributeVisitor();
        this.graphMerger = new GraphMerger(incrVector);
    }

    public LinkedHashMap<Feature, Integer> getFeatures() {
        return graphMerger.exaser.getFeatureCounts();
    }

//    @Override
//    public void visit(NodeList n, List<IntraGroum> arg) {
//        for (Object node : n) {
//            if (!(node instanceof Statement)) {
//                super.visit(n, arg);
//            }
//
//            IntraGroum head = arg.isEmpty() ? null : arg.get(0);
//            arg.clear();
//
//            ((Node) node).accept(this, arg);
//            IntraGroum tmp = arg.isEmpty() ? null : arg.get(0);
//            head = MergeHelper.sequentialMerge(head, tmp);//每条语句顺连
//            arg.add(head);
//        }
//    }

    public void buildGraph(Node node, List<IntraGroum> arg, boolean isFinal) {
        node.accept(this, arg);
        IntraGroum groum = arg.isEmpty() ? null : arg.get(0);
        if (groum != null && isFinal) {
            graphMerger.buildlFinalGroum(groum);//添加数据依赖 ***
        }
    }

    @Override
    public void visit(ConstructorDeclaration n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //当前节点
        AbstractNode extract = extractFromJavaParser.extract(n);
        n.getParameters().forEach(p -> {
            InvolvedVar involvedVar = extractFromJavaParser.extractVar(p);
            extract.addAttribute(involvedVar);//把参数添加作为属性
        });
        IntraGroum head = new IntraGroum(extract);

        //函数body
        AtomicReference<IntraGroum> tail = new AtomicReference<>();
        n.getBody().accept(this, arg);
        tail.set(arg.get(0) == null ? null : arg.get(0));//不单独处理每条语句的写法

        IntraGroum merged = graphMerger.sequentialMerge(head, tail.get());//连接当前节点和函数body
        merged = graphMerger.sequentialMerge(head0, merged);
        arg.clear();
        arg.add(merged);
//        n.getModifiers().forEach(p -> p.accept(this, arg));
//        n.getName().accept(this, arg);
//        n.getParameters().forEach(p -> p.accept(this, arg));
//        n.getReceiverParameter().ifPresent(l -> l.accept(this, arg));
//        n.getThrownExceptions().forEach(p -> p.accept(this, arg));
//        n.getTypeParameters().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(MethodDeclaration n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //当前节点
        AbstractNode extract = extractFromJavaParser.extract(n);
        n.getParameters().forEach(p -> {
            InvolvedVar involvedVar = extractFromJavaParser.extractVar(p);
            extract.addAttribute(involvedVar);//把参数添加作为属性
        });
        IntraGroum head = new IntraGroum(extract);

        //函数body
        AtomicReference<IntraGroum> tail = new AtomicReference<>();
        n.getBody().ifPresent(l -> {
            l.accept(this, arg);
//            l.getStatements().forEach(p -> {
//                p.accept(this, arg);
//                IntraGroum tmp = arg.isEmpty() ? null : arg.get(0);
//                tail.set(MergeHelper.sequentialMerge(tail.get(), tmp));//每条语句顺连
//                arg.clear();
//            });
        });
        tail.set(arg.get(0) == null ? null : arg.get(0));//不单独处理每条语句的写法

        IntraGroum merged = graphMerger.sequentialMerge(head, tail.get());//连接当前节点和函数body
        merged = graphMerger.sequentialMerge(head0, merged);
        arg.clear();
        arg.add(merged);
//        n.getType().accept(this, arg);
//        n.getModifiers().forEach(p -> p.accept(this, arg));
//        n.getReceiverParameter().ifPresent(l -> l.accept(this, arg));//?
//        n.getThrownExceptions().forEach(p -> p.accept(this, arg));
//        n.getTypeParameters().forEach(p -> p.accept(this, arg));
//        n.getAnnotations().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(MethodCallExpr n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //函数调用本身
        AbstractNode extract = extractFromJavaParser.extract(n);
        IntraGroum tail = new IntraGroum(extract);

        //级联调用的前半部分
        arg.clear();
        Set<InvolvedVar> attrs = new HashSet<>();
        n.getScope().ifPresent(l -> {
            l.accept(attributeVisitor, attrs);
            l.accept(this, arg);
        });
        IntraGroum scope = arg.isEmpty() ? null : arg.get(0);
        head0 = graphMerger.sequentialMerge(head0, scope);//连接父节点和scope

        //函数参数部分
        arg.clear();
        AtomicReference<IntraGroum> head = new AtomicReference<>();
        n.getArguments().forEach(p -> {
            p.accept(this, arg);
            IntraGroum tmp = arg.isEmpty() ? null : arg.get(0);
            if (tmp == null) {
                p.accept(attributeVisitor, attrs);
            }
            head.set(graphMerger.parallelMerge(head.get(), tmp));//参数平行连接
            arg.clear();
        });
        extract.addAttributes(attrs);
        IntraGroum merged = graphMerger.sequentialMerge(head0, head.get());//连接scope和函数参数

        merged = graphMerger.sequentialMerge(merged, tail);//连接头和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ObjectCreationExpr n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //函数调用本身
        AbstractNode extract = extractFromJavaParser.extract(n);
        IntraGroum tail = new IntraGroum(extract);

        //级联调用的前半部分
        n.getScope().ifPresent(l -> {
            if (l instanceof NameExpr) {
                extract.addAttribute(extractFromJavaParser.extractVar((NameExpr) l));
            } else {
                l.accept(this, arg);
            }
        });
        IntraGroum before = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //函数参数部分
        AtomicReference<IntraGroum> head = new AtomicReference<>();
        n.getArguments().forEach(p -> {
            p.accept(this, arg);
            IntraGroum tmp = arg.isEmpty() ? null : arg.get(0);
            if (tmp == null) {
                Set<InvolvedVar> attrs = new HashSet<>();
                p.accept(attributeVisitor, attrs);
                extract.addAttributes(attrs);
            }
            head.set(graphMerger.parallelMerge(head.get(), tmp));//参数平行连接
            arg.clear();
        });
        //匿名body
        n.getAnonymousClassBody().ifPresent(l -> l.forEach(v -> v.accept(this, arg)));
        IntraGroum head1 = arg.isEmpty() ? null : arg.get(0);
        head.set(graphMerger.parallelMerge(head.get(), head1));//平行？连接参数和匿名body

        IntraGroum merged = graphMerger.sequentialMerge(head.get(), tail);//连接参数和函数调用节点

        merged = graphMerger.sequentialMerge(before, merged);//连接级联调用和当前节点
        merged = graphMerger.sequentialMerge(head0, merged);
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ArrayCreationExpr n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //层级
        AtomicReference<IntraGroum> head = new AtomicReference<>();
        n.getLevels().forEach(p -> {
            p.accept(this, arg);
            IntraGroum tmp = arg.isEmpty() ? null : arg.get(0);
            head.set(graphMerger.parallelMerge(head.get(), tmp));//参数平行连接
            arg.clear();
        });
        //初始化
        n.getInitializer().ifPresent(l -> l.accept(this, arg));
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);
        head.set(graphMerger.parallelMerge(head.get(), tail));//平行？连接参数和匿名body

        //函数调用本身
        AbstractNode extract = extractFromJavaParser.extract(n);
        tail = new IntraGroum(extract);
        IntraGroum merged = graphMerger.sequentialMerge(head.get(), tail);//连接参数和函数调用节点

        merged = graphMerger.sequentialMerge(head0, merged);//连接级联调用和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(VariableDeclarator n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //当前节点
        n.getInitializer().ifPresent(l -> l.accept(this, arg));
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail != null) {
            List<AbstractNode> sinkNodes = tail.getSinkNodes();
            assert sinkNodes.size() == 1 && sinkNodes.get(0) instanceof ActionNode;
            sinkNodes.get(0).addAttribute(extractFromJavaParser.extractVar(n));//涉及的变量
        }

        IntraGroum merged = graphMerger.sequentialMerge(head0, tail);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(IntersectionType n, List<IntraGroum> arg) {//type
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassExpr n, List<IntraGroum> arg) {//var
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldAccessExpr n, List<IntraGroum> arg) {
        n.getScope().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);
        AbstractNode extract = extractFromJavaParser.extract(n);
        IntraGroum tail = new IntraGroum(extract);
        IntraGroum merged = graphMerger.sequentialMerge(head, tail);
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(NameExpr n, List<IntraGroum> arg) {//？只把变量加到尾部节点里？
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail != null) {
            List<AbstractNode> sinkNodes = tail.getSinkNodes();
            assert sinkNodes.size() == 1 && sinkNodes.get(0) instanceof ActionNode;
            sinkNodes.get(0).addAttribute(extractFromJavaParser.extractVar(n));//涉及的变量
        }
    }

    @Override
    public void visit(AssignExpr n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //赋值右侧为输入或者没有
        n.getValue().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);

        //当前节点
        arg.clear();
        n.getTarget().accept(this, arg);
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);
//        if (tail == null) {
//            AbstractNode extract = extractFromJavaParser.extract(n);
//            tail = new IntraGroum(extract);
//        }
//        assert !tail.getNodes().isEmpty();
        if (tail != null) {
            List<AbstractNode> sinkNodes = tail.getSinkNodes();
            assert sinkNodes.size() == 1 && sinkNodes.get(0) instanceof ActionNode;
            Set<InvolvedVar> attrs = new HashSet<>();
            n.accept(attributeVisitor, attrs);
            sinkNodes.get(0).addAttributes(attrs);//涉及的变量
        }

        tail = graphMerger.sequentialMerge(head, tail);//连接左右
        IntraGroum merged = graphMerger.sequentialMerge(head0, tail);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }


    @Override
    public void visit(UnaryExpr n, List<IntraGroum> arg) {
        switch (n.getOperator()) {
            case POSTFIX_DECREMENT:
            case POSTFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case PREFIX_INCREMENT:
                //父节点
                IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
                arg.clear();

                //赋值为变量或者没有
                n.getExpression().accept(this, arg);
                IntraGroum tail = arg.isEmpty() ? null : arg.get(0);
                if (tail == null) {
                    AbstractNode extract = extractFromJavaParser.extract(n);
                    Set<InvolvedVar> attrs = new HashSet<>();
                    n.accept(attributeVisitor, attrs);
                    extract.addAttributes(attrs);//涉及的变量
                    tail = new IntraGroum(extract);
                }

                IntraGroum merged = graphMerger.sequentialMerge(head0, tail);//连接父节点和当前节点
                arg.clear();
                arg.add(merged);
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(BinaryExpr n, List<IntraGroum> arg) {
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        n.getLeft().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);
        Set<InvolvedVar> attrs = new HashSet<>();
        if (head == null) {
            n.getLeft().accept(attributeVisitor, attrs);
        }
        arg.clear();

        n.getRight().accept(this, arg);
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail == null) {
            n.getRight().accept(attributeVisitor, attrs);
        }
        List<AbstractNode> nodes = head != null ? head.getNodes() : tail != null ? tail.getNodes() : new ArrayList<>();
        for (AbstractNode node : nodes) {
            node.addAttributes(attrs);//涉及的变量
        }
        IntraGroum merged = graphMerger.parallelMerge(head, tail);

        merged = graphMerger.sequentialMerge(head0, merged);
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(InstanceOfExpr n, List<IntraGroum> arg) {
        super.visit(n, arg);///////
    }

    @Override
    public void visit(BreakStmt n, List<IntraGroum> arg) {
        super.visit(n, arg);//todo？？？
    }

    @Override
    public void visit(ContinueStmt n, List<IntraGroum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(CastExpr n, List<IntraGroum> arg) {
        n.getExpression().accept(this, arg);
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //cast应该有一个顺连结构？
        IntraGroum tail = new IntraGroum(extractFromJavaParser.extract(n));
        IntraGroum merged = graphMerger.sequentialMerge(head0, tail);
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(TryStmt n, List<IntraGroum> arg) {
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        IntraGroum head = null;
        //java8会有resource
        String complianceLevel = ConfigurationProperties.getProperty("complianceLevel");
        if (complianceLevel.contains("8")) {
            n.getResources().accept(this, arg);
            head = arg.isEmpty() ? null : arg.get(0);
            arg.clear();
        }

        n.getTryBlock().accept(this, arg);
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);
        head = graphMerger.sequentialMerge(head, tail);//try的resource到try-block

        arg.clear();
        n.getCatchClauses().accept(this, arg);
        tail = arg.isEmpty() ? null : arg.get(0);
        head = graphMerger.parallelMerge(head, tail);//try和catch为平行?

        arg.clear();
        n.getFinallyBlock().ifPresent(blockStmt -> {
            blockStmt.accept(this, arg);
        });
        tail = arg.isEmpty() ? null : arg.get(0);
        tail = graphMerger.sequentialMerge(head, tail);//连接try和finally

        IntraGroum merged = graphMerger.sequentialMerge(head0, tail);
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ConditionalExpr n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.IF);

        //if条件部分
        n.getCondition().accept(this, arg);
        IntraGroum head1 = arg.isEmpty() ? null : arg.get(0);
        if (head1 == null) {
            Set<InvolvedVar> attrs = new HashSet<>();
            n.getCondition().accept(attributeVisitor, attrs);
            controlNode.addAttributes(attrs);//涉及的变量
        } else {
            List<AbstractNode> nodes = head1.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head1 = graphMerger.sequentialMerge(head1, new IntraGroum(controlNode));//连接条件和控制节点

        arg.clear();
        n.getThenExpr().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);//then节点
        List<AbstractNode> nodes = head == null ? null : head.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        arg.clear();
        n.getElseExpr().accept(this, arg);
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);//else节点
        nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        tail = graphMerger.parallelMerge(head, tail);//连接then和else
        IntraGroum merged = graphMerger.sequentialMerge(head1, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = graphMerger.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(IfStmt n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.IF);

        //if条件部分
        n.getCondition().accept(this, arg);
        IntraGroum head1 = arg.isEmpty() ? null : arg.get(0);
        if (head1 == null) {
            Set<InvolvedVar> attrs = new HashSet<>();
            n.getCondition().accept(attributeVisitor, attrs);
            controlNode.addAttributes(attrs);//涉及的变量
        } else {
            List<AbstractNode> nodes = head1.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head1 = graphMerger.sequentialMerge(head1, new IntraGroum(controlNode));//连接条件和控制节点

        arg.clear();
        n.getThenStmt().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);//then节点
        List<AbstractNode> nodes = head == null ? null : head.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        arg.clear();
        n.getElseStmt().ifPresent(l -> l.accept(this, arg));
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);//else节点
        nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        tail = graphMerger.parallelMerge(head, tail);//连接then和else
        IntraGroum merged = graphMerger.sequentialMerge(head1, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = graphMerger.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(SwitchStmt n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.IF);

        //当前节点
        n.getSelector().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);
        if (head == null) {
            Set<InvolvedVar> attrs = new HashSet<>();
            n.getSelector().accept(attributeVisitor, attrs);
            controlNode.addAttributes(attrs);//涉及的变量
        } else {
            List<AbstractNode> nodes = head.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head = graphMerger.sequentialMerge(head, new IntraGroum(controlNode));//连接条件和控制节点

        //body部分
        AtomicReference<IntraGroum> tail = new AtomicReference<>();
        n.getEntries().forEach(p -> {
            arg.clear();
            p.accept(this, arg);
            IntraGroum tmp = arg.isEmpty() ? null : arg.get(0);
            tail.set(graphMerger.parallelMerge(tail.get(), tmp));//每个entry平行连接
        });
        tail.set(graphMerger.sequentialMerge(head, tail.get()));

        IntraGroum merged = graphMerger.sequentialMerge(head0, tail.get());//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(DoStmt n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.WHILE);

        n.getBody().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);//body节点
        List<AbstractNode> nodes = head == null ? null : head.getNodes();
        controlNode.addScope(nodes);//添加范围节点
        head = graphMerger.sequentialMerge(head, new IntraGroum(controlNode));//连接条件和控制节点

        //do条件部分
        arg.clear();
        n.getCondition().accept(this, arg);
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail == null) {
            Set<InvolvedVar> attrs = new HashSet<>();
            n.getCondition().accept(attributeVisitor, attrs);
            controlNode.addAttributes(attrs);//涉及的变量
        } else {
            nodes = tail.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }

        IntraGroum merged = graphMerger.sequentialMerge(head, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = graphMerger.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(WhileStmt n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.WHILE);

        //条件部分
        n.getCondition().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);
        if (head == null) {
            Set<InvolvedVar> attrs = new HashSet<>();
            n.getCondition().accept(attributeVisitor, attrs);
            controlNode.addAttributes(attrs);//涉及的变量
        } else {
            List<AbstractNode> nodes = head.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head = graphMerger.sequentialMerge(head, new IntraGroum(controlNode));//连接条件和控制节点

        arg.clear();
        n.getBody().accept(this, arg);
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);//body节点
        List<AbstractNode> nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        IntraGroum merged = graphMerger.sequentialMerge(head, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = graphMerger.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ForEachStmt n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.FOR);

        //迭代部分
        n.getIterable().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);
        if (head == null) {
            Set<InvolvedVar> attrs = new HashSet<>();
            n.getIterable().accept(attributeVisitor, attrs);
            controlNode.addAttributes(attrs);//涉及的变量
        } else {
            List<AbstractNode> nodes = head.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head = graphMerger.sequentialMerge(head, new IntraGroum(controlNode));//连接迭代变量和控制节点

        arg.clear();
        n.getBody().accept(this, arg);
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);//body节点
        List<AbstractNode> nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点
        if (nodes != null) {
            for (AbstractNode node :nodes) {
                node.addAttribute(extractFromJavaParser.extractVar(n.getVariableDeclarator()));
            }
        }

        IntraGroum merged = graphMerger.sequentialMerge(head, tail);//连接条件和执行部分
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        merged = graphMerger.sequentialMerge(head0, merged);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(ForStmt n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();
        //控制节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.IF);

        //for变量初始化
        n.getInitialization().forEach(p -> p.accept(this, arg));
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);
        List<AbstractNode> nodes = head == null ? null : head.getNodes();
        controlNode.addScope(nodes);//添加范围节点

        //条件部分
        arg.clear();
        n.getCompare().ifPresent(l -> l.accept(this, arg));
        IntraGroum tail = arg.isEmpty() ? null : arg.get(0);
        if (tail == null) {
            Set<InvolvedVar> attrs = new HashSet<>();
            n.getCompare().ifPresent(c -> c.accept(attributeVisitor, attrs));
            controlNode.addAttributes(attrs);//涉及的变量
        } else {
            nodes = tail.getNodes();
            controlNode.addScope(nodes);//添加范围节点
        }
        head = graphMerger.sequentialMerge(head, tail);//连接变量和条件

        head = graphMerger.sequentialMerge(head, new IntraGroum(controlNode));//连接条件和控制节点

        //body部分
        arg.clear();
        n.getBody().accept(this, arg);
        tail = arg.isEmpty() ? null : arg.get(0);
        nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点
        head = graphMerger.sequentialMerge(head, tail);//连接条件和body

        //update部分
        arg.clear();
        n.getUpdate().forEach(p -> p.accept(this, arg));
        tail = arg.isEmpty() ? null : arg.get(0);//else节点
        nodes = tail == null ? null : tail.getNodes();
        controlNode.addScope(nodes);//添加范围节点
        tail = graphMerger.sequentialMerge(head, tail);//连接body和update
        for (AbstractNode node : controlNode.getScope()) {
            controlNode.addAttributes(node.getAttributes());
        }

        IntraGroum merged = graphMerger.sequentialMerge(head0, tail);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }


    @Override
    public void visit(ReturnStmt n, List<IntraGroum> arg) {
        //父节点
        IntraGroum head0 = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //当前节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.RETURN);
        Set<InvolvedVar> attrs = new HashSet<>();
        n.accept(attributeVisitor, attrs);
        controlNode.addAttributes(attrs);//涉及的变量
        IntraGroum tail = new IntraGroum(controlNode);

        //expression节点
        n.getExpression().ifPresent(l -> {
            l.accept(this, arg);
        });
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);
        if (head != null) {
            controlNode.addScope(head.getNodes());
        }

        tail = graphMerger.sequentialMerge(head, tail);//连接expression和return

        IntraGroum merged = graphMerger.sequentialMerge(head0, tail);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }


    @Override
    public void visit(ThrowStmt n, List<IntraGroum> arg) {
        n.getExpression().accept(this, arg);
        IntraGroum head = arg.isEmpty() ? null : arg.get(0);
        arg.clear();

        //当前节点
        ControlNode controlNode = new ControlNode(n, ControlNode.Type.Throw);
        Set<InvolvedVar> attrs = new HashSet<>();
        n.accept(attributeVisitor, attrs);
        controlNode.addAttributes(attrs);//涉及的变量
        IntraGroum tail = new IntraGroum(controlNode);
        if (head != null) {
            controlNode.addScope(head.getNodes());
        }

        IntraGroum merged = graphMerger.sequentialMerge(head, tail);//连接父节点和当前节点
        arg.clear();
        arg.add(merged);
    }

    @Override
    public void visit(LambdaExpr n, List<IntraGroum> arg) {
        super.visit(n, arg);//?
    }

    @Override
    public void visit(MethodReferenceExpr n, List<IntraGroum> arg) {
        super.visit(n, arg);//?
    }
}
