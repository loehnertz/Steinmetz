package controller.analysis.clustering.labelpropagation

import controller.analysis.clustering.AbstractNeo4jAlgorithmManager
import model.graph.EdgeAttributes
import model.graph.Graph
import model.neo4j.relationship.CallsRelation
import org.neo4j.ogm.model.Result
import utility.Neo4jConnector


class LabelPropagationManager(graph: Graph, projectName: String) : AbstractNeo4jAlgorithmManager(graph, projectName) {
    override val nodeLabel: String = LabelProbagationNodeLabel

    override val clusterIdentifer: String = ClusterIdentifer

    override fun executeGraphAlgorithm(iterations: Int): Result {
        return Neo4jConnector.executeCypher(
            "CALL algo.labelPropagation.stream('$nodeLabel', '$CallsRelation', {direction: 'BOTH', weightProperty: '${EdgeAttributes::couplingScore.name}', iterations: $iterations})\n" +
            "YIELD nodeId, $clusterIdentifer\n" +
            "RETURN algo.asNode(nodeId) AS $UnitIdentifier, $clusterIdentifer"
        )
    }

    companion object {
        private const val LabelProbagationNodeLabel = "labelprogatation"
        private const val ClusterIdentifer = "label"
    }
}
