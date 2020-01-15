FROM openjdk:11

# Install dependencies
RUN apt update && apt install -y maven build-essential libgomp1

# Building the JAR
RUN mkdir /build
WORKDIR /build
COPY . .
RUN mvn dependency:resolve
RUN mvn clean package

# Create working directory
RUN mkdir /app
WORKDIR /app

# Copy the external executables into the image
RUN mkdir executables
RUN cp -R /build/lib/executables/* executables/

# Copy the latest build into the image
RUN cp /build/target/steinmetz-1.0-SNAPSHOT-jar-with-dependencies.jar backend.jar

# Boot up the server
EXPOSE 5656
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-Xmx5G", "-Xms5G", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "backend.jar"]
