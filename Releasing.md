Pre-requisites
==============

1. Machine: preferably Linux over Windows, to avoid any weirdness between Cygwin and Git for Windows
    1. Oracle JDK 8 (see note in [Installing Jenkins on Red Hat distributions](https://wiki.jenkins-ci.org/display/JENKINS/Installing+Jenkins+on+Red+Hat+distributions) about CentOS's default Java)
    1. Maven 3.2 or better
    1. A recent enough Git
    1. Make sure the `COMPUTERNAME` environment variable is defined, as the end-to-end tests rely on its presence.  One can use the [EnvInject plugin](https://wiki.jenkins-ci.org/display/JENKINS/EnvInject+Plugin) to set it during the execution of the release job.
1. A GitHub clone you can pull from and push to non-interactively. (Consider configuring GitHub with a public key and use the SSH protocol for everything)
1. A "Jenkins infrastructure" account.  They have some sort of LDAP server that provides SSO for JIRA, Confluence and Artifactory.
    1. If you can log in to https://repo.jenkins-ci.org with your account, you're set to punch in those credentials in your `~/.m2/settings.xml` file:
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <settings
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd" 
      xmlns="http://maven.apache.org/SETTINGS/1.1.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <servers>
        <server>
          <id>repo.jenkins-ci.org</id>
          <username>TODO</username>
          <password>TODO</password>
        </server>
      </servers>
    </settings>
    ```
    2. A great test is to try to perform a `mvn deploy`, which should attempt to upload some files to the snapshot repo and will need those credentials.
	3. If you're worried about putting your "Jenkins infrastructure" password  in plain-text in that file, Maven has a password encryption facility that relies on a master password in another file.  Presumably, you secure access to the 2nd file by placing it on a thumbdrive that you carry with you when you're not at your computer, etc.
1. A TFS server or a Azure DevOps account, configured as per `Testing.md`

Release
=======

1. Pre-release.  Perform these manual steps on your workstation:
    1. Run a full build, with all its end-to-end tests; it takes about 5 minutes:
    ```
    mvn clean verify --batch-mode -Dtfs_server_name=&lt;TFS host name or Azure DevOps account host name> -Dtfs_user_name=&lt;user> -Dtfs_user_password=&lt;password>
    ```
    2. Look at the commits since the last release by going to https://github.com/jenkinsci/tfs-plugin/releases and clicking the "XX commits to master since this release" link.  It will be easiest to surf the associated pull requests, so hit Ctrl+F, search for "Merge pull request" and Ctrl+click every #XXX link to the right of the highlights.
    3. Fill in the categories of the `ReleaseNotes.md` template, usually in one of the following formats:
        1. &lt;Summary>. Thanks to @&lt;GitHub user name> for pull request #&lt;pull request number>.
        2. &lt;Summary>, via pull request #&lt;pull request number>.
    4. Decide on the release version and on the next development version, based on the rules of [Semantic Versioning](http://semver.org/).
    5. Update `ReleaseNotes.md` with the release version and merge/push to `master`.
    6. Merge any "wiki" changes to `master`.
2. Automated release.  Create a Jenkins job as follows:
    1. General
        1. Check "This project is parameterised"
            1. String parameter **releaseVersion**
            2. String parameter **developmentVersion**
    2. SCM
        1. Git
            1. Repository Url: **git@github.com:jenkinsci/tfs-plugin.git**
            2. Credentials: (select your previously-entered private key file)
            3. Name: **origin**
            4. Refspec: **+refs/heads/master:refs/remotes/origin/master**
			5. Branch Specifier: **refs/heads/master**
			6. Repository browser: **githubweb**
			7. Additional Behaviours:
			    1. Clean before checkout
				2. Check out to specific local branch (to avoid ["Git fatal: ref HEAD is not a symbolic ref" while using maven release plugin](https://stackoverflow.com/a/21184154/))
					1. Branch name: **master**
    3. Build Environment
		1. Add timestamps to the Console Output
		2. Inject environment variables to the build process
		    1. **COMPUTERNAME** (the host name of the Jenkins node that will run the job)
            2. **TFS_SERVER_NAME** (the TFS host name or Azure DevOps account host name)
        3. Use secret text(s) or file(s)
			1. **TFS_USER_NAME** and **TFS_USER_PASSWORD** are initialized from a credential
    4. Build. Add the following steps:
        1. "Shell script" step to check and prepare (filling in the blanks at the `git config` lines near the end)
        ```bash
        set +e

        # verify releaseVersion and developmentVersion
        if [[ "$releaseVersion" != +([0-9])\.+([0-9])\.+([0-9]) ]]
        then
            echo "ERROR: '$releaseVersion' is not a valid releaseVersion"
            exit 1
        fi

        if [[ "$developmentVersion" != +([0-9])\.+([0-9])\.+([0-9])-SNAPSHOT ]]
        then
            echo "ERROR: '$developmentVersion' is not a valid developmentVersion"
            exit 1
        fi


        # test SSH connection to Git
        ssh -Tv git@github.com
        if [[ $? != "1" ]]
        then
            echo "ERROR: Unable to connect to GitHub via SSH"
            exit 1
        fi

        git config --local user.name '<your full name>'
        git config --local user.email '<your e-mail address>'

        exit 0
        ```
        2. "Maven" step as a dry-run, running all tests and performing a SNAPSHOT deploy
        ```
        deploy
        dependency:go-offline
        --batch-mode
        -Dtfs_server_name=${TFS_SERVER_NAME}
        -Dtfs_user_name=${TFS_USER_NAME}
        -Dtfs_user_password=${TFS_USER_PASSWORD}
        ```
        3. "Maven" step to actually release
        ```
        clean
        release:prepare
        release:perform
        --batch-mode
        -Dtag=tfs-${releaseVersion}
        -DreleaseVersion=${releaseVersion}
        -DdevelopmentVersion=${developmentVersion}
        ```
        4. "Shell script" step for post-release actions (filling in the blanks at the `git config` lines near the beginning)
        ```bash
        cd target/checkout
        git config --local user.name '<your full name>'
        git config --local user.email '<your e-mail address>'
         
        git checkout -b update_documentation_for_$releaseVersion origin/master
         
        cat > ReleaseNotes.md <<EndOfReleaseNotes
        These notes are for release **(to be determined)**.
        Other releases and their notes can be found at the [tfs-plugin GitHub Releases](https://github.com/jenkinsci/tfs-plugin/releases) page.
         
        * Major:
            * TODO
        * Minor:
            * TODO
         
        EndOfReleaseNotes
        git commit -a -m "Clear out the release notes for the next release"
         
        git push origin update_documentation_for_$releaseVersion
        ```
        5. Files to archive:
        ```
        tfs/target/tfs.hpi,ReleaseNotes.md,README.md,images/**
        ```
3. Post-release.  Perform these manual steps on your workstation:
    1. Download the artifacts from Jenkins
    2. Create a pull request from the **update_documentation_for_$releaseVersion** branch, then merge it.
    3. Edit the tag on GitHub:
        1. Copy-paste most of `ReleaseNotes.md`.
        2. Upload the artifacts downloaded from Jenkins.
        3. Publish release.
    4. Update `README.md`.
    5. Update affected issues with "Fixed in".
    6. Check the update mirror after about a day, to make sure the new version was replicated across the CDN: http://updates.jenkins-ci.org/update-center.json
