package controller.analysis.clustering.weaklyconnectedcomponents

import controller.analysis.clustering.AbstractNeo4jAlgorithmManager
import model.graph.EdgeAttributes
import model.graph.Graph
import model.neo4j.relationship.CallsRelation
import org.neo4j.ogm.model.Result
import utility.Neo4jConnector


class WeaklyConnectedComponentsManager(graph: Graph, projectName: String) : AbstractNeo4jAlgorithmManager(graph, projectName) {
    override val nodeLabel: String = WeaklyConnectedComponentsNodeLabel

    override val clusterIdentifer: String = ClusterIdentifer

    override fun executeGraphAlgorithm(iterations: Int): Result {
        return Neo4jConnector.executeCypher(
            "CALL algo.unionFind.stream('$nodeLabel', '$CallsRelation', {weightProperty: '${EdgeAttributes::couplingScore.name}'})\n" +
            "YIELD nodeId, $clusterIdentifer\n" +
            "RETURN algo.asNode(nodeId) AS $UnitIdentifier, $clusterIdentifer"
        )
    }

    companion object {
        private const val WeaklyConnectedComponentsNodeLabel = "weaklyconnectedcomponents"
        private const val ClusterIdentifer = "setId"
    }
}
