# Welcome to EthereumJ for Android

# About
EthereumJ is a pure-Java implementation of the Ethereum protocol. For high-level information about Ethereum and its goals, visit [ethereum.org](https://ethereum.org). The [ethereum white paper](https://github.com/ethereum/wiki/wiki/%5BEnglish%5D-White-Paper) provides a complete conceptual overview, and the [yellow paper](http://gavwood.com/Paper.pdf) provides a formal definition of the protocol.

This is an Android-optimized fork of [EthereumJ](https://github.com/ethereum/ethereumj).

This repository consists of:
 * [ethereum-core](ethereumj-core): a android compatible library of ethereumj-core .
 * [app]: a simple mockup GUI for exploring Ethereum functionality and usage.

# Deviations from EthereumJ

### Code:
- replaced Spring Framework Dependency Injection with Dagger 2 DI;
- replaced org.springframework.util.FileSystemUtils with org.apache.commons.io.FileUtils;
- replaced some worldManager injections with specific item injections from worldManager which were used;
- replaced org.springframework.util.StringUtils.isEmpty with String.isEmpty();
- replaced @PostConstruct annotation with explicit method invocation in contructor;
- replaced java.nio.file.Files with org.apache.commons.io.FileUtils;
- replaced javax.xml.bind.DatatypeConverter.hexStringToByteArray with custom implementation;
- commented javax.swing references in Utils.java;

### Gradle:
- changed from java build to android build;
- updated netty-all from 4.0.23 to 4.0.28;
- disabled org.antlr:antlr4-runtime:4.5 ( use 4.0 from com.yuvalshavit:antlr-denter:1.1 );
- replaced org.slf4j:slf4j-api:1.7.7 with org.slf4j:slf4j-android:1.7.12
- removed org.javassist:javassist:3.15.0-GA;
- removed log4j:log4j:${log4jVersion};
- removed org.hibernate:hibernate-core:${hibernateVersion};
- removed org.hibernate:hibernate-entitymanager:${hibernateVersion};
- removed commons-dbcp:commons-dbcp:1.4;
- removed redis.clients:jedis:2.6.0;
- removed org.slf4j:slf4j-log4j12:${slf4jVersion};
- removed log4j:apache-log4j-extras:${log4jVersion};

# Todo
- Android-specific BlockStore Implementation
- Unit Tests

This is a work in progress.

# Gotchas
Old devices will fail to auth in time and will be rejected by peers, please use modern devices.

# Building from source

 - Clone this repository and run (make sure clean from previous builds)
 `./gradlew build`
 - Install onto Android Device 
 `adb install -r app/build/outputs/apk/app-debug.apk`

# License
EthereumJ is released under the [MIT license](LICENSE).
