angular.module('app').controller('DateWidgetCtrl', ['$scope', ($scope) ->

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

])
