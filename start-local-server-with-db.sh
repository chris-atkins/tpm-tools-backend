# cleanup any old stuff
docker network create tpm-tools-network || true
docker stop tpm-tools-db || true
docker stop tpm-tools-backend || true
docker rm tpm-tools-db || true
docker rm tpm-tools-backend || true
docker rmi tpm-tools-backend-image || true

docker run --restart=always --name tpm-tools-db -e POSTGRES_DB=tpm-tools -e POSTGRES_USER=local-user -e POSTGRES_PASSWORD=pwd2 --network tpm-tools-network -p 5432:5432 -d postgres:16

sleep 30

docker build . -t tpm-tools-backend-image -f m2Dockerfile
docker run -d --restart=on-failure:5 -p 8080:8080 -e DB_CONNECTION_STRING=jdbc:postgresql://tpm-tools-db:5432/tpm-tools -e DB_USER=local-user -e DB_PASSWORD=pwd2 --network tpm-tools-network --name tpm-tools-backend tpm-tools-backend-image

sleep 30