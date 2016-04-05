angular.module('app').controller('UnsubscribeCtrl', ['$scope', '$injector', ($scope, $injector) ->
  $http = $injector.get '$http'
  $stateParams = $injector.get '$stateParams'
  userId = $stateParams.userId
  if userId
    url = "/api/lunch/users/#{userId}"
    $scope.loading = true
    promise = $http.delete url
    promise.success ->
      $scope.success = true
      $scope.loading = false
])
