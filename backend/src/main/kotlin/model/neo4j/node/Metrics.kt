package model.neo4j.node

import controller.analysis.metrics.inputquality.InputQuality
import model.neo4j.GraphEntity
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity


@NodeEntity
class Metrics(var projectName: String, var dynamicAnalysisQuality: Int) : GraphEntity {
    @Id
    @GeneratedValue
    override var id: Long? = null

    companion object Factory {
        fun create(projectName: String, metrics: controller.analysis.metrics.Metrics): Metrics {
            return Metrics(projectName = projectName, dynamicAnalysisQuality = metrics.inputQuality.dynamicAnalysis)
        }

        fun convertToDataClass(metricsNode: Metrics): controller.analysis.metrics.Metrics {
            return controller.analysis.metrics.Metrics(inputQuality = InputQuality(dynamicAnalysis = metricsNode.dynamicAnalysisQuality))
        }
    }
}
