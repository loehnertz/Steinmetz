package model.neo4j

import model.relation.BelongsTo
import model.relation.Calls
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING
import org.neo4j.ogm.annotation.Relationship.OUTGOING


@NodeEntity
class Unit(var identifier: String, var packageIdentifier: String) : Model {
    @Id
    @GeneratedValue
    override var id: Long? = null

    @Relationship(type = Calls, direction = INCOMING)
    var calledByUnits: MutableSet<Unit> = mutableSetOf()

    @Relationship(type = Calls, direction = OUTGOING)
    var callsUnits: MutableSet<Unit> = mutableSetOf()

    @Relationship(type = BelongsTo, direction = OUTGOING)
    var service: Service? = null

    fun calls(unit: Unit) {
        this.callsUnits.add(unit)
        unit.calledByUnits.add(this)
    }

    fun belongsTo(service: Service) {
        this.service = service
        service.units.add(this)
    }
}
