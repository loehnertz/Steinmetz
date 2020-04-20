package model.neo4j.relationship

import model.neo4j.GraphEntity
import model.neo4j.node.Unit
import org.neo4j.ogm.annotation.*


@RelationshipEntity(type = CallsRelation)
class CallsRelationship(
    @StartNode var caller: Unit,
    @EndNode var callee: Unit,
    @Property var dynamicCouplingScore: Int = 0,
    @Property var semanticCouplingScore: Int = 0,
    @Property var evolutionaryCouplingScore: Int = 0
) : GraphEntity {
    @Id
    @GeneratedValue
    override var id: Long? = null
}
