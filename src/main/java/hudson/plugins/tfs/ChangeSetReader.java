package hudson.plugins.tfs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import hudson.model.AbstractBuild;
import hudson.plugins.tfs.model.TeamFoundationChangeLogSet;
import hudson.plugins.tfs.model.TeamFoundationChangeSet;
import hudson.scm.ChangeLogParser;
import hudson.util.Digester2;

/**
 * TeamFoundation change log reader.
 * 
 * @author Erik Ramfelt
 */ 
public class ChangeSetReader extends ChangeLogParser {

    @Override
    public TeamFoundationChangeLogSet parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        FileReader reader = new FileReader(changelogFile);
        TeamFoundationChangeLogSet logSet = parse(build, reader);
        reader.close();
        return logSet;
    }

    public TeamFoundationChangeLogSet parse(AbstractBuild<?,?> build, Reader reader) throws IOException, SAXException {
        List<TeamFoundationChangeSet> changesetList = new ArrayList<TeamFoundationChangeSet>();
        Digester digester = new Digester2();
        digester.push(changesetList);

        digester.addObjectCreate("*/changeset", TeamFoundationChangeSet.class);
        digester.addSetProperties("*/changeset");
        digester.addBeanPropertySetter("*/changeset/date", "dateStr");
        digester.addBeanPropertySetter("*/changeset/user");
        digester.addBeanPropertySetter("*/changeset/comment");
        digester.addSetNext("*/changeset", "add");

        digester.addObjectCreate("*/changeset/items/item", TeamFoundationChangeSet.Item.class);
        digester.addSetProperties("*/changeset/items/item");
        digester.addBeanPropertySetter("*/changeset/items/item", "path");
        digester.addSetNext("*/changeset/items/item", "add");
        
        digester.parse(reader);

        return new TeamFoundationChangeLogSet(build, changesetList);
    }
}
