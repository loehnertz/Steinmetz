package model.neo4j

import model.relation.BelongsTo
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING

@NodeEntity
class Service(var name: String) : Model {
    @Id
    @GeneratedValue
    override var id: Long? = null

    @Relationship(type = BelongsTo, direction = INCOMING)
    var units: MutableSet<Unit> = mutableSetOf()
}
