FROM java:8
MAINTAINER Eason<xuyish09@gmail.com>

VOLUME /tmp
COPY ./target/tcc-demo-springcloud-order-0.0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT [ "java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "/app.jar" ]

EXPOSE 8884 8884