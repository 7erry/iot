FROM centos:latest

MAINTAINER 7erry <terry@hazelcast.com>

RUN curl -O https://download.java.net/java/GA/jdk12.0.1/69cfe15208a647278a19ef0990eea691/12/GPL/openjdk-12.0.1_linux-x64_bin.tar.gz
RUN tar xvf openjdk-12.0.1_linux-x64_bin.tar.gz
RUN mv jdk-12.0.1 /opt/

RUN mkdir -p /opt/hazelcast-iot

ADD target /opt/hazelcast-iot/target
ADD setup /opt/hazelcast-iot/setup
ADD web /opt/hazelcast-iot/web
ADD debug.xml /opt/hazelcast-iot
ADD schema /opt/hazelcast-iot/schema

WORKDIR "/opt/hazelcast-iot"
CMD ["/opt/jdk-12.0.1/bin/java", "-jar","target/hazelcastiot-server.jar"]

EXPOSE 80 5055 5701

