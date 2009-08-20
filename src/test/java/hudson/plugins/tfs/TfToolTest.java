package hudson.plugins.tfs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.*;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

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
        when(proc.join()).thenReturn(100);
        when(launcher.launch(isA(String[].class), isA(boolean[].class), isA(String[].class), (InputStream) isNull(), isA(OutputStream.class), isA(FilePath.class))).thenReturn(proc);

        tool.execute(new String[]{"history"});
    }

    @Test
    public void assertSuccessReturnCodeDoesNotThrowAbortException() throws Exception {
        when(proc.join()).thenReturn(TfTool.SUCCESS_EXIT_CODE);
        when(launcher.launch(isA(String[].class), isA(boolean[].class), isA(String[].class), (InputStream) isNull(), isA(OutputStream.class), isA(FilePath.class))).thenReturn(proc);

        tool.execute(new String[]{"history"});
    }

    @Test
    public void assertPartialSuccessReturnCodeDoesNotThrowAbortException() throws Exception {
        when(proc.join()).thenReturn(TfTool.PARTIAL_SUCCESS_EXIT_CODE);
        when(launcher.launch(isA(String[].class), isA(boolean[].class), isA(String[].class), (InputStream) isNull(), isA(OutputStream.class), isA(FilePath.class))).thenReturn(proc);

        tool.execute(new String[]{"history"});
    }

    @Test
    public void assertExecutableReturnsWithReader() throws Exception {
        when(launcher.launch(isA(String[].class), isA(boolean[].class), isA(String[].class), (InputStream) isNull(), isA(OutputStream.class), isA(FilePath.class))).thenReturn(proc);

        Reader reader = tool.execute(new String[]{"history"});
        assertNotNull("Reader should not be null", reader);
        
        verify(launcher).launch(aryEq(new String[]{"tf", "history"}), aryEq(new boolean[]{false, false}), (String[])anyObject(), (InputStream)anyObject(), (OutputStream)anyObject(), (FilePath)anyObject());
    }

    @Test
    public void assertMaskedCommands() throws Exception {
        when(launcher.launch(isA(String[].class), isA(boolean[].class), isA(String[].class), (InputStream) isNull(), isA(OutputStream.class), isA(FilePath.class))).thenReturn(proc);

        tool.execute(new String[]{"history"}, new boolean[]{true});
        
        verify(launcher).launch(aryEq(new String[]{"tf", "history"}), aryEq(new boolean[]{false, true}), (String[])anyObject(), (InputStream)anyObject(), (OutputStream)anyObject(), (FilePath)anyObject());
    }
    
    @Test
    public void assertGetListenerReturnsSameListenerSuppliedInConstructor() {
        assertSame("The listener was not the same as supplied to the tool", taskListener, tool.getListener());
    }
}
