These notes are for release **5.0.0**.  Other releases and their notes can be found at the [tfs-plugin GitHub Releases](https://github.com/jenkinsci/tfs-plugin/releases) page.

* Major:
    * Integration with Git repositories hosted on TFS/Team Services, via pull requests #86, #87, #88, #89, #90, #91, #92, #93, #96, #97, #98, #99, #100, #101, #103, #105, #104, #102, #108, #107, #109.
    * JENKINS-30330: Only request workspaces for the current computer, via pull request #95.
    * JENKINS-34446: Fix bug - TFS PLUGIN environnement variables in project path.  Thanks to @sylvainmouquet for pull request #75.
    * Deactivate logging of each resource get in build log.  Thanks to @mosabua for pull request #106.

* Minor:
    * Updated repo browser label.  Thanks to @mosabua for pull request #74.
    * Fix Changeset number display for Delivery Pipeline.  Thanks to @pskumar448 for pull request #76, ultimately merged via pull request #78.
    * Implemented getAffectedFiles in ChangeSet.  Thanks to @drphrozen for pull request #77.
    * Cloaked paths should be case insensitive when polling for changes.  Thanks to @watsonlu for pull request #79.
    * Work around TFS SDK defect when re-creating an older workspace.  Made possible with pull request #80.
    * Automatically configure used webbrowser access. Thanks to @mosabua for pull request #84.
    * Clarify the plugin's SCM is for Team Foundation Version Control (TFVC), via pull request #94. 

* Tools and infrastructure:
    * Upgrade Jenkins dependencies to 1.580, via pull request #81.
    * Add David to the list of developers in pom.xml, via pull request #82.
    * changed language level check to use 1.6.  Thanks to @mosabua for pull request #83.
    * Test setup improvements.  Thanks to @mosabua for pull request #85.

* Special thanks go out to:
    * @DavidStaheli for all the code reviews and the end-to-end testing.
    * @yacaovsnc for carefully reviewing and testing the documentation.
    * @mosabua for running tests on a representative environment.
    * @AndreyAlifanov for helping test pull request #95.
    * @jasholl and @AlexRukhlin for testing and validating the endpoints.
