Team Foundation Server plugin for Jenkins
============================
Copyright &copy; Erik Ramfelt, Olivier Dagenais, CloudBees, Inc. and others.
Licensed under [MIT Licence].
 
## Summary
This plugin integrates [Team Foundation Version Control] (also known as TFVC) and Git to Jenkins by connecting to Team Foundation Server (TFS) and Visual Studio Team Services (Team Services).

## Quick links
* The legacy [wiki] page on the Jenkins Confluence instance
* Build status of master and pull requests: [![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=plugins/tfs-plugin)](https://jenkins.ci.cloudbees.com/job/plugins/job/tfs-plugin) (thanks to [CloudBees]!)
* Issues are tracked by the [Jenkins JIRA]
* Download the latest release [from the Jenkins CDN](http://updates.jenkins-ci.org/latest/tfs.hpi) or [from the GitHub Releases page](https://github.com/jenkinsci/tfs-plugin/releases)

## What can you do with it?

That depends on which version control system you use in TFS/Team Services:

### Team Foundation Version Control

Allows you to use a TFVC repository as an SCM in Jenkins jobs. At the moment, this plugin supports:
* Retrieving read-only copies of files and folders from a TFVC repository.
* Polling a TFVC repository to automatically start builds when there are changes.
* Links from the Jenkins change sets to the TFVC repository web interface. _(Also known as a repository browser)_
* Creating a label in the TFVC repository

The plugin will automatically create a workspace in TFS/Team Services and map a work folder (in the Jenkins workspace) to it.


### Git

The TFS plug-in for Jenkins enhances the Git plug-in for Jenkins by adding some integration features:
* A push trigger, to request builds of specific commits in Git repositories without needing to schedule SCM polling
* A build step that adds a "build pending" status to the associated pull request and/or commit in TFS/Team Services
* A post-build action that add a "build completed" status to the associated pull request and/or commit in TFS/Team Services
* Some endpoints for TFS/Team Services to use to activate the integration.  Please refer to the [Jenkins with Visual Studio Team Services](https://www.visualstudio.com/en-us/docs/service-hooks/services/jenkins) page for instructions on configuring the integration.


# Supported versions

The following sub-sections list the various versions of software that were tested and are thus supported.  The plugin might work with other versions, they just haven't been tested.

## Team Foundation Server (TFS) / Visual Studio Team Services (Team Services)

The following table indicates compatibility and support for versions of TFS and Team Services.

> Version | Supported by the TFS plugin? | Mainstream Support End Date
> ------- | ------ | ---------------------------
> [Visual Studio Team Services] | :white_check_mark: | n/a
> Visual Studio Team Foundation Server 2015 | :white_check_mark: | [2020/10/13](https://support.microsoft.com/en-us/lifecycle?p1=18576)
> Microsoft Visual Studio Team Foundation Server 2013 | :white_check_mark: | [2019/04/09](https://support.microsoft.com/en-us/lifecycle?p1=17358)
> Microsoft Visual Studio Team Foundation Server 2012 | :white_check_mark: | [2018/01/09](https://support.microsoft.com/en-us/lifecycle?p1=16804)
> Microsoft Visual Studio Team Foundation Server 2010 | :x: | :warning: [2015/07/14](https://support.microsoft.com/en-us/lifecycle?p1=15011)
> Microsoft Visual Studio Team System 2008 Team Foundation Server | :x: | :warning: [2013/04/09](https://support.microsoft.com/en-us/lifecycle?p1=13083)
> Microsoft Visual Studio 2005 Team Foundation Server | :x: | :warning: [2011/07/12](https://support.microsoft.com/en-us/lifecycle?p1=10449)

## Operating Systems

The plugin has been tested against the following operating systems and versions, with the latest updates as of 2015/08/27.

Name | Version
---- | -------
Windows Server | 2012 R2
Mac OS X | Yosemite 10.10.5
Ubuntu Linux | Server 14.04 LTS

## Jenkins

The plugin is built against Jenkins version **1.580** and that's the version integration tests are run against.

# Configuration

## Requirements

### 4.0.0 and later (New!)

Ever since release 4.0.0, a command-line client or tool is no longer necessary as all the interaction with TFS or Team Services is done using the [TFS SDK for Java].  The native libraries needed by the SDK are automatically copied to a sub-directory under the agent user's home folder.

### 3.2.0 and earlier

Versions 3.2.0 and earlier of the plugin required a command line tool to be installed on the build agents to retrieve source code from the TFVC repository.

1. Install either Microsoft Visual Studio or [Microsoft Team Explorer Everywhere] Command-Line Client (CLC) on the build agents
2. Add `tf.exe` (Visual Studio) OR one of `tf.cmd` or `tf` (TEE CLC) to the `PATH` of the build agents' user(s).

## Global configuration

To make use of the Git integration with TFS/Team Services and/or to use automatic credentials configuration with the TFVC SCM, it is necessary to first configure your team project collection(s).  Follow these instructions for each team project collection (most organizations will only have one).

1. Add credentials:
    1. Select **Jenkins** > **Credentials**
    2. Select **Add domain**
        1. In the _Domain Name_ field, enter the host's friendly name, such as `fabrikam-fiber-inc`
        2. In the _Description_ field, you can enter some notes, such as who maintains the server, etc.
        3. Next to _Specification_, select **Add** > **Hostname**
            1. In the _Include_ field, enter the Fully-Qualified Domain Name (FQDN), such as `fabrikam-fiber-inc.visualstudio.com`
        4. Click **OK**
    3. Select **Add Credentials**
        1. For the _Kind_ field, select **Username with password**
        2. For the _Scope_ field, select **Global (Jenkins, nodes, items, all child items, etc)**
        3. See the _User name and password_ section below for the values of the _Username_ and _Password_; a Personal Access Token (PAT) is strongly recommended.  If the credentials will be used for TFVC, select **All scopes**, otherwise select the following _Authorized Scopes_:
            1. `Code (read)`
            2. `Code (status)`
        4. You can use the _Description_ field to record details about the PAT, such as its intended collection, the selected authorization scopes and expiration date.  For example: `fabrikam-fiber-inc, code read+status, expires 2017-08-05`
        5. Click **OK**   
2. Add the collection URL and associate it with the right credential:
    1. Select **Jenkins** > **Manage Jenkins** > **Configure System**
    2. Scroll to **TFS/Team Services** and click **Add**
        1. If using Team Services, the value of the _Collection URL_ field should omit `/DefaultCollection`.
        2. Select the associated `Credentials` value created earlier.
        3. Click **Test Connection**.
    3. Click **Save**

### Advanced

In some environments, the "home" directory is mounted over a network and shared between many computers, including Jenkins servers and their associated build nodes, which eventually leads to corruption of the configuration directory used for TFVC workspaces.  If you have such an environment, check the box next to **Store TFVC configuration in computer-specific folders** to use a sub-directory for each computer. :warning: WARNING :warning: Turning this on is equivalent to setting the `TEE_PROFILE_DIRECTORY` environment variable and thus any manual operations performed using the Command-Line Client (CLC) will need to be performed with the `TEE_PROFILE_DIRECTORY` environment variable set accordingly.

## Job configuration

### Team Foundation Version Control

If your source code is in a TFVC repository, this section is for you.

![SCM configuration](tfs-job-config4.png)

Field | Description
----- | -----------
`Collection URL` | The URL to the [Team Project Collection](https://msdn.microsoft.com/en-us/library/dd236915(v=vs.120).aspx). If you added your team project collection(s) in the global configuration, the field will show you a list to pick from. Examples: `https://tfs02.codeplex.com`, `https://fabrikam-fiber-inc.visualstudio.com`, `http://tfs:8080/tfs/DefaultCollection`
`Project path` | The Team Project and path to retrieve from the server. The project path must start with `$/`, and contain any sub path that exists in the project repository. Example: `$/Fabrikam-Fiber-TFVC/AuthSample-dev`
`Credentials` | If you added your team project collection(s) in the global configuration, select **Automatic** and the credentials will be looked up automatically, otherwise you can select **Manual** and configure the `User name` and `User password` fields.
`Manual` > `User name` | The name of the user that will be connecting to TFS/Team Services to query history, checkout files, etc. See _User name and password_ below for a full description.
`Manual` > `User password` | The password, alternate password or personal access token associated with the user. See _User name and password_ below for more details.
`Use update` | If this option is checked, then the workspace will not be deleted and re-created at the start of each build, making the build faster, but this causes the artifacts from the previous build to remain when a new build starts.
`Local workfolder` | The name of the local work folder. The specified folder will contain the files retrieved from the repository. Default is `.`, ie the files will be downloaded into the Hudson workspace folder.
`Workspace name` | The name of the workspace that Jenkins should use when creating and deleting workspaces on the server. The workspace name supports three macros; `${JOB_NAME}` is replaced by the job name, `${USER_NAME}` is replaced by the user name Jenkins is running as and `${NODE_NAME}` is replaced by the name of the node. Default workspace name is `Hudson-${JOB_NAME}-${NODE_NAME}`.
`Cloaked paths` | A collection of server paths to cloak to exclude from the workspace and from the build trigger. Multiple entries must be placed onto separate lines.
`Repository browser` | Select `Microsoft Team Foundation Server/Visual Studio Team Services` to turn on links inside Jenkins jobs (in the **Changes** page) back to TFS/Team Services, for easier traceability.  If the TFS server is reached by users through a different URL than that provided in `Collection URL`, such as the Fully-Qualified Domain Name (FQDN), provide a value for the `URL` sub-field.

### Git

If your source code is in a Git repository located on a TFS/Team Services server, this section is for you.  **Make sure you first followed the instructions in "Global configuration" and added your team project collections, associated with credentials.**

![Git configuration](git-job-config.png)

If you didn't have the Git plug-in for Jenkins already, installing the TFS plug-in for Jenkins should have brought it on as a dependency.

1. Use the **Git** _Source Code Management_ and add the URL to your Git repository in TFS/Team Services, omitting the `/DefaultCollection` if you are using Team Services.
2. If you haven't done so already, follow the instructions in the "User name and password" section to generate a Personal Access Token, and then add a "Credential" as specified in the "Global configuration" section.  You should then be able to select it in the _Credentials_ field.
3. To be able to build the merge commits created for pull requests in TFS/Team Services, click the **Advanced...** button
    1. In the _Name_ field, enter **origin** (or some unique name if you already have other repositories)
    2. In the _Refspec_ field, enter `+refs/heads/*:refs/remotes/origin/* +refs/pull/*:refs/remotes/origin-pull/*` (replacing "origin" as necessary)
4. Scroll down to _Build Triggers_ and you can now check the **Build when a change is pushed to TFS/Team Services** checkbox.
5. Scroll down to _Build_, select **Add build step** > **Set build pending status in TFS/Team Services**, moving it _first_ in the list of steps, to notify TFS/Team Services as early as possible that a Jenkins build has been started.
6. Add other build steps, as necessary. 
7. Scroll down to _Post-build Actions_, select **Add post-build action** > **Set build completion status in TFS/Team Services**.

### User name and password

#### Team Foundation Server (on-premises)

For \[on-premises\] Team Foundation Server, the _User name_ can be specified in two ways:

1. `EXAMPLE-DOMAIN\user`
2. `user@domain.example.com`

#### Visual Studio Team Services (Team Services, previously known as Visual Studio Online)

For Team Services, there are also two options:

1. Personal access tokens (recommended)
    1. In Team Services, click your name in the top right corner and select **Security**.
    2. In the _Personal access tokens_ area, select **Add**.
    3. Describe the token (use something like "Jenkins server at jenkins.example.com"), select an expiry timeframe, double-check the Team Services account the token will be valid for and, if the user account will be used for TFVC, select **All scopes** otherwise you can select smaller scopes based on what features you will need.
    4. Click **\[Create Token\]** and copy the generated personal access token to the clipboard.
    5. Back to Jenkins, enter the e-mail address associated with your Team Services account as the _User name_ and the generated personal access token as the _User password_.
2.  Alternate credentials
    1. In Team Services, click your name in the top right corner and select **Security**.
    2. In the _Alternate credentials_ area, select **Enable alternate authentication credentials**.
    3. Enter a secondary user name and password, then click **\[Save\]**.
    4. Back to Jenkins, re-enter those credentials in the _User name_ and _User password_ fields.


## Checkout by label (New since version 3.2.0)

The plugin now supports checking out from a specific label or any valid [versionspec](https://www.visualstudio.com/docs/tfvc/use-team-foundation-version-control-commands#use-a-versionspec-argument-to-specify-affected-versions-of-items).  Here's how to configure a job to do that:

> :information_source: Polling the server doesn't make sense when you want to build for a specific label because polling is not \[currently\] label-aware and could queue a build **every polling interval**. :information_source:

1. Turn **off** SCM polling by making sure the **Poll SCM** checkbox is _cleared_ (unchecked).
2. Tick the **This build is parameterised** checkbox
    1. Add a **String Parameter**
    2. Set its _Name_ to **VERSION_SPEC**
    3. Set its _Description_ to the following:
    ```
    Enter a valid version spec to use when checking out.
    Labels are prefixed with "L" and changesets are prefixed with "C".
    See the following for a versionspec reference: https://www.visualstudio.com/docs/tfvc/use-team-foundation-version-control-commands#use-a-versionspec-argument-to-specify-affected-versions-of-items
    Examples: "LFoo", "C42"
    ```
3. **Save** the job.

Now, the next time you want to queue a build, you will need to provide a value for the **VERSION_SPEC** parameter.  The build will then perform a checkout of the source as of the specified **VERSION_SPEC**.

## Proxy server support (New since version 4.1.0)

In the event Jenkins is deployed on a network with no direct access to other networks (such as the internet), the TFS plugin now supports connecting through proxy servers.

> :information_source: Support for proxy servers requiring authentication was added in version 5.1.0. :information_source:

Follow the instructions at [JenkinsBehindProxy](https://wiki.jenkins-ci.org/display/JENKINS/JenkinsBehindProxy) to configure Jenkins' use of a proxy server, which the TFS plugin also uses.


## Build environment variables

The plugin will set the following environment variables for the build, after a checkout:

* **TFS_WORKSPACE** \- The name of the workspace.
* **TFS_WORKFOLDER** \- The full path to the working folder.
* **TFS_PROJECTPATH** \- The TFVC project path that is mapped to the workspace.
* **TFS_SERVERURL** \- The URL to the Team Project Collection.
* **TFS_USERNAME** \- The user name that is used to connect to TFS/Team Services.
* **TFS_CHANGESET** \- The change set number that is checked out in the workspace


# FAQ

### How should I set up the plugin for my CodePlex project?

* Find out the server for your project, which is displayed in the source code page for your project at codeplex.com.
* The user name must be suffixed with `_cp` and the domain is `snd`. If your user name is redsolo, then enter "`snd\redsolo_cp`" as the user name in the plugin configuration.
* Note that the user must be a member of the project to be able to create a workspace on the CodePlex server.

That's all you need to do to start retrieving files from your project at codeplex.com.

### The plugin is having problems parsing the dates that TF outputs, what can I do?

> :information_source: If you can upgrade to version 4 and up, then you can avoid a whole class of TF output parsing difficulties, otherwise, read on. :information_source:

The TF command line outputs date according to the locale and Microsofts own specification. Sometimes the outputed date can not be parsed by any of the default locale dependent parsers that the JDK includes (_for some more details, see_ _[JENKINS-4184]_ _and_ _[JENKINS-4021]_). This will throw an exception in the change set parsing and fail the build.

To fix this, do the following:
* Change the locale by Windows Regional Settings to United States and English on the server and all hudson nodes. After that tf.exe should output dates in english, which can be parsed properly.
* Start Hudson using the UnitedStates, English locale. Either set it using `-Duser.language=en -Duser.country=US` on the command line or check the documentation for the container that Hudson is running within.

# Timeline

## Future

The best way to get an idea of what will be coming in future releases is to look at the [list of open pull requests](https://github.com/jenkinsci/tfs-plugin/pulls).

## Present

The next release will be 5.0.0.  See what's been committed [since 4.1.0](https://github.com/jenkinsci/tfs-plugin/compare/tfs-4.1.0...master) and the upcoming [ReleaseNotes.md](ReleaseNotes.md).

## Past

Details about previous releases can be found on the [Releases page](https://github.com/jenkinsci/tfs-plugin/releases).

[wiki]: http://wiki.jenkins-ci.org/display/JENKINS/Team+Foundation+Server+Plugin
[MIT Licence]: http://opensource.org/licenses/MIT
[CloudBees]: https://www.cloudbees.com/
[Jenkins JIRA]: http://issues.jenkins-ci.org/secure/IssueNavigator.jspa?mode=hide&reset=true&jqlQuery=project+%3D+JENKINS+AND+status+in+%28Open%2C+%22In+Progress%22%2C+Reopened%29+AND+component+%3D+%27tfs-plugin%27
[Team Foundation Version Control]: https://msdn.microsoft.com/en-us/library/ms181237.aspx
[Visual Studio Team Services]: https://www.visualstudio.com/products/visual-studio-team-services-vs
[TFS SDK for Java]: http://blogs.msdn.com/b/bharry/archive/2011/05/16/announcing-a-java-sdk-for-tfs.aspx
[Microsoft Team Explorer Everywhere]: http://www.microsoft.com/en-us/download/details.aspx?id=40785
[JENKINS-4021]: https://issues.jenkins-ci.org/browse/JENKINS-4021
[JENKINS-4184]: https://issues.jenkins-ci.org/browse/JENKINS-4184
