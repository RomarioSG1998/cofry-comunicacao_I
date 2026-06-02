# Stage 1: Build the application using Maven and JDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Compile and package the application
RUN mvn clean package -DskipTests

# Copy all runtime dependencies to target/dependency
RUN mvn dependency:copy-dependencies -DincludeScope=runtime

# Stage 2: Run the application using a slim JRE 21 image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the compiled JAR and dependency libraries
COPY --from=build /app/target/Cofry-Back-1.0-SNAPSHOT.jar app.jar
COPY --from=build /app/target/dependency /app/lib

# Expose port 8082 (matching Main.java PORT configuration)
EXPOSE 8082

# Run the application by specifying the classpath and the Main class
CMD ["java", "-cp", "app.jar:lib/*", "org.example.Main"]