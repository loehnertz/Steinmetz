package model

import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.OUTGOING

@NodeEntity
class Unit(var identifier: String) : Model {
    @Id
    @GeneratedValue
    override var id: Long? = null

    @Relationship(type = BelongsTo, direction = OUTGOING)
    var service: Service? = null

    fun belongsTo(service: Service) {
        this.service = service
        service.units.add(this)
    }
}
