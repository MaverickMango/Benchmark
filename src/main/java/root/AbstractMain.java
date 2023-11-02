package root;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.helper.Preparation;
import root.util.ConfigurationProperties;

import java.io.File;

public class AbstractMain {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMain.class);

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

        //optional
        options.addOption("complianceLevel", true, "default 1.8");
        options.addOption("binExecuteTestClasses", true, "");
        options.addOption("javaClassesInfoPath", true, "");
        options.addOption("testClassesInfoPath", true, "");
    }

    public final CommandLineParser parser = new DefaultParser();
    protected Preparation preparation;

    public AbstractMain(String[] args) {
        this.preparation = initialize(args);
    }

    public Preparation getPreparation() {
        return this.preparation;
    }

    private Preparation initialize(String[] args) {
        boolean res = progressArguments(args);
        if (!res)
            return null;
        try {
            Preparation helper = new Preparation();
            helper.initialize(true, true);
            return helper;
        } catch (Exception e) {
            logger.error("Error occurred when MutatorHelper initialization: " + e.getMessage());
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
        if (commandLine.hasOption("originalCommit")) {
            ConfigurationProperties.setProperty("originalCommit", commandLine.getOptionValue("originalCommit"));
        }
        if (commandLine.hasOption("complianceLevel")) {
            ConfigurationProperties.setProperty("complianceLevel", commandLine.getOptionValue("complianceLevel"));
        } else {
            ConfigurationProperties.setProperty("complianceLevel", "1.8");
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
