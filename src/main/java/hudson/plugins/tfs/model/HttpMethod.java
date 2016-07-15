package hudson.plugins.tfs.model;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.Charset;

public enum HttpMethod {
    GET {
        @Override
        public void sendRequest(final HttpURLConnection connection, final String body) throws IOException{
            // do nothing for GET
        }
    },
    POST,
    HEAD {
        @Override
        public void sendRequest(final HttpURLConnection connection, final String body) throws IOException{
            // do nothing for HEAD
        }
    },
    PATCH,
    OPTIONS,
    PUT,
    DELETE,
    TRACE,
    ;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public void sendRequest(final HttpURLConnection connection, final String body) throws IOException {
        // https://www.visualstudio.com/en-us/docs/integrate/get-started/rest/basics#http-method-override
        try {
            connection.setRequestMethod("POST");
        }
        catch (final ProtocolException e) {
            // shouldn't happen
            throw new Error(e);
        }
        connection.setRequestProperty("X-HTTP-Method-Override", this.name());

        if (body != null) {
            final byte[] bytes = body.getBytes(UTF8);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF8");
            connection.setRequestProperty("Content-Length", Integer.toString(bytes.length, 10));
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream stream = null;
            try {
                stream = connection.getOutputStream();
                stream.write(bytes);
            }
            finally {
                IOUtils.closeQuietly(stream);
            }
        }
    }
}
