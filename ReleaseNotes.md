These notes are for release **5.157.1**.
Other releases and their notes can be found at the [tfs-plugin GitHub Releases](https://github.com/jenkinsci/tfs-plugin/releases) page.
 
* Minor:
    * BugFix: There was an issue in enabling RM/Pipeline webhook for projects under a folder. Now using full name of the project to ensure webhook works.
    * BugFix: Encoding the event payload to UTF-8 before publishing the RM/Pipeline webhook event. This is because the payload checksum is calculated after converting the payload to UTF-8.
    
