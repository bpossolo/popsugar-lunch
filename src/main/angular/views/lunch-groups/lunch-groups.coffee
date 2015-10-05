angular.module('app').controller('LunchGroupsCtrl', ['$scope', '$injector', ($scope, $injector) ->

  $stateParams = $injector.get '$stateParams'
  $http = $injector.get '$http'
  User = $injector.get 'User'

  $scope.loading = true
  $scope.type = $stateParams.type
  $scope.city = $stateParams.city or 'sanfrancisco'
  $scope.sfLunchGroups = []
  $scope.laLunchGroups = []
  $scope.nyLunchGroups = []

  switch $scope.city
    when 'sanfrancisco'
      $scope.groups = $scope.sfLunchGroups
    when 'losangeles'
      $scope.groups = $scope.laLunchGroups
    when 'newyork'
      $scope.groups = $scope.nyLunchGroups

  if $scope.type is 'popsugar-pals'
    groupType = 'PopsugarPals'
  else
    groupType = 'Regular'

  config =
    cache: true
    params:
      type: groupType
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
      group.users = User.enhance group.users
    $scope.loading = false

  return
])
