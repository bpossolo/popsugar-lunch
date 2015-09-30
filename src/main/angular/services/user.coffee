angular.module('app').factory 'User', ->

  PingboardUrl = 'https://popsugar.pingboard.com/users/'
  DefaultAvatar = '/assets/images/stormtrooper.jpg'

  class User

    constructor: (user) ->
      _.assign this, user
      this.avatarUrl = user.pingboardAvatarUrlSmall or DefaultAvatar
      this.pingboardUrl = PingboardUrl + user.pingboardId

    link: (user) ->
      if this.buddy
        @unlink this.buddy
      if user.buddy
        user.unlink user.buddy
      this.buddyKey = user.key
      this.buddy = user
      user.buddyKey = this.key
      user.buddy = this

    unlink: (user) ->
      this.buddyKey = null
      this.buddy = null
      user.buddyKey = null
      user.buddy = null

    isLinkedTo: (user) ->
      this.buddyKey is user.key and user.buddyKey is this.key

    @enhance: (userDtos = []) ->
      users = []
      for userDto in userDtos
        user = new User userDto
        users.push user
      # now that the users are all enhanced
      # link up the buddies
      map = _.indexBy users, 'key'
      for user in users
        if user.buddyKey
          buddy = map[user.buddyKey]
          user.buddy = buddy
      users
