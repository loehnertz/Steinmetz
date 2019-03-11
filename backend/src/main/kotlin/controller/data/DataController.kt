package controller.data

import model.graph.node.Unit
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import utility.Neo4jConnector


class DataController {
    fun getGraph(basePackageIdentifier: String): Graph {
        val filter = Filter("packageIdentifier", ComparisonOperator.STARTING_WITH, basePackageIdentifier)
        val units = Neo4jConnector.retrieveEntities(Unit::class.java, filter).map { it as Unit }
        val relationships = GraphExtractor.convertUnitListToRelationships(units)
        return Graph(relationships = relationships)
    }
}
