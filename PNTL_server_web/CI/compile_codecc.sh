#! /bin/bash

mvn -f ../pom.xml clean package -Dmaven.test.skip=true -Dencoding=utf-8 -Dmaven.compiler.fork=true
