#!/bin/bash

mvn clean
gulp bump
git commit -a -m 'version bump'
git push origin master
gulp build
mvn appengine:update
