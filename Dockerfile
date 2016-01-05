FROM tomcat:8.0-jre8

MAINTAINER Stanislav Levental

ADD build/libs/simianarmy-*.war /usr/local/tomcat/webapps/simianarmy.war

CMD ["catalina.sh", "run"]