package model.graph.node

import model.graph.GraphModel
import model.graph.relationship.BelongsToRelation
import model.graph.relationship.CallsRelation
import model.graph.relationship.CallsRelationship
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING
import org.neo4j.ogm.annotation.Relationship.OUTGOING


@NodeEntity
class Unit(var identifier: String, var packageIdentifier: String) : GraphModel {
    @Id
    @GeneratedValue
    override var id: Long? = null

    @Relationship(type = CallsRelation, direction = INCOMING)
    var calleeRelationships: MutableSet<CallsRelationship> = mutableSetOf()

    @Relationship(type = CallsRelation, direction = OUTGOING)
    var callerRelationships: MutableSet<CallsRelationship> = mutableSetOf()

    @Relationship(type = BelongsToRelation, direction = OUTGOING)
    var service: Service? = null

    fun calls(callee: Unit, couplingScore: Int?) {
        val relationship = CallsRelationship(caller = this, callee = callee, couplingScore = couplingScore)

        this.callerRelationships.add(relationship)
        callee.calleeRelationships.add(relationship)
    }

    fun belongsTo(service: Service) {
        this.service = service
        service.units.add(this)
    }
}
