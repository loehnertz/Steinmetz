package utility

import model.neo4j.GraphEntity
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.model.Result
import org.neo4j.ogm.session.Session
import org.neo4j.ogm.session.SessionFactory


object Neo4jConnector {
    private const val Neo4jBoltConnectionUri = "bolt://localhost"
    private const val NodeModelPackageIdentifier = "model.neo4j.node"
    private const val RelationshipModelPackageIdentifier = "model.neo4j.relationship"
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

    fun executeCypher(cypher: String): Result {
        val session = createSession()
        val result = session.query(cypher, mapOf("" to ""))
        session.clear()
        return result
    }

    private fun createSession(): Session {
        return sessionFactory.openSession()
    }
}
