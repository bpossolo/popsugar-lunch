angular.module('app').controller('LunchGroupsCtrl', ['$scope', '$injector', ($scope, $injector) ->

  $stateParams = $injector.get '$stateParams'
  $http = $injector.get '$http'
  User = $injector.get 'User'

  $scope.sfLunchGroups = []
  $scope.laLunchGroups = []
  $scope.nyLunchGroups = []

  config =
    params: {}

  if $stateParams.type is 'popsugar-pals'
    config.params.type = 'PopsugarPals'
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
      group.users = User.enhance group.users

  monthNames = [
    'January'
    'February'
    'March'
    'April'
    'May'
    'June'
    'July'
    'August'
    'September'
    'October'
    'November'
    'December'
  ]

  now = new Date()
  month = monthNames[now.getMonth()]
  year = now.getFullYear()
  $scope.date = "#{month} #{year}"

  return
])
