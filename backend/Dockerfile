FROM openjdk:12

# Create working directory
RUN mkdir /app
WORKDIR /app

# Copy the latest build into the image
COPY ./target/steinmetz-1.0-SNAPSHOT-jar-with-dependencies.jar ./backend.jar

# Boot up the server
EXPOSE 5656
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "backend.jar"]
