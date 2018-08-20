package hudson.plugins.tfs.rm;

import hudson.model.InvisibleAction;
import java.util.List;

/**
 * Implements ReleaseWebhook Post build action.
 * @author Kalyan
 */
public class ReleaseWebHookSummaryAction extends InvisibleAction {

    private List<ReleaseWebHookStatus> webHookStatus;

    public ReleaseWebHookSummaryAction(final List<ReleaseWebHookStatus> webHookStatus) {
        this.webHookStatus = webHookStatus;
    }

    public List<ReleaseWebHookStatus> getWebHookStatus() {
        return this.webHookStatus;
    }
}
