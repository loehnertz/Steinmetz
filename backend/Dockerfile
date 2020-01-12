FROM openjdk:11

# Create working directory
RUN mkdir /app
WORKDIR /app

# Copy the external executables into the image
RUN mkdir executables
COPY ./lib/executables/* executables/

# Install dependencies for executables
RUN apt-get update
RUN apt-get install -y build-essential libgomp1

# Copy the latest build into the image
COPY ./target/steinmetz-1.0-SNAPSHOT-jar-with-dependencies.jar backend.jar

# Boot up the server
EXPOSE 5656
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-Xmx5G", "-Xms5G", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "backend.jar"]
