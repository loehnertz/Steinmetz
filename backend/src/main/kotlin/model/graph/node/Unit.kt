package model.graph.node

import model.graph.GraphEntity
import model.graph.relationship.BelongsToRelation
import model.graph.relationship.CallsRelation
import model.graph.relationship.CallsRelationship
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING
import org.neo4j.ogm.annotation.Relationship.OUTGOING
import org.neo4j.ogm.cypher.BooleanOperator
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import utility.Neo4jConnector


@NodeEntity
class Unit(var identifier: String, var packageIdentifier: String) : GraphEntity {
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
        // TODO: Check if relationship already exists and update the score accordingly

        val relationship = CallsRelationship(caller = this, callee = callee, couplingScore = couplingScore)

        this.callerRelationships.add(relationship)
        callee.calleeRelationships.add(relationship)
    }

    fun updateCouplingScore(callee: Unit, newCouplingScore: Int) {
        val relationship = this.callerRelationships.find { it.caller == this && it.callee == callee }
        if (relationship != null) relationship.couplingScore = newCouplingScore
    }

    fun belongsTo(service: Service) {
        this.service = service
        service.units.add(this)
    }

    fun doesNotBelongTo(service: Service) {
        this.service = null
        service.units.removeIf { it.identifier == this.identifier && it.packageIdentifier == this.packageIdentifier }
    }

    companion object Factory {
        @Suppress("SENSELESS_COMPARISON")
        fun create(identifier: String, packageIdentifier: String): Unit {
            val filters = buildFilters(identifier, packageIdentifier)
            val existingEntity = Neo4jConnector.retrieveEntity(Unit::class.java, filters)

            return if (existingEntity != null) {
                val unit = existingEntity as Unit
                if (unit.calleeRelationships == null) unit.calleeRelationships = mutableSetOf()
                if (unit.callerRelationships == null) unit.callerRelationships = mutableSetOf()
                unit
            } else {
                Unit(identifier, packageIdentifier)
            }
        }

        private fun buildFilters(identifier: String, packageIdentifier: String): Filters {
            val filters = Filters()

            val identifierFilter = Filter("identifier", ComparisonOperator.EQUALS, identifier)
            filters.add(identifierFilter)

            val packageIdentifierFilter = Filter("packageIdentifier", ComparisonOperator.EQUALS, packageIdentifier)
            packageIdentifierFilter.booleanOperator = BooleanOperator.AND
            filters.add(packageIdentifierFilter)

            return filters
        }
    }
}
