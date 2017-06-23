Contributing to the Team Foundation Server plugin for Jenkins
=============================================================

##Building Sources
This is a Maven project with Java sources. The mvn package command will produce an HPI file that is the Jenkins Plugin file that will need to be installed on your Jenkins Server.
- SET JAVA_HOME to the location of the JRE to build with (JRE 1.8 works)
- Clone the repo from https://github.com/jenkinsci/tfs-plugin.git
- change dir to the tfs-plugin folder
- run "mvn package"
    - Initial build will have to download lots of libraries. This could take a few minutes.
- This produces tfs-plugin\tfs\target\tfs.hpi

##Using IntelliJ IDEA
To use Intellij IDEA as the editor for this project simply do the following after getting sources:
1) Open the tfs-plugin folder (root folder) in IntelliJ (I installed IntelliJ 17 community edition from https://www.jetbrains.com/idea/)
2) Go to File->Project Structure and click on Project
3) Specify the Project SDK (Java 1.8 works)

You should now be able to build from within IntelliJ
- NOTE to build the hpi file you will have to 
    - bring up the Maven Projects tool window (View->Tool Windows->Maven Projects) and click the "run maven goal" button
    - Then type in "package" in the goal text box

##Debugging the Plugin
See https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial for information on how to debug the plugin.
From within IntelliJ:
1) Create a new Run configuration
    1) Type = Maven
    1) Name = run hpi
    1) Working directory should be the full path to "../tfs-plugin/tfs" (NOTE this is NOT the root folder)
    1) Command Line 
    
        `
        hpi:run -Djetty.port=8090 -Dhudson.plugins.tfs.telemetry.isDeveloperMode=true
        `
    
        1) or use whatever port you want
    1) On the Runner tab
        1) Environment Variables == MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n
1) Run or Debug this configuration using the play and debug icons (top right)
    1) Set any breakpoints you want in IntelliJ
    1) Navigate to http://localhost:8090/jenkins

Note: this runs Jenkins on your local OS not in a docker image. As such, any configurations you make are preserved between runs.

##Installing Jenkins Locally for Manual Testing
The easiest method is to run Jenkins in a docker image.
1) Install Docker for your OS (https://www.docker.com/community-edition)
1) Install and run the jenkins image

    `
    docker run --name localJenkins -p 9191:9191 -p 50001:50001 --env "JENKINS_OPTS=--httpPort=9191" --env JENKINS_SLAVE_AGENT_PORT=50001 --env hudson.plugins.tfs.telemetry.isDeveloperMode=true jenkins
    `
    
    - NOTES: 
        - Note that this command line avoids port 8080 (the default) in case you have VSO deployed as well
        - Look in the output for the admin password
        - The output sent to the console is also where you will see any logger output
        - Note the environment variable "hudson.plugins.tfs.telemetry.isDeveloperMode". It is important to set this variable so that AppInsights data is sent to right key
        - This installs a linux jenkins server on Docker (NOT one based on Windows or the host OS)
1) Setup Jenkins
    1) Go to http://localhost:9191
    1) Enter the admin password
    1) Install default plugins
    1) Run Jenkins
1) Install Plugin Manually
    1) Go to http://localhost:9191/pluginManager/advanced
    1) Browse to the tfs.hpi file and Upload it
    1) To update the plugin, repeat steps 1 and 2 and then restart Jenkins by going to http://localhost:9191/restart
1) To Stop Jenkins and start from scratch

    `
    docker stop localJenkins
    docker container prune
    `
    
    Then repeat step 2 and 3 above
