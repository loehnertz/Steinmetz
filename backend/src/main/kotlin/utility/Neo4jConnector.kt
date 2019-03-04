package utility

import model.Model
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory

const val Neo4jBoltConnectionUri = "bolt://localhost"
const val ModelPackageIdentifier = "model"

object Neo4jConnector {
    private val configuration: Configuration = Configuration.Builder().uri(Neo4jBoltConnectionUri).build()!!
    private val sessionFactory: SessionFactory = SessionFactory(configuration, ModelPackageIdentifier)

    fun saveModelEntity(entity: Model) {
        val session = sessionFactory.openSession()
        session.save(entity)
        session.clear()
    }
}
