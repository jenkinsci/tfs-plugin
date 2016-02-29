These notes are for release **4.1.0**.  Other releases and their notes can be found at the [tfs-plugin GitHub Releases](https://github.com/jenkinsci/tfs-plugin/releases) page.

* Major:
    * Add workspace cloaking. Thanks to Aaron Alexander (@ajalexander) and Luke Watson (@watsonlu) who helped implement [JENKINS-4709](https://issues.jenkins-ci.org/browse/JENKINS-4709). Made possible through the following pull requests: #28, #60, #64 and #68.
    * Support connecting through a proxy server. Thanks to Yorick Bosman (@ytterx) for fixing [JENKINS-6933](https://issues.jenkins-ci.org/browse/JENKINS-6933).  Made possible through pull requests #66 and #72.
* Minor:
    * Fix VSTS workspace management by upgrading to TFS SDK to 14.0.2 and better detection of existing workspaces. (pull request #67)
    * Fix repository browser URL persistence. Thanks to Manfred Moser (@mosabua) who fixed [JENKINS-30703](https://issues.jenkins-ci.org/browse/JENKINS-30703) with pull request #69.
