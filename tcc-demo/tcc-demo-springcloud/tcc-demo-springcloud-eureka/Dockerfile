FROM daocloud.io/library/java:openjdk-8-jre-alpine

MAINTAINER Eason<xuyish09@gmail.com>

VOLUME /tmp
COPY ./target/tcc-demo-springcloud-eureka-0.0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT [ "java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "/app.jar" ]
CMD [ "--spring.profiles.active=peer1" ]

EXPOSE 8762 8762