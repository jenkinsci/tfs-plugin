package hudson.plugins.tfs.model;

import hudson.model.AbstractBuild;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.plugins.tfs.util.BuildVariableResolver; 
import hudson.Util;

import org.kohsuke.stapler.DataBoundConstructor;

public final class ProjectData {
    public final String projectPath;
    public final String localPath;
    
    @DataBoundConstructor
    public ProjectData(String projectPath, String localPath) {
        this.projectPath = Util.removeTrailingSlash(Util.fixNull(projectPath).trim());
        this.localPath = Util.fixEmptyAndTrim(localPath);
    }
    
    public String getProjectPath() {
        return projectPath;
    }

    public String getLocalPath() {
    		return localPath;
    }
    
    public String getProjectPath(Run<?,?> run) {
        return Util.replaceMacro(substituteBuildParameter(run, projectPath), new BuildVariableResolver(run.getParent()));
    }
  
    public static String substituteBuildParameter(Run<?,?> run, String text) {
        if (run instanceof AbstractBuild<?, ?>){
            AbstractBuild<?,?> build = (AbstractBuild<?, ?>) run;
            if (build.getAction(ParametersAction.class) != null)
                return build.getAction(ParametersAction.class).substitute(build, text);
        }
        return text;
    }
    
    public static ProjectData[] getProjects(String projectPath, String localPath, ProjectData[] projects)
    {
    	ProjectData[] newProjects = new ProjectData[projects.length + 1];
    	newProjects[0] = new ProjectData(projectPath, localPath);
    	for (int ndx=0; ndx < projects.length; ++ndx) {
    		newProjects[ndx+1] = new ProjectData(projects[ndx].getProjectPath(), projects[ndx].getLocalPath());
    	}
    	
    	return newProjects;
    }
    
    public static ProjectData[] getProjects(Run<?,?> run, String projectPath, String localPath, ProjectData[] projects)
    {
    	ProjectData[] newProjects = new ProjectData[projects.length + 1];
    	newProjects[0] = new ProjectData(
    			Util.replaceMacro(substituteBuildParameter(run, projectPath), new BuildVariableResolver(run.getParent())),
    			Util.replaceMacro(substituteBuildParameter(run, localPath), new BuildVariableResolver(run.getParent())));
    	for (int ndx=0; ndx < projects.length; ++ndx) {
    		newProjects[ndx+1] = new ProjectData(projects[ndx].getProjectPath(run), projects[ndx].getLocalPath());
    	}
    	
    	return newProjects;
    }
}
