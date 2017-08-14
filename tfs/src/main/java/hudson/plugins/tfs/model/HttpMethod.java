//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.httpclient.methods.GetMethod;
import com.microsoft.tfs.core.httpclient.methods.HeadMethod;
import com.microsoft.tfs.core.httpclient.methods.PostMethod;
import com.microsoft.tfs.core.httpclient.methods.StringRequestEntity;
import hudson.plugins.tfs.util.MediaType;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

public enum HttpMethod {
    GET {
        @Override
        public com.microsoft.tfs.core.httpclient.HttpMethod createClientMethod(final String uri, final String body) {
            return new GetMethod(uri);
        }

    },
    POST,
    HEAD {
        @Override
        public com.microsoft.tfs.core.httpclient.HttpMethod createClientMethod(final String uri, final String body) {
            return new HeadMethod(uri);
        }

    },
    PATCH {
        @Override
        public com.microsoft.tfs.core.httpclient.HttpMethod createClientMethod(final String uri, final String body) {
            return innerCreateClientMethod(uri, body, MediaType.APPLICATION_JSON_PATCH_JSON);
        }

    },
    OPTIONS,
    PUT,
    DELETE,
    TRACE,
    ;

    public com.microsoft.tfs.core.httpclient.HttpMethod createClientMethod(final String uri, final String body) {
        return innerCreateClientMethod(uri, body, MediaType.APPLICATION_JSON);
    }

    PostMethod innerCreateClientMethod(final String uri, final String body, final String contentType) {
        final PostMethod method = new PostMethod(uri);
        // https://www.visualstudio.com/en-us/docs/integrate/get-started/rest/basics#http-method-override
        method.addRequestHeader("X-HTTP-Method-Override", this.name());
        final String charset = MediaType.UTF_8.toString();
        final StringRequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(body, contentType, charset);
        }
        catch (final UnsupportedEncodingException e) {
            // this shouldn't happen
            throw new Error(e);
        }
        method.setRequestEntity(requestEntity);
        return method;
    }

}
