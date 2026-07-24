%pre
 
NUANCE_JETTY_USER=njetty
NUANCE_JETTY_GROUP=njetty
 
/usr/bin/getent passwd ${NUANCE_JETTY_USER} >/dev/null || /usr/sbin/useradd --system --no-create-home -c "Created by Nuance Communications, Inc. to run Nuance processes" ${NUANCE_JETTY_USER}
/usr/bin/getent group ${NUANCE_JETTY_GROUP} >/dev/null || /usr/sbin/groupadd -r ${NUANCE_JETTY_GROUP}
 
# Stop all jetty's on upgrade
if [ $1 -gt 1 ]; then
  if [ -x /usr/bin/systemctl ]; then
    /usr/bin/systemctl stop jetty1 jetty2 jetty3 jetty4
  fi

  for instance in 1 2 3 4; do
    if [ -x /sbin/service ]; then
      /sbin/service jetty$instance stop
    fi
  done
fi

exit 0

%post

mkdir -p /var/lib/jetty
chown njetty:njetty /var/lib/jetty

BASEDIR=$RPM_INSTALL_PREFIX/Nuance/webapi

# Copy Jetty init.d scripts into /etc/init.d and systemd scripts into /etc/systemd/system
for instance in 1 2 3 4; do
  INITDFILE=$BASEDIR/jetty-service/jetty$instance
  SYSTEMDFILE=$INITDFILE.service
  if [ -f $INITDFILE ] && [ -f $SYSTEMDFILE ]; then
    cp -f $INITDFILE /etc/init.d
    cp -f $SYSTEMDFILE /etc/systemd/system
    chmod a+x /etc/init.d/jetty$instance /etc/systemd/system/jetty$instance.service
    chmod 644 /etc/systemd/system/jetty$instance.service
  else
    echo "Files $INITDFILE or $SYSTEMDFILE are missing from this package ... exiting!"
    exit 10
  fi

  # Add init.d services
  /sbin/chkconfig --add jetty$instance

  # Prefer systemd, if available
  if [ -x /usr/bin/systemctl ]; then
    /sbin/chkconfig --del jetty$instance
    /usr/bin/systemctl enable jetty$instance
  fi
done

exit 0

%preun

BASEDIR=$RPM_INSTALL_PREFIX/Nuance/webapi

# On uninstall, stop services and remove the services.
if [ $1 -lt 1 ]; then
  if [ -x /usr/bin/systemctl ]; then
    /usr/bin/systemctl disable jetty1 jetty2 jetty3 jetty4
  fi

  for instance in 1 2 3 4; do
    if [ -x /sbin/service ]; then
      /sbin/service jetty$instance stop
    fi
    /sbin/chkconfig --del jetty$instance
    rm -f /etc/systemd/system/jetty$instance.service
    rm -f /etc/init.d/jetty$instance
  done

  if [ -x /usr/bin/systemctl ]; then
    /usr/bin/systemctl daemon-reload >& /dev/null
  fi
fi

exit 0
