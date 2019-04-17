FROM openjdk:latest
WORKDIR /opt/iaasimov
COPY iaasimov-0.5.1-SNAPSHOT.jar /opt/iaasimov
COPY keystore.p12 /opt/iaasimov
EXPOSE "8000:8080"

CMD java -Xms4G -Xmx4G -jar /opt/iaasimov/iaasimov-0.5.1-SNAPSHOT.jar