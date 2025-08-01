#!/bin/bash

set -eu

CONTAINER=lab5-g07-keycloak-1
BASE=/opt/keycloak

docker exec $CONTAINER mkdir -p $BASE/realm
docker exec $CONTAINER $BASE/bin/kc.sh export --dir $BASE/realm --realm ez_car_rent --users different_files
docker cp $CONTAINER:$BASE/realm .
docker exec $CONTAINER rm -r $BASE/realm