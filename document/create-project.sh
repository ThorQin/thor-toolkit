#!/bin/bash
mvn archetype:generate 
  -DarchetypeCatalog=http://10.63.2.111:8081/nexus/content/repositories/snapshots
  -DarchetypeGroupId=com.github.thorqin
  -DarchetypeArtifactId=thor-toolkit-archetype
  -DarchetypeVersion=1.2-SNAPSHOT
