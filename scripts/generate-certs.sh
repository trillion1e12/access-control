#!/bin/sh
# Creates, for local testing only: a certificate authority, the Nginx server
# certificate for localhost, and one Kiosk client certificate. Files go to certs/.
set -eu

cd "$(dirname "$0")/../certs"

openssl req -x509 -newkey rsa:2048 -nodes -days 365 \
    -subj "/CN=TMC Local Test CA" -keyout ca.key -out ca.crt

openssl req -newkey rsa:2048 -nodes \
    -subj "/CN=localhost" -keyout server.key -out server.csr
printf "subjectAltName=DNS:localhost,IP:127.0.0.1\n" > server.ext
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
    -days 365 -extfile server.ext -out server.crt

openssl req -newkey rsa:2048 -nodes \
    -subj "/CN=kiosk-gate-1" -keyout kiosk.key -out kiosk.csr
openssl x509 -req -in kiosk.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
    -days 365 -out kiosk.crt

rm -f server.csr server.ext kiosk.csr
echo "Certificates written to $(pwd)"
