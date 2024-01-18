package root.analysis.groum;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static guru.nidi.graphviz.attribute.Attributes.*;
import static guru.nidi.graphviz.attribute.Label.Justification.LEFT;
import static guru.nidi.graphviz.attribute.Rank.RankDir.*;
import static guru.nidi.graphviz.model.Factory.*;

public class VisualizeTest {

    @Test
    public void test() throws IOException {
//        //immutable
//        Graph g = graph("example1").directed()
//                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
//                .nodeAttr().with(Font.name("arial"))
//                .linkAttr().with("class", "link-class")
//                .with(
//                        node("a").with(Color.RED).link(node("b")),
//                        node("b").link(
//                                to(node("c")).with(attr("weight", 5), Style.DASHED)
//                        )
//                );
//        Graphviz.fromGraph(g).height(100).render(Format.PNG).toFile(new File("example/ex1.png"));
//        //mutable
//        MutableGraph g1 = mutGraph("example1").setDirected(true).add(
//                mutNode("a").add(Color.RED).addLink(mutNode("b")));
//        Graphviz.fromGraph(g1).width(200).render(Format.PNG).toFile(new File("example/ex1m.png"));
//        //imperative
//        MutableGraph g = mutGraph("example1").setDirected(true).use((gr, ctx) -> {
//            mutNode("b");
//            nodeAttrs().add(Color.RED);
//            mutNode("a").addLink(mutNode("b"));
//        });
//        Graphviz.fromGraph(g).width(200).render(Format.PNG).toFile(new File("example/ex1i.png"));

//        //processor
//        Graph graph = graph().with(node("bad word").link("good word"));
//        Graphviz g = Graphviz.fromGraph(graph)
//                .preProcessor((source, options, processOptions) -> source.replace("bad word", "unicorn"))
//                .postProcessor((result, options, processOptions) ->
//                        result.mapString(svg ->
//                                SvgElementFinder.use(svg, finder -> {
//                                    finder.findNode("unicorn").setAttribute("class", "pink");
//                                })));
//        g.render(Format.PNG).toFile(new File("example/ex9.png"));

        //complex
        Node
                main = node("label").with(Label.html("<b>" + "&lt;label&gt;" + "</b><br/>" + "attrs")).with(Shape.RECTANGLE),
                init = node(Label.markdown("**_init_**")),
                execute = node("execute"),
                compare = node("compare").with(Shape.RECTANGLE, Style.FILLED, Color.hsv(.7, .3, 1.0)),
                mkString = node("mkString").with(Label.lines(LEFT, "make", "a", "multi-line")),
                printf = node("printf");

        Graph g = graph("example2").directed().with(
                    main.link(
                            to(node("parse").link(execute)).with(LinkAttr.weight(8)),
                            to(init).with(Style.DOTTED),
                            node("cleanup"),
                            to(printf).with(Style.BOLD, Label.of("100 times"), Color.RED)),
                        execute.link(mkString, printf, init, compare),
//                            to(compare).with(Color.RED)),
                init.link(mkString));

        Graphviz.fromGraph(g).width(900).render(Format.PNG).toFile(new File("example/ex2.png"));

//        //cluster
//        final Graph g2 = graph("ex1").directed().with(
//                graph().cluster()
//                        .nodeAttr().with(Style.FILLED, Color.WHITE)
//                        .graphAttr().with(Style.FILLED, Color.LIGHTGREY, Label.of("process #1"))
//                        .with(node("a0").link(node("a1").link(node("a2")))),
//                graph("x").cluster()
//                        .nodeAttr().with(Style.FILLED)
//                        .graphAttr().with(Color.BLUE, Label.of("process #2"))
//                        .with(node("b0").link(node("b1").link(node("b2")))),
//                node("start").with(Shape.M_DIAMOND).link("a0", "b0"),
//                node("a0").with(Style.FILLED, Color.RED.gradient(Color.BLUE)).link("b1"),
//                node("b1").link("a2"),
//                node("a2").link("end"),
//                node("b2").link("end"),
//                node("end").with(Shape.M_SQUARE)
//        );
//        Graphviz.fromGraph(g2).width(200).render(Format.PNG).toFile(new File("example/ex2.png"));

    }
}
