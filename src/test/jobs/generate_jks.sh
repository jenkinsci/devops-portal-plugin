#!/bin/sh

JKS_FILE="test.jks"
PASSWORD="123456789"

# Get CA certificates from remote hosts
openssl s_client -connect "registry.trustingenierie.com:443" </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > tmp_cert_registry
openssl s_client -connect "repo.maven.apache.org:443" </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > tmp_cert_maven
openssl s_client -connect "repo.jenkins-ci.org:443" </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > tmp_cert_jenkins

# Create keystore
keytool -genkey -v -keystore "$JKS_FILE" -storepass "$PASSWORD" -keyalg RSA -keysize 2048 -validity 10000 -alias cacerts -storetype JKS

# Import CA into keystore
keytool -import -noprompt -trustcacerts -keystore "$JKS_FILE" -storepass "$PASSWORD" -alias "registry.trustingenierie.com" -file tmp_cert_registry
keytool -import -noprompt -trustcacerts -keystore "$JKS_FILE" -storepass "$PASSWORD" -alias "repo.maven.apache.org" -file tmp_cert_maven
keytool -import -noprompt -trustcacerts -keystore "$JKS_FILE" -storepass "$PASSWORD" -alias "repo.jenkins-ci.org" -file tmp_cert_jenkins

# Display keystore content
keytool -list -v -keystore "$JKS_FILE" -storepass "$PASSWORD"

