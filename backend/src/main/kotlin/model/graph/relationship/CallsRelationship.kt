package model.graph.relationship

import model.graph.node.Unit
import org.neo4j.ogm.annotation.*


@RelationshipEntity(type = CallsRelation)
class CallsRelationship(
        @StartNode var caller: Unit,
        @EndNode var callee: Unit,
        @Property val couplingScore: Int? = null
) {
    @Id
    @GeneratedValue
    private val relationshipId: Long? = null
}
