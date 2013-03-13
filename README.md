Installation du poste : 
 

## Repo Maven
############################
- JDK 6 : http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html
- Maven : http://maven.apache.org/
- git :
- Android SDK : USE AN EXISTING IDE > Download the SDK Tools for http://developer.android.com/sdk/index.html
- Eclipse IDE for Java EE Developers : http://www.eclipse.org/downloads/
- Android Eclipse plugin : http://developer.android.com/sdk/installing/installing-adt.html

# Charger les version android
############################
cd $ANDROID_HOME/tools
./android
Tous télécharger (srrement plus simple)

# Pour le creer a partir du ANDROID_HOME
git clone https://github.com/mosabua/maven-android-sdk-deployer.git
cd maven-android-sdk-deployer

mvn install -P 4.1.2


## Define system variable
############################
JAVA_HOME=
MAVEN_HOME=
ANDROID_HOME=

PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools

## Installation IDE
############################
- Help > Install new software >
   > m2e : Maven integration pour Eclipse
   
- Help > eclipse Marcket place > Rechercher : Android
  > Android Configurator for M2E
     
   

