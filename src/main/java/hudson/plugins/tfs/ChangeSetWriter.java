package hudson.plugins.tfs;

import hudson.Util;
import hudson.plugins.tfs.model.ChangeSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Team Foundation change log writer.
 * 
 * @author Erik Ramfelt
 */
public class ChangeSetWriter {

    /**
     * Writes the list of change sets to the file
     * @param changeSets list of change sets
     * @param changelogFile file to write change sets to
     */
    public void write(List<ChangeSet> changeSets, File changelogFile) throws IOException {
        FileWriter writer = new FileWriter(changelogFile);
        try {
            write(changeSets, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * Writes the list of change sets to the writer
     * @param changeSets list of change sets
     * @param output output writer
     */    
    public void write(List<ChangeSet> changeSets, Writer output) {
        PrintWriter writer = new PrintWriter(output);
        
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<changelog>");
        
        for (ChangeSet changeSet : changeSets) {
            writer.println(String.format("\t<changeset version=\"%s\">", changeSet.getVersion()));
            write(changeSet, writer);
            writer.println("\t</changeset>");
        }
        
        writer.println("</changelog>");
        writer.flush();
    }

    private void write(ChangeSet changeSet, PrintWriter writer) {
        writer.println(String.format("\t\t<date>%s</date>", Util.XS_DATETIME_FORMATTER.format(changeSet.getDate())));
        if (Util.fixEmpty(changeSet.getDomain()) == null) {
            writer.println(String.format("\t\t<user>%s</user>", changeSet.getUser()));
        } else {
            writer.println(String.format("\t\t<user>%s\\%s</user>", changeSet.getDomain(), changeSet.getUser()));
        }
        writer.println(String.format("\t\t<comment>%s</comment>", changeSet.getComment()));
        if (changeSet.getItems().size() > 0) {
            writer.println("\t\t<items>");
            for (ChangeSet.Item item : changeSet.getItems()) {
                writer.println(String.format("\t\t\t<item action=\"%s\">%s</item>", item.getAction(), item.getPath()));
            }
            writer.println("\t\t</items>");
        }
    }
}
