httpProxy = require 'http-proxy'
proxy = httpProxy.createProxyServer()

rules = [
  pattern: /^\/api\/lunch\//
  target: 'http://localhost:8888/'
]

# add cors Access-Control-Allow-Origin response header to all proxied requests
proxy.on 'proxyRes', (proxyRes, req, res) ->
  origin = req.headers.origin
  if origin and origin in allowedOrigins
    res.setHeader 'Access-Control-Allow-Origin', origin

module.exports = (req, res, next) ->
  for rule in rules
    if rule.pattern.test req.url
      options =
        target: rule.target
        changeOrigin: true
      proxy.web req, res, options
      return
  next()
