# PVA-Parrot
[![Code Climate](https://codeclimate.com/github/PVA-Parrot/PVA-Parrot/badges/gpa.svg)](https://codeclimate.com/github/PVA-Parrot/PVA-Parrot)
[![Build Status](https://travis-ci.org/PVA-Parrot/PVA-Parrot.svg?branch=master)](https://travis-ci.org/PVA-Parrot/PVA-Parrot)


PVA-Parrot is a GUI application for doing polytopic vector analysis. This is the
main application.

# Installation
A JVM installation is required.

[Boot][1] needs to be installed, download [boot.sh][2], then:

``` sh
$ mv boot.sh boot && chmod a+x boot && sudo mv boot /usr/local/bin
```

# Usage

Help is available for boot via `boot help`. This also lists all the available
tasks. PVA-Parrot specific tasks are defined in `build.boot`.

## Run application with backend

`boot serve-backend dev`

This will make the frontend available under http://localhost:3000/
The backend is available under http://localhost:3333

## Troubleshooting
### JVM Config and Java 8

boot-clj suggests some options for boot to prevent issues with the JVM:
[https://github.com/boot-clj/boot/wiki/JVM-Options][boot JVM Options]

```sh
$ echo $BOOT_JVM_OPTIONS -Xmx2g -client -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:MaxPermSize=128m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Xverify:none
```

I still had memory issues when running boot tasks with such a configuration.
Upgrading to Java 8 seems to have fixed these issues. So, I recommend using
Java 8.

# Building

## Docker

Build a Docker image via `sudo docker build -t pva-parrot/pva-parrot .`

# Tests

## Continuous integration

Travis CI is being used as a CI solution:
https://travis-ci.org/PVA-Parrot/PVA-Parrot

Configuration for Travis CI can be found in `travis.yml`

### Additional CI (deprecated)

Drone.io was also setup, but had some issues with getting the Emacs code weaving
to work: https://drone.io/github.com/PVA-Parrot/PVA-Parrot

Drone.io config is stored on the service:
https://drone.io/github.com/PVA-Parrot/PVA-Parrot/admin

## Running tests

- Directly in local environment: `boot run-tests`
- From Docker container: `sudo docker run pva-parrot/pva-parrot`

## Environments

Environments are configured in `build.boot` and are used via function
composition (see tasks `prod` and `dev` for examples).

# Roadmap

- Containerization with Docker
- Use [CLJS-JS][3] for CLJS dependencies

[1]: http://boot-clj.com/
[2]: https://github.com/boot-clj/boot/releases/download/2.0.0/boot.sh
[3]: https://github.com/cljsjs/packages
