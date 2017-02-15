PopSugar Lunch for Four is a web application that randomly selects registered users to go to lunch together in order to promote intra-office social interaction.  

The app is designed to run on Google App Engine and has been built using GAE for Java SDK.  

In order to build the application, you need Eclipse, Maven and the Google Plugin for Eclipse.  

To perform a release run the following commands:  

```bash
mvn clean  
gulp bump  
git commit -a -m 'version bump'  
git push origin master  
gulp build  
mvn appengine:update  
```

Or execute the release script which does all that work for you:  
```bash
./src/main/bash/release.sh
```
