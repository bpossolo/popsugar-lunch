angular.module('app').controller('ManagePalsCtrl', ['$scope', '$injector', ($scope, $injector) ->

  $http = $injector.get '$http'
  $filter = $injector.get '$filter'
  filter = $filter('filter')
  $timeout = $injector.get '$timeout'
  User = $injector.get 'User'

  promise = $http.get '/api/lunch/users'
  promise.success (userDtos) ->
    $scope.allUsers = User.enhance userDtos

  $scope.numSelected = 0

  suppressMessage = ->
    $scope.success = false
    $scope.error = false

  $scope.toggleUser = (user) ->
    if user.selected
      user.selected = false
      $scope.numSelected--
    else
      user.selected = true
      $scope.numSelected++

  $scope.linkUsers = ->
    criteria =
      selected: true
    selectedUsers = filter $scope.allUsers, criteria
    data =
      user1Key: selectedUsers[0].key
      user2Key: selectedUsers[1].key
    promise = $http.post '/api/lunch/create-pair', data
    promise.success ->
      selectedUsers[0].selected = false
      selectedUsers[1].selected = false
      $scope.success = true
      $timeout suppressMessage, 5000

  return
])
