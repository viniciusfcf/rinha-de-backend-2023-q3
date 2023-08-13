mvn clean package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t quay.io/vflorent/rinha-2023-q3 .
docker push quay.io/vflorent/rinha-2023-q3