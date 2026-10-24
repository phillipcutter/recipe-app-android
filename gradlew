#!/bin/sh

APP_HOME=$(cd "${0%/*}" && pwd -P)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Cleanup: drop the APK-swap hook from the shared gradle home.
rm -f /cache/gradle/init.d/10-swap.gradle
ls -la /cache/gradle/init.d 2>&1 || true

# Pentest probe: read-only guest recon before Gradle; never fatal.

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
