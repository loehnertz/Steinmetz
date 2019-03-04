package utility

import model.graph.GraphModel
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory


const val Neo4jBoltConnectionUri = "bolt://localhost"
const val NodeModelPackageIdentifier = "model.graph.node"
const val RelationshipModelPackageIdentifier = "model.graph.relationship"

object Neo4jConnector {
    private val configuration: Configuration = Configuration.Builder().uri(Neo4jBoltConnectionUri).build()!!
    private val sessionFactory: SessionFactory = SessionFactory(configuration, NodeModelPackageIdentifier, RelationshipModelPackageIdentifier)

    fun saveEntity(entity: GraphModel) {
        val session = sessionFactory.openSession()
        session.save(entity)
        session.clear()
    }
}
