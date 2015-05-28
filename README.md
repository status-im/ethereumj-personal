# Welcome to EthereumJ for Android

# About
EthereumJ is a pure-Java implementation of the Ethereum protocol. For high-level information about Ethereum and its goals, visit [ethereum.org](https://ethereum.org). The [ethereum white paper](https://github.com/ethereum/wiki/wiki/%5BEnglish%5D-White-Paper) provides a complete conceptual overview, and the [yellow paper](http://gavwood.com/Paper.pdf) provides a formal definition of the protocol.

This is an Android-optimized fork of [EthereumJ](https://github.com/ethereum/ethereumj).

This repository consists of:
 * [ethereum-core](ethereumj-core): a android compatible library of ethereumj-core .
 * [app]: a simple mockup GUI for exploring Ethereum functionality and usage.

# Todo
This is a work in progress.

# Gotchas
Old devices will fail to auth in time and will be rejected by peers, please use modern devices.

# Building from source

 - Clone this repository and run
 `./gradlew antlr4; ./gradlew build -x test`
 - Install onto Android Device 
 `adb install -r app/build/outputs/apk/app-debug.apk`

# License
EthereumJ is released under the [MIT license](LICENSE).
