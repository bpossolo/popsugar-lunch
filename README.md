PopSugar Lunch for Four is a web application that randomly selects registered users to go to lunch together in order to promote intra-office social interaction.  

The app is designed to run on Google App Engine and has been built using GAE for Java SDK.  

In order to build the application, you need Eclipse, Maven and the Google Plugin for Eclipse.  

To perform a release run the following commands:  
1. mvn clean  
2. gulp bump  
3. git commit -a -m 'version bump'  
4. git push origin master  
5. gulp build  
6. mvn appengine:update  
