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
