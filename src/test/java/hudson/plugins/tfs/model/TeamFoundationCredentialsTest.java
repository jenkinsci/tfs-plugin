package hudson.plugins.tfs.model;

import static org.junit.Assert.*;

import org.junit.Test;


public class TeamFoundationCredentialsTest {

    @Test
    public void assertLoginString() {
        TeamFoundationCredentials cred = new TeamFoundationCredentials("user", "pass", "domain");
        assertEquals("user@domain,pass", cred.getLoginStr());
    }
}
