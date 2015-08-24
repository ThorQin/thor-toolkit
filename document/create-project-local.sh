#!/bin/bash
mvn archetype:generate \
  -DarchetypeCatalog=local \
  -DarchetypeGroupId=com.github.thorqin \
  -DarchetypeArtifactId=thor-toolkit-archetype \
  -DarchetypeVersion=1.1-SNAPSHOT