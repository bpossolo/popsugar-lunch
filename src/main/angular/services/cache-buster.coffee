# appends a version query param to requests for html templates
angular.module('app').factory 'cacheBuster', ($window) ->
  request: (conf) ->
    if /^\/(views|widget)/.test conf.url
      conf.params ?= {}
      conf.params.v = $window.appVersion
    conf

angular.module('app').config ($httpProvider) ->
  $httpProvider.interceptors.push 'cacheBuster'
