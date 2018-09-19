How to build?
1. You need to have Sawtooth Java SDK installed in your local maven repository
   Steps to do that would be:
   > Clone https://github.com/hyperledger/sawtooth-sdk-java.git
   > From project root folder where pom.xml is found, run
     mvn clean generate-sources package install
     Note: You might need to set proxy settings in maven configuration settings.xml file
           Alternatively supply proxy settings to the mvn command above.
2. Go to contentprotection's pom.xml file location, run
   mvn clean package

=========
Thank You
=========