<template>
    <div>
        <div
                class="box"
                v-for="clusterId in clusterIds"
                :key="clusterId"
        >
            <h2 class="subtitle">Service #{{ clusterId }}</h2>
            <p>{{ cluster2UnitsMap[clusterId] }}</p>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'UnitTree',
        computed: {
            cluster2UnitsMap: function () {
                return this.computeCluster2UnitsMap();
            },
        },
        data() {
            return {}
        },
        methods: {
            computeCluster2UnitsMap() {
                const cluster2UnitsMap = {};

                for (let clusterId of this.clusterIds) {
                    const nodesInCluster = this.retrieveNodesByClusterId(clusterId);
                    cluster2UnitsMap[clusterId] = this.transformNodesToUnits(nodesInCluster);
                }

                return cluster2UnitsMap;
            },
            retrieveNodesByClusterId(clusterId) {
                return this.graphData.nodes.filter((node) => node.attributes.cluster === clusterId);
            },
            transformNodesToUnits(nodes) {
                const units = nodes.map((node) => `${node.unit["packageIdentifier"]}.${node.unit["identifier"]}`);
                const unitPackagesList = units.map((unit) => unit.split('.'));

                const tree = {};
                for (let unitPackages in unitPackagesList[0]) {
                    tree[unitPackagesList[0][unitPackages]] = {};
                }
                console.log(tree);

                return units;
            },
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
        },
    }
</script>

<style scoped>
    .subtitle {
        font-weight: bold;
    }
</style>
