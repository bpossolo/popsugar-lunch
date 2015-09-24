angular.module('app').controller('LunchGroupsCtrl', ['$scope', '$injector', ($scope, $injector) ->

  $stateParams = $injector.get '$stateParams'
  $http = $injector.get '$http'

  $scope.sfLunchGroups = []
  $scope.laLunchGroups = []
  $scope.nyLunchGroups = []

  config =
    params: {}

  if $stateParams.type is 'popsugar-pals'
    config.params.type = 'PopSugarPals'
  else
    config.params.type = 'Regular'

  promise = $http.get '/api/lunch/groups', config
  promise.success (groups) ->
    for group in groups
      switch group.location
        when 'SanFrancisco'
          $scope.sfLunchGroups.push group
        when 'LosAngeles'
          $scope.laLunchGroups.push group
        when 'NewYork'
          $scope.nyLunchGroups.push group

  return
])
