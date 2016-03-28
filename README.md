# BlogEngine
Blog engine based on Java + MongoDB

## Requirements

* Apache Ant
* Apache Tomcat 7
* Java 7
* MongoDB 3.0.4
* Freemarker 2.3.20

## Step-by-step

1. Install Java 7, Apache Ant, Apache Tomcat 7, MongoDB 3.0.4
2. Download to lib directory:
    * bson-3.0.4.jar
    * mongodb-driver-core-3.0.4.jar
    * mongodb-driver-3.0.4.jar
    * javax.servlet.jar (3.0)
    * freemarker-2.3.20.jar 
3. Run `ant reset-db` to load mock data.
4. Run `ant pack` to create *blogEngine.war*
5. Move *blogEngine.war* to `/var/lib/tomcat7/webapps/` folder of Tomcat.
6. Create `/var/lib/tomcat7/freemarker-templates` and copy templates there.