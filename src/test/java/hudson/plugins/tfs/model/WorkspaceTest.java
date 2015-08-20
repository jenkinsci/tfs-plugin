package hudson.plugins.tfs.model;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class WorkspaceTest {

    @Mock private Server server; 
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
}
