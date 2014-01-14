@echo off

    set propertiesFile=%~dp0aemHist.properties
    @echo ACCUREV_LAST_TRANSACTION=%ACCUREV_LAST_TRANSACTION%
    @echo ACCUREV_TRANSACTION=%ACCUREV_LAST_TRANSACTION%>%propertiesFile%

