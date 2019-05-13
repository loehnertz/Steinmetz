<template>
    <Network
            ref="graph"
            :nodes="graphNodes"
            :edges="graphEdges"
            :options="graphOptions"
    >
    </Network>
</template>

<script>
    import {Network} from 'vue2vis';

    const DefaultColor = 'orange';
    const ClusterNodeKeyword = '$cluster';
    const LayoutSeed = 55609697;

    export default {
        name: 'Graph',
        components: {
            Network,
        },
        data() {
            return {
                graphNodeIds: new Set(),
                graphNodes: [],
                graphEdges: [],
                graphOptions: {
                    nodes: {
                        font: {
                            face: 'Titillium Web',
                            size: 21,
                        },
                        margin: 15,
                        shape: 'box',
                    },
                    edges: {
                        scaling: {
                            customScalingFunction: this.getEdgeScalingFunction(),
                        },
                        smooth: {
                            type: 'dynamic',
                        },
                    },
                    layout: {
                        randomSeed: LayoutSeed,
                    },
                    interaction: {
                        hideEdgesOnDrag: true,
                    },
                    manipulation: {
                        enabled: true,
                        addNode: false,
                        addEdge: false,
                        deleteNode: false,
                        deleteEdge: false,
                    },
                    physics: {
                        barnesHut: {
                            gravitationalConstant: 0,
                        },
                    },
                },
                nodesInOpenClusters: [],
            }
        },
        watch: {
            graphData: function (graphData) {
                if (graphData) this.rerenderGraph();
            },
            isClustered: function () {
                this.rerenderGraph();
            },
            showClusterNodes: function () {
                this.rerenderGraph();
            },
        },
        methods: {
            flushGraph() {
                this.graphNodeIds = new Set();
                this.graphNodes = [];
                this.graphEdges = [];
            },
            constructGraph(nodes, relationships) {
                this.configureGravitation(relationships.length);
                const clusterIds = this.setNodes(nodes);
                this.setEdges(relationships, clusterIds);
            },
            rerenderGraph() {
                this.flushGraph();
                this.constructGraph(this.graphData["nodes"], this.graphData["edges"]);
            },
            configureGravitation(relationshipAmount) {
                this.graphOptions.physics.barnesHut.gravitationalConstant = -(relationshipAmount * 1000)
            },
            setNodes(nodes) {
                const clusterIds = nodes.map((node) => {
                    return node["attributes"]["cluster"]
                });
                const clusterAmount = new Set(clusterIds).size;

                for (let node of nodes) {
                    let unitNode = this.buildUnitNode(
                        node["unit"]["identifier"],
                        node["unit"]["packageIdentifier"],
                        node["attributes"]["cluster"],
                        clusterAmount,
                    );

                    if (!this.graphNodeIds.has(unitNode.id)) {
                        this.graphNodes.push(unitNode);
                        this.graphNodeIds.add(unitNode.id);
                    }
                }

                return clusterIds;
            },
            setEdges(relationships, clusterIds) {
                for (let relationship of relationships) {
                    const startNode = relationship.start;
                    const endNode = relationship.end;
                    const attributes = relationship.attributes;

                    const edge = this.buildUnitEdge(
                        this.constructUnitNodeId(startNode["identifier"], startNode["packageIdentifier"]),
                        this.constructUnitNodeId(endNode["identifier"], endNode["packageIdentifier"]),
                        attributes["couplingScore"],
                    );

                    this.graphEdges.push(edge);
                }

                if (this.isClustered) this.clusterGraph(clusterIds, this.showClusterNodes);
            },
            constructUnitNodeId(identifier, packageIdentifier) {
                return `${packageIdentifier}.${identifier}`;
            },
            constructClusterNodeId(clusterId) {
                return `${ClusterNodeKeyword}:${clusterId}`;
            },
            buildUnitNode(identifier, packageIdentifier, clusterId, clusterAmount) {
                return {
                    id: this.constructUnitNodeId(identifier, packageIdentifier),
                    cid: clusterId,
                    title: this.generateGraphPopup(`${packageIdentifier}.${identifier}`),
                    label: identifier,
                    borderWidth: 5,
                    color: {
                        background: 'whitesmoke',
                        border: this.getNodeBorderColor(clusterId, clusterAmount),
                    },
                }
            },
            buildServiceNode(clusterId, hidden) {
                return {
                    id: this.constructClusterNodeId(clusterId),
                    cid: clusterId,
                    title: this.generateGraphPopup(`Service ${clusterId}`),
                    borderWidth: 5,
                    color: {
                        background: 'whitesmoke',
                        border: DefaultColor,
                    },
                    shape: 'hexagon',
                    hidden: hidden,
                }
            },
            buildUnitEdge(startNodeId, endNodeId, weight) {
                return {
                    from: startNodeId,
                    to: endNodeId,
                    title: this.generateGraphPopup(`${weight}`),
                    value: weight,
                    color: {
                        color: 'green',
                        highlight: 'fuchsia',
                    },
                    arrows: {
                        to: {
                            enabled: true,
                            type: 'arrow',
                        },
                    },
                    arrowStrikethrough: false,
                }
            },
            buildServiceEdge(unitNodeId, serviceNodeId) {
                return {
                    from: unitNodeId,
                    to: serviceNodeId,
                    color: {
                        color: DefaultColor,
                        highlight: DefaultColor,
                    },
                }
            },
            clusterGraph(clusterIds, showClusterNodes) {
                if (showClusterNodes) {
                    const unit2unitEdges = this.graphEdges;
                    this.graphEdges = [];

                    for (let unitEdge of unit2unitEdges) {
                        const fromClusterNode = this.constructClusterNodeId(this.findNodeById(unitEdge.from).cid);
                        const toClusterNode = this.constructClusterNodeId(this.findNodeById(unitEdge.to).cid);

                        if (fromClusterNode !== toClusterNode) {
                            const clusterUnitEdge = this.buildUnitEdge(fromClusterNode, toClusterNode, unitEdge.value);
                            const existingEdgeIndex = this.graphEdges.findIndex((edge) => {
                                return edge.from === clusterUnitEdge.from && edge.to === clusterUnitEdge.to;
                            });

                            if (existingEdgeIndex === -1) {
                                this.graphEdges.push(clusterUnitEdge);
                            } else {
                                this.graphEdges[existingEdgeIndex].value += unitEdge.value;
                            }
                        }
                    }
                }

                for (let unitNode of this.graphNodes) {
                    const clusterEdge = this.buildServiceEdge(unitNode.id, this.constructClusterNodeId(unitNode.cid));

                    this.graphEdges.push(clusterEdge);
                }

                for (let clusterId of clusterIds) {
                    const clusterNode = this.buildServiceNode(clusterId, !showClusterNodes);

                    if (!this.graphNodeIds.has(clusterNode.id)) {
                        this.graphNodes.push(clusterNode);
                        this.graphNodeIds.add(clusterNode.id);
                    }
                }
            },
            findNodeById(nodeId) {
                return this.graphNodes.find((node) => {
                    return node.id === nodeId;
                });
            },
            generateGraphPopup(title) {
                return (
                    `<span class="box" style="font-family: 'Titillium Web', sans-serif; padding: 11px !important;">${title}</span>`
                );
            },
            // Adapted from: https://krazydad.com/tutorials/makecolors.php
            getNodeBorderColor(coloringKey, maxColoringKey) {
                if (!coloringKey || !maxColoringKey) return DefaultColor;

                const i = (coloringKey * 255 / maxColoringKey);
                const r = Math.round(Math.sin(0.024 * i) * 127 + 128);
                const g = Math.round(Math.sin(0.024 * i + 2) * 127 + 128);
                const b = Math.round(Math.sin(0.024 * i + 4) * 127 + 128);

                return `rgb(${r}, ${g}, ${b})`;
            },
            getEdgeScalingFunction() {
                return function (min, max, total, value) {
                    if (max === min) {
                        return 0.5;
                    } else {
                        let scale = 1 / (max - min);
                        return Math.max(0, (value - min) * scale);
                    }
                }
            },
        },
        props: {
            graphData: {
                type: Object,
                default: () => ({}),
            },
            isClustered: {
                type: Boolean,
                default: () => (true),
            },
            showClusterNodes: {
                type: Boolean,
                default: () => (false),
            }
        },
    }
</script>

<style scoped>
    @import '~vue2vis/dist/vue2vis.css';
</style>
