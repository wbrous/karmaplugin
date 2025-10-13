# Use Eclipse Temurin JDK 22 (OpenJDK distribution)
FROM eclipse-temurin:22-jdk

# Install Maven
ARG MAVEN_VERSION=3.9.6
ARG USER_HOME_DIR="/root"
ARG SHA=706f01b20dec0305a822ab614d51f32b07ee11d0218175e55450242e49d2156386483b506b3a4e8a03ac8611bae96395fd5eec15f50d3013d5deed6d1ee18224
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN apt-get update && \
    apt-get install -y curl && \
    mkdir -p /usr/share/maven /usr/share/maven/ref && \
    curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - && \
    tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 && \
    rm -f /tmp/apache-maven.tar.gz && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# Set the working directory
WORKDIR /workspace

# Copy Maven settings if needed
# COPY settings.xml /usr/share/maven/conf/

CMD ["mvn", "--version"]

