Team Foundation Server plugin for Jenkins
============================
Copyright &copy; Erik Ramfelt, Olivier Dagenais, CloudBees, Inc. and others.
Licensed under [MIT Licence].
 
## Summary
This plugin integrates [Team Foundation Version Control], also known as TFVC, to Jenkins by connecting to Team Foundation Server (TFS) and Visual Studio Team Services (VSTS).

## Quick links
* The legacy [wiki] page on the Jenkins Confluence instance
* Build status of master and pull requests: [![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=plugins/tfs-plugin)](https://jenkins.ci.cloudbees.com/job/plugins/job/tfs-plugin) (thanks to [CloudBees]!)
* Issues are tracked by the [Jenkins JIRA]
* Download the latest release [from the Jenkins CDN](http://updates.jenkins-ci.org/latest/tfs.hpi) or [from the GitHub Releases page](https://github.com/jenkinsci/tfs-plugin/releases)

## What can you do with it?

Allows you to use TFS and VSTS as an SCM in Jenkins jobs. At the moment, this plugin supports:
* Retrieving read-only copies of files and folders from TFS/VSTS.
* Polling TFS/VSTS to automatically start builds when there are changes.
* Links from the Jenkins change sets to the TFS/VSTS web interface. _(Also known as a repository browser)_
* Creating a label in TFS

The plugin will automatically create a workspace in TFS/VSTS and map a work folder (in the Jenkins workspace) to it.

[wiki]: http://wiki.jenkins-ci.org/display/JENKINS/Team+Foundation+Server+Plugin
[MIT Licence]: http://opensource.org/licenses/MIT
[CloudBees]: https://www.cloudbees.com/
[Jenkins JIRA]: http://issues.jenkins-ci.org/secure/IssueNavigator.jspa?mode=hide&reset=true&jqlQuery=project+%3D+JENKINS+AND+status+in+%28Open%2C+%22In+Progress%22%2C+Reopened%29+AND+component+%3D+%27tfs-plugin%27
[Team Foundation Version Control]: https://msdn.microsoft.com/en-us/library/ms181237.aspx
