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
            }
        },
        watch: {
            graphData: function (graphData) {
                if (graphData) {
                    this.flushGraph();
                    this.constructGraph(this.graphData["nodes"], this.graphData["edges"]);
                }
            },
        },
        methods: {
            flushGraph() {
                this.graphNodeIds = new Set();
                this.graphNodes = [];
                this.graphEdges = [];
            },
            constructGraph(units, relationships) {
                this.configureGravitation(relationships.length);
                const clusterIds = this.setNodes(units);
                this.setEdges(relationships, clusterIds);
            },
            configureGravitation(relationshipAmount) {
                this.graphOptions.physics.barnesHut.gravitationalConstant = -(relationshipAmount * 100)
            },
            setNodes(units) {
                const clusterIds = units.map((unit) => {
                    return unit["attributes"]["cluster"]
                });
                const clusterAmount = new Set(clusterIds).size;

                for (let unit of units) {
                    let node = this.buildUnitNode(
                        unit["identifier"],
                        unit["packageIdentifier"],
                        unit["attributes"]["cluster"],
                        clusterAmount,
                    );

                    if (!this.graphNodeIds.has(node.id)) {
                        this.graphNodes.push(node);
                        this.graphNodeIds.add(node.id);
                    }
                }

                return clusterIds;
            },
            setEdges(relationships, clusterIds) {
                for (let relationship of relationships) {
                    let startNode = relationship.start;
                    let endNode = relationship.end;

                    let edge = this.buildUnitEdge(
                        this.constructUnitNodeId(startNode["identifier"], startNode["packageIdentifier"]),
                        this.constructUnitNodeId(endNode["identifier"], endNode["packageIdentifier"]),
                        100,
                    );

                    this.graphEdges.push(edge);
                }

                if (this.isClustered) this.clusterGraph(clusterIds);
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
                    title: this.generateNodePopup(`${packageIdentifier}.${identifier}`),
                    label: identifier,
                    borderWidth: 5,
                    color: {
                        background: 'whitesmoke',
                        border: this.getNodeBorderColor(clusterId, clusterAmount),
                    },
                }
            },
            buildServiceNode(clusterId) {
                return {
                    id: this.constructClusterNodeId(clusterId),
                    cid: clusterId,
                    title: this.generateNodePopup(`Service ${clusterId}`),
                    borderWidth: 5,
                    color: {
                        background: 'whitesmoke',
                        border: DefaultColor,
                    },
                    shape: 'hexagon',
                }
            },
            buildUnitEdge(startNodeId, endNodeId, length) {
                return {
                    from: startNodeId,
                    to: endNodeId,
                    length: length,
                    value: 1,
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
            clusterGraph(clusterIds) {
                const unit2unitEdges = this.graphEdges;
                this.graphEdges = [];

                for (let clusterId of clusterIds) {
                    const clusterNode = this.buildServiceNode(clusterId);

                    if (!this.graphNodeIds.has(clusterNode.id)) {
                        this.graphNodes.push(clusterNode);
                        this.graphNodeIds.add(clusterNode.id);
                    }
                }

                for (let unitNode of this.graphNodes.filter((node) => {
                    return !node.id.startsWith(ClusterNodeKeyword)
                })) {
                    const clusterEdge = this.buildServiceEdge(unitNode.id, this.constructClusterNodeId(unitNode.cid));
                    this.graphEdges.push(clusterEdge);
                }

                for (let unitEdge of unit2unitEdges) {
                    const fromClusterNode = this.constructClusterNodeId(this.findNodeById(unitEdge.from).cid);
                    const toClusterNode = this.constructClusterNodeId(this.findNodeById(unitEdge.to).cid);

                    if (fromClusterNode !== toClusterNode) {
                        const clusterUnitEdge = this.buildUnitEdge(fromClusterNode, toClusterNode, 555);
                        const existingEdgeIndex = this.graphEdges.findIndex((edge) => {
                            return edge.from === clusterUnitEdge.from && edge.to === clusterUnitEdge.to
                        });

                        if (existingEdgeIndex === -1) {
                            this.graphEdges.push(clusterUnitEdge);
                        } else {
                            this.graphEdges[existingEdgeIndex].value = this.graphEdges[existingEdgeIndex].value + 1;
                        }
                    }
                }
            },
            findNodeById(nodeId) {
                return this.graphNodes.find((node) => {
                    return node.id === nodeId;
                });
            },
            generateNodePopup(title) {
                return `<span style="font-family: 'Titillium Web', sans-serif;">${title}</span>`;
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
        },
    }
</script>

<style scoped>
    @import "~vue2vis/dist/vue2vis.css";
</style>
