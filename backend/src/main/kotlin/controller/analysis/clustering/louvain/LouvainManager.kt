package controller.analysis.clustering.louvain

import controller.analysis.clustering.ClusteringAlgorithmManager
import model.graph.Graph
import model.graph.Node
import model.graph.NodeAttributes
import model.graph.Unit
import model.neo4j.relationship.CallsRelation
import org.neo4j.ogm.model.Result
import utility.Neo4jConnector


class LouvainManager(private val graph: Graph, private val projectName: String) : ClusteringAlgorithmManager {
    override fun apply(tunableParameter: Double?): Graph {
        addLouvainLabel()
        val result: Result = executeGraphAlgorithm()
        removeLouvainLabel()

        for (resultMap: MutableMap<String, Any> in result) {
            val unitNode: model.neo4j.node.Unit = (resultMap[UnitIdentifier] as model.neo4j.node.Unit)
            val cluster: Int = (resultMap[ClusterIdentifer] as Long).toInt() + 1  // It otherwise starts zero-indexed, which no other clustering algorithm does
            val unit = Unit(identifier = unitNode.identifier, packageIdentifier = unitNode.packageIdentifier)
            val node = Node(unit = unit, attributes = NodeAttributes(cluster = cluster))
            graph.addOrUpdateNode(node)
        }

        return graph
    }

    private fun executeGraphAlgorithm(): Result {
        return Neo4jConnector.executeCypher(
                "CALL algo.louvain.stream('$LouvainNodeLabel', '$CallsRelation', {})\n" +
                        "YIELD nodeId, $ClusterIdentifer\n" +
                        "RETURN algo.asNode(nodeId) AS $UnitIdentifier, $ClusterIdentifer"
        )
    }

    private fun addLouvainLabel() {
        Neo4jConnector.executeCypher(
                "MATCH (u:Unit {${model.neo4j.node.Unit::projectName.name}:'$projectName'})\n" +
                        "SET u:$LouvainNodeLabel\n" +
                        "RETURN u"
        )
    }

    private fun removeLouvainLabel() {
        Neo4jConnector.executeCypher(
                "MATCH (u:$LouvainNodeLabel {${model.neo4j.node.Unit::projectName.name}:'$projectName'})\n" +
                        "REMOVE u:$LouvainNodeLabel\n" +
                        "RETURN u"
        )
    }

    companion object {
        private const val LouvainNodeLabel = "Louvain"
        private const val ClusterIdentifer = "community"
        private const val UnitIdentifier = "unit"
    }
}
