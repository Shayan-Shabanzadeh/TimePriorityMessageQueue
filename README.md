---

# Rabbit Consumer Service

The Rabbit Consumer Service is a Spring Boot application designed to consume and process messages from a RabbitMQ message broker. This README provides an overview of the project's structure, key components, and how to run the service.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Prerequisites](#prerequisites)
3. [Getting Started](#getting-started)
4. [Configuration](#configuration)
5. [Project Structure](#project-structure)
6. [Dependencies](#dependencies)
7. [Testing](#testing)

## Project Overview

The Rabbit Consumer Service is built using Spring Boot and integrates with RabbitMQ for message consumption. It consists of the following main components:

- **Message Consumer**: Responsible for receiving and queuing incoming messages for processing. The service consumes and processes messages based on their priority using an aging algorithm to prevent starvation.

- **Message Publisher**: Publishes messages to the RabbitMQ exchange.

- **Message Controller**: Provides an API endpoint for publishing messages to the service.

- **MessageDto**: Represents the message data transfer object used for message processing.

- **Rabbit Configuration (RabbitConf)**: Defines RabbitMQ-related configurations, including exchanges, queues, and message handling.


## Prerequisites

Before running the Rabbit Consumer Service, make sure you have the following prerequisites installed:

- [Java Development Kit (JDK) 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Maven](https://maven.apache.org/download.cgi) for building the project
- A running RabbitMQ server or access to a RabbitMQ service

## Getting Started

1. Clone this repository to your local machine:

   ```bash
   git clone https://github.com/Shayan-Shabanzadeh/TimePriorityMessageQueue.git
   ```

2. Navigate to the project directory:

   ```bash
   cd rabbit-consumer
   ```

3. Build the project using Maven:

   ```bash
   mvn clean install
   ```

4. Run the application:

   ```bash
   java -jar target/rabbit-consumer-0.0.1-SNAPSHOT.jar
   ```

The Rabbit Consumer Service should now be running and ready to consume messages from the configured RabbitMQ broker.

## Configuration

The application's configuration can be customized through the `application.properties` or `application.yml` file. Key configuration properties include RabbitMQ connection details, queue settings, and logging.

Example `application.properties`:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=myusername
spring.rabbitmq.password=mypassword
# Add other RabbitMQ and application properties here
```

## Project Structure

The project follows a standard Spring Boot application structure. The main components are organized as follows:

- `src/main/java/ir/bontech/rabbitconsumer`: Java source code
- `src/main/resources`: Application configuration files

## Dependencies

This project relies on several dependencies managed by Maven. Key dependencies include:

- Spring Boot: For building and running the application.
- Spring AMQP: For RabbitMQ integration.
- Lombok: For simplifying Java code with annotations.
- Logback: For logging.
- Mockito and JUnit: For testing.

You can find the complete list of dependencies in the `pom.xml` file.

## Testing

The project includes unit tests using JUnit and Mockito. You can run the tests using the following command:

```bash
mvn test
```

Additional integration tests may be required depending on your specific use case.



---