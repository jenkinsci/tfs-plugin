These notes are for release **5.3.0**.  Other releases and their notes can be found at the [tfs-plugin GitHub Releases](https://github.com/jenkinsci/tfs-plugin/releases) page.

* Major:
    * Add a global configuration option to select the user account name mapping strategy.  Thanks to @smalik86 and @smoyen for helping design and test the changes in pull request #140.
    * Fix exception when invoked from TFS Release Management (JENKINS-40283). Thanks to @DavidStaheli for pull request #142.
    * JENKINS-40155: Fix event's scope deserialization, via pull request #143.
    * Pushes without commits are ignored.  Thanks to @CSchulz, @cniweb and @DavidStaheli for their help in fixing this defect via pull request #141.

* Minor:
    * Clarified documentation on SCM trigger configuration.  Thanks to @CSchulz for pull request #139.
