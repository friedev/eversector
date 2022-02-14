#!/bin/sh
# Builds and packages EverSector for release
# Usage: build.sh [-r|--release]
set -eu
export PATH='/bin:/usr/bin:/usr/local/bin'

rm -rf EverSector EverSector.zip
./gradlew clean build jar

cp build/distributions/EverSector.tar .
tar -xvf EverSector.tar
rm EverSector.tar

cp -r README.md LICENSE.txt CHANGELOG.md licenses assets EverSector

# Zip for release
if [ "${1+x}" ] && { [ "$1" = '-r' ] || [ "$1" = '--release' ]; }; then
	zip -r EverSector EverSector
fi

exit 0
