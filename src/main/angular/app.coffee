angular.module('app', ['ui.router']).config(['$injector', ($injector) ->

  $locationProvider = $injector.get '$locationProvider'
  $locationProvider.html5Mode true

  $urlRouterProvider = $injector.get '$urlRouterProvider'
  $urlRouterProvider.otherwise '/'

  $stateProvider = $injector.get '$stateProvider'
  $stateProvider
  .state 'home',
    url: '/'
    templateUrl: '/views/home/home.html'
    controller: 'HomeCtrl'
  .state 'lunch-groups',
    url: '/groups/{type}'
    templateUrl: '/views/lunch-groups/lunch-groups.html'
    controller: 'LunchGroupsCtrl'
  .state 'lunch-groups-city',
    url: '/groups/{type}/{city}'
    templateUrl: '/views/lunch-groups/lunch-groups.html'
    controller: 'LunchGroupsCtrl'
  .state 'manage-pals',
    url: '/manage-pals'
    templateUrl: '/views/manage-pals/manage-pals.html'
    controller: 'ManagePalsCtrl'
  .state 'unsubscribe',
    url: '/unsubscribe/{userId}'
    templateUrl: '/view/unsubscribe/unsubscribe.html'
    controller: 'UnsubscribeCtrl'

  return
])
.run(['$injector', '$rootScope', ($injector, $rootScope) ->

  $location = $injector.get '$location'

  $rootScope.isHome = ->
    $location.path() is '/'

  $rootScope.isLunchGroups = ->
    /^\/groups/.test $location.path()

  return
])
