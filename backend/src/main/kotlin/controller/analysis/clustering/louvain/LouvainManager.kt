package controller.analysis.clustering.louvain

import controller.analysis.clustering.ClusteringAlgorithmManager
import model.graph.*
import model.graph.Unit
import model.neo4j.relationship.CallsRelation
import org.neo4j.ogm.model.Result
import utility.Neo4jConnector


class LouvainManager(private val graph: Graph, private val projectName: String) : ClusteringAlgorithmManager {
    override fun apply(tunableParameter: Double?): Graph {
        addNecessaryLabels()
        val result: Result = executeGraphAlgorithm()
        removeNecessaryLabels()

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
                "CALL algo.louvain.stream('$LouvainNodeLabel', '$CallsRelation', {weightProperty:'${EdgeAttributes::couplingScore.name}'})\n" +
                        "YIELD nodeId, $ClusterIdentifer\n" +
                        "RETURN algo.asNode(nodeId) AS $UnitIdentifier, $ClusterIdentifer"
        )
    }

    private fun addNecessaryLabels() {
        for (edge in graph.edges) {
            Neo4jConnector.executeCypher(
                    "MATCH " +
                            "(s:Unit {" +
                            "${model.neo4j.node.Unit::identifier.name}:'${edge.start.identifier}', " +
                            "${model.neo4j.node.Unit::packageIdentifier.name}:'${edge.start.packageIdentifier}'" +
                            "})" +
                            "-[r]->" +
                            "(e:Unit {" +
                            "${model.neo4j.node.Unit::identifier.name}:'${edge.end.identifier}', " +
                            "${model.neo4j.node.Unit::packageIdentifier.name}:'${edge.end.packageIdentifier}'" +
                            "})\n" +
                            "SET r.${edge.attributes::couplingScore.name} = ${edge.attributes.couplingScore}\n"
            )
        }

        Neo4jConnector.executeCypher(
                "MATCH (u:Unit {${model.neo4j.node.Unit::projectName.name}:'$projectName'})\n" +
                        "SET u:$LouvainNodeLabel\n"
        )
    }

    private fun removeNecessaryLabels() {
        for (edge in graph.edges) {
            Neo4jConnector.executeCypher(
                    "MATCH " +
                            "(s:Unit {" +
                            "${model.neo4j.node.Unit::identifier.name}:'${edge.start.identifier}', " +
                            "${model.neo4j.node.Unit::packageIdentifier.name}:'${edge.start.packageIdentifier}'" +
                            "})" +
                            "-[r]->" +
                            "(e:Unit {" +
                            "${model.neo4j.node.Unit::identifier.name}:'${edge.end.identifier}', " +
                            "${model.neo4j.node.Unit::packageIdentifier.name}:'${edge.end.packageIdentifier}'" +
                            "})\n" +
                            "REMOVE r.${edge.attributes::couplingScore.name}\n"
            )
        }

        Neo4jConnector.executeCypher(
                "MATCH (u:$LouvainNodeLabel {${model.neo4j.node.Unit::projectName.name}:'$projectName'})\n" +
                        "REMOVE u:$LouvainNodeLabel\n"
        )
    }

    companion object {
        private const val LouvainNodeLabel = "louvain"
        private const val ClusterIdentifer = "community"
        private const val UnitIdentifier = "unit"
    }
}
