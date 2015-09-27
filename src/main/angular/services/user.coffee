angular.module('app').factory 'User', ->

  PingboardUrl = 'https://popsugar.pingboard.com/users/'
  DefaultAvatar = '/assets/images/stormtrooper.jpg'

  class User

    constructor: (user) ->
      _.assign this, user
      this.avatarUrl = user.pingboardAvatarUrlSmall or DefaultAvatar
      this.pingboardUrl = PingboardUrl + user.pingboardId

    @enhance: (userDtos = []) ->
      users = []
      for userDto in userDtos
        user = new User userDto
        users.push user
      users
