package hudson.plugins.tfs;

import java.io.File;
import java.io.IOException;

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
import hudson.model.TaskListener;
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
    public boolean checkout(AbstractBuild arg0, Launcher arg1, FilePath arg2, BuildListener arg3, File arg4) throws IOException, InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean pollChanges(AbstractProject arg0, Launcher arg1, FilePath arg2, TaskListener arg3) throws IOException, InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SCMDescriptor<TeamFoundationServerScm> getDescriptor() {
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
