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


@Suppress("unused")
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

    fun calls(callee: Unit, staticCouplingScore: Int, dynamicCouplingScore: Int = 0, semanticCouplingScore: Int = 0, evolutionaryCouplingScore: Int = 0): CallsRelationship {
        val existingRelationship: CallsRelationship? = retrieveExistingRelationship(callee)
        if (existingRelationship != null) {
            val newStaticCouplingScore: Int = existingRelationship.staticCouplingScore + staticCouplingScore
            val newDynamicCouplingScore: Int = existingRelationship.dynamicCouplingScore + dynamicCouplingScore
            val newSemanticCouplingScore: Int = existingRelationship.semanticCouplingScore + semanticCouplingScore
            val newEvolutionaryCouplingScore: Int = existingRelationship.evolutionaryCouplingScore + evolutionaryCouplingScore
            updateCouplingScores(existingRelationship, newStaticCouplingScore = newStaticCouplingScore, newDynamicCouplingScore = newDynamicCouplingScore, newSemanticCouplingScore = newSemanticCouplingScore, newEvolutionaryCouplingScore = newEvolutionaryCouplingScore)
            return existingRelationship
        }

        val relationship: CallsRelationship = buildRelationship(callee = callee, staticCouplingScore = staticCouplingScore, dynamicCouplingScore = dynamicCouplingScore, semanticCouplingScore = semanticCouplingScore, evolutionaryCouplingScore = evolutionaryCouplingScore)
        insertRelationship(relationship)

        return relationship
    }

    private fun updateCouplingScores(existingRelationship: CallsRelationship, newStaticCouplingScore: Int, newDynamicCouplingScore: Int, newSemanticCouplingScore: Int, newEvolutionaryCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, staticCouplingScore = newStaticCouplingScore, dynamicCouplingScore = newDynamicCouplingScore, semanticCouplingScore = newSemanticCouplingScore, evolutionaryCouplingScore = newEvolutionaryCouplingScore))
    }

    private fun updateStaticCouplingScore(existingRelationship: CallsRelationship, newStaticCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, staticCouplingScore = newStaticCouplingScore, dynamicCouplingScore = existingRelationship.dynamicCouplingScore, semanticCouplingScore = existingRelationship.semanticCouplingScore, evolutionaryCouplingScore = existingRelationship.evolutionaryCouplingScore))
    }

    private fun updateDynamicCouplingScore(existingRelationship: CallsRelationship, newDynamicCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, staticCouplingScore = existingRelationship.staticCouplingScore, dynamicCouplingScore = newDynamicCouplingScore, semanticCouplingScore = existingRelationship.semanticCouplingScore, evolutionaryCouplingScore = existingRelationship.evolutionaryCouplingScore))
    }

    private fun updateSemanticCouplingScore(existingRelationship: CallsRelationship, newSemanticCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, staticCouplingScore = existingRelationship.staticCouplingScore, dynamicCouplingScore = existingRelationship.dynamicCouplingScore, semanticCouplingScore = newSemanticCouplingScore, evolutionaryCouplingScore = existingRelationship.evolutionaryCouplingScore))
    }

    private fun updateEvolutionaryCouplingScore(existingRelationship: CallsRelationship, newEvolutionaryCouplingScore: Int) {
        removeRelationship(existingRelationship)
        insertRelationship(buildRelationship(existingRelationship.callee, staticCouplingScore = existingRelationship.staticCouplingScore, dynamicCouplingScore = existingRelationship.dynamicCouplingScore, semanticCouplingScore = existingRelationship.semanticCouplingScore, evolutionaryCouplingScore = newEvolutionaryCouplingScore))
    }

    private fun buildRelationship(callee: Unit, staticCouplingScore: Int, dynamicCouplingScore: Int, semanticCouplingScore: Int, evolutionaryCouplingScore: Int): CallsRelationship {
        return CallsRelationship(caller = this, callee = callee, staticCouplingScore = staticCouplingScore, dynamicCouplingScore = dynamicCouplingScore, semanticCouplingScore = semanticCouplingScore, evolutionaryCouplingScore = evolutionaryCouplingScore)
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
