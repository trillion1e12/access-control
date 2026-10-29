# QR code-based Attendance and Access Control System

A Java - Spring Boot implementation for 2 services of the serviced-based architecture style:

1. User & attendance service
2. QR & access service

## How to run

1. Generate RSA key pair (for JWT):

    ```bash
    openssl genrsa > private.key
    openssl rsa -in private.key -pubout -out public.key
    ```

2. Copy the keys to resources folders:

    ```bash
    cp public.key private.key attendance/src/main/resources/
    cp public.key access/src/main/resources/
    rm public.key private.key
    ```

3. Start the database and kafka containers:

    ```bash
    docker compose up -d
    ```

4. Run the 2 services (on 2 terminal):

    Terminal 1

    ```bash
    cd attendance && ./gradlew bootRun
    ```

    Terminal 2
    ```bash
    cd access && ./gradlew bootRun
    ```

