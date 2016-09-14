These notes are for release **5.2.0**.  Other releases and their notes can be found at the [tfs-plugin GitHub Releases](https://github.com/jenkinsci/tfs-plugin/releases) page.

* Major:
    * Import Release Management post-build action.  Thanks to @vishnugms for pull request #132
    * Collect build results for an upstream TFS/Team Services build, via pull request #131
    * Fix TeamRestClient for TFS on Windows and improve "Test connection" button, via pull request #135
    * Fuzzy TFS/Team Services URL matching, via pull request #136
    * Automatic integration, via pull request #134
    * Update the documentation, especially around integration features, via pull request #137

* Minor:
    * Fix error when building URL using UriHelper#join, via pull request #129
    * Converted ERROR into NOTICE & improved wording, via pull request #130
    * Set the User-Agent header with plugin version, via pull request #133

* Tools and infrastructure:
    * Fix `hpi:run`, via pull request #124
