package hudson.plugins.tfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import hudson.model.Run;
import hudson.scm.RepositoryBrowser;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import hudson.plugins.tfs.model.ChangeLogSet;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.scm.ChangeLogParser;
import hudson.util.Digester2;

/**
 * TeamFoundation change log reader.
 *
 * @author Erik Ramfelt
 */
public class ChangeSetReader extends ChangeLogParser {

    @Override
    public ChangeLogSet parse(final Run build, final RepositoryBrowser<?> browser, final File changelogFile) throws IOException, SAXException {
        try (FileInputStream stream = new FileInputStream(changelogFile); Reader reader = new InputStreamReader(stream, Charset.defaultCharset())) {
            return parse(build, browser, reader);
        }
    }

    /** Performs the actual parsing. */
    public ChangeLogSet parse(final Run build, final RepositoryBrowser<?> browser, final Reader reader) throws IOException, SAXException {
        List<ChangeSet> changesetList = new ArrayList<ChangeSet>();
        Digester digester = new Digester2();
        digester.push(changesetList);

        digester.addObjectCreate("*/changeset", ChangeSet.class);
        digester.addSetProperties("*/changeset");
        digester.addBeanPropertySetter("*/changeset/date", "dateStr");
        digester.addBeanPropertySetter("*/changeset/user");
        digester.addBeanPropertySetter("*/changeset/checked_in_by_user", "checkedInBy");
        digester.addBeanPropertySetter("*/changeset/comment");
        digester.addSetNext("*/changeset", "add");

        digester.addObjectCreate("*/changeset/items/item", ChangeSet.Item.class);
        digester.addSetProperties("*/changeset/items/item");
        digester.addBeanPropertySetter("*/changeset/items/item", "path");
        digester.addSetNext("*/changeset/items/item", "add");

        digester.parse(reader);

        return new ChangeLogSet(build, browser, changesetList);
    }
}
