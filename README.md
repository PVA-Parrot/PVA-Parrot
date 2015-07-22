# PVA-Parrot

PVA-Parrot is a GUI application for doing polytopic vector analysis. This is the
main application.

# Installation

## Docker container

A Docker container is available from DockerHub `pvaparrot/pva-parrot`.

## Manually

A JVM installation is required.

[Boot][1] needs to be installed, download [boot.sh][2], then:

`mv boot.sh boot && chmod a+x boot && sudo mv boot /usr/local/bin`

Once you run a boot task, it will install the required dependencies.

# Usage

Help is available for boot via `boot help`. This also lists all the available
tasks. PVA-Parrot specific tasks are defined in `build.boot`.

## Run application with backend

Simply run `boot dev`

This will make the frontend available under http://localhost:3000/
The backend is available under http://localhost:3333

## Troubleshooting
### JVM Config and Java 8

boot-clj suggests some options for boot to prevent issues with the JVM:
[https://github.com/boot-clj/boot/wiki/JVM-Options][boot JVM Options]

`echo $BOOT_JVM_OPTIONS -Xmx2g -client -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:MaxPermSize=128m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Xverify:none`

I still had memory issues when running boot tasks with such a configuration.
Upgrading to Java 8 seems to have fixed these issues. So, I recommend using
Java 8.

# Building

## Docker

Build a Docker image via `sudo docker build -t pvaparrot/pva-parrot .`

## Documentation

API documentation can be generated with `boot apidoc`. The resulting
documentation can be found in `target/doc`.

# Tests

## Continuous integration

[![Build Status](https://circleci.com/gh/PVA-Parrot/PVA-Parrot.svg?style=shield&circle-token=24f51fc606459d9ab4e663493f91bf07cb16f584)](https://circleci.com/gh/PVA-Parrot/PVA-Parrot)

Circle CI is being used as a CI solution:
https://circleci.com/gh/PVA-Parrot/PVA-Parrot

Configuration can be found in `circle.yml`

The Docker image gets build on the CI, tested and then pushed to Docker Hub
automatically.

## Running tests

- Directly in local environment: `boot tests`
- From Docker container: `sudo docker run pvaparrot/pva-parrot`

## Environments

Environments are configured in `build.boot` and are used via function
composition (see tasks `prod` and `dev` for examples).

# Roadmap

- Containerization with Docker
- Use [CLJS-JS][3] for CLJS dependencies

[1]: http://boot-clj.com/
[2]: https://github.com/boot-clj/boot/releases/download/2.0.0/boot.sh
[3]: https://github.com/cljsjs/packages
