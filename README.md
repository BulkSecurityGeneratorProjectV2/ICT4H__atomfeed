atomfeed
========

[![Build Status](https://travis-ci.org/ICT4H-TW/atomfeed.png)](https://travis-ci.org/ICT4H-TW/atomfeed)

build
-----
To build:
* mvn compile
* mvn test

To install (assuming a Postgres DB is already created):
* mvn install -P IT -DskipTests

design
------
Our design assumes:
* We have a database with autoincrementing ids for non-time-based feed pagination.
* We need to support a clustered environment, so our only synchronisation point is the database.
