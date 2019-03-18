package model.neo4j.node

import model.neo4j.GraphEntity
import model.neo4j.relationship.BelongsToRelation
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING


@NodeEntity
class Service(var name: String) : GraphEntity {
    @Id
    @GeneratedValue
    override var id: Long? = null

    @Relationship(type = BelongsToRelation, direction = INCOMING)
    var units: MutableSet<Unit> = mutableSetOf()
}
