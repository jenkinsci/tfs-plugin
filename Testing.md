Functional Tests
================
There's only so much testing you can do with test friends such as mocks and network replayers.  To enable more comprehensive and realistic testing, some automated tests were written to talk to live servers.  Some manual, one-time set-up is required to facilitate the process.

How to configure your TFS server
--------------------------------
You'll need to be (or involve) an administrator.  It is assumed your TFS server is installed at the default port (8080) and virtual directory (/tfs).

1. Add a **jenkins-tfs-plugin** user to the domain.
    1. Set its password to **for-test-only**, with no password expiry. (you may need to temporarily disable the 
*Password must meet complexity requirements* policy, under Computer Configuration > Policies > Windows Settings > Security Settings > Account Policies > Password Policy)
2. Launch the **Team Foundation Server Administration Console**
    1. Navigate to root > Application Tier > Team Project Collections
    2. Click **Create Collection**
    3. Set the *Name* to **jenkins-tfs-plugin**
    4. Indicate in the *Description* field that this team project collection was created to isolate the automated tests from normal usage of the server.  A hyperlink to this page would probably be a good idea.
    5. Click **Verify**
    6. Assuming everything was fine, click **Create**
    7. Wait about 1 minute for the process to complete.
    8. Assuming the team project collection was created OK, click **Close**
3. Follow the instructions at [Create a team project](https://msdn.microsoft.com/library/ms181477.aspx) to create a *Team Project*, as specified below:
    1. When prompted for the *Name*, enter **FunctionalTests**
    2. Indicate in the *Description* field that this team project was created for the automated functional tests.  A hyperlink to this page would probably be a good idea.
    3. The choice of process template is not important.
    4. Select **Team Foundation Version Control** as the *version control system*.
4. Follow the instructions at [Add team members](https://msdn.microsoft.com/en-us/library/jj920206.aspx) to add the **jenkins-tfs-plugin** user as a member of the **FunctionalTests** team project
5. Test it!
    1. Open a web browser in InPrivate/incognito/private mode.  This will make sure you aren't re-using authentication cookies or saved credentials.
    2. Navigate to the **jenkins-tfs-plugin** team project collection on the server.  Example:  `http://tfs.corp.example.com:8080/tfs/jenkins-tfs-plugin`
    3. Log in as **jenkins-tfs-plugin** with password **for-test-only**
    4. Confirm you can browse to the **FunctionalTests** team project.


How to configure your Team Services account
-------------------------------------------
TODO: write this when Visual Studio Team Services is supported.


How to configure your development environment
---------------------------------------------
Tests that need to connect to a server will only run during the `verify` phase *if* the `tfs_server_name` property was provided to Maven.  The value of this property is the *fully-qualified DNS name (FQDN)* of the server, because a non-qualified host name appears to trigger NTLM authentication attempts.

Example:

    mvn clean verify -Dtfs_server_name=tfs.corp.example.com

