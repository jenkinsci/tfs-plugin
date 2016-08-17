package hudson.plugins.tfs.model.servicehooks;

/**
 * Provides different formats of an event message
 */
public class FormattedEventMessage {
    private String text;
    private String html;
    private String markdown;

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(final String html) {
        this.html = html;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(final String markdown) {
        this.markdown = markdown;
    }
}
