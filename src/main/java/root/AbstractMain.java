package root;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.entities.Stats;
import root.util.ConfigurationProperties;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;

public class AbstractMain {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMain.class);

    public Date bornTime;

    protected static Options options = new Options();

    static {
        //must-have
        options.addOption("proj", true, "");
        options.addOption("id", true, "");
        options.addOption("location", true, "");
        options.addOption("srcJavaDir", true, "");
        options.addOption("srcTestDir", true, "");
        options.addOption("binJavaDir", true, "");
        options.addOption("binTestDir", true, "");
        options.addOption("testInfos", true, "");
        options.addOption("originalCommit", true, "");
        options.addOption("dependencies", true, "separated by " + File.pathSeparator);
        options.addOption("patchesDir", true, "patches directory");
        options.addOption("sliceRoot", true, "slice log root directory");

        //optional
        options.addOption("purify", true, "true|false, denotes whether to purify the failing test. default is 'true'");
        options.addOption("slicingMode", true, "all|fault|diff, denotes slicer's mode. default is 'all'");
        options.addOption("complianceLevel", true, "default 1.8");
        options.addOption("binExecuteTestClasses", true, "");
        options.addOption("javaClassesInfoPath", true, "");
        options.addOption("testClassesInfoPath", true, "");
        options.addOption("resultOutput", true, "");
    }

    public static final CommandLineParser parser = new DefaultParser();

    public ProjectPreparation initialize(String[] args) {
        bornTime = new Date();
        Stats.getCurrentStats();
        boolean res = progressArguments(args);
        if (!res)
            return null;
        try {
            ProjectPreparation helper = new ProjectPreparation();
            helper.initialize(true, true);
            return helper;
        } catch (Exception e) {
            logger.error("Error occurred when ProjectPreparation initialization: " + e.getMessage());
        }
        return null;
    }

    private boolean progressArguments(String[] commandSummary) {
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, commandSummary);
        } catch (ParseException e) {
            logger.error("Wrong Arguments have been passed!");
            help();
            return false;
        }

        if (commandLine.hasOption("help")) {
            help();
            return false;
        }
        boolean check = true;
        check &= checkProperty(commandLine, "proj", null);
        check &= checkProperty(commandLine, "id", null);
        check &= checkProperty(commandLine, "location", null);
        check &= checkProperty(commandLine, "srcJavaDir", null);
        check &= checkProperty(commandLine, "srcTestDir", null);
        check &= checkProperty(commandLine, "binJavaDir", null);
        check &= checkProperty(commandLine, "binTestDir", null);
        check &= checkProperty(commandLine, "testInfos", null);
        check &= checkProperty(commandLine, "dependencies", null);
        check &= checkProperty(commandLine, "patchesDir", null);
        check &= checkProperty(commandLine, "sliceRoot", null);
        check &= checkProperty(commandLine, "originalCommit", null);
        check &= checkProperty(commandLine, "complianceLevel", "1.8");
        check &= checkProperty(commandLine, "slicingMode", "all");
        check &= checkProperty(commandLine, "purify", "true");
        check &= checkProperty(commandLine, "resultOutput", Paths.get("./").toAbsolutePath().toString());
        if (!check) {
            logger.error("Lack of must-have argument(s)!");
            help();
            return false;
        }
        return true;
    }

    private boolean checkProperty(CommandLine commandLine, String name, String elseValue) {
        boolean check = true;
        if (commandLine.hasOption(name)) {
            ConfigurationProperties.setProperty(name, commandLine.getOptionValue(name));
        } else if (elseValue != null) {
            ConfigurationProperties.setProperty(name, elseValue);
        } else {
            check = ConfigurationProperties.hasProperty(name);
        }
        return check;
    }

    private void help() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("Main", options);
        logger.info("");

        System.exit(0);
    }
}
