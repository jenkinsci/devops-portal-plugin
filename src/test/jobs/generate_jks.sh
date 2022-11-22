#!/bin/sh

# Ce script permet de créer un fichier JKS avec les certificats appropriés pour lancer la tâche d'audit.
# Il permet d'inclure les certificats du serveur registry.trustingenierie.com et de maven central.

JKS_FILE="test.jks"
PASSWORD="123456789"

# Récupération des certificats
openssl s_client -connect "registry.trustingenierie.com:443" </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > tmp_cert_registry
openssl s_client -connect "repo.maven.apache.org:443" </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > tmp_cert_maven
openssl s_client -connect "repo.jenkins-ci.org:443" </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > tmp_cert_jenkins

# Ajout des signatures dans le JKS
keytool -import -noprompt -trustcacerts -keystore "$JKS_FILE" -storepass "$PASSWORD" -alias "registry.trustingenierie.com" -file tmp_cert_registry
keytool -import -noprompt -trustcacerts -keystore "$JKS_FILE" -storepass "$PASSWORD" -alias "repo.maven.apache.org" -file tmp_cert_maven
keytool -import -noprompt -trustcacerts -keystore "$JKS_FILE" -storepass "$PASSWORD" -alias "repo.jenkins-ci.org" -file tmp_cert_jenkins

# Affichage du contenu du JKS
keytool -list -v -keystore "$JKS_FILE" -storepass "$PASSWORD"
