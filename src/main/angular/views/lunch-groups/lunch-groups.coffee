angular.module('app').controller('LunchGroupsCtrl', ['$scope', '$injector', ($scope, $injector) ->

  $stateParams = $injector.get '$stateParams'
  $http = $injector.get '$http'
  User = $injector.get 'User'

  $scope.loading = true

  $scope.sfLunchGroups = []
  $scope.laLunchGroups = []
  $scope.nyLunchGroups = []

  if $stateParams.type is 'popsugar-pals'
    $scope.type = 'PopsugarPals'
  else
    $scope.type = 'Regular'

  config =
    params:
      type: $scope.type
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
