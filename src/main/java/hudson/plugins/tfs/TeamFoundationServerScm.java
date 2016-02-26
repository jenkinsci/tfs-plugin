package hudson.plugins.tfs;

import static hudson.Util.fixEmpty;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.util.Secret;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.actions.CheckoutAction;
import hudson.plugins.tfs.actions.RemoveWorkspaceAction;
import hudson.plugins.tfs.browsers.TeamFoundationServerRepositoryBrowser;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.WorkspaceConfiguration;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.BuildVariableResolver;
import hudson.plugins.tfs.util.BuildWorkspaceConfigurationRetriever;
import hudson.plugins.tfs.util.BuildWorkspaceConfigurationRetriever.BuildWorkspaceConfiguration;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.FormValidation;
import hudson.util.LogTaskListener;
import hudson.util.Scrambler;
import hudson.util.VariableResolver;

import org.kohsuke.stapler.QueryParameter;

/**
 * SCM for Microsoft Team Foundation Server.
 * 
 * @author Erik Ramfelt
 */
public class TeamFoundationServerScm extends SCM {

    public static final String WORKSPACE_ENV_STR = "TFS_WORKSPACE";
    public static final String WORKFOLDER_ENV_STR = "TFS_WORKFOLDER";
    public static final String PROJECTPATH_ENV_STR = "TFS_PROJECTPATH";
    public static final String SERVERURL_ENV_STR = "TFS_SERVERURL";
    public static final String USERNAME_ENV_STR = "TFS_USERNAME";
    public static final String WORKSPACE_CHANGESET_ENV_STR = "TFS_CHANGESET";
    
    private static final String VERSION_SPEC = "VERSION_SPEC";

    private final String serverUrl;
    private final String projectPath;
    private final Collection<String> cloakedPaths;
    private final String localPath;
    private final String workspaceName;
    private @Deprecated String userPassword;
    private /* almost final */ Secret password;
    private final String userName;
    private final boolean useUpdate;
    
    private TeamFoundationServerRepositoryBrowser repositoryBrowser;

    private transient String normalizedWorkspaceName;
    private transient String workspaceChangesetVersion;
    
    private static final Logger logger = Logger.getLogger(TeamFoundationServerScm.class.getName());

    @Deprecated
    public TeamFoundationServerScm(String serverUrl, String projectPath, String localPath, boolean useUpdate, String workspaceName, String userName, String password) {
        this(serverUrl, projectPath, null, localPath, useUpdate, workspaceName, userName, Secret.fromString(password));
    }

    TeamFoundationServerScm(String serverUrl, String projectPath, String cloakedPaths, String localPath, boolean useUpdate, String workspaceName) {
        this(serverUrl, projectPath, cloakedPaths, localPath, useUpdate, workspaceName, null, (Secret)null);
    }

    @DataBoundConstructor
    public TeamFoundationServerScm(String serverUrl, String projectPath, String cloakedPaths, String localPath, boolean useUpdate, String workspaceName, String userName, Secret password) {
        this.serverUrl = serverUrl;
        this.projectPath = projectPath;
        this.cloakedPaths = splitCloakedPaths(cloakedPaths);
        this.useUpdate = useUpdate;
        this.localPath = (Util.fixEmptyAndTrim(localPath) == null ? "." : localPath);
        this.workspaceName = (Util.fixEmptyAndTrim(workspaceName) == null ? "Hudson-${JOB_NAME}-${NODE_NAME}" : workspaceName);
        this.userName = userName;
        this.password = password;
    }

    /* Migrate legacy data */
    private Object readResolve() {
        if (password == null && userPassword != null) {
            password = Secret.fromString(Scrambler.descramble(userPassword));
            userPassword = null;
        }
        return this;
    }

