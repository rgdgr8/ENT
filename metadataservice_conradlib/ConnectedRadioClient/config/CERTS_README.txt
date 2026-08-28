the cacerts file must contain all certificats for gradle to contact eso artifactory and sonarqube

Import new certificate

keytool -importcert -alias eso_cert -file <fileneme.crt> -keystore cacerts -storepass changeit


keytool -list -v -keystore cacerts -storepass changeit