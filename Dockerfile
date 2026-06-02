FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY omni-web/target/*.jar app.jar

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=pro"]