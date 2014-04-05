#!/bin/sh
mkdir -p src/generated/java
xjc -b jaxb/bindings.xjb -p com.connectifier.xeroclient.models -d src/generated/java ../XeroAPI-Schemas/v2.00/
