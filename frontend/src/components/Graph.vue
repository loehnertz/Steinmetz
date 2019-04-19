<template>
    <Network
            :nodes="graphNodes"
            :edges="graphEdges"
            :options="graphOptions"
    >
    </Network>
</template>

<script>
    import {Network} from 'vue2vis';

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
                    edges: {
                        scaling: {
                            customScalingFunction: this.getEdgeScalingFunction(),
                        },
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
                if (graphData) this.constructGraph(this.graphData["edges"]);
            },
        },
        methods: {
            constructGraph(relationships) {
                this.configureGravitation(relationships.length);
                this.setNodes(relationships);
                this.setEdges(relationships);
            },
            configureGravitation(relationshipAmount) {
                this.graphOptions.physics.barnesHut.gravitationalConstant = -(relationshipAmount * 100)
            },
            setNodes(relationships) {
                for (let relationship of relationships) {
                    let startNode = this.buildNode(relationship.start["identifier"], relationship.start["packageIdentifier"]);
                    let endNode = this.buildNode(relationship.end["identifier"], relationship.end["packageIdentifier"]);

                    if (!this.graphNodeIds.has(startNode.id)) {
                        this.graphNodes.push(startNode);
                        this.graphNodeIds.add(startNode.id);
                    }

                    if (!this.graphNodeIds.has(endNode.id)) {
                        this.graphNodes.push(endNode);
                        this.graphNodeIds.add(endNode.id);
                    }
                }
            },
            setEdges(relationships) {
                for (let relationship of relationships) {
                    let startNode = relationship.start;
                    let endNode = relationship.end;
                    let attributes = relationship.attributes;

                    let edge = this.buildEdge(
                        this.constructNodeId(startNode["identifier"], startNode["packageIdentifier"]),
                        this.constructNodeId(endNode["identifier"], endNode["packageIdentifier"]),
                        '',
                        attributes["couplingScore"],
                    );

                    this.graphEdges.push(edge);
                }
            },
            constructNodeId(identifier, packageIdentifier) {
                return `${packageIdentifier}.${identifier}`;
            },
            buildNode(identifier, packageIdentifier) {
                return {
                    id: this.constructNodeId(identifier, packageIdentifier),
                    title: identifier,
                    label: identifier,
                    borderWidth: 2,
                    color: {
                        background: 'whitesmoke',
                        border: 'orange',
                    },
                }
            },
            buildEdge(startNodeId, endNodeId, label, weight) {
                return {
                    from: startNodeId,
                    to: endNodeId,
                    label: label,
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
</style>
