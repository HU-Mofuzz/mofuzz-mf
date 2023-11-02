# Mofuzz Multifile

This project is part of multiple study projects and master theses. 
It contains the corresponding implementations for the architectural approaches described. 
The tools that are implemented in this repository represent the basis for the experiment setup for answering different research questions in the theses.

Linked project for the instrumentation of different spreadsheet applications are linked here:
- [DocumentLoader + JLOP Helper Classes](https://github.com/HU-Mofuzz/JLOP-Helper) for instrumenting LibreOffice
- [JNA-Fork](https://github.com/HU-Mofuzz/jna-fork) for instrumenting Microsoft Excel

## How to build
Please make sure you have a valid `JAVA_HOME` environment variable.
You can download the gradle binaries [here](https://gradle.org/). In case you have no local gradle installation you can use the wrapper scripts provided by this project. On Max/Linux use `gradlew`, on Windows `gradle.bat`

This is a gradle project, you can simply run `gradle build`. This will execute every Unit-Test and will build every submodule.
To obtain the Document-Server docker image please run `gradle bootBuilImage`.
