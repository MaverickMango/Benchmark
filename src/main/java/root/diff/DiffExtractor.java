package root.diff;

import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
//import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
//import gumtree.spoon.AstComparator;
//import gumtree.spoon.diff.Diff;
//import gumtree.spoon.diff.operations.Operation;
//import spoon.reflect.cu.SourcePosition;
//import spoon.reflect.declaration.CtElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DiffExtractor {

    private static final Logger logger = LoggerFactory.getLogger(DiffExtractor.class);

    public void diff(String srcPath, String dstPath) {
        try {
//            AstComparator astComparator = new AstComparator();
//            Diff compare = astComparator.compare(new File(srcPath), new File(dstPath));
//            CtElement element = compare.changedNode();
//            CtElement element1 = compare.commonAncestor();
//            for (Operation operation: compare.getRootOperations()) {
//                CtElement srcNode = operation.getSrcNode();
//                int line = srcNode.getPosition().getLine();
//            }
        } catch (Exception e) {
            logger.error("Error occurred when getting diff from gumtree-spoon\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void diffFromGumtree(String srcPath, String dstPath) {
        try {
//            JdtTreeGenerator jdtTreeGenerator = new JdtTreeGenerator();
//            Tree srcTree = jdtTreeGenerator.generateFrom().file(srcPath).getRoot(); // instantiates and applies the JDT generator
//            Tree dstTree = jdtTreeGenerator.generateFrom().file(dstPath).getRoot();
//            EditScript actions = compareEditScripts(srcTree, dstTree);
//            List<Action> l = actions.asList();
//            for (Action action :l) {
//                Tree node = action.getNode();
//                Iterator<Map.Entry<String, Object>> metadata = node.getMetadata();
//            }
        } catch (Exception e) {
            logger.error("Error occurred when getting diff from gumtree");
        }
    }

    private EditScript compareEditScripts(Tree srcTree, Tree dstTree) {
        Matcher defaultMatcher = Matchers.getInstance().getMatcher(); // retrieves the default matcher
        MappingStore mappings = defaultMatcher.match(srcTree, dstTree); // computes the mappings between the trees
        EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator(); // instantiates the simplified Chawathe script generator
        EditScript actions = editScriptGenerator.computeActions(mappings); // computes the edit script
        return actions;
    }
}
