package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.LabelChildOption;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.LabelResult;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.VersionControlLabel;
import com.microsoft.tfs.core.clients.versioncontrol.specs.LabelItemSpec;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;
import ms.tfs.versioncontrol.clientservices._03._LabelResult;
import ms.tfs.versioncontrol.clientservices._03._LabelResultStatus;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Mockito.when;

public class LabelCommandTest extends AbstractCallableCommandTest {

    @Test public void assertLogging() throws Exception {
        final LabelResult labelResult = new LabelResult(new _LabelResult("label", "scope", _LabelResultStatus.Created));
        final LabelResult[] labelResults = {labelResult};

        when(vcc.createLabel(
                Matchers.<VersionControlLabel>anyObject(),
                Matchers.<LabelItemSpec[]>anyObject(),
                Matchers.<LabelChildOption>anyObject())).thenReturn(labelResults);

        final LabelCommand command = new LabelCommand(server, "labelName", "hudson-createLabel-TFS2013", "$/project/path") {
            @Override
            public Server createServer() {
                return server;
            }
        };
        final Callable<Void, Exception> callable = command.getCallable();

        callable.call();

        assertLog(
                "Creating label 'labelName' on '$/project/path' as of the current version in workspace 'hudson-createLabel-TFS2013'...",
                "Created label 'labelName'."
        );
    }

    @Override protected AbstractCallableCommand createCommand(final ServerConfigurationProvider serverConfig) {
        return new LabelCommand(serverConfig, "labelName", "workspaceName", "$/projectPath");
    }
}
