# Ubuntu Installation
This was tested on Ubuntu 18.04.3 LTS but should be similar on other linux distributions.

Please mind that at the time of the writing (Dec 2019) the compilation is possible only with Java 1.8.

Here are the steps needed to be done:

1. Install the prerequisite tools: sudo apt install git maven openjdk-8-jdk
2. Clone this repository
3. Clone the [IcedTea-Web](https://github.com/AdoptOpenJDK/IcedTea-Web) project.
4. `export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/` - in case you have multiple java installations it is safer
5. Go to the IcedTea-Web clone directory and type: mvn install
6. Go back to this clone and type: ./mvnw install




