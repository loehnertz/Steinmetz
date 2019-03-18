package model.neo4j.relationship

import model.neo4j.GraphEntity
import model.neo4j.node.Unit
import org.neo4j.ogm.annotation.*


@RelationshipEntity(type = CallsRelation)
class CallsRelationship(
        @StartNode var caller: Unit,
        @EndNode var callee: Unit,
        @Property var couplingScore: Int = 1
) : GraphEntity {
    @Id
    @GeneratedValue
    override var id: Long? = null
}
