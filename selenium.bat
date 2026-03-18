@echo off
REM ───────────────────────────────────────────────────
REM  selenium.bat — alias to run selenium-cli fat JAR
REM  Add this file's directory to your PATH:
REM    set PATH=%PATH%;C:\TestAutomation\selenium-cli
REM ───────────────────────────────────────────────────
java -jar "%~dp0target\selenium-cli-1.0.0.jar" %*

