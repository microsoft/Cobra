#!/bin/bash

usage() {
  echo "Unzip or untar the jetty distribution into this folder and use this script to apply the webapi deltas"
  echo "Usage: $0 <jetty-version> <jetty-dist-dir>"
}

if [ $# -ne 2 ] || [ ! -d $2 ]; then
  usage
  exit 1
fi

JETTY_VERSION=$1
JETTY_DIST_DIR=$2

rm -rfv ${JETTY_DIST_DIR}/demo-base
cp -av jetty-service ${JETTY_DIST_DIR}
cp -av nina-webapi-base-* ${JETTY_DIST_DIR}
cp -av webapps ${JETTY_DIST_DIR}

chmod +w ${JETTY_DIST_DIR}/etc/webdefault.xml

#Add the default error-page
sed -i.bak '/<\/web-app>/i \
\ \ <error-page>\
\ \ \ \ <location>/error.html</location>\
\ \ </error-page>\
\
'  ${JETTY_DIST_DIR}/etc/webdefault.xml
rm ${JETTY_DIST_DIR}/etc/webdefault.xml.bak

#Change dirAllowed to false
sed -i.bak '/<param-name>dirAllowed<\/param-name>/{n;s/.*/      <param-value>false<\/param-value>/;}' ${JETTY_DIST_DIR}/etc/webdefault.xml
rm ${JETTY_DIST_DIR}/etc/webdefault.xml.bak

#Turn off HTTP OPTIONS
sed -i.bak '/<http-method>TRACE<\/http-method>/s/.*/&\
      <http-method>OPTIONS<\/http-method>/' ${JETTY_DIST_DIR}/etc/webdefault.xml
rm ${JETTY_DIST_DIR}/etc/webdefault.xml.bak
sed -i.bak '/<http-method-omission>TRACE<\/http-method-omission>/s/.*/&\
      <http-method-omission>OPTIONS<\/http-method-omission>/' ${JETTY_DIST_DIR}/etc/webdefault.xml
rm ${JETTY_DIST_DIR}/etc/webdefault.xml.bak



cd ${JETTY_DIST_DIR}
zip -ry ../../nuance-webapi-jetty-${JETTY_VERSION}.zip *
cd ..
rm -rf ${JETTY_DIST_DIR}
