FROM ubuntu:14.04

MAINTAINER Renzo Borgatti <reborg@reborg.net>

# Add mirrors
RUN sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list

# Update OS
RUN apt-get update
RUN apt-get -y upgrade

# Install base packages
RUN apt-get install -y build-essential
RUN apt-get install -y software-properties-common
RUN apt-get install -y zip unzip curl wget vim git man htop

# Add files
ADD docker-bash.rc /root/.bashrc

# Set environment variables
ENV HOME /root

# Install Oracle Java JDK 8
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer
RUN update-java-alternatives -s java-8-oracle
RUN apt-get install -y oracle-java8-set-default
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle/

# Define working directory
WORKDIR /fluorine

COPY target/fluorine-client.jar fluorine-client.jar

CMD ["java", "-jar", "fluorine-client.jar"]
