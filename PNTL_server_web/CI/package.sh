#!/bin/bash
mvn clean package -Dmaven.test.skip=true -f ../pom.xml
mv ../blackhole-chkflow-network/blackhole-chkflow-server/target/chkflow-server.war ./
mv ../blackhole-chkflow-console/target/chkflow-console.war ./
