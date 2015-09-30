angular.module('app').controller('ManagePalsCtrl', ['$scope', '$injector', ($scope, $injector) ->

  $http = $injector.get '$http'
  $filter = $injector.get '$filter'
  filter = $filter('filter')
  $timeout = $injector.get '$timeout'
  User = $injector.get 'User'

  DefaultButtonLabel = 'Select 2 Users'

  promise = $http.get '/api/lunch/users'
  promise.success (userDtos) ->
    $scope.allUsers = User.enhance userDtos

  $scope.numSelected = 0
  $scope.toggleUserConnectionBtnLabel = DefaultButtonLabel

  suppressMessage = ->
    $scope.success = null
    $scope.error = false

  getSelectedUsers = ->
    criteria =
      selected: true
    selectedUsers = filter $scope.allUsers, criteria
    selectedUsers

  linkUsers = (userA, userB) ->
    data =
      userAKey: userA.key
      userBKey: userB.key
    promise = $http.post '/api/lunch/buddies', data
    promise.success ->
      userA.link userB
      userA.selected = false
      userB.selected = false
      $scope.numSelected = 0
      $scope.success = 'Users linked!'
      $timeout suppressMessage, 5000

  unlinkUsers = (userA, userB) ->
    config =
      params:
        userAKey: userA.key
        userBKey: userB.key
    promise = $http.delete '/api/lunch/buddies', config
    promise.success ->
      userA.unlink userB
      userA.selected = false
      userB.selected = false
      $scope.numSelected = 0
      $scope.success = 'Users unlinked!'
      $timeout suppressMessage, 5000

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
    if $scope.numSelected is 2
      selectedUsers = getSelectedUsers()
      userA = selectedUsers[0]
      userB = selectedUsers[1]
      if userA.isLinkedTo userB
        $scope.toggleUserConnectionBtnLabel = 'Unlink Users'
      else
        $scope.toggleUserConnectionBtnLabel = 'Link Users'
    else
      $scope.toggleUserConnectionBtnLabel = DefaultButtonLabel

  $scope.toggleUserConnection = ->
    selectedUsers = getSelectedUsers()
    if selectedUsers.length is not 2
      return
    userA = selectedUsers[0]
    userB = selectedUsers[1]
    if userA.isLinkedTo userB
      unlinkUsers userA, userB
    else
      linkUsers userA, userB

  return
])
