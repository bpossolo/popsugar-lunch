angular.module('app').controller('RegisterCtrl', ['$scope', '$injector', ($scope, $injector) ->

  $http = $injector.get '$http'
  $timeout = $injector.get '$timeout'

  $scope.location = 'none'

  locationPromise = $http.get '/api/lunch/my-location'
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
    promise = $http.post '/api/lunch/create-user', data
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

    # signup.setEnabled(false);
    # successMsg.setVisible(false);
    # String userName = name.getValue();
    # String userEmail = email.getValue();
    # String locationVal = location.getValue(location.getSelectedIndex());
    # if( isBlank(userName) || isBlank(userEmail) || isBlank(locationVal) || NO_LOCATION.equals(locationVal) ){
    #   validationError.setVisible(true);
    #   signup.setEnabled(true);
    # }
    # else{
    #   Location userLocation = Location.valueOf(locationVal);
    #   validationError.setVisible(false);
    #   createUser(userName, userEmail, userLocation);
    # }

    # rpcService.createUser(userName, userEmail, userLocation, new AsyncCallback<Void>() {
    #   @Override
    #   public void onSuccess(Void result) {
    #     successMsg.setVisible(true);
    #     name.setValue(null);
    #     email.setValue(null);
    #     location.setSelectedIndex(0);
    #     signup.setEnabled(true);
    #   }

    #   @Override
    #   public void onFailure(Throwable caught) {
    #     Window.alert("Oops! Looks like there is something wrong with our server! Try again in a few moments.");
    #     signup.setEnabled(true);
    #   }
    # });

  # rpcService.getLunchGroups(new AsyncCallback<LunchGroupDTO>() {
  #     @Override
  #     public void onSuccess(LunchGroupDTO data) {
  #       weekLabel.setText("Lunch groups for week of " + data.getWeek());
  #       if( data.getUserLocation() != null )
  #         location.setSelectedIndex(data.getUserLocation().ordinal() + 1);
  #       for( LunchGroup group : data.getGroups() ){
  #         LunchGroupPanel lgp = new LunchGroupPanel(group);
  #         switch( group.getLocation() ){
  #         case SanFrancisco :
  #           sfLunchGroupsContainer.add(lgp);
  #           break;
  #         case NewYork :
  #           nyLunchGroupsContainer.add(lgp);
  #           break;
  #         case LosAngeles :
  #           laLunchGroupsContainer.add(lgp);
  #           break;
  #         }

  #       }
  #     };

  #     @Override
  #     public void onFailure(Throwable caught) {
  #       Window.alert("Oops! Looks like there is something wrong with our server! We couldn't fetch the current lunch groups.");
  #     }
  #   });

  return
])