    // Bean properties need for job configuration
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
        return Secret.toString(password);
    }

    public Secret getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public String getCloakedPaths() {
        return serializeCloakedPathCollectionToString(this.cloakedPaths);
    }
    // Bean properties END

    static String serializeCloakedPathCollectionToString(final Collection<String> cloakedPaths) {
        return cloakedPaths == null ? StringUtils.EMPTY : StringUtils.join(cloakedPaths, "\n");
    }

    String getWorkspaceName(AbstractBuild<?,?> build, Computer computer) {
        normalizedWorkspaceName = workspaceName;
        if (build != null) {
            normalizedWorkspaceName = substituteBuildParameter(build, normalizedWorkspaceName);
            normalizedWorkspaceName = Util.replaceMacro(normalizedWorkspaceName, new BuildVariableResolver(build.getProject(), computer));
        }
        normalizedWorkspaceName = normalizedWorkspaceName.replaceAll("[\"/:<>\\|\\*\\?]+", "_");
        normalizedWorkspaceName = normalizedWorkspaceName.replaceAll("[\\.\\s]+$", "_");
        return normalizedWorkspaceName;
    }

    public String getServerUrl(Run<?,?> run) {
        return substituteBuildParameter(run, serverUrl);
    }

    String getProjectPath(Run<?,?> run) {
        return Util.replaceMacro(substituteBuildParameter(run, projectPath), new BuildVariableResolver(run.getParent()));
    }

    Collection<String> getCloakedPaths(final Run<?, ?> run) {
        final List<String> paths = new ArrayList<String>();
        if (cloakedPaths != null) {
            final BuildVariableResolver resolver = new BuildVariableResolver(run.getParent());
            for (final String cloakedPath : cloakedPaths) {
                final String path = substituteBuildParameter(run, cloakedPath);
                final String enhancedPath = Util.replaceMacro(path, resolver);
                paths.add(enhancedPath);
            }
        }
        return paths;
    }

    private String substituteBuildParameter(Run<?,?> run, String text) {
        if (run instanceof AbstractBuild<?, ?>){
            AbstractBuild<?,?> build = (AbstractBuild<?, ?>) run;
            if (build.getAction(ParametersAction.class) != null) {
                return build.getAction(ParametersAction.class).substitute(build, text);
            }
        }
        return text;
    }
    
    static Collection<String> splitCloakedPaths(final String cloakedPaths) {
        final List<String> cloakedPathsList = new ArrayList<String>();
        if (cloakedPaths != null && cloakedPaths.length() > 0) {
            final StringBuilder cloakedPath = new StringBuilder(cloakedPaths.length());
            for (final char character : cloakedPaths.toCharArray()) {
                switch (character) {
                    case '\n':
                        if (cloakedPath.length() > 0) {
                            cloakedPathsList.add(cloakedPath.toString().trim());
                            cloakedPath.setLength(0);
                        }
                        break;
                    default:
                        cloakedPath.append(character);
                        break;
                }
            }
            if (cloakedPath.length() > 0) {
                cloakedPathsList.add(cloakedPath.toString().trim());
            }
        }
        return cloakedPathsList;
    }

    @Override
    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspaceFilePath, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        Server server = createServer(launcher, listener, build);
        try {
            WorkspaceConfiguration workspaceConfiguration = new WorkspaceConfiguration(server.getUrl(), getWorkspaceName(build, Computer.currentComputer()), getProjectPath(build), getCloakedPaths(build), getLocalPath());
            
            final AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();
            // Check if the configuration has changed
            if (previousBuild != null) {
                BuildWorkspaceConfiguration nodeConfiguration = new BuildWorkspaceConfigurationRetriever().getLatestForNode(build.getBuiltOn(), previousBuild);
                if ((nodeConfiguration != null) &&
                        nodeConfiguration.workspaceExists() 
                        && (! workspaceConfiguration.equals(nodeConfiguration))) {
                    listener.getLogger().println("Deleting workspace as the configuration has changed since a build was performed on this computer.");
                    new RemoveWorkspaceAction(workspaceConfiguration.getWorkspaceName()).remove(server);
                    nodeConfiguration.setWorkspaceWasRemoved();
                    nodeConfiguration.save();
                }
            }
            
            build.addAction(workspaceConfiguration);
            VariableResolver<String> buildVariableResolver = build.getBuildVariableResolver();
            String singleVersionSpec = buildVariableResolver.resolve(VERSION_SPEC);
            final String projectPath = workspaceConfiguration.getProjectPath();
            final Project project = server.getProject(projectPath);
            final int changeSet = recordWorkspaceChangesetVersion(build, listener, project, projectPath, singleVersionSpec);

            CheckoutAction action = new CheckoutAction(workspaceConfiguration.getWorkspaceName(), workspaceConfiguration.getProjectPath(), workspaceConfiguration.getCloakedPaths(), workspaceConfiguration.getWorkfolder(), isUseUpdate());
            List<ChangeSet> list;
            if (StringUtils.isNotEmpty(singleVersionSpec)) {
                list = action.checkoutBySingleVersionSpec(server, workspaceFilePath, singleVersionSpec);
            }
            else {
                final VersionSpec previousBuildVersionSpec = determineVersionSpecFromBuild(previousBuild, 1, changeSet);
                final ChangesetVersionSpec currentBuildVersionSpec = new ChangesetVersionSpec(changeSet);
                list = action.checkout(server, workspaceFilePath, previousBuildVersionSpec, currentBuildVersionSpec);
            }
            ChangeSetWriter writer = new ChangeSetWriter();
            writer.write(list, changelogFile);
        } finally {
            server.close();
        }
        return true;
    }

    static VersionSpec determineVersionSpecFromBuild(final AbstractBuild<?, ?> build, final int offset, final int maximumChangeSetNumber) {
        final VersionSpec result;
        if (build != null) {
            final TFSRevisionState revisionState = build.getAction(TFSRevisionState.class);
            if (revisionState != null) {
                final int changeSetNumber = revisionState.changesetVersion + offset;
                if (changeSetNumber <= maximumChangeSetNumber) {
                    result = new ChangesetVersionSpec(changeSetNumber);
                } else {
                    result = null;
                }
            } else {
                result = null;
            }
        }
        else {
            result = null;
        }
        return result;
    }

    int recordWorkspaceChangesetVersion(final AbstractBuild<?, ?> build, final BuildListener listener, final Project project, final String projectPath, final String singleVersionSpec) throws IOException, InterruptedException {
        final VersionSpec workspaceVersion;
        if (!StringUtils.isEmpty(singleVersionSpec)) {
            workspaceVersion = VersionSpec.parseSingleVersionFromSpec(singleVersionSpec, null);
        }
        else {
            workspaceVersion = new DateVersionSpec(build.getTimestamp());
        }
        int buildChangeset;
        setWorkspaceChangesetVersion(null);
        buildChangeset = project.getRemoteChangesetVersion(workspaceVersion);
        setWorkspaceChangesetVersion(Integer.toString(buildChangeset, 10));

        // by adding this action, we prevent calcRevisionsFromBuild() from being called
        build.addAction(new TFSRevisionState(buildChangeset, projectPath));

        return buildChangeset;
    }

    void setWorkspaceChangesetVersion(String workspaceChangesetVersion) {
        this.workspaceChangesetVersion = workspaceChangesetVersion;
    }

    @Override
    public boolean pollChanges(AbstractProject hudsonProject, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        Run<?,?> lastRun = hudsonProject.getLastBuild();
        if (lastRun == null) {
            return true;
        } else {
            Server server = createServer(launcher, listener, lastRun);
            try {
                return (server.getProject(getProjectPath(lastRun)).getDetailedHistoryWithoutCloakedPaths(
                            lastRun.getTimestamp(),
                            Calendar.getInstance(),
                            getCloakedPaths(lastRun)
                        ).size() > 0);
            } finally {
                server.close();
            }
        }
    }
    
    @Override
    public boolean processWorkspaceBeforeDeletion(AbstractProject<?, ?> project, FilePath workspace, Node node) throws IOException, InterruptedException {
        Run<?,?> lastRun = project.getLastBuild();
        if ((lastRun == null) || !(lastRun instanceof AbstractBuild<?, ?>)) {
            return true;
        }
        
        // Due to an error in Hudson core (pre 1.321), null was sent in for all invocations of this method
        // Therefore we try to work around the problem, and see if its only built on one node or not. 
        if (node == null) { 
            while (lastRun != null) {
                AbstractBuild<?,?> build = (AbstractBuild<?, ?>) lastRun;
                Node buildNode = build.getBuiltOn();
                if (node == null) {
                    node = buildNode;
                } else {
                    if (!buildNode.getNodeName().equals(node.getNodeName())) {
                        logger.warning("Could not wipe out workspace as there is no way of telling what Node the request is for. Please upgrade Hudson to a newer version.");
                        return false;
                    }
                }                
                lastRun = lastRun.getPreviousBuild();
            }
            if (node == null) {
                return true;
            }
            lastRun = project.getLastBuild();
        }
        
        BuildWorkspaceConfiguration configuration = new BuildWorkspaceConfigurationRetriever().getLatestForNode(node, lastRun);
        if ((configuration != null) && configuration.workspaceExists()) {
            LogTaskListener listener = new LogTaskListener(logger, Level.INFO);
            Launcher launcher = node.createLauncher(listener);        
            Server server = createServer(launcher, listener, lastRun);
            try {
                if (new RemoveWorkspaceAction(configuration.getWorkspaceName()).remove(server)) {
                    configuration.setWorkspaceWasRemoved();
                    configuration.save();
                }
            } finally {
                server.close();
            }
        }
        return true;
    }
    
    protected Server createServer(final Launcher launcher, final TaskListener taskListener, Run<?,?> run) throws IOException {
        return new Server(launcher, taskListener, getServerUrl(run), getUserName(), getUserPassword());
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
    public FilePath getModuleRoot(FilePath workspace) {
        return workspace.child(getLocalPath());
    }

    @Override
    public TeamFoundationServerRepositoryBrowser getBrowser() {
        return repositoryBrowser;
    }

    @Override
    public void buildEnvVars(AbstractBuild<?,?> build, Map<String, String> env) {
        super.buildEnvVars(build, env);
        if (normalizedWorkspaceName != null) {
            env.put(WORKSPACE_ENV_STR, normalizedWorkspaceName);
        }
        if (env.containsKey("WORKSPACE")) {
            env.put(WORKFOLDER_ENV_STR, env.get("WORKSPACE") + File.separator + getLocalPath());
        }
        if (projectPath != null) {
            env.put(PROJECTPATH_ENV_STR, projectPath);
        }
        if (serverUrl != null) {
            env.put(SERVERURL_ENV_STR, serverUrl);
        }
        if (userName != null) {
            env.put(USERNAME_ENV_STR, userName);
        }
        if (workspaceChangesetVersion != null && workspaceChangesetVersion.length() > 0) {
            env.put(WORKSPACE_CHANGESET_ENV_STR, workspaceChangesetVersion);
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends SCMDescriptor<TeamFoundationServerScm> {
        
        public static final Pattern WORKSPACE_NAME_REGEX = Pattern.compile("[^\"/:<>\\|\\*\\?]+[^\\s\\.\"/:<>\\|\\*\\?]$", Pattern.CASE_INSENSITIVE);
        public static final Pattern USER_AT_DOMAIN_REGEX = Pattern.compile("^([^\\/\\\\\"\\[\\]:|<>+=;,\\*@]+)@([a-z][a-z0-9.-]+)$", Pattern.CASE_INSENSITIVE);
        public static final Pattern DOMAIN_SLASH_USER_REGEX = Pattern.compile("^([a-z][a-z0-9.-]+)\\\\([^\\/\\\\\"\\[\\]:|<>+=;,\\*@]+)$", Pattern.CASE_INSENSITIVE);
        public static final Pattern PROJECT_PATH_REGEX = Pattern.compile("^\\$\\/.*", Pattern.CASE_INSENSITIVE);
        public static final Pattern CLOAKED_PATHS_REGEX = Pattern.compile("\\s*\\$[^\\n;]+(\\s*[\\n]\\s*\\$[^\\n;]+){0,}\\s*", Pattern.CASE_INSENSITIVE);
        private transient String tfExecutable;
        
        public DescriptorImpl() {
            super(TeamFoundationServerScm.class, TeamFoundationServerRepositoryBrowser.class);
            load();
        }

        @Override
        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            TeamFoundationServerScm scm = (TeamFoundationServerScm) super.newInstance(req, formData);
            scm.repositoryBrowser = RepositoryBrowsers.createInstance(TeamFoundationServerRepositoryBrowser.class,req,formData,"browser");
            return scm;
        }

        private FormValidation doRegexCheck(final Pattern[] regexArray,
                final String noMatchText, final String nullText, String value) {
            value = fixEmpty(value);
            if (value == null) {
                if (nullText == null) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error(nullText);
                }
            }
            for (Pattern regex : regexArray) {
                if (regex.matcher(value).matches()) {
                    return FormValidation.ok();
                }
            }
            return FormValidation.error(noMatchText);
        }

        public FormValidation doProjectPathCheck(@QueryParameter final String value) {
            return doRegexCheck(new Pattern[]{PROJECT_PATH_REGEX},
                    "Project path must begin with '$/'.",
                    "Project path is mandatory.", value );
        }
        
        public FormValidation doWorkspaceNameCheck(@QueryParameter final String value) {
            return doRegexCheck(new Pattern[]{WORKSPACE_NAME_REGEX},
                    "Workspace name cannot end with a space or period, and cannot contain any of the following characters: \"/:<>|*?", 
                    "Workspace name is mandatory", value);
        }

        public FormValidation doCloakedPathsCheck(@QueryParameter final String value) {
            return doRegexCheck(new Pattern[]{CLOAKED_PATHS_REGEX},
                    "Each cloaked path must begin with '$/'. Multiple paths must be separated by blank lines.",
                    null, value );
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Team Foundation Server";
        }
    }

    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build,
            Launcher launcher, TaskListener listener) throws IOException,
            InterruptedException {
        /*
         * This method does nothing, since the work has already been done in
         * the checkout() method, as per the documentation:
         * """
         * As an optimization, SCM implementation can choose to compute SCMRevisionState
         * and add it as an action during check out, in which case this method will not called.
         * """
         */
        return null;
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(
            AbstractProject<?, ?> project, Launcher launcher,
            FilePath workspace, TaskListener listener, SCMRevisionState baseline)
            throws IOException, InterruptedException {

        final Launcher localLauncher = launcher != null ? launcher : new Launcher.LocalLauncher(listener);
        if (!(baseline instanceof TFSRevisionState))
        {
            // This plugin was just upgraded, we don't yet have a new-style baseline,
            // so we perform an old-school poll
            boolean shouldBuild = pollChanges(project, localLauncher, workspace, listener);
            return shouldBuild ? PollingResult.BUILD_NOW : PollingResult.NO_CHANGES;
        }
        final TFSRevisionState tfsBaseline = (TFSRevisionState) baseline;
        if (!projectPath.equalsIgnoreCase(tfsBaseline.projectPath))
        {
            // There's no PollingResult.INCOMPARABLE, so we use the next closest thing
            return PollingResult.BUILD_NOW;
        }
        Run<?, ?> build = project.getLastBuild();
        final Server server = createServer(localLauncher, listener, build);
        final Project tfsProject = server.getProject(projectPath);
        try {
            final ChangeSet latest = tfsProject.getLatestUncloakedChangeset(tfsBaseline.changesetVersion, cloakedPaths);
            final TFSRevisionState tfsRemote =
                    (latest != null)
                    ? new TFSRevisionState(latest.getVersion(), projectPath)
                    : tfsBaseline;

            // TODO: we could return INSIGNIFICANT if all the changesets
            // contain the string "***NO_CI***" at the end of their comment
            final Change change = 
                    tfsBaseline.changesetVersion == tfsRemote.changesetVersion
                    ? Change.NONE
                    : Change.SIGNIFICANT;
            return new PollingResult(tfsBaseline, tfsRemote, change);
        } catch (final Exception e) {
            e.printStackTrace(listener.fatalError(e.getMessage()));
            return PollingResult.NO_CHANGES;
        } finally {
            server.close();
        }
    }
}
