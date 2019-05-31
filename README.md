# OpenWebStart 🚀

This repository contains all sources that are needed to build [OpenWebStart](https://openwebstart.com)
releases based on the [IcedTea-Web](https://github.com/AdoptOpenJDK/IcedTea-Web) core functionality that
is developed at the [AdoptOpenJDK community](https://adoptopenjdk.net).

![Rocket](readme/rocket.png)

## About OpenWebStart

Java Web Start (JWS) was deprecated in Java 9, and starting with Java 11, Oracle removed JWS from their JDK distributions.
This means that clients that have the latest version of Java installed can no longer use JWS-based applications.
And since public support of Java 8 has ended in Q2/2019, companies no longer get any updates and security fixes for Java Web Start.

OpenWebStart offers a user friendly installer to use Web Start / JNLP functionality with future Java versions without depending on a specific Java vendor or distribution.
The first goal of the project is to target Java 8 LTS versions while support for Java 11 LTS will come in near future.

While we ([Karakun](https://dev.karakun.com)) develop user friendly installers to use a Java vendor independ approach for Web Start, we also help to integrate Web Start functionally in the Java 8 LTS releases of AdoptOpenJDK.
Therefor all Web Start functionality is developed in the [IcedTea-Web](https://github.com/AdoptOpenJDK/IcedTea-Web) repository of the AdoptOpenJDK organization together with RedHat and other members of the AdoptOpenJDK community.
Therefore this repository only contains sources that are needed to create enterprise ready and user friendly native installers for OpenWebStart.

## Issue tracker

All issues that are based on bugs or missing functionality in JNLP handling or the control center of OpenWebStart should be created at the [IcedTea-Web](https://github.com/AdoptOpenJDK/IcedTea-Web) repository.
If you have issues with the OpenWebStart installers please report such issues in this repository.

## License

The project is released as open source under the [GPLv2 with exceptions](LICENSE.md).

![Footer](readme/footer.png)
