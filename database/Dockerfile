FROM neo4j:3.5.4

ENV PLUG_IN_VERSION 3.5.4.0
ENV GRAPH_ALGORITHMS_URI https://github.com/neo4j-contrib/neo4j-graph-algorithms/releases/download/${PLUG_IN_VERSION}/graph-algorithms-algo-${PLUG_IN_VERSION}.jar

RUN wget $GRAPH_ALGORITHMS_URI && mv graph-algorithms-algo-${PLUG_IN_VERSION}.jar ${NEO4J_HOME}/plugins/graph-algorithms-algo-${PLUG_IN_VERSION}.jar
RUN echo "dbms.security.procedures.unrestricted=algo.*" >> ${NEO4J_HOME}/conf/neo4j.conf

EXPOSE 7474 7473 7687

CMD ["neo4j"]
