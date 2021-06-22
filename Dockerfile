FROM java:8
VOLUME /tmp
EXPOSE 8111
ADD /build/libs/*.jar app.jar
CMD java -Xms256m -Xmx512m -jar app.jar