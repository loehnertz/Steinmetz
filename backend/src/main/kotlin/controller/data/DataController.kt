package controller.data

import controller.data.graph.GraphConverter
import controller.data.graph.GraphInserter
import model.graph.Graph
import model.neo4j.node.Unit
import model.resource.NewProjectRequest
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import utility.Neo4jConnector


class DataController {
    fun insertProject(newProjectRequest: NewProjectRequest): Graph {
        GraphInserter(
                projectName = newProjectRequest.projectName,
                projectPlatform = newProjectRequest.projectPlatform,
                basePackageIdentifier = newProjectRequest.basePackageIdentifier,
                staticAnalysisArchive = newProjectRequest.staticAnalysisArchive
        ).insert()
        return getGraph(newProjectRequest.projectName)
    }

    fun getGraph(projectName: String): Graph {
        val filter = Filter(Unit::projectName.name, ComparisonOperator.EQUALS, projectName)
        val units = Neo4jConnector.retrieveEntities(Unit::class.java, filter).map { it as Unit }
        val relationships = GraphConverter.convertUnitListToRelationships(units)
        return Graph(relationships = relationships)
    }
}
