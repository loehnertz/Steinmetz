package controller.analysis.clustering

import model.graph.Graph
import model.graph.Node
import model.graph.NodeAttributes
import model.graph.Unit
import org.neo4j.ogm.model.Result
import utility.Neo4jConnector


abstract class AbstractNeo4jAlgorithmManager(private val graph: Graph, private val projectName: String) : ClusteringAlgorithmManager {
    abstract val nodeLabel: String

    abstract val clusterIdentifer: String

    abstract fun executeGraphAlgorithm(iterations: Int): Result

    override fun apply(iterations: Int): Graph {
        addNecessaryLabels()
        val result: Result = executeGraphAlgorithm(iterations)
        removeNecessaryLabels()

        for (resultMap: MutableMap<String, Any> in result) {
            val unitNode: model.neo4j.node.Unit = (resultMap[UnitIdentifier] as model.neo4j.node.Unit)
            val cluster: Int = (resultMap[clusterIdentifer] as Long).toInt() + 1  // It otherwise starts zero-indexed, which no other clustering algorithm does
            val unit = Unit(identifier = unitNode.identifier, packageIdentifier = unitNode.packageIdentifier)
            val node = Node(unit = unit, attributes = NodeAttributes(cluster = cluster))
            graph.addOrUpdateNode(node)
        }

        return graph
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
            "SET u:$nodeLabel\n"
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
            "MATCH (u:$nodeLabel {${model.neo4j.node.Unit::projectName.name}:'$projectName'})\n" +
            "REMOVE u:$nodeLabel\n"
        )
    }

    companion object {
        const val UnitIdentifier = "unit"
    }
}
