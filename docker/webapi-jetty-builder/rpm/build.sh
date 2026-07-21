set -e

VERSION="10.0.26"
ARCHIVE="nuance-webapi-jetty-$VERSION.zip"

BUILDDIR=rpmBuild
RELEASE=$(date -u +%Y%m%d%H%M).el7

mkdir $BUILDDIR
mv $ARCHIVE $BUILDDIR
pushd $BUILDDIR
unzip $ARCHIVE
rm -f $ARCHIVE
popd

./build_webapi_jetty_rpm.pl --ShortName=nina-webapi-jetty --Version="$VERSION" --BuildTime="$RELEASE" --ScriptletFile="Nuance_webapi_jetty_scriptlets.sh" --FilesDir=`pwd`/$BUILDDIR

mv /tmp/rpm_builds/RPMS/noarch/nina-webapi-jetty*.rpm .

rm -rf $BUILDDIR
rm -rf /tmp/rpm_builds
