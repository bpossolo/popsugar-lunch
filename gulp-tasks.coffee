gulp = require 'gulp'
gulpif = require 'gulp-if'
coffee = require 'gulp-coffee'
del = require 'del'
gutil = require 'gulp-util'
concat = require 'gulp-concat'
browserSync = require('browser-sync').create()
sass = require 'gulp-ruby-sass'
historyApiFallback = require 'connect-history-api-fallback'
proxy = require './proxy'

# -----------------------------------------------------------
# Variables
# -----------------------------------------------------------

OutputDir = 'target/popsugar-lunch'
reloadOpts =
  stream: true

# -----------------------------------------------------------
# Source files
# -----------------------------------------------------------

scssFiles = [
  'src/main/scss/*.scss'
  'src/main/angular/**/*.scss'
]
clientScriptFiles = [
  'bower/angular/angular.min.js'
  'bower/angular-ui-router/release/angular-ui-router.min.js'
  'bower/lodash/lodash.min.js'
  'src/main/angular/**/*.coffee'
]
htmlFiles = [
  'src/main/angular/**/*.html'
  'src/main/webapp/index.html'
]

# -----------------------------------------------------------
# Tasks
# -----------------------------------------------------------

gulp.task 'clean', (cb) ->
  artifacts = [
    "#{OutputDir}/main.js"
    "#{OutputDir}/main.css"
    "#{OutputDir}/views"
    "#{OutputDir}/assets"
    "#{OutputDir}/index.html"
  ]
  del artifacts, cb

gulp.task 'client-scripts', ->
  options =
    bare: true
  gulp
    .src(clientScriptFiles)
    .pipe(gulpif(/\.coffee$/, coffee(options).on('error', gutil.log)))
    .pipe(concat('main.js'))
    .pipe(gulp.dest("#{OutputDir}"))
    .pipe(browserSync.reload(reloadOpts))

gulp.task 'sass', ->
  sass('src/main/scss/main.scss')
    .on('error', sass.logError)
    .pipe(gulp.dest("#{OutputDir}"))
    .pipe(browserSync.reload(reloadOpts))

gulp.task 'html', ->
  gulp
    .src(htmlFiles)
    .pipe(gulp.dest("#{OutputDir}"))
    .pipe(browserSync.reload(reloadOpts))

gulp.task 'assets', ->
  gulp
    .src('src/main/webapp/assets/**')
    .pipe(gulp.dest("#{OutputDir}/assets"))

gulp.task 'build', ['client-scripts', 'sass', 'html', 'assets']

gulp.task 'serve', ['build'], ->
  config =
    open: false
    server:
      baseDir: "#{OutputDir}"
      index: 'index.html'
      middleware: [proxy, historyApiFallback()]
  browserSync.init config
  gulp.watch scssFiles, ['sass']
  gulp.watch clientScriptFiles, ['client-scripts']
  gulp.watch htmlFiles, ['html']

gulp.task 'default', ['build']
