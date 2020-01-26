package controller.analysis.clustering.louvain

import controller.analysis.clustering.AbstractNeo4jAlgorithmManager
import model.graph.EdgeAttributes
import model.graph.Graph
import model.neo4j.relationship.CallsRelation
import org.neo4j.ogm.model.Result
import utility.Neo4jConnector


class LouvainManager(graph: Graph, projectName: String) : AbstractNeo4jAlgorithmManager(graph, projectName) {
    override val nodeLabel: String = LouvainNodeLabel

    override val clusterIdentifer: String = ClusterIdentifer

    override fun executeGraphAlgorithm(iterations: Int): Result {
        return Neo4jConnector.executeCypher(
            "CALL algo.louvain.stream('$nodeLabel', '$CallsRelation', {direction: 'BOTH', weightProperty: '${EdgeAttributes::couplingScore.name}'})\n" +
            "YIELD nodeId, $clusterIdentifer\n" +
            "RETURN algo.asNode(nodeId) AS $UnitIdentifier, $clusterIdentifer"
        )
    }

    companion object {
        private const val LouvainNodeLabel = "louvain"
        private const val ClusterIdentifer = "community"
    }
}
