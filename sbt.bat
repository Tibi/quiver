rem set SBT_OPTS=-Dhttp.proxyHost=www-proxy.admin.ch -Dhttp.proxyPort=8080
java %SBT_OPTS% -Xmx256M -jar sbt-launcher-0.5.2.jar %*