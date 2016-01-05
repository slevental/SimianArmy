FROM tomcat:8.0-jre8

MAINTAINER Stanislav Levental

RUN rm -rf /usr/local/tomcat/webapps/*

ADD build/libs/simianarmy-*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]