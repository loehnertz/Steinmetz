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
    const NormalNodeShape = 'dot';
    const InterfaceNodeShape = 'diamond';
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
                            size: 150,
                        },
                        margin: 15,
                        shape: NormalNodeShape,
                    },
                    edges: {
                        scaling: {
                            customScalingFunction: this.getEdgeScalingFunction(),
                        },
                        selectionWidth: this.getSelectionWidthFunction(),
                        smooth: {
                            type: 'cubicBezier',
                        },
                    },
                    layout: {
                        randomSeed: LayoutSeed,
                    },
                    interaction: {
                        hideEdgesOnDrag: true,
                    },
                    manipulation: {
                        enabled: false,
                    },
                    physics: {
                        forceAtlas2Based: {
                            gravitationalConstant: -100000,
                            springConstant: 0.5,
                        },
                        solver: 'forceAtlas2Based',
                        timestep: 5.5,
                    },
                },
            }
        },
        watch: {
            graphData: function (graphData) {
                if (graphData) this.rerenderGraphWithDelay();
            },
            isClustered: function () {
                this.rerenderGraphWithDelay();
            },
            showClusterNodes: function () {
                this.rerenderGraphWithDelay();
            },
        },
        methods: {
            flushGraph() {
                this.graphNodeIds = new Set();
                this.graphNodes = [];
                this.graphEdges = [];
            },
            constructGraph(nodes, relationships) {
                if (nodes && relationships) {
                    this.setNodes(nodes);
                    this.setEdges(relationships);
                    this.watchStabilization();
                }
            },
            rerenderGraph() {
                this.flushGraph();
                this.constructGraph(this.graphData["nodes"], this.graphData["edges"]);
            },
            rerenderGraphWithDelay() {
                this.flushGraph();
                setTimeout(() => this.constructGraph(this.graphData["nodes"], this.graphData["edges"]), 1000);
            },
            setNodes(nodes) {
                for (let node of nodes) {
                    let unitNode = this.buildUnitNode(
                        node["unit"]["identifier"],
                        node["unit"]["packageIdentifier"],
                        this.unnullifyUnitByteSize(node["attributes"]),
                        node["attributes"]["cluster"],
                        this.clusterIds.size,
                    );

                    if (!this.graphNodeIds.has(unitNode.id)) {
                        this.graphNodes.push(unitNode);
                        this.graphNodeIds.add(unitNode.id);
                    }
                }
            },
            setEdges(relationships) {
                for (let relationship of relationships) {
                    const startNodeIdentifier = this.constructUnitNodeId(relationship.start["identifier"], relationship.start["packageIdentifier"]);
                    const endNodeIdentifier = this.constructUnitNodeId(relationship.end["identifier"], relationship.end["packageIdentifier"]);
                    const attributes = relationship.attributes;

                    let startNodeClusterId;
                    let endNodeClusterId;

                    if (this.isClustered) {
                        startNodeClusterId = this.findNodeById(startNodeIdentifier).cid;
                        endNodeClusterId = this.findNodeById(endNodeIdentifier).cid;
                    }

                    const edge = this.buildUnitEdge(
                        startNodeIdentifier,
                        endNodeIdentifier,
                        attributes["couplingScore"],
                        (startNodeClusterId !== endNodeClusterId),
                    );

                    if (!this.graphEdges.find((currentEdge) => edge.from === currentEdge.from && edge.to === currentEdge.to)) {
                        this.graphEdges.push(edge);
                    }
                }

                if (this.isClustered) this.clusterGraph(this.showClusterNodes);
            },
            constructUnitNodeId(identifier, packageIdentifier) {
                return `${packageIdentifier}.${identifier}`;
            },
            constructClusterNodeId(clusterId) {
                return `${ClusterNodeKeyword}:${clusterId}`;
            },
            buildUnitNode(identifier, packageIdentifier, size, clusterId, clusterAmount) {
                return {
                    id: this.constructUnitNodeId(identifier, packageIdentifier),
                    cid: clusterId,
                    title: this.generateGraphPopup(`${packageIdentifier}.${identifier}<br>Size: ${size} Bytes`),
                    label: identifier,
                    borderWidth: 5,
                    color: {
                        background: 'whitesmoke',
                        border: this.getNodeBorderColor(clusterId, clusterAmount),
                    },
                    size: size / 10,
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
                    size: 500,
                    shape: 'hexagon',
                    hidden: hidden,
                }
            },
            buildUnitEdge(startNodeId, endNodeId, weight, isInterface) {
                let color = 'green';

                if (isInterface) {
                    color = 'blue';
                    this.updateNodeShape(startNodeId, InterfaceNodeShape);
                    this.updateNodeShape(endNodeId, InterfaceNodeShape);
                }

                return {
                    from: startNodeId,
                    to: endNodeId,
                    title: this.generateGraphPopup(`${startNodeId} &rarr; ${endNodeId}<br>Coupling: ${weight}`),
                    value: weight,
                    color: {
                        color: color,
                        highlight: 'firebrick',
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
            clusterGraph(showClusterNodes) {
                if (showClusterNodes) {
                    this.graphOptions.physics.forceAtlas2Based.springConstant = 0.8;

                    const unit2unitEdges = this.graphEdges;
                    this.graphEdges = [];

                    for (let unitEdge of unit2unitEdges) {
                        const fromClusterNode = this.constructClusterNodeId(this.findNodeById(unitEdge.from).cid);
                        const toClusterNode = this.constructClusterNodeId(this.findNodeById(unitEdge.to).cid);

                        if (fromClusterNode !== toClusterNode) {
                            const clusterUnitEdge = this.buildUnitEdge(fromClusterNode, toClusterNode, unitEdge.value, false);
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
                } else {
                    this.graphOptions.physics.forceAtlas2Based.springConstant = 0.08;
                }

                for (let unitNodeIndex in this.graphNodes) {
                    if (showClusterNodes && !this.graphNodes[unitNodeIndex]["id"].startsWith(ClusterNodeKeyword)) {
                        this.graphNodes[unitNodeIndex]["size"] = 100;
                    }

                    const unitNode = this.graphNodes[unitNodeIndex];
                    const clusterEdge = this.buildServiceEdge(unitNode.id, this.constructClusterNodeId(unitNode.cid));
                    clusterEdge.smooth = {type: 'dynamic'};

                    this.graphEdges.push(clusterEdge);
                }

                for (let clusterId of this.clusterIds) {
                    const clusterNode = this.buildServiceNode(clusterId, !showClusterNodes);

                    if (!this.graphNodeIds.has(clusterNode.id)) {
                        this.graphNodes.push(clusterNode);
                        this.graphNodeIds.add(clusterNode.id);
                    }
                }
            },
            unnullifyUnitByteSize(unitAttributes) {
                if (!unitAttributes["footprint"] || !unitAttributes["footprint"]["byteSize"]) return 1024;
                return unitAttributes["footprint"]["byteSize"];
            },
            watchStabilization() {
                const stabilizationWatcher = setInterval(() => {
                    if (!this.$refs["graph"]) return;
                    if (this.$refs["graph"].network.physics.stabilizationIterations >= this.$refs["graph"].network.physics.options.stabilization.iterations) {
                        this.$refs["graph"].network.physics.stabilized = true;
                        clearInterval(stabilizationWatcher);
                    }
                }, 1000);
            },
            updateNodeShape(nodeId, nodeShape) {
                const nodeIndex = this.graphNodes.findIndex((node) => node.id === nodeId);
                this.graphNodes[nodeIndex]["shape"] = nodeShape;
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
                        const scale = 10 / (max - min);
                        return Math.max(0, (value - min) * scale);
                    }
                }
            },
            getSelectionWidthFunction() {
                return function (width) {
                    return width * 5;
                }
            }
        },
        props: {
            graphData: {
                type: Object,
                default: () => ({}),
            },
            clusterIds: {
                type: Set,
                default: () => (new Set()),
            },
            isClustered: {
                type: Boolean,
                default: () => (true),
            },
            showClusterNodes: {
                type: Boolean,
                default: () => (false),
            },
            liveRerenderModeActive: {
                type: Boolean,
                default: () => (false),
            },
        },
    }
</script>

<style scoped>
    @import '~vue2vis/dist/vue2vis.css';
</style>
