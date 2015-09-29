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
    $scope.success = null
    $scope.error = false

  $scope.userFilter = (user, index, users) ->
    regexp = new RegExp $scope.search, 'i'
    return regexp.test(user.name) or regexp.test(user.buddy?.name)

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
      userAKey: selectedUsers[0].key
      userBKey: selectedUsers[1].key
    promise = $http.post '/api/lunch/buddies', data
    promise.success ->
      selectedUsers[0].selected = false
      selectedUsers[0].buddyKey = selectedUsers[1].key
      selectedUsers[0].buddy = selectedUsers[1]
      selectedUsers[1].selected = false
      selectedUsers[1].buddyKey = selectedUsers[0].key
      selectedUsers[1].buddy = selectedUsers[0]
      $scope.success = 'Users linked!'
      $timeout suppressMessage, 5000

  $scope.unlinkUsers = ->
    criteria =
      selected: true
    selectedUsers = filter $scope.allUsers, criteria
    data =
      userAKey: selectedUsers[0].key
      userBKey: selectedUsers[1].key
    promise = $http.delete '/api/lunch/buddies', data
    promise.success ->
      selectedUsers[0].selected = false
      selectedUsers[0].buddyKey = null
      selectedUsers[0].buddy = null
      selectedUsers[1].selected = false
      selectedUsers[1].buddyKey = null
      selectedUsers[1].buddy = null
      $scope.success = 'Users unlinked!'
      $timeout suppressMessage, 5000

  return
])
