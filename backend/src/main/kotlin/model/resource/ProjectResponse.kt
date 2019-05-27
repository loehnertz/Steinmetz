package model.resource

import model.graph.Graph
import model.metrics.Metrics


class ProjectResponse(
        val graph: Graph,
        val metrics: Metrics
)
