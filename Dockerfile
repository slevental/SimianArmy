FROM tomcat:8.0-jre8

MAINTAINER Stanislav Levental

ADD build/libs/simianarmy-*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]