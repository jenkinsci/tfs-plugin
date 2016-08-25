These notes are for release **5.1.0**.  Other releases and their notes can be found at the [tfs-plugin GitHub Releases](https://github.com/jenkinsci/tfs-plugin/releases) page.

* Major:
    * Link back to the TFS/Team Services build that triggered the Jenkins build, via pull request #114.
    * Display "pull request merged" event details in build summary & details, via pull request #117.
    * Associated work items link back to the Jenkins build, via pull request #118.
    * Add support for per-computer TFVC configuration folders, via pull request #119.
    * [[JENKINS-13663](https://issues.jenkins-ci.org/browse/JENKINS-13663)] The TFVC SCM can be used with collection credential pairs, via pull request #122.
    * Withstand all sorts of workspace-related mishaps, via pull request #125.
    * Improve support for proxy servers (including those that need authentication), via pull request #126.

* Minor:
    * Detect missing team project collection configuration, via pull request #111.
    * Reduce the chance of error with Team Services URLs, via pull request #110.
    * Improve validation of Collection URL, via pull request #127.

* Tools and infrastructure:
    * Upgrade TFS SDK to version 14.0.3, via pull request #115.
    * Remove unused endpoint variations, via pull request #112.
    * Switch to Jackson for events parsing, via pull request #116.
    * Improve construction of TeamFoundationServerScm, via pull request #120.
    * Use collection URI in payloads, if possible, via pull request #121.
