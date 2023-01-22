# cleanup any old stuff
docker network create tpm-tools-network || true
docker stop tpm-mysql || true
docker stop tpm-tools-backend || true
docker rm tpm-mysql || true
docker rm tpm-tools-backend || true
docker rmi tpm-tools-backend-image || true


docker run --restart=always --name tpm-mysql -e MYSQL_ROOT_PASSWORD=pwd -e MYSQL_DATABASE=tpm-tools -e MYSQL_USER=local-user -e MYSQL_PASSWORD=pwd2 --network tpm-tools-network -p 3306:3306 -d mysql:8.0.27

sleep 60



docker build . -t tpm-tools-backend-image

docker run -d --restart=on-failure:5 -p 8080:8080 -e DB_CONNECTION_STRING=jdbc:mysql://tpm-mysql:3306/tpm-tools -e DB_USER=local-user -e DB_PASSWORD=pwd2 --network tpm-tools-network --name tpm-tools-backend tpm-tools-backend-image