angular.module('app').controller('RegisterCtrl', ['$scope', '$injector', ($scope, $injector) ->

  $http = $injector.get '$http'
  $timeout = $injector.get '$timeout'

  $scope.location = 'none'

  locationPromise = $http.get '/api/lunch/location'
  locationPromise.success (data) ->
    if data.location
      $scope.location = data.location
  locationPromise.error ->
    return

  suppressMessage = ->
    $scope.success = false
    $scope.error = false

  $scope.register = ->
    name = $scope.name or ''
    name = name.trim()
    email = $scope.email or ''
    email = email.trim()
    location = $scope.location
    if not name or not email or location is 'none'
      $scope.error = true
      $timeout suppressMessage, 5000
      return
    data =
      name: name
      email: email
      location: location
    $scope.loading = true
    promise = $http.put '/api/lunch/users', data
    promise.success ->
      $scope.loading = false
      $scope.success = true
      $scope.name = null
      $scope.email = null
      $timeout suppressMessage, 5000
    promise.error ->
      $scope.loading = false
      $scope.error = true
      $timeout suppressMessage, 5000
    return

  return
])
