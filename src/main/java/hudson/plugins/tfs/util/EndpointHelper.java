package hudson.plugins.tfs.util;

import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

public class EndpointHelper {

    public static void error(final int code, final Throwable cause) {
        throw new HttpResponses.HttpResponseException(cause) {
            public void generateResponse(final StaplerRequest req, final StaplerResponse rsp, final Object node) throws IOException, ServletException {
                rsp.setStatus(code);
                rsp.setHeader("X-Error-Message", cause.getMessage());
                rsp.setContentType("text/plain;charset=UTF-8");

                final PrintWriter w = new PrintWriter(rsp.getWriter());
                // TODO: serialize "cause" to JSON write that, instead
                cause.printStackTrace(w);
                w.close();
            }
        };
    }

}
