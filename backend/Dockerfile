FROM openjdk:11

# Install dependencies
RUN apt update && apt install -y maven build-essential libgomp1

# Building the JAR
RUN mkdir /build
WORKDIR /build
COPY lib-repo lib-repo
COPY pom.xml pom.xml
RUN mvn dependency:resolve
COPY src src
RUN mvn clean package

# Create working directory
RUN mkdir /app
WORKDIR /app

# Copy the external executables into the image
RUN mkdir executables
COPY lib/executables/* executables/

# Move the latest build to the correct location
RUN mv /build/target/steinmetz-1.0-SNAPSHOT-jar-with-dependencies.jar backend.jar

# Boot up the server
EXPOSE 5656
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "backend.jar"]
