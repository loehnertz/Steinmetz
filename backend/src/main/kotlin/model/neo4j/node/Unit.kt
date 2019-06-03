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
class Unit(var identifier: String, var packageIdentifier: String, var projectName: String, var size: Long) : GraphEntity {
    @Id
    @GeneratedValue
    override var id: Long? = null

    @Relationship(type = CallsRelation, direction = INCOMING)
    var calleeRelationships: MutableSet<CallsRelationship> = mutableSetOf()

    @Relationship(type = CallsRelation, direction = OUTGOING)
    var callerRelationships: MutableSet<CallsRelationship> = mutableSetOf()

    @Relationship(type = BelongsToRelation, direction = OUTGOING)
    var service: Service? = null

    fun calls(callee: Unit, dynamicCouplingScore: Int = 0, semanticCouplingScore: Double = 0.0, logicalCouplingScore: Int = 0): CallsRelationship {
        val existingRelationship: CallsRelationship? = retrieveExistingRelationship(callee)
        if (existingRelationship != null) {
            val newDynamicCouplingScore: Int = existingRelationship.dynamiCouplingScore + dynamicCouplingScore
            val newSemanticCouplingScore: Double = existingRelationship.semanticCouplingScore + semanticCouplingScore
            val newLogicalCouplingScore: Int = existingRelationship.logicalCouplingScore + logicalCouplingScore
            updateCouplingScores(existingRelationship, newDynamicCouplingScore = newDynamicCouplingScore, newSemanticCouplingScore = newSemanticCouplingScore, newLogicalCouplingScore = newLogicalCouplingScore)
            return existingRelationship
        }

        val relationship: CallsRelationship = buildRelationship(callee = callee, dynamicCouplingScore = dynamicCouplingScore, semanticCouplingScore = semanticCouplingScore, logicalCouplingScore = logicalCouplingScore)
        insertRelationship(relationship)

        return relationship
    }

    private fun updateCouplingScores(existingRelationship: CallsRelationship, newDynamicCouplingScore: Int, newSemanticCouplingScore: Double, newLogicalCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, dynamicCouplingScore = newDynamicCouplingScore, semanticCouplingScore = newSemanticCouplingScore, logicalCouplingScore = newLogicalCouplingScore))
    }

    private fun updateDynamicCouplingScore(existingRelationship: CallsRelationship, newDynamicCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, dynamicCouplingScore = newDynamicCouplingScore, semanticCouplingScore = existingRelationship.semanticCouplingScore, logicalCouplingScore = existingRelationship.logicalCouplingScore))
    }

    private fun updateSemanticCouplingScore(existingRelationship: CallsRelationship, newSemanticCouplingScore: Double) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, semanticCouplingScore = newSemanticCouplingScore, dynamicCouplingScore = existingRelationship.dynamiCouplingScore, logicalCouplingScore = existingRelationship.logicalCouplingScore))
    }

    private fun updateLogicalCouplingScore(existingRelationship: CallsRelationship, newLogicalCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, logicalCouplingScore = newLogicalCouplingScore, dynamicCouplingScore = existingRelationship.dynamiCouplingScore, semanticCouplingScore = existingRelationship.semanticCouplingScore))
    }

    private fun buildRelationship(callee: Unit, dynamicCouplingScore: Int, semanticCouplingScore: Double, logicalCouplingScore: Int): CallsRelationship {
        return CallsRelationship(caller = this, callee = callee, dynamiCouplingScore = dynamicCouplingScore, semanticCouplingScore = semanticCouplingScore, logicalCouplingScore = logicalCouplingScore)
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
        fun create(identifier: String, packageIdentifier: String, projectName: String, size: Long): Unit {
            val filters: Filters = buildFilters(identifier, packageIdentifier, projectName)
            val existingEntity: GraphEntity? = Neo4jConnector.retrieveEntity(Unit::class.java, filters)

            return if (existingEntity != null) {
                val unit: Unit = existingEntity as Unit
                if (unit.calleeRelationships == null) unit.calleeRelationships = mutableSetOf()
                if (unit.callerRelationships == null) unit.callerRelationships = mutableSetOf()
                unit
            } else {
                Unit(identifier, packageIdentifier, projectName, size)
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
