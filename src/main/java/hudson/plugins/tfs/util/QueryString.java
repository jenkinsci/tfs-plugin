// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See sibling License.txt file

package hudson.plugins.tfs.util;

import java.util.LinkedHashMap;

public class QueryString extends LinkedHashMap<String, String> {
    @Override
    public String toString() {
        return UriHelper.serializeParameters(this);
    }
}
