//CHECKSTYLE:OFF
package hudson.plugins.tfs.util;

public class StringHelper {

    public static boolean endsWithIgnoreCase(final String haystack, final String needle)
    {
        if (haystack == null)
            throw new IllegalArgumentException("Parameter 'haystack' is null.");
        if (needle == null)
            throw new IllegalArgumentException("Parameter 'needle' is null.");

        final int nl = needle.length();
        final int hl = haystack.length();
        if (nl == hl)
        {
            return haystack.equalsIgnoreCase(needle);
        }

        if (nl > hl)
        {
            return false;
        }

        // Inspired by https://stackoverflow.com/a/19154150/
        final int toffset = hl - nl;
        return haystack.regionMatches(true, toffset, needle, 0, nl);
    }

    public static boolean equal(final String a, final String b) {
        return innerEqual(a, b, false);
    }

    public static boolean equalIgnoringCase(final String a, final String b) {
        return innerEqual(a, b, true);
    }

    static boolean innerEqual(final String a, final String b, final boolean ignoreCase) {
        if (a == null) {
            return b == null;
        }
        if (b == null) {
            return false;
        }
        final int length = a.length();
        if (length != b.length()) {
            return false;
        }
        return a.regionMatches(ignoreCase, 0, b, 0, length);
    }

    public static String determineContentTypeWithoutCharset(final String contentType) {
        if (contentType == null) {
            return null;
        }
        final int indexOfSemicolon = contentType.indexOf(';');
        if (indexOfSemicolon != -1) {
            final String beforeCharset = contentType.substring(0, indexOfSemicolon);
            return beforeCharset.trim();
        }
        return contentType;
    }
}
