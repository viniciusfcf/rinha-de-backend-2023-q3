mvn clean package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-app-imperativo-jvm .