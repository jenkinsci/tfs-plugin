package hudson.plugins.tfs.util;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Secret;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

public class VstsRestClient {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    static String createAuthorization(final StandardUsernamePasswordCredentials credentials) {
        final String username = credentials.getUsername();
        final Secret secretPassword = credentials.getPassword();
        final String password = secretPassword.getPlainText();
        final String credPair = username + ":" + password;
        final byte[] credBytes = credPair.getBytes(UTF8);
        final String base64enc = DatatypeConverter.printBase64Binary(credBytes);
        final String result = "Basic " + base64enc;
        return result;
    }

}
