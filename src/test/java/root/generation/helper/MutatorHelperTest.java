package root.generation.helper;

import org.junit.jupiter.api.Test;
import root.AbstractMain;
import root.util.CommandSummary;

import java.io.File;

class MutatorHelperTest {

    @Test
    void initialize() {
        CommandSummary cs = new CommandSummary();
        cs.append("-location", (new File("./")).getAbsolutePath());
        cs.append("-srcJavaDir", "src/main/java");
        cs.append("-srcTestDir", "src/test/java");
        cs.append("-binJavaDir", "build/classes/java/main");
        cs.append("-binTestDir", "build/classes/java/test");
        AbstractMain main = new AbstractMain();
        main.initialize(cs.flat());
    }
}