PopSugar Lunch for Four is a web application that randomly selects registered users to go to lunch together in order to promote intra-office social interaction.

The app is designed to run on Google App Engine and has been built using GAE for Java 1.8.3

In order to build the application, you need Eclipse and the Google Plugin for Eclipse.
After opening the project in Eclipse, add the GAE 1.8.3 runtime to the classpath.
Then add the GWT 2.5.1 runtime to the classpath.
Finally, add the following two libraries to the classpath (they must not be copied into WAR-INF/lib):
* appengine-api-stubs.jar
* appengine-testing.jar
Both of the jar files are available in the GAE SDK's lib folder.
