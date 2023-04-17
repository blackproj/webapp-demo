# Demo web app Java
[![made-with-java](https://img.shields.io/badge/Made%20with-Java-1f425f.svg)](https://www.java.com/en/) ![Made with love in Italy](https://madewithlove.now.sh/it?colorB=%231472a4) [![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT) [![Open Source? Yes!](https://badgen.net/badge/Open%20Source%20%3F/Yes%21/blue?icon=github)](https://github.com/Naereen/badges/)

Web app fully developed in Java from scratch with database support (MVC pattern)

* Register a new user to access the web app and use its functionalities
    * User password uses random salt, hash SHA-256 and AES-128 as cryptography from Java security
* Login into the app with or without cookies support. A web filter is used to prevent unauthorized login
    * User cookie are made from random salt, hash SHA-256 and AES-128 as criptography from Java security
    * Session ending automatically after 15 minutes of inactivity
* Upload plain text file as user project
    * Apache Tika is used as content detection parser and to mitigate Time-of-check to Time-of-use (TOCTTOU)
    * XSS mitigation through blacklist as sanity checking
* Logged user can list other user projects
* Any feature available employ a different user with different permission on database to improve security
* Logout user

## Getting Started

* First of all, register a new user to gain access to entire web app (choose an avatar is mandatory, only PNG or JPEG).
* After a successful login you can see the other users proposal (only plain text TXT) if someone has uploaded one yet or upload it a new one.

## Dependencies

* Java 8+
* Apache Tomcat 8+
* MySQL connector 8.0.27 or other connector for database
* Apache Tika 1.24.1
* JCIP annotation 

## Installation
* Download latest version.
* Setup your environment on Eclipse or similiar or use directly the web app deploying WAR file (deploy folder). 

## Documentation
* Full documentation is provided with statical and dynamical analysis only on Italian (manual folder).

## License

This project is licensed under the MIT License - see the LICENSE file for futher details.

_For any questions or doubts, feel free to contact me at gabriele.patta@outlook.com_