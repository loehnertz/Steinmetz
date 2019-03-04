package model.neo4j

import model.relation.BelongsTo
import model.relation.Calls
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING
import org.neo4j.ogm.annotation.Relationship.OUTGOING
import xml.Method

@NodeEntity
class Unit(var identifier: String, var packageIdentifier: String, var methods: Method) : Model {
    @Id
    @GeneratedValue
    override var id: Long? = null

    @Relationship(type = Calls, direction = INCOMING)
    var calledByUnits: MutableSet<Unit> = mutableSetOf()

    @Relationship(type = Calls, direction = OUTGOING)
    var callingUnits: MutableSet<Unit> = mutableSetOf()

    @Relationship(type = BelongsTo, direction = OUTGOING)
    var service: Service? = null

    fun belongsTo(service: Service) {
        this.service = service
        service.units.add(this)
    }
}
