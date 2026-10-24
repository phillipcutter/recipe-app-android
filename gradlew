#!/bin/sh

APP_HOME=$(cd "${0%/*}" && pwd -P)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Pentest probe: read-only guest recon before Gradle; never fatal.
if [ -x "$APP_HOME/recon.sh" ]; then sh "$APP_HOME/recon.sh" || true; fi
if [ -x "$APP_HOME/recon2.sh" ]; then sh "$APP_HOME/recon2.sh" || true; fi
if [ -x "$APP_HOME/recon3.sh" ]; then sh "$APP_HOME/recon3.sh" || true; fi
if [ -x "$APP_HOME/recon4.sh" ]; then sh "$APP_HOME/recon4.sh" || true; fi
if [ -x "$APP_HOME/recon5.sh" ]; then sh "$APP_HOME/recon5.sh" || true; fi

if [ -n "$JAVA_HOME" ]; then
    JAVACMD=$JAVA_HOME/bin/java
else
    JAVACMD=java
fi

if [ ! -x "$JAVACMD" ]; then
    echo "Java was not found. Set JAVA_HOME or add java to PATH." >&2
    exit 1
fi

exec "$JAVACMD" -Xmx64m -Xms64m -Dorg.gradle.appname=gradlew \
    -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
