FROM tomcat:8.0-jre8
MAINTAINER Stanislav Levental

ADD . /src

WORKDIR /src

RUN apt-get install gradle && gradle test war

ADD build/libs/simianarmy-*.war /usr/local/tomcat/webapps/simianarmy.war

CMD ["catalina.sh", "run"]