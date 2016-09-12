// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See sibling License.txt file

package hudson.plugins.tfs.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class UriHelper {

    private static final Map<String, Integer> SCHEMES_TO_DEFAULT_PORTS;
    public static final String UTF_8 = "UTF-8";

    static {
        final Map<String, Integer> defaultPorts =
                new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        defaultPorts.put("ftp", 21);
        defaultPorts.put("ssh", 22);
        defaultPorts.put("http", 80);
        defaultPorts.put("https", 443);
        SCHEMES_TO_DEFAULT_PORTS = Collections.unmodifiableMap(defaultPorts);
    }

    /**
     * Compares two {@link URI} instances to determine if they are equivalent.
     * For example,
     * {@code HTTP://WWW.EXAMPLE.COM:80/}
     * and
     * {@code http://www.example.com}
     * are considered equivalent.
     * This method handles a few more cases than {@link URI#equals(Object)}, such that the scheme's
     * default port number will be considered, as will the default path for hosts.
     *
     * @param a the first URI
     * @param b the second URI
     * @return {@code true} if a and b represent the same resource; {@code false} otherwise.
     */
    public static boolean areSame(final URI a, final URI b) {
        if (a == null) {
            return b == null;
        }
        if (b == null) {
            return false;
        }

        if (!StringHelper.equalIgnoringCase(a.getScheme(), b.getScheme())) {
            return false;
        }

        if (!StringHelper.equalIgnoringCase(a.getHost(), b.getHost())) {
            return false;
        }

        final int aPort = normalizePort(a);
        final int bPort = normalizePort(b);
        if (aPort != bPort) {
            return false;
        }

        final String aPath = normalizePath(a);
        final String bPath = normalizePath(b);
        if (!StringHelper.equal(aPath, bPath)) {
            return false;
        }

        if (!StringHelper.equal(a.getQuery(), b.getQuery())) {
            return false;
        }

        if (!StringHelper.equal(a.getFragment(), b.getFragment())) {
            return false;
        }

        return true;
    }

    public static boolean areSameGitRepo(final URI a, final URI b) {
        return UriHelper.areSame(a, b);
    }

    static int normalizePort(final URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            final String scheme = uri.getScheme();
            if (scheme != null) {
                if (SCHEMES_TO_DEFAULT_PORTS.containsKey(scheme)) {
                    port = SCHEMES_TO_DEFAULT_PORTS.get(scheme);
                }
            }
        }
        return port;
    }

    static String normalizePath(final URI uri) {
        String path = uri.getPath();
        if (path == null) {
            path = "/";
        }
        else {
            if (!path.endsWith("/")) {
                path = path + "/";
            }
        }
        return path;
    }

    public static boolean hasPath(final URI uri) {
        final String path = uri.getPath();
        if (path != null) {
            if (path.length() > 0 && !path.equals("/")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWellFormedUriString(final String uriString) {
        try {
            new URI(uriString);
            return true;
        }
        catch (final URISyntaxException ignored) {
            return false;
        }
    }

    public static URI join(final URI collectionUri, final Object... components) {
        return join(collectionUri.toString(), components);
    }

    public static URI join(final String collectionUrl, final Object... components) {
        final StringBuilder sb = new StringBuilder(collectionUrl);
        final boolean baseEndedWithSlash = endsWithSlash(sb);

        boolean first = true;
        for (final Object component : components) {
            boolean hasSlash = false;
            if (component instanceof QueryString) {
                final QueryString queryString = (QueryString) component;
                if (first) {
                    if (!baseEndedWithSlash) {
                        sb.append('/');
                    }
                }
                sb.append("?");
                sb.append(queryString.toString());
                // a QueryString must be the last of the components
                break;
            }
            else {
                if (first) {
                    first = false;
                    if (!baseEndedWithSlash) {
                        sb.append('/');
                    }
                }
                else {
                    sb.append('/');
                }
                try {
                    final String encodedComponent = URLEncoder.encode(component.toString(), UTF_8);
                    sb.append(encodedComponent);
                }
                catch (final UnsupportedEncodingException e) {
                    throw new Error(e);
                }
            }
        }

        final String uriString = sb.toString();
        return URI.create(uriString);
    }

    static boolean endsWithSlash(final StringBuilder stringBuilder) {
        final int length = stringBuilder.length();
        return length > 0 && stringBuilder.charAt(length - 1) == '/';
    }

    public static String serializeParameters(final Map<String, String> parameters) {
        try {
            final StringBuilder sb = new StringBuilder();
            final Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, String> entry;
                String key;
                String encodedKey;
                String value;
                String encodedValue;

                entry = iterator.next();
                key = entry.getKey();
                encodedKey = URLEncoder.encode(key, UTF_8);
                sb.append(encodedKey);
                value = entry.getValue();
                if (value != null) {
                    encodedValue = URLEncoder.encode(value, UTF_8);
                    sb.append('=').append(encodedValue);
                }
                while (iterator.hasNext()) {
                    sb.append('&');
                    entry = iterator.next();
                    key = entry.getKey();
                    encodedKey = URLEncoder.encode(key, UTF_8);
                    sb.append(encodedKey);
                    value = entry.getValue();
                    if (value != null) {
                        encodedValue = URLEncoder.encode(value, UTF_8);
                        sb.append('=').append(encodedValue);
                    }
                }
            }
            return sb.toString();
        }
        catch (final UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
