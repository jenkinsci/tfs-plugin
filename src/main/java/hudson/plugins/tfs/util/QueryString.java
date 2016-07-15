// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See sibling License.txt file

package hudson.plugins.tfs.util;

import java.util.LinkedHashMap;

public class QueryString extends LinkedHashMap<String, String> {

    public QueryString() {
    }

    public QueryString(final String... nameValuePairs) {
        final int length = nameValuePairs.length;
        if (length % 2 != 0) {
            final String message = "This QueryString constructor needs an even number of parameters";
            throw new IllegalArgumentException(message);
        }
        for (int i = 0; i < length; i+=2) {
            put(nameValuePairs[i], nameValuePairs[i + 1]);
        }
    }

    @Override
    public String toString() {
        return UriHelper.serializeParameters(this);
    }
}
