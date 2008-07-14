package hudson.plugins.tfs;

import static hudson.Util.fixEmpty;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.actions.CheckoutAction;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.FormFieldValidator;
import hudson.util.Scrambler;

/**
 * SCM for Microsoft Team Foundation Server.
 * 
 * @author Erik Ramfelt
 */
public class TeamFoundationServerScm extends SCM {

    private final String serverUrl;
    private final String projectPath;
    private final String localPath;
    private final String workspaceName;
    private final String userPassword;
    private final String userName;
    private final boolean useUpdate;

    private transient String normalizedWorkspaceName;

    @DataBoundConstructor
    public TeamFoundationServerScm(String serverUrl, String projectPath, String localPath, boolean useUpdate, String workspaceName, String userName, String userPassword) {
        this.serverUrl = serverUrl;
        this.projectPath = projectPath;
        this.useUpdate = useUpdate;
        this.localPath = (Util.fixEmptyAndTrim(localPath) == null ? "." : localPath);
        this.workspaceName = (Util.fixEmptyAndTrim(workspaceName) == null ? "Hudson-${JOB_NAME}" : workspaceName);
        this.userName = userName;
        this.userPassword = Scrambler.scramble(userPassword);
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public boolean isUseUpdate() {
        return useUpdate;
    }

    public String getUserPassword() {
        return Scrambler.descramble(userPassword);
    }

    public String getUserName() {
        return userName;
    }

    public String getNormalizedWorkspaceName(AbstractProject<?,?> project) {
        if (normalizedWorkspaceName == null) {
            normalizedWorkspaceName = workspaceName;
            Matcher matcher = Pattern.compile("\\$\\{JOB_NAME\\}", Pattern.CASE_INSENSITIVE).matcher(normalizedWorkspaceName);
            if (matcher.find()) {
                normalizedWorkspaceName = matcher.replaceAll(project.getName());
            }
            matcher = Pattern.compile("\\$\\{USER_NAME\\}", Pattern.CASE_INSENSITIVE).matcher(normalizedWorkspaceName);
            if (matcher.find()) {
                normalizedWorkspaceName = matcher.replaceAll(System.getProperty("user.name"));
            }
        }
        return normalizedWorkspaceName;
    }

    @Override
    public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspaceFilePath, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        Server server = createServer(new TfTool(getDescriptor().getTfExecutable(), launcher, listener, workspaceFilePath));
        
        CheckoutAction action = new CheckoutAction(getNormalizedWorkspaceName(build.getProject()), 
                projectPath, localPath, useUpdate);
        try {
            List<ChangeSet> list = action.checkout(server, (build.getPreviousBuild() != null ? build.getPreviousBuild().getTimestamp() : null));
            ChangeSetWriter writer = new ChangeSetWriter();
            writer.write(list, changelogFile);
        } catch (ParseException pe) {
            listener.fatalError(pe.getMessage());
            throw new AbortException();
        }
        return true;
    }

    @Override
    public boolean pollChanges(AbstractProject hudsonProject, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        Run<?,?> lastBuild = hudsonProject.getLastBuild();
        if (lastBuild == null) {
            return true;
        } else {
            Server server = createServer(new TfTool(getDescriptor().getTfExecutable(), launcher, listener, workspace));
            try {
                return (server.getProject(this.projectPath).getBriefHistory(
                            lastBuild.getTimestamp(), 
                            Calendar.getInstance()
                        ).size() > 0);
            } catch (ParseException pe) {
                listener.fatalError(pe.getMessage());
                throw new AbortException();
            }
        }
    }
    
    protected Server createServer(TfTool tool) {
        return new Server(tool, getServerUrl(), getUserName(), getUserPassword());
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
        return new ChangeSetReader();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return PluginImpl.TFS_DESCRIPTOR;
    }

    public static class DescriptorImpl extends SCMDescriptor<TeamFoundationServerScm> {
        
        public static final String USER_AT_DOMAIN_REGEX = "\\w+@\\w+";
        public static final String DOMAIN_SLASH_USER_REGEX = "\\w+\\\\\\w+";
        private String tfExecutable;
        
        protected DescriptorImpl() {
            super(TeamFoundationServerScm.class, null);
            load();
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
        
        public void doUsernameCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            new FormFieldValidator(req, rsp, false) {
                @Override
                protected void check() throws IOException, ServletException {
                    String value = fixEmpty(request.getParameter("value"));
                    if ((value == null) || 
                            value.matches(DOMAIN_SLASH_USER_REGEX) || 
                            value.matches(USER_AT_DOMAIN_REGEX)) {
                        ok();
                        return;
                    }
                    error("Login name must contain the name of the domain and user");
                }
            }.process();
        }
        
        public void doFieldCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            try {
                int hudsonMinorVersion = Integer.parseInt(Hudson.VERSION.substring(Hudson.VERSION.indexOf('.') + 1));
                if (hudsonMinorVersion >= 216) {
                    Hudson.getInstance().doFieldCheck(req, rsp);
                }
            } catch (NumberFormatException nfe) {
            }
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
    }
}
