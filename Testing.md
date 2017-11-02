Functional Tests
================
There's only so much testing you can do with test friends such as mocks and network replayers.  To enable more comprehensive and realistic testing, some automated tests were written to talk to live servers.  Some manual, one-time set-up is required to facilitate the process.

How to configure your TFS server
--------------------------------
You'll need to be (or involve) an administrator.  It is assumed your TFS server is installed at the default port (8080) and virtual directory (/tfs).

1. Launch the **Team Foundation Server Administration Console**
    1. Navigate to root > Application Tier > Team Project Collections
    2. Click **Create Collection**
    3. Set the *Name* to **jenkins-tfs-plugin**
    4. Indicate in the *Description* field that this team project collection was created to isolate the automated tests from normal usage of the server.  A hyperlink to this page would probably be a good idea.
    5. Click **Verify**
    6. Assuming everything was fine, click **Create**
    7. Wait about 1 minute for the process to complete.
    8. Assuming the team project collection was created OK, click **Close**
2. Follow the instructions at [Create a team project](https://msdn.microsoft.com/library/ms181477.aspx) to create a *Team Project*, as specified below:
    1. When prompted for the *Name*, enter **FunctionalTests**
    2. Indicate in the *Description* field that this team project was created for the automated functional tests.  A hyperlink to this page would probably be a good idea.
    3. The choice of process template is not important.
    4. Select **Team Foundation Version Control** as the *version control system*.
4. Follow the instructions at [Add team members](https://msdn.microsoft.com/en-us/library/jj920206.aspx) to add the test user as a member of the **FunctionalTests** team project
5. Ensure the user has access to the team project
    1. Open a web browser in InPrivate/incognito/private mode.  This will make sure you aren't re-using authentication cookies or saved credentials.
    2. Navigate to the **jenkins-tfs-plugin** team project collection on the server.  Example:  `http://tfs.corp.example.com:8080/tfs/jenkins-tfs-plugin`
    3. Log in as the test user.
    4. Confirm you can browse to the **FunctionalTests** team project.


How to configure your Team Services account
-------------------------------------------
TODO: write this


How to configure your development environment
---------------------------------------------
Tests that need to connect to a server will only run during the `verify` phase *if* the `tfs_server_name` property was provided to Maven.  The value of this property is the *fully-qualified DNS name (FQDN)* of the server, because a non-qualified host name appears to trigger NTLM authentication attempts.
You should also provide the following properties:

1. tfs_server_name - set this to the host name of the server (ex. tfs.corp.example.com)
2. tfs_collection_url - set this to the full url of the collection (ex. http://tfs.corp.example.com:8081/tfs/jenkins-tfs-plugin)
3. tfs_user_name - set this to the test user you gave permissions to above (the default value if not provided is **jenkins-tfs-plugin**)
4. tfs_user_password - set this to the password of the test user (the default value if not provided is **for-test-only**)
5. Set the COMPUTERNAME environment variable as the end-to-end tests rely on its presence

Example:

    mvn clean verify -Dtfs_collection_url="http://tfs.corp.example.com:8081/tfs/jenkins-tfs-plugin" -Dtfs_server_name=tfs.corp.example.com -Dtfs_user_name=jenkins-tfs-plugin -Dtfs_user_password=for-test-only

