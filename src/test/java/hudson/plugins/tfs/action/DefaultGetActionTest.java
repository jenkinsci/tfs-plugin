package hudson.plugins.tfs.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
import org.mockito.AdditionalMatchers;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;


public class DefaultGetActionTest {

    @Test
    public void assertCheckoutOnACleanWorkspace() {
        DefaultGetAction action = new DefaultGetAction();
    }
}
