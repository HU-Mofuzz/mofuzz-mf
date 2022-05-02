package de.hub.mse.emf.multifile;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.nio.file.Files;

@Getter
@ToString
public class Args {

    @Setter
    @Parameter(names = {"--failDirectory", "--failDir"}, description = "Directory for structured saving of fail data")
    private String failDirectory;

    @Setter
    @Parameter(names = {"--workingDirectory", "--workingDir"}, description = "Working directory where the test files will be generated")
    private String workingDirectory = "test_svg";

    @Setter
    @Parameter(names = {"--testDirectory", "--testDir"}, description = "Directory for the Zest test files")
    private String testDirectory;

    @Parameter(names = "--initialFiles", description = "The amount of initial files to be generated to start of")
    private int filesToGenerate = 2;

    @Parameter(names = "--modelDepth", description = "Maximum depth of the models generated")
    private int modelDepth = 4;

    @Parameter(names = "--modelWidth", description = "Maximum width of a model")
    private int modelWidth = 4;

    @Parameter(names = {"--linkProb"}, description = "Probability of links used in one model")
    private double linkProbability = 0.5f;

    @Parameter(names = {"--linkNumber", "--links"}, description = "Number of links used in one model")
    private int linkNumber = 0;

    @Parameter(names = {"--duration", "--minutes"}, description = "The duration of the test run in minutes")
    private int durationMinutes = 1;

    @Parameter(names = {"--help", "/h", "-h"}, description = "Print this argument description", help = true)
    private boolean help;

    @Parameter(description = "Test method")
    private String testMethod;

    public Args() {
    }

    public boolean isWorkingDirectorySet() {
        return workingDirectory != null;
    }

    public boolean isTestDirectorySet() {
        return testDirectory != null;
    }

    public boolean isFailDirectorySet() {
        return failDirectory != null;
    }

}
