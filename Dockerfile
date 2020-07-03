FROM openjdk:11-jre-slim

RUN mkdir /app

WORKDIR /app

ADD ./api/target/ /app

EXPOSE 8081

CMD java -cp classes:dependency/* com.kumuluz.ee.EeApplication
