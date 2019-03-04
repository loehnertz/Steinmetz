package utility

import model.graph.GraphModel
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory


const val Neo4jBoltConnectionUri = "bolt://localhost"
const val ModelPackageIdentifier = "model.graph"

object Neo4jConnector {
    private val configuration: Configuration = Configuration.Builder().uri(Neo4jBoltConnectionUri).build()!!
    private val sessionFactory: SessionFactory = SessionFactory(configuration, ModelPackageIdentifier)

    fun saveEntity(entity: GraphModel) {
        val session = sessionFactory.openSession()
        session.save(entity)
        session.clear()
    }
}
