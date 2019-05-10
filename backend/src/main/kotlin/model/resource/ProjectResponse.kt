package model.resource

import controller.analysis.metrics.Metrics
import model.graph.Graph


class ProjectResponse(
        val graph: Graph,
        val metrics: Metrics
)
