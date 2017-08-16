These notes are for release **5.121.0**.
Other releases and their notes can be found at the [tfs-plugin GitHub Releases](https://github.com/jenkinsci/tfs-plugin/releases) page.
 
* Major:
    * Jenkins-40806 bug fix. Thanks to @varyvol for pull request #144.
    * Added Application Insights telemetry via pull request #163.
    * Added JenkinsEventNotifier to send Job Completion events back to TFS/VSTS via pull request #167.
* Minor:
    * Ensure TFS/Team Services build variables are added to Jenkins environment variables. Thanks to @jeffyoung for pull request #166.
    * Fix memory leak. Thanks to @holgercn for pull request #148.
    * Added FindBugs to build. Thanks to @varyvol for pull request #162 and #165.
    * Overwrite when getting TFVC files. Thanks to @pescuma for pull request #152.
    * Don't include cloaked paths in version history. Thanks to @pescuma for pull request #153.
    * Fix to TFS push trigger event. Thanks to @smile21prc for pull request #164.
    * Update docs for release 5.3.4. Thanks to @yacaovsnc for pull request #155.
    * Updates to readme. Thanks to @mosabua for pull request #156.
    * Fixes to .gitignore. Thanks to @mosabua for pull request #157.
    * Fixes to FunctionalTests. Thanks to @yacaovsnc for pull request #160.
    * Added CheckStyle to the build via pull request #168.
