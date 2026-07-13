FROM gradle:8.14-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle :app:clean :app:build -x test --no-daemon


FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]