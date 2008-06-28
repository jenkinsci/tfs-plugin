package hudson.plugins.tfs;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.action.DefaultGetAction;
import hudson.plugins.tfs.action.DefaultHistoryAction;
import hudson.plugins.tfs.action.DefaultPollAction;
import hudson.plugins.tfs.model.TeamFoundationChangeSet;
import hudson.plugins.tfs.model.TeamFoundationProject;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.FormFieldValidator;

/**
 * SCM for Microsoft Team Foundation Server.
 * 
 * @author Erik Ramfelt
 */
public class TeamFoundationServerScm extends SCM {

    private String server;
    private String project;

    private String workspaceName;
    private boolean cleanCopy;

    @DataBoundConstructor
    public TeamFoundationServerScm(String server, String project, boolean cleanCopy, String workspaceName) {
        this.server = server;
        this.project = project;
        this.cleanCopy = cleanCopy;
        this.workspaceName = workspaceName;
    }

    private TeamFoundationProject createTeamFoundationProject() {
        return new TeamFoundationProject(server, project);
    }

    public String getServer() {
        return server;
    }

    public String getProject() {
        return project;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public boolean isCleanCopy() {
        return cleanCopy;
    }

    @Override
    public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        TfTool tool = new TfTool(getDescriptor().tfExecutable, launcher, listener, workspace);
        
        DefaultGetAction action = new DefaultGetAction();
        action.getFiles(tool, cleanCopy);
        
        if (build.getPreviousBuild() == null) {
            createEmptyChangeLog(changelogFile, listener, "changesets");
        } else {
            DefaultHistoryAction historyAction = new DefaultHistoryAction();
            List<TeamFoundationChangeSet> changeSets = historyAction.getChangeSets(tool, createTeamFoundationProject(), build.getPreviousBuild().getTimestamp(), Calendar.getInstance());
            
            ChangeSetWriter writer = new ChangeSetWriter();
            writer.write(changelogFile, changeSets);
        }
        return true;
    }

    @Override
    public boolean pollChanges(AbstractProject hudsonProject, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        Run<?,?> lastBuild = hudsonProject.getLastBuild();
        if (lastBuild == null) {
            return true;
        } else {
            TfTool tool = new TfTool(getDescriptor().tfExecutable, launcher, listener, workspace);
            DefaultPollAction action = new DefaultPollAction();
            return action.hasChanges(tool , createTeamFoundationProject(), lastBuild.getTimestamp());
        }
    }

    @Override
    public boolean requiresWorkspaceForPolling() {
        return false;
    }

    @Override
    public boolean supportsPolling() {
        return true;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return PluginImpl.TFS_DESCRIPTOR;
    }

    public static class DescriptorImpl extends SCMDescriptor<TeamFoundationServerScm> {
        
        private String tfExecutable;
        
        protected DescriptorImpl() {
            super(TeamFoundationServerScm.class, null);
        }

        public String getTfExecutable() {
            if (tfExecutable == null) {
                return "tf";
            } else {
                return tfExecutable;
            }
        }
        
        public void doExecutableCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            new FormFieldValidator.Executable(req, rsp).process();
        }
        
        @Override
        public boolean configure(StaplerRequest req) throws FormException {
            tfExecutable = Util.fixEmpty(req.getParameter("tfs.tfExecutable").trim());
            save();
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Team Foundation Server";
        }
        
        @Override
        public TeamFoundationServerScm newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(TeamFoundationServerScm.class, formData);
        }
    }
}
