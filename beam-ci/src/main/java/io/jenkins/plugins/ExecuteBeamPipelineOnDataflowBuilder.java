package io.jenkins.plugins;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jenkins.tasks.SimpleBuildStep;

public class ExecuteBeamPipelineOnDataflowBuilder extends Builder implements SimpleBuildStep {

    private final String pathToMainClass;
    private final String pipelineOptions;
    private final String buildReleaseOptions;
    private boolean useJava; // if false, use Python
    private boolean useGradle; // if false, use Maven
    // todo more configurations may be needed for credentials and getting into the right directory with the pom file

    @DataBoundConstructor
    public ExecuteBeamPipelineOnDataflowBuilder(String pathToMainClass, String pipelineOptions, String buildReleaseOptions, boolean useJava, boolean useGradle) {
        this.pathToMainClass = pathToMainClass;
        this.pipelineOptions = pipelineOptions;
        this.buildReleaseOptions = buildReleaseOptions;
        this.useJava = useJava;
        this.useGradle = useGradle;
    }

    public String getPathToMainClass() {
        return pathToMainClass;
    }

    public String getPipelineOptions() {
        return pipelineOptions;
    }

    public String getBuildReleaseOptions() {
        return buildReleaseOptions;
    }

    public boolean getUseJava() {
        return useJava;
    }

    public boolean getUseGradle() {
        return useGradle;
    }

    private void buildCommand(ProcessBuilder processBuilder) {
        ArrayList<String> command;
        if (this.useJava && this.useGradle) { // gradle
            command = new ArrayList<>(Arrays.asList("gradle", "clean", "execute", "-DmainClass=" + this.pathToMainClass));
        } else if (this.useJava) { // maven
            command = new ArrayList<>(Arrays.asList("mvn", "compile", "exec:java", "-Dexec.mainClass=" + this.pathToMainClass));
        } else { // python
            command = new ArrayList<>(Arrays.asList("python", "-m", this.pathToMainClass));
        }
        // Add pipeline and build release options if included
        if (!this.pipelineOptions.equals(""))
            command.add(this.pipelineOptions);
        if (!this.buildReleaseOptions.equals(""))
            command.add(this.buildReleaseOptions);
        processBuilder.command(command);
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        //ProcessBuilder processBuilder = new ProcessBuilder();

        // right now just testing to see that all configurations are received correctly
        listener.getLogger().println("path to main class : " + this.pathToMainClass);
        listener.getLogger().println("pipeline options : " + this.pipelineOptions);
        listener.getLogger().println("build release options : " + this.buildReleaseOptions);
        listener.getLogger().println("use java: " + this.useJava);
        listener.getLogger().println("use gradle: " + this.useGradle);

        // todo ensure we're in the right directory to run the following command
        // thinking that this could mean JENKINS_HOME/workspace/JOB_NAME
        // todo eventually call buildCommand
//        processBuilder.command(list);
//        Process process = processBuilder.start();

        // logging results from the command
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line;
//        while((line = reader.readLine()) != null) {
//            listener.getLogger().println(line);
//        }
//
//        int exitCode = process.waitFor();
//        listener.getLogger().println("\n Exited with error code : " + exitCode);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckPathToMainClass(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error("Missing path to main class.");
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Execute Beam Pipeline on Dataflow";
        }

    }

}
