FROM openjdk:8-jre

VOLUME /tmp
COPY build/libs/pageviews-0.0.1.jar /pageviews.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/pageviews.jar"]