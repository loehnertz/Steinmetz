# Steinmetz
A visual approach to the assisted decomposition of monolithic software architectures into microservices


## Features

- Generate microservice recommendations for any monolithic (Java) back-end application
- Only provide, the source code, a VCS log, and optionally a profiler recording
- Create microservice candidates using seven state-of-the-art graph clustering algorithms
- Let a custom genetic algorithm optimize the optional input parameters regarding the clustering for you
- Receive an advanced visualization of the microservice candidate recommendations in three different formats

<br><br>
![screenshot-1](https://i.imgur.com/9lYAGPm.png)


## Idea

This application was created as part of my [master's thesis](https://github.com/loehnertz/master-thesis).
It implements the theoretic methodology that I devised in the thesis. The methodology and implementation are 
language-agnostic in its core although for now, the implementation only supports applications written in Java.
Nevertheless, it is possible to extend _Steinmetz_ to work with other languages.
Further down this README, you can find a [basic guide](#Contribute).


## Running

The tool consists of a back-end written in Kotlin as well as front-end built with Vue.js.
The entirety of the application is bundled into Docker images and can be executed with `docker-compose`.
After having it installed, while in the root project directory, just run `docker-compose up -d`.
Instead of the entire repository, you can also just create this `docker-compose.yml` anywhere and run `docker-compose up -d`:
```yaml
version: "3"
services:
  frontend:
    image: loehnertz/steinmetz-frontend
    ports:
      - 8056:80
    volumes:
      - data:/app
    depends_on:
      - backend
  backend:
    image: loehnertz/steinmetz-backend
    ports:
      - 5005:5005
      - 5656:5656
    environment:
      NEO4J_HOST: "database"
      NEO4J_PORT: "7687"
      EXTERNAL_EXECUTABLE_PATH: "/app/executables/"
    depends_on:
      - database
  database:
    image: loehnertz/neo4j-graph-algos
    ports:
      - 7474
      - 7687
    environment:
      NEO4J_AUTH: "none"
      NEO4J_dbms_memory_heap_max__size: "4G"
    volumes:
      - data:/data
      - data:/logs
volumes:
  data:
```


## Usage

### Preparation
1) Create a `.zip` file of the root directory of your application's source code (e.g., `src`).
2) Create a compatible log file via the VCS that you are using following this table:
   | VCS      | Command                                                                                              |
   |----------|------------------------------------------------------------------------------------------------------|
   | Git      | `git log --all --numstat --date=short --pretty=format:'--%h--%ad--%aN' --no-renames > ./vcs-log.txt` |

3) (Optional) Create a profiler recording of your running application. Currently for the Java programming language,
   a _Java Flight Recorder_ (JFR) recording is supported. The JFR is a sampling-based profiler, meaning that generally
   the longer you are recording your application during its runtime, the more precise the results are going to be.
   To do that, run your application with at least JRE 11 or higher, adding the following arguments (note the file name at the end):
   `-XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:+FlightRecorder -XX:StartFlightRecording=settings=profile,maxsize=10000MB,filename=steinmetz.jfr`

### Analysis
1) Visit [http://localhost:8056](http://localhost:8056) in your browser.
2) Select a name for your project, this can be anything.
3) Specify the base package identifier, meaning the root of your application structure (e.g., `com.example.myapplication`).
4) Specify the programming language that your application is written in.
5) Specify the VCS (version control system) you are using during development.
6) Select the files creating during the _Preparation_ step:
   - _Static Analysis_: The `.zip` of the source code.
   - _Dynamic Analysis_ (Optional): The `.jfr` profiler recording.
   - _Semantic Analysis_: The `.zip` of the source code.
   - _Evolutionary Analysis_: The `.txt` VCS log.
7) Click the `Upload` button to upload your data and begin the analyis.
   Note that the provided data is just uploaded to the locally running back-end instance.
   We do not store any of your data. If you don't believe us, that's why the code is open-source.

### Exploration
_Follows soon_


## Contribute

_Follows soon_


## License

This project is licensed under the [Apache 2.0 license](LICENSE).
