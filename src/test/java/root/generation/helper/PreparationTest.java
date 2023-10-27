package root.generation.helper;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import root.AbstractMain;
import root.util.CommandSummary;

import java.io.File;

public class PreparationTest {
    static CommandSummary cs = new CommandSummary();
    static {
        cs.append("-location", "/home/liumengjiao/Desktop/CI/bugs/Closure_10_bug");
        cs.append("-srcJavaDir", "src");
        cs.append("-srcTestDir", "test");
        cs.append("-binJavaDir", "build/classes");
        cs.append("-binTestDir", "build/test");
        cs.append("-complianceLevel", "1.6");
    }
    static AbstractMain main ;
    public static Preparation preparation;

    @BeforeAll
    static void beforeAll() {
        main = new AbstractMain(cs.flat());
        preparation = main.getPreparation();
    }

}