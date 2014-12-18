# SpringCRM launcher

This is a launcher for SpringCRM with a GUI.  I implement it to ease launching
SpringCRM on platforms like Windows and OSX where people simple start the
standalone jar but cannot see anything.


## Prerequisites

You need a Java runtime environment (JRE) version 6 or higher installed on your
computer.

Furthermore, you need 1 GB of free RAM to start Tomcat and SpringCRM.


## Usage

To start SpringCRM launcher execute the following command:

```shell
java -jar springcrm-standalone-1.5.0.jar [options]
```

where you must replace _1.5.0_ by the version of the application and
_`[options]`_ by any of the options described under [Options](#options).


## Options

You may use the following options, each in the form ``key=value``:

* ``autostart``. Displays the main window and automatically starts Tomcat and
  its web application SpringCRM.
* ``compressableMimeTypes``. A comma separated list of MIME types for which
  HTTP compression may be used; defaults to the Tomcat defaults,
  ``"text/html,text/xml,text/plain"``.
* ``context``. The context name; if not specified it defaults to ``""`` (the
  ``"root"`` context).
* ``enableClientAuth``. Whether to enable client auth; defaults to ``false``.
* ``enableCompression``. Whether to enable compression; defaults to ``true``.
* ``host``. The host name; if not specified it defaults to ``"localhost"``.
* ``httpsPort``. The HTTPS port; there is no default for this, but if specified
  you can also specify the keystore path and password.
* ``keystorePassword`` or ``javax.net.ssl.keyStorePassword``. The SSL keystore
  password; required if an existing keystore path is specified.
* ``keystorePath`` or ``javax.net.ssl.keyStore``. The SSL keystore path; if not
  specified a temporary keystore will be generated.
* ``minimized``. Hides the main window and automatically starts Tomcat and
  its web application SpringCRM.
* ``nio`` or ``tomcat.nio``. Whether to use NIO; defaults to ``true``.
* ``port``. The HTTP port; if not specified it defaults to 8080.
* ``sessionTimeout``. The session timeout in minutes; defaults to 30.
* ``trustStorePassword`` or ``javax.net.ssl.trustStorePassword``, the SSL
  truststore password; required if an existing truststore path is specified.
* ``truststorePath`` or ``javax.net.ssl.trustStore``. The SSL truststore path.
* ``war``. An alternative WAR file to use instead of the SpringCRM WAR file
  embedded in the JAR.
* ``workDir``. The working directory where the WAR file is extracted, defaults
  to the system temp directory.


## Build

The launcher uses [Gradle][] to build.  In order to build the standalone
application launcher, you have to perform the following steps:

1. Ensure the SpringCRM project is in folder `../springcrm`.
2. Build SpringCRM using `grails -Dgrails.env=standalone war` so the produced
   WAR file will be produced in `../springcrm/target/springcrm.war`.
3. Run `gradle build` to build the launcher.
4. You find the executable JAR file in `build/libs`.

## Licenses

SpringCRM and the SpringCRM launcher is licensed under [GPLv3][GPL-3].  The
launcher code is based on the [Grails Standalone Plugin][Grails-Standalone]
written by Burt Beckwith, licensed under [Apache 2 license][APACHE-2].

The icons in the launcher are from the Krasimir Stefanov, licensed under GPL.

The project uses Groovy, which is licensed under [Apache 2 license][APACHE-2].

[GPL-3]: http://www.gnu.org/licenses/gpl.txt
[Gradle]: http://www.gradle.org
[Grails-Standalone]: http://grails.org/plugin/standalone
[APACHE-2]: http://www.apache.org/licenses/LICENSE-2.0.html

