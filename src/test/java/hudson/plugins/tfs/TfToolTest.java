package hudson.plugins.tfs;

import org.hamcrest.Description;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.*;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

import java.util.Arrays;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TfToolTest {
    private FilePath workspace;
    @Mock private Launcher launcher;
    @Mock private Proc proc;
    @Mock private TaskListener taskListener;
    
    private TfTool tool;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        workspace = Util.createTempFilePath();
        tool = new TfTool("tf", launcher, taskListener, workspace);
    }
    
    @After
    public void teardown() throws Exception {
        workspace.deleteRecursive();
    }

    @Test(expected=AbortException.class)
    public void assertUnexpectReturnCodeThrowsAbortException() throws Exception {
        when(launcher.launch(isA(Launcher.ProcStarter.class))).thenReturn(proc);
        when(proc.join()).thenReturn(100);

        tool.execute(new String[]{"history"});
    }

    @Test
    public void assertSuccessReturnCodeDoesNotThrowAbortException() throws Exception {
        when(launcher.launch(isA(Launcher.ProcStarter.class))).thenReturn(proc);
        when(proc.join()).thenReturn(TfTool.SUCCESS_EXIT_CODE);

        tool.execute(new String[]{"history"});
    }

    @Test
    public void assertPartialSuccessReturnCodeDoesNotThrowAbortException() throws Exception {
        when(launcher.launch(isA(Launcher.ProcStarter.class))).thenReturn(proc);
        when(proc.join()).thenReturn(TfTool.PARTIAL_SUCCESS_EXIT_CODE);

        tool.execute(new String[]{"history"});
    }

    @Test
    public void assertExecutableReturnsWithReader() throws Exception {
        when(launcher.launch(isA(Launcher.ProcStarter.class))).thenReturn(proc);

        Reader reader = tool.execute(new String[]{"history"});
        assertNotNull("Reader should not be null", reader);
        
        verify(launcher).launch(argThat(
                new ProcStarterMatcher(Arrays.asList("tf", "history"), new boolean[]{false, false})));
    }

    @Test
    public void assertMaskedCommands() throws Exception {
        when(launcher.launch(isA(Launcher.ProcStarter.class))).thenReturn(proc);

        tool.execute(new String[]{"history"}, new boolean[]{true});
        
        verify(launcher).launch(argThat(
                new ProcStarterMatcher(Arrays.asList("tf", "history"), new boolean[]{false, true})));
    }
    
    @Test
    public void assertGetListenerReturnsSameListenerSuppliedInConstructor() {
        assertSame("The listener was not the same as supplied to the tool", taskListener, tool.getListener());
    }

    private static class ProcStarterMatcher extends BaseMatcher<Launcher.ProcStarter> {
        private List<String> cmds;
        private boolean[] masks;
        private ProcStarterMatcher(List<String> cmds, boolean[] masks) {
            this.cmds = cmds;
            this.masks = masks;
        }
        public boolean matches(Object item) {
            if (!(item instanceof Launcher.ProcStarter)) return false;
            Launcher.ProcStarter ps = (Launcher.ProcStarter)item;
            return cmds.equals(ps.cmds()) && Arrays.equals(masks, ps.masks());
        }
        public void describeTo(Description description) { }
    }
}
