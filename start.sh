cleanup() {
    echo "Stopping Docker containers..."
    docker compose down
    exit 0
}

trap cleanup SIGINT

cd /d/Startup/Microservices/auth || { echo "Directory not found"; exit 1; }
docker compose up -d
mvn spring-boot:run


cleanup