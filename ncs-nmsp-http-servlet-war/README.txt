------------------------------------------------------------------------
README
com.nuance.mobility.ncs-nmsp: ncs-nmsp-http-servlet
------------------------------------------------------------------------
This eclipse/maven project has been generated using a maven archetype.

============================================================
MSTATION ALARMING INTEGRATION
============================================================
To add nuance watcher integration including MStation alarming add:

- in the dependencies section of the pom.xml file

<!-- watcher including alarming integration -->
<dependency>
   <groupId>com.nuance.mobility</groupId>
   <artifactId>mobility-watcher-alarm</artifactId>
</dependency> 

- in src/main/java/ncs-nmsp-http-servlet-application-context.xml after mobility-jetty-application-context.xml

<!-- Instanciates watcher integration, including alarming, (always keep after jetty)-->
<import resource="classpath:mobility-watcher-alarm-application-context.xml" />

============================================================
WATCHER WITHOUT ALARMING INTEGRATION 
============================================================
To add nuance watcher integration WITHOUT MStation alarming add:

- in the dependencies section of the pom.xml file

<!-- watcher ONLY -->
<dependency>
   <groupId>com.nuance.mobility</groupId>
   <artifactId>mobility-watcher</artifactId>
</dependency> 

- in src/main/java/ncs-nmsp-http-servlet-application-context.xml after mobility-jetty-application-context.xml

<!-- Instanciates watcher integration, WITHOUT alarming, (always keep after jetty)-->
<import resource="classpath:mobility-watcher-application-context.xml" />