package hudson.plugins.tfs;

import hudson.Util;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.DateUtil;

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
        writer.println(String.format("\t\t<date>%s</date>", DateUtil.TFS_DATETIME_FORMATTER.get().format(changeSet.getDate())));
        if (Util.fixEmpty(changeSet.getDomain()) == null) {
            writer.println(String.format("\t\t<user>%s</user>", escapeForXml(changeSet.getUser())));
        } else {
            writer.println(String.format("\t\t<user>%s\\%s</user>", escapeForXml(changeSet.getDomain()), escapeForXml(changeSet.getUser())));
        }
        if (Util.fixEmpty(changeSet.getCheckedInBy()) != null) {
            writer.println(String.format("\t\t<checked_in_by_user>%s</checked_in_by_user>", escapeForXml(changeSet.getCheckedInBy())));
        }
        writer.println(String.format("\t\t<comment>%s</comment>", escapeForXml(changeSet.getComment())));
        if (changeSet.getItems().size() > 0) {
            writer.println("\t\t<items>");
            for (ChangeSet.Item item : changeSet.getItems()) {
                writer.println(String.format("\t\t\t<item action=\"%s\">%s</item>", escapeForXml(item.getAction()), escapeForXml(item.getPath())));
            }
            writer.println("\t\t</items>");
        }
    }

    /**
     * 
     * Converts the input in the way that it can be written to the XML.
     * Special characters are converted to XML understandable way.
     * 
     * @param object The object to be escaped.
     * @return Escaped string that can be written to XML.
     */
    private String escapeForXml(Object object)
    {
        if(object == null)
        {
            return null;
        }

        //Loop through and replace the special chars.
        String string = object.toString();
        int size = string.length();
        char ch;
        StringBuilder escapedString = new StringBuilder(size);
        for(int index = 0;index < size;index ++)
        {
            //Convert special chars.
            ch = string.charAt(index);
            switch(ch)
            {
                case '&'  : escapedString.append("&amp;");  break;
                case '<'  : escapedString.append("&lt;");   break;
                case '>'  : escapedString.append("&gt;");   break;
                case '\'' : escapedString.append("&apos;"); break;
                case '\"' : escapedString.append("&quot;");break;
                default:    escapedString.append(ch);
            }
        }

        return escapedString.toString();
    }
}
