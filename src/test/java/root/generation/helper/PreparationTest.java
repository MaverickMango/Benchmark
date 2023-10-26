package root.generation.helper;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import root.AbstractMain;
import root.util.CommandSummary;

import java.io.File;

public class PreparationTest {
    static CommandSummary cs = new CommandSummary();
    static {
        cs.append("-location", "./");
        cs.append("-srcJavaDir", "src/main/java");
        cs.append("-srcTestDir", "src/test/java");
        cs.append("-binJavaDir", "build/classes/java/main");
        cs.append("-binTestDir", "build/classes/java/test");
    }
    static AbstractMain main ;
    public static Preparation preparation;

    @BeforeAll
    static void beforeAll() {
        main = new AbstractMain(cs.flat());
        preparation = main.getPreparation();
    }

}