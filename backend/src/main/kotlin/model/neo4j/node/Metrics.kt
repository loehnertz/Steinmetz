package model.neo4j.node

import model.metrics.InputQuality
import model.neo4j.GraphEntity
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity


@NodeEntity
class Metrics(var projectName: String, var dynamicAnalysisQuality: Int?) : GraphEntity {
    @Id
    @GeneratedValue
    override var id: Long? = null

    companion object Factory {
        fun create(projectName: String, metrics: model.metrics.Metrics): Metrics {
            return Metrics(
                    projectName = projectName,
                    dynamicAnalysisQuality = metrics.inputQuality?.dynamicAnalysis
            )
        }

        fun convertToDataClass(metricsNode: Metrics): model.metrics.Metrics {
            return model.metrics.Metrics(
                    inputQuality = InputQuality(
                            dynamicAnalysis = metricsNode.dynamicAnalysisQuality!!
                    )
            )
        }
    }
}
