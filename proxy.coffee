httpProxy = require 'http-proxy'
proxy = httpProxy.createProxyServer()

rules = [
  pattern: /^\/api\/lunch\//
  target: 'http://localhost:8888/'
]

module.exports = (req, res, next) ->
  for rule in rules
    if rule.pattern.test req.url
      options =
        target: rule.target
        changeOrigin: true
      proxy.web req, res, options
      return
  next()
