package utility

import model.graph.GraphEntity
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.session.Session
import org.neo4j.ogm.session.SessionFactory


const val Neo4jBoltConnectionUri = "bolt://localhost"
const val NodeModelPackageIdentifier = "model.graph.node"
const val RelationshipModelPackageIdentifier = "model.graph.relationship"

object Neo4jConnector {
    private val configuration: Configuration = Configuration.Builder().uri(Neo4jBoltConnectionUri).build()!!
    private val sessionFactory: SessionFactory = SessionFactory(configuration, NodeModelPackageIdentifier, RelationshipModelPackageIdentifier)

    fun saveEntity(entity: GraphEntity) {
        val session = createSession()
        session.save(entity)
        session.clear()
    }

    fun saveEntities(entities: Iterable<GraphEntity>) {
        val session = createSession()
        session.save(entities)
        session.clear()
    }

    fun retrieveEntity(entityClass: Class<out GraphEntity>, filter: Filter): GraphEntity? {
        return retrieveEntities(entityClass, filter).firstOrNull()
    }

    fun retrieveEntity(entityClass: Class<out GraphEntity>, filters: Filters): GraphEntity? {
        return retrieveEntities(entityClass, filters).firstOrNull()
    }

    fun retrieveEntities(entityClass: Class<out GraphEntity>, filter: Filter): Collection<GraphEntity> {
        val session = createSession()
        val entities = session.loadAll(entityClass, filter)
        session.clear()
        return entities
    }

    fun retrieveEntities(entityClass: Class<out GraphEntity>, filters: Filters): Collection<GraphEntity> {
        val session = createSession()
        val entities = session.loadAll(entityClass, filters)
        session.clear()
        return entities
    }

    private fun createSession(): Session {
        return sessionFactory.openSession()
    }
}
