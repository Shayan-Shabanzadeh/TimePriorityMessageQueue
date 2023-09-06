# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container at the specified path
COPY /target/rabbit-consumer-0.0.1-SNAPSHOT.jar app.jar

# Copy the application properties from the host into the container
COPY /src/main/resources/application-docker.properties /app/

# Expose the port your application will run on
EXPOSE 8080


# Use a different Debian mirror
RUN sed -i 's#https://mirror.iranserver.com/debian#https://deb.debian.org#g' /etc/apt/sources.list

# Install netcat
RUN apt-get update && apt-get install -y netcat


# Add a custom script to wait for RabbitMQ to be ready
COPY wait-for-rabbitmq.sh /app/wait-for-rabbitmq.sh
RUN chmod +x /app/wait-for-rabbitmq.sh

# Start the Spring Boot application
CMD ["/app/wait-for-rabbitmq.sh", "java", "-jar", "app.jar"]





