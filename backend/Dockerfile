FROM openjdk:11

# Create working directory
RUN mkdir /app
WORKDIR /app

# Copy the latest build into the image
COPY ./target/steinmetz-1.0-SNAPSHOT-jar-with-dependencies.jar backend.jar

# Copy the external executables into the image
RUN mkdir executables
COPY ./lib/executables/* executables/

# Install dependencies for executables
RUN apt-get update
RUN apt-get install -y build-essential libgomp1

# Boot up the server
EXPOSE 5656
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "backend.jar"]
