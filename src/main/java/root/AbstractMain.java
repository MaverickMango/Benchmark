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
    public Stats currentStat;

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

        //optional
        options.addOption("complianceLevel", true, "default 1.8");
        options.addOption("binExecuteTestClasses", true, "");
        options.addOption("javaClassesInfoPath", true, "");
        options.addOption("testClassesInfoPath", true, "");
        options.addOption("resultOutput", true, "");
    }

    public static final CommandLineParser parser = new DefaultParser();

    public ProjectPreparation initialize(String[] args) {
        bornTime = new Date();
        currentStat = Stats.getCurrentStats();
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
        if (commandLine.hasOption("proj")) {
            ConfigurationProperties.setProperty("proj", commandLine.getOptionValue("proj"));
        }
        if (commandLine.hasOption("id")) {
            ConfigurationProperties.setProperty("id", commandLine.getOptionValue("id"));
        }
        if (commandLine.hasOption("location")) {
            ConfigurationProperties.setProperty("location", commandLine.getOptionValue("location"));
        }
        if (commandLine.hasOption("srcJavaDir")) {
            ConfigurationProperties.setProperty("srcJavaDir", commandLine.getOptionValue("srcJavaDir"));
        }
        if (commandLine.hasOption("srcTestDir")) {
            ConfigurationProperties.setProperty("srcTestDir", commandLine.getOptionValue("srcTestDir"));
        }
        if (commandLine.hasOption("binJavaDir")) {
            ConfigurationProperties.setProperty("binJavaDir", commandLine.getOptionValue("binJavaDir"));
        }
        if (commandLine.hasOption("binTestDir")) {
            ConfigurationProperties.setProperty("binTestDir", commandLine.getOptionValue("binTestDir"));
        }
        if (commandLine.hasOption("testInfos")) {
            ConfigurationProperties.setProperty("testInfos", commandLine.getOptionValue("testInfos"));
        }
        if (commandLine.hasOption("dependencies")) {
            String dependencies = commandLine.getOptionValue("dependencies");
            ConfigurationProperties.setProperty("dependencies", dependencies);
        }
        if (commandLine.hasOption("patchesDir")) {
            ConfigurationProperties.setProperty("patchesDir", commandLine.getOptionValue("patchesDir"));
        }
        if (commandLine.hasOption("originalCommit")) {
            ConfigurationProperties.setProperty("originalCommit", commandLine.getOptionValue("originalCommit"));
        }
        if (commandLine.hasOption("complianceLevel")) {
            ConfigurationProperties.setProperty("complianceLevel", commandLine.getOptionValue("complianceLevel"));
        } else {
            ConfigurationProperties.setProperty("complianceLevel", "1.8");
        }
        if (commandLine.hasOption("resultOutput")) {
            ConfigurationProperties.setProperty("resultOutput", commandLine.getOptionValue("resultOutput"));
        } else {
            ConfigurationProperties.setProperty("resultOutput", Paths.get("./").toAbsolutePath().toString());
        }

        return true;
    }

    private void help() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("Main", options);
        logger.info("");

        System.exit(0);
    }
}
