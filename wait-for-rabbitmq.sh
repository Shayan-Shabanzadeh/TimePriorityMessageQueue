#!/bin/sh

# Wait for RabbitMQ to be ready
until nc -z -v -w30 rabbitmq 5672; do
  echo "Waiting for RabbitMQ to be ready..."
  # Sleep for a while before checking again
  sleep 5
done

# Once RabbitMQ is ready, execute the provided command
exec "$@"
