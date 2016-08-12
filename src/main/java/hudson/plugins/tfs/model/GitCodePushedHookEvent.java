package hudson.plugins.tfs.model;

import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.git.extensions.impl.IgnoreNotifyCommit;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.TeamHookCause;
import hudson.plugins.tfs.TeamPushTrigger;
import hudson.plugins.tfs.TeamEventsEndpoint;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.triggers.SCMTrigger;
import jenkins.model.Jenkins;
import jenkins.triggers.SCMTriggerItem;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GitCodePushedHookEvent extends AbstractHookEvent {

    private static final Logger LOGGER = Logger.getLogger(GitCodePushedHookEvent.class.getName());

    @Override
    public JSONObject perform(final JSONObject requestPayload) {
        throw new IllegalStateException("GitCodePushedHookEvent was disabled");
    }

}
