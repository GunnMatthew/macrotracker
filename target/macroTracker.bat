@echo off
java --module-path "%~dp0dependancies\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -jar "%~dp0macrotracker.jar"
