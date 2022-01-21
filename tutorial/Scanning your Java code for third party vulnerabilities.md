# Scanning your Java code for third party vulnerabilities

In a previous article I showed you how to detect third party vulnerabilities in Python. In this article I will show you how you can scan your Java code for the same issue.

# Example #1: Scanning the libraries of a well known Open Source project

Software is complex and thanks to Open Source we can quickly develop new applications by leveraging on the hard work of people who decided to share their work; Software also never sits still and new functionality introduces new bugs which can be exploited by malicious attackers. 

To illustrate the problem, I will download a vulnerable version of the well known Open Source application server:

```shell=
[josevnz@dmaf5 EnableSysadmin]$ curl --location --fail --output ~/Downloads/XXXX-A.B.C-zzzz.zip https://XXX.ZZZZ.org/dist/XXX/server/A.B.C/XXXX-A.B.C-zzzz.zip
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 88.8M  100 88.8M    0     0  5142k      0  0:00:17  0:00:17 --:--:-- 1194k

cd ~/Downloads
unzip XXXX-A.B.C-zzzz.zip
```

There are many tools out there to scan for vulnerabilities in your applications, I want to show you [OWASP dependency analyzer](https://owasp.org/www-project-dependency-check/) by Jeremy Long, so let's grab a copy:

```shell
[josevnz@dmaf5 Downloads]$ curl --fail --output ~/Downloads/dependency-check-6.5.3-release.zip --location https://github.com/jeremylong/DependencyCheck/releases/download/v6.5.3/dependency-check-6.5.3-release.zip
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   669  100   669    0     0   4430      0 --:--:-- --:--:-- --:--:--  4430
100 23.0M  100 23.0M    0     0  7653k      0  0:00:03  0:00:03 --:--:-- 8899k
```
And use it to check vulnerabilities on this application:

```shell
[josevnz@dmaf5 Downloads]$ ~/Downloads/dependency-check/bin/dependency-check.sh --prettyPrint --format HTML -scan /home/josevnz/Downloads/XXXX-A.B.C-zzzz/lib/
[INFO] Checking for updates
[INFO] Download Started for NVD CVE - Modified
[INFO] Download Complete for NVD CVE - Modified  (278 ms)
[INFO] Processing Started for NVD CVE - Modified
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by com.fasterxml.jackson.module.afterburner.util.MyClassLoader (file:/home/josevnz/Downloads/dependency-check/lib/jackson-module-afterburner-2.13.1.jar) to method java.lang.ClassLoader.findLoadedClass(java.lang.String)
WARNING: Please consider reporting this to the maintainers of com.fasterxml.jackson.module.afterburner.util.MyClassLoader
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
[INFO] Processing Complete for NVD CVE - Modified  (5150 ms)
[INFO] Begin database maintenance
[INFO] Updated the CPE ecosystem on 116793 NVD records
[INFO] Cleaned up 2 orphaned NVD records
[INFO] End database maintenance (24739 ms)
[INFO] Begin database defrag
[INFO] End database defrag (4660 ms)
[INFO] Check for updates complete (39967 ms)
[INFO] 

Dependency-Check is an open source tool performing a best effort analysis of 3rd party dependencies; false positives and false negatives may exist in the analysis performed by the tool. Use of the tool and the reporting provided constitutes acceptance for use in an AS IS condition, and there are NO warranties, implied or otherwise, with regard to the analysis or its use. Any use of the tool and the reporting provided is at the user’s risk. In no event shall the copyright holder or OWASP be held liable for any damages whatsoever arising out of or in connection with the use of this tool, the analysis performed, or the resulting report.


   About ODC: https://jeremylong.github.io/DependencyCheck/general/internals.html
   False Positives: https://jeremylong.github.io/DependencyCheck/general/suppression.html

💖 Sponsor: https://github.com/sponsors/jeremylong


[INFO] Analysis Started
[INFO] Finished Archive Analyzer (1 seconds)
[INFO] Finished File Name Analyzer (0 seconds)
[INFO] Finished Jar Analyzer (1 seconds)
[INFO] Finished Central Analyzer (9 seconds)
[ERROR] ----------------------------------------------------
[ERROR] .NET Assembly Analyzer could not be initialized and at least one 'exe' or 'dll' was scanned. The 'dotnet' executable could not be found on the path; either disable the Assembly Analyzer or add the path to dotnet core in the configuration.
[ERROR] ----------------------------------------------------
[INFO] Finished Dependency Merging Analyzer (0 seconds)
[INFO] Finished Version Filter Analyzer (0 seconds)
[INFO] Finished Hint Analyzer (0 seconds)
[INFO] Created CPE Index (2 seconds)
[INFO] Finished CPE Analyzer (6 seconds)
[INFO] Finished False Positive Analyzer (0 seconds)
[INFO] Finished NVD CVE Analyzer (0 seconds)
[INFO] Finished Sonatype OSS Index Analyzer (3 seconds)
[INFO] Finished Vulnerability Suppression Analyzer (0 seconds)
[INFO] Finished Dependency Bundling Analyzer (0 seconds)
[INFO] Analysis Complete (23 seconds)
[INFO] Writing report to: /home/josevnz/Downloads/./dependency-check-report.html
```

![Scan results for XXXX](https://github.com/josevnz/Covid19Informer/raw/main/tutorial/owasp_scan.png)

The report shows than the version I choose has several issues; The vendor fixed all of them on the latest release.

Not everything is perfect, and the tool [could generate false positives](https://jeremylong.github.io/DependencyCheck/general/suppression.html); But this is still a great start.

I'll show you next a different project, one we can fix ourselves

# Example #2 Analyze a homegrown COVID-19 town statistics reporter

This project downloads the COVID-19 statistics from the state of CT government portal and performs basic filtering before displaying the results.

Download snd compile with [Gradle](https://docs.gradle.org/current/userguide/userguide.html):

```shell
[josevnz@dmaf5 Covid19Informer]$ git clone https://github.com/jeremylong/Covid19Informer.git
[josevnz@dmaf5 Covid19Informer]$ gradle test jar 
[josevnz@dmaf5 Covid19Informer]$ gradle distTar
[josevnz@dmaf5 Covid19Informer]$ /bin/tar --directory $HOME --extract --verbose --file build/distributions/Covid19Informer-0.0.1.tar
[josevnz@dmaf5 Covid19Informer]$
:<<NOTE
You could also install by hand like this (I like the Gradle wrapper enough to avoid doing this):
/bin/mkdir --parent --verbose $HOME/Covid19Informer-0.0.1
/bin/cp --verbose build/libs/Covid19Informer-0.0.1.jar $HOME/Covid19Informer-0.0.1
/bin/curl --fail --location --output $HOME/Covid19Informer-0.0.1/lanterna-3.1.1.jar --url https://repo1.maven.org/maven2/com/googlecode/lanterna/lanterna/3.1.1/lanterna-3.1.1.jar
/bin/curl --fail --location --output $HOME/Covid19Informer-0.0.1/commons-io-2.11.0.jar --url https://repo1.maven.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar
NOTE 
```

And run it, to get an idea what this small application does. Normally we won't use a Uber jar in production, but instead we will deploy our java application and it's dependencies (jars) into a deployment directory:

```shell
josevnz@dmaf5 Covid19Informer]$ $HOME/Covid19Informer-0.0.1/bin/Covid19Informer
...
INFO: Covid19DataPerTown[lastUpdateDate=2022-01-19, townNumber=135, town='Stamford', totalCases=26239, confirmedCases=24448, probableCases=1791, caseRate=135.0, totalDeaths=349, confirmedDeaths=308, probableDeaths=41, peopleTested=121435, rateTestedPer100k=93672.0, numberOfTests=383071, numberOfPositives=33471, numberOfNegatives=308, numberOfIndeterminates=422]
Jan 20, 2022 7:39:13 PM com.kodegeek.covid19.towndata.TownDataRetriever$Covid19DataPerTown lambda$printCovidData$1
INFO: Covid19DataPerTown[lastUpdateDate=2022-01-19, townNumber=151, town='Waterbury', totalCases=29064, confirmedCases=25500, probableCases=3564, caseRate=151.0, totalDeaths=444, confirmedDeaths=378, probableDeaths=66, peopleTested=99611, rateTestedPer100k=92603.0, numberOfTests=423805, numberOfPositives=36882, numberOfNegatives=378, numberOfIndeterminates=787]
```

Analyze the dependencies:

```shell
[josevnz@dmaf5 Covid19Informer]$ ~/Downloads/dependency-check/bin/dependency-check.sh --prettyPrint --format HTML -scan $HOME/Covid19Informer-0.0.1/lib
[INFO] Checking for updates
[INFO] Skipping NVD check since last check was within 4 hours.
[INFO] Skipping RetireJS update since last update was within 24 hours.
[INFO] Check for updates complete (62 ms)
[INFO] 

Dependency-Check is an open source tool performing a best effort analysis of 3rd party dependencies; false positives and false negatives may exist in the analysis performed by the tool. Use of the tool and the reporting provided constitutes acceptance for use in an AS IS condition, and there are NO warranties, implied or otherwise, with regard to the analysis or its use. Any use of the tool and the reporting provided is at the user’s risk. In no event shall the copyright holder or OWASP be held liable for any damages whatsoever arising out of or in connection with the use of this tool, the analysis performed, or the resulting report.


   About ODC: https://jeremylong.github.io/DependencyCheck/general/internals.html
   False Positives: https://jeremylong.github.io/DependencyCheck/general/suppression.html

💖 Sponsor: https://github.com/sponsors/jeremylong


[INFO] Analysis Started
[INFO] Finished Archive Analyzer (0 seconds)
[INFO] Finished File Name Analyzer (0 seconds)
[INFO] Finished Jar Analyzer (0 seconds)
[INFO] Finished Central Analyzer (0 seconds)
[INFO] Finished Dependency Merging Analyzer (0 seconds)
[INFO] Finished Version Filter Analyzer (0 seconds)
[INFO] Finished Hint Analyzer (0 seconds)
[INFO] Created CPE Index (1 seconds)
[INFO] Finished CPE Analyzer (2 seconds)
[INFO] Finished False Positive Analyzer (0 seconds)
[INFO] Finished NVD CVE Analyzer (0 seconds)
[INFO] Finished Sonatype OSS Index Analyzer (0 seconds)
[INFO] Finished Vulnerability Suppression Analyzer (0 seconds)
[INFO] Finished Dependency Bundling Analyzer (0 seconds)
[INFO] Analysis Complete (2 seconds)
[INFO] Writing report to: /home/josevnz/Documents/Covid19Informer/./dependency-check-report.html
```
Application is clean. But it doesn't mean we caught all the issues. Let me elaborate:
* We didn't check the test or integration dependencies if any. Only the runtime dependencies (which is good enough for most cases)
* This approach is reactive; it means we are catching the problem **after** it happens, not during the development cycle.

Let see next how we can do better when we have access to the source code.


# Example #3: Being proactive and scanning Covid19Informer as soon we can compile our code

In order to use it as part of our continuous integration we add it into the build.gradle.kts

```kotlin
plugins {
    `java-library`
    application
    id("org.owasp.dependencycheck") version "6.5.3"
}
```

The using it is just matter of calling gradle like this:
```shell
[josevnz@dmaf5 Covid19Informer]$ gradle dependencyCheckAnalyze --info
...
One or more dependencies were identified with known vulnerabilities in Covid19Informer:

h2-1.4.199.jar (pkg:maven/com.h2database/h2@1.4.199, cpe:2.3:a:h2database:h2:1.4.199:*:*:*:*:*:*:*) : CVE-2021-23463, CVE-2021-42392


See the dependency-check report for more details.


Element event queue destroyed: org.apache.commons.jcs.engine.control.event.ElementEventQueue@12440215
In DISPOSE, [NODEAUDIT] fromRemote [false]
In DISPOSE, [NODEAUDIT] auxiliary [NODEAUDIT]
...
BUILD SUCCESSFUL in 4s
2 actionable tasks: 2 executed
Some of the file system contents retained in the virtual file system are on file systems that Gradle doesn't support watching. The relevant state was discarded to ensure changes to these locations are properly detected. You can override this by explicitly enabling file system watching.
Watching 24 directories to track changes

```

A few things note:
* The build went through but there was a warning: A vulnerable jar was found in my gradle cache (File Path: /home/josevnz/.gradle/caches/modules-2/files-2.1/com.h2database/h2/1.4.199/7bf08152984ed8859740ae3f97fae6c72771ae45/h2-1.4.199.jar). 
It is there from a previous test I did, so just keep this in mind if you run into some false positives when using this tool.
* This gradle scanner downloads a lot of data the first time. After that it stabilizes using the local cache content.

For that reason, let me show you another different Gradle plugin that can also be used to scan for vulnerabilities.

# Example #4: Using a different tool to scan Covid19Informer as soon is compiled

The folks from Sonatype created a Gradle plugin you can use to scan your project called [scan-gradle-plugin](https://github.com/sonatype-nexus-community/scan-gradle-plugin/#readme), which is baked by their [OSS Index catalog](https://ossindex.sonatype.org/)

By now you can see where this is going; by having this check as part of your Java compilation toolset you can have your [continuous integration](https://en.wikipedia.org/wiki/Continuous_integration) toold running this scan every time the code changes, reporting any anomalies back to you, before the code gets deployed into production.

To make this happen we add the following to the build.gradle.kts file:

```kotlin
plugins {
    `java-library`
    id ("org.sonatype.gradle.plugins.scan") version "2.2.2"
}

ossIndexAudit {
    username = System.getenv("ossindexaudit_user")
    password = System.getenv("ossindexaudit_password")
}
```
And then scan for vulnerabilities (they recommend creating a free account that has no limits on the number of times you call the service at https://ossindex.sonatype.org/):

```shell
[josevnz@dmaf5 Covid19Informer]$ gradlew test jar
# These 2 variables will be 'injected' on your continuous integration environment
[josevnz@dmaf5 Covid19Informer]$ read -r -p "Please enter your Sonatype user (like myemail@example.com): " ossindexaudit_user
[josevnz@dmaf5 Covid19Informer]$ read -r -p -s "Please enter your Sonatype account password" ossindexaudit_user
[josevnz@dmaf5 Covid19Informer]$ export ossindexaudit_user ossindexaudit_user
[josevnz@dmaf5 Covid19Informer]$ gradle ossIndexAudit 

> Task :ossIndexAudit
 ________  ________  ________  ________  ___       _______           ________  ________  ________  ________
|\   ____\|\   __  \|\   __  \|\   ___ \|\  \     |\  ___ \         |\   ____\|\   ____\|\   __  \|\   ___  \
\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \ \  \    \ \   __/|        \ \  \___|\ \  \___|\ \  \|\  \ \  \\ \  \
 \ \  \  __\ \   _  _\ \   __  \ \  \ \\ \ \  \    \ \  \_|/__       \ \_____  \ \  \    \ \   __  \ \  \\ \  \
  \ \  \|\  \ \  \\  \\ \  \ \  \ \  \_\\ \ \  \____\ \  \_|\ \       \|____|\  \ \  \____\ \  \ \  \ \  \\ \  \
   \ \_______\ \__\\ _\\ \__\ \__\ \_______\ \_______\ \_______\        ____\_\  \ \_______\ \__\ \__\ \__\\ \__\
    \|_______|\|__|\|__|\|__|\|__|\|_______|\|_______|\|_______|       |\_________\|_______|\|__|\|__|\|__| \|__|
                                                                       \|_________|


  _      _                       _   _
 /_)    /_`_  _  _ _/_   _  _   (/  /_`_._  _   _/ _
/_)/_/ ._//_// //_|/ /_//_//_' (_X /  ///_'/ //_/_\
   _/                _//

Gradle Scan version: 2.2.2
------------------------------------------------------------------------------------------------------------------------------------------------------

Checking vulnerabilities in 1 dependencies
No vulnerabilities found!

BUILD SUCCESSFUL in 758ms
1 actionable task: 1 executed
[josevnz@dmaf5 Covid19Informer]$ 
```

So everything is good, right?

Not quite. Let's take a closer look at my project again:

```kotlin
dependencies {
    implementation("com.googlecode.lanterna:lanterna:3.1.1")
    implementation("commons-cli:commons-cli:20040117.000000")
    testImplementation("junit:junit:4.13")
}
```

The scanner found my compilation dependencies BUT ignored my testing dependencies, in this case [Junit](https://github.com/junit-team/junit4/wiki/Use-with-Gradle); it turns out the version I'm using on my build file has a vulnerability from 2020: [JUnit CVE-2020-15250](https://nvd.nist.gov/vuln/detail/CVE-2020-15250)
Why did that happened? You need to have 'isAllConfigurations = true' property set to true on the build.gradle.kts:

```kotlin
ossIndexAudit {
    username = System.getenv("ossindexaudit_user")
    password = System.getenv("ossindexaudit_password")
    isAllConfigurations = true
}
```

Let's try again:

```shell
[josevnz@dmaf5 Covid19Informer]$ gradle jar; gradle ossIndexAudit 

BUILD SUCCESSFUL in 2s
3 actionable tasks: 3 up-to-date

> Task :ossIndexAudit FAILED
 ________  ________  ________  ________  ___       _______           ________  ________  ________  ________
|\   ____\|\   __  \|\   __  \|\   ___ \|\  \     |\  ___ \         |\   ____\|\   ____\|\   __  \|\   ___  \
\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \ \  \    \ \   __/|        \ \  \___|\ \  \___|\ \  \|\  \ \  \\ \  \
 \ \  \  __\ \   _  _\ \   __  \ \  \ \\ \ \  \    \ \  \_|/__       \ \_____  \ \  \    \ \   __  \ \  \\ \  \
  \ \  \|\  \ \  \\  \\ \  \ \  \ \  \_\\ \ \  \____\ \  \_|\ \       \|____|\  \ \  \____\ \  \ \  \ \  \\ \  \
   \ \_______\ \__\\ _\\ \__\ \__\ \_______\ \_______\ \_______\        ____\_\  \ \_______\ \__\ \__\ \__\\ \__\
    \|_______|\|__|\|__|\|__|\|__|\|_______|\|_______|\|_______|       |\_________\|_______|\|__|\|__|\|__| \|__|
                                                                       \|_________|


  _      _                       _   _
 /_)    /_`_  _  _ _/_   _  _   (/  /_`_._  _   _/ _
/_)/_/ ._//_// //_|/ /_//_//_' (_X /  ///_'/ //_/_\
   _/                _//

Gradle Scan version: 2.2.2
------------------------------------------------------------------------------------------------------------------------------------------------------

Checking vulnerabilities in 2 dependencies
Found vulnerabilities in 1 dependencies
[1/1] - pkg:maven/junit/junit@4.7 - 1 vulnerability found!

   Vulnerability Title:  [CVE-2020-15250] In JUnit4 from version 4.7 and before 4.13.1, the test rule TemporaryFolder cont...
   ID:  7ea56ad4-8a8b-4e51-8ed9-5aad83d8efb1
   Description:  In JUnit4 from version 4.7 and before 4.13.1, the test rule TemporaryFolder contains a local information disclosure vulnerability. On Uni...
   CVSS Score:  (5.5/10, Medium)
   CVSS Vector:  CVSS:3.0/AV:L/AC:L/PR:N/UI:R/S:U/C:H/I:N/A:N
   CVE:  CVE-2020-15250
   Reference:  https://ossindex.sonatype.org/vulnerability/7ea56ad4-8a8b-4e51-8ed9-5aad83d8efb1?component-type=maven&component-name=junit.junit&utm_source=ossindex-client&utm_medium=integration&utm_content=1.7.0


FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':ossIndexAudit'.
> Vulnerabilities detected, check log output to review them

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.
Use '--warning-mode all' to show the individual deprecation warnings.
See https://docs.gradle.org/7.0/userguide/command_line_interface.html#sec:command_line_warnings

BUILD FAILED in 1s
1 actionable task: 1 executed
```

Great!. Fixing this is trivial for us, just upgrade Junit to the latest coordinates (at this time junit:junit:4.13.2) in build.gradle.kts and compile again:

```kotlin
dependencies {
    implementation("com.googlecode.lanterna:lanterna:3.1.1")
    implementation("commons-io:commons-io:2.11.0")
    testImplementation("junit:junit:4.13.2")
}
```

```shell
Checking vulnerabilities in 3 dependencies
No vulnerabilities found!

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.
Use '--warning-mode all' to show the individual deprecation warnings.
See https://docs.gradle.org/7.0/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 2s
1 actionable task: 1 executed
```

### Side-note: What about an IDE. Will they tell if you are using an older version of a library?

Most of them will do, and you should not ignore those warnings. For example the Community Edition of IntelliJ caught the old Junit and marked my build gradle file with a warning:

[](file:///intellij_gradle_warning.png)

Other IDE like VSCode do the same.

## There are any other tools out there?

The answer is yes!. They do support other programing languages and tools, and you are encouraged to try them and see if they fit your use cases:

* [Sast-Scan](https://github.com/ShiftLeftSecurity/sast-scan): It supports an pretty wide array of languages and platforms it can scan, even containers.

## What did we learn so far?

* How to analyze projects using [OWASP Dependency check](https://owasp.org/www-project-dependency-check/)
* How to fix our projects if a vulnerable dependency is found (in our case by fixing the build.gradle.kts file)
* How to add [vulnerability checks](https://blog.sonatype.com/new-sonatype-scan-gradle-plugin) to your continuous integration using the sonatype-scan-gradle-plugin.

Now you are more prepared to check your projects for third party vulnerabilities.
