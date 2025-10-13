# Use a Java + Maven base
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copy pom first to use cache for dependencies
COPY pom.xml ./

# Download dependencies without compiling
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build package (you can skip tests if desired)
RUN mvn clean package -B

# (Optional) we could make a minimal image with just the jar, or just extract it
# But for Freestyle job, we mainly want the jar in target/
