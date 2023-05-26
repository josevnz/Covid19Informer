# COVID-19 informer

This small program downloads town COVID-19 information made available on the CT Data Portal, and parses it to display information of interest.

* https://data.ct.gov/stories/s/COVID-19-data/wa3g-tfvc/

# Author

Jose Vicente Nunez (kodegeek.com@protonmail.com)

# Installation

To compile this project you will need:
* [Gradle](https://gradle.org/install/) 7.1+
* [Kotlin](https://kotlinlang.org/) plugin 1.6.10+
* [Junit](https://github.com/junit-team/junit4) for [unit testing](https://github.com/junit-team/junit4/wiki/Use-with-Gradle) 4.13+

This project has a copy of the gradle wrapper included. To install:

```shell
git clone git@github.com:josevnz/Covid19Informer.git
cd Covid19Informer
./gradlew wrapper --gradle-version=7.3.3 --distribution-type=bin
```

# Compilation

```shell
gradle clean test jar distTar
```

# Installation
```shell
/bin/tar --directory $HOME --extract --verbose --file build/distributions/Covid19Informer-*.tar 
```
## Don't miss the tutorial that comes with the code

[Scannin your code for third party vulnerabilities](tutorial/Scanning%20your%20Java%20code%20for%20third%20party%20vulnerabilities.md)