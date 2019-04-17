package model.neo4j.node

import model.neo4j.GraphEntity
import model.neo4j.relationship.BelongsToRelation
import model.neo4j.relationship.CallsRelation
import model.neo4j.relationship.CallsRelationship
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
class Unit(var identifier: String, var packageIdentifier: String, var projectName: String) : GraphEntity {
    @Id
    @GeneratedValue
    override var id: Long? = null

    @Relationship(type = CallsRelation, direction = INCOMING)
    var calleeRelationships: MutableSet<CallsRelationship> = mutableSetOf()

    @Relationship(type = CallsRelation, direction = OUTGOING)
    var callerRelationships: MutableSet<CallsRelationship> = mutableSetOf()

    @Relationship(type = BelongsToRelation, direction = OUTGOING)
    var service: Service? = null

    fun calls(callee: Unit, couplingScore: Int = 1): CallsRelationship {
        val existingRelationship = retrieveExistingRelationship(callee)
        if (existingRelationship != null) {
            val newCouplingScore = existingRelationship.couplingScore + couplingScore
            updateCouplingScore(existingRelationship, newCouplingScore)
            return existingRelationship
        }

        val relationship = buildRelationship(callee, couplingScore)
        insertRelationship(relationship)

        return relationship
    }

    fun increaseCouplingScore(existingRelationship: CallsRelationship, addedCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, (existingRelationship.couplingScore + addedCouplingScore)))
    }

    private fun updateCouplingScore(existingRelationship: CallsRelationship, newCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, newCouplingScore))
    }

    private fun buildRelationship(callee: Unit, couplingScore: Int): CallsRelationship {
        return CallsRelationship(caller = this, callee = callee, couplingScore = couplingScore)
    }

    private fun retrieveExistingRelationship(callee: Unit): CallsRelationship? {
        try {
            return this.callerRelationships.find {
                it.callee.identifier == callee.identifier && it.callee.packageIdentifier == callee.packageIdentifier && it.callee.projectName == callee.projectName
            }
        } catch (e: Exception) {
            println(e.localizedMessage).also { throw e }
        }
    }

    private fun insertRelationship(relationship: CallsRelationship) {
        this.callerRelationships.add(relationship)
        relationship.callee.calleeRelationships.add(relationship)
    }

    private fun removeRelationship(existingRelationship: CallsRelationship) {
        this.callerRelationships.remove(existingRelationship)
        existingRelationship.callee.calleeRelationships.remove(existingRelationship)
    }

    fun belongsTo(service: Service) {
        this.service = service
        service.units.add(this)
    }

    fun doesNotBelongTo(service: Service) {
        this.service = null
        service.units.removeIf { it.identifier == this.identifier && it.packageIdentifier == this.packageIdentifier && it.projectName == this.projectName }
    }

    companion object Factory {
        @Suppress("SENSELESS_COMPARISON")
        fun create(identifier: String, packageIdentifier: String, projectName: String): Unit {
            val filters = buildFilters(identifier, packageIdentifier, projectName)
            val existingEntity = Neo4jConnector.retrieveEntity(Unit::class.java, filters)

            return if (existingEntity != null) {
                val unit = existingEntity as Unit
                if (unit.calleeRelationships == null) unit.calleeRelationships = mutableSetOf()
                if (unit.callerRelationships == null) unit.callerRelationships = mutableSetOf()
                unit
            } else {
                Unit(identifier, packageIdentifier, projectName)
            }
        }

        private fun buildFilters(identifier: String, packageIdentifier: String, projectName: String): Filters {
            val filters = Filters()

            val identifierFilter = Filter(Unit::identifier.name, ComparisonOperator.EQUALS, identifier)
            filters.add(identifierFilter)

            val packageIdentifierFilter = Filter(Unit::packageIdentifier.name, ComparisonOperator.EQUALS, packageIdentifier)
            packageIdentifierFilter.booleanOperator = BooleanOperator.AND
            filters.add(packageIdentifierFilter)

            val projectNameFilter = Filter(Unit::projectName.name, ComparisonOperator.EQUALS, projectName)
            projectNameFilter.booleanOperator = BooleanOperator.AND
            filters.add(projectNameFilter)

            return filters
        }
    }
}
