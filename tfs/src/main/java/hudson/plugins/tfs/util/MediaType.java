package hudson.plugins.tfs.util;

import java.nio.charset.Charset;

public class MediaType {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_JSON_UTF_8 = "application/json; charset=utf-8";
    public static final String APPLICATION_JSON_PATCH_JSON = "application/json-patch+json";
    public static final String APPLICATION_JSON_PATCH_JSON_UTF_8 = "application/json-patch+json; charset=utf-8";
    public static final String APPLICATION_ZIP = "application/zip";
    public static final String TEXT_PLAIN = "text/plain";
}
