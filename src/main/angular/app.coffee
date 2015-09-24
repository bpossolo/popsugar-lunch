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
  .state 'manage-pals',
    url: '/manage-pals'
    templateUrl: '/views/manage-pals/manage-pals.html'
    controller: 'ManagePalsCtrl'

  return
])
.run(['$injector', '$rootScope', ($injector, $rootScope) ->

  $location = $injector.get '$location'

  $rootScope.isHome = ->
    $location.path() is '/'

  return
])
