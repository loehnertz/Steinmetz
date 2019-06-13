<template>
    <div>
        <h2 class="title">{{ clusteringAlgorithm }}</h2>
        <br>
        <p :style="{ fontSize: fontSize }">Amount Clusters: {{ nonNullAmountOfClusters }}</p>
        <p :style="{ fontSize: fontSize }">Amount Inter-Cluster Edges: {{ nonNullAmountOfInterClusterEdges }}</p>
        <p :style="{ fontSize: fontSize }">Ratio Inter-Cluster Edge Weights: {{ nonNullPercentageInterClusterEdgeWeights }}%</p>
        <br>
        <p :style="{ fontSize: fontSize }">Total Coupling Modularity (Q): {{ nonNullTotalCouplingModularity }}</p>
        <p :style="{ fontSize: fontSize }">Average Coupling Modularity (ØQ(C)): {{ nonNullAverageCouplingModularity }}</p>
        <p :style="{ fontSize: fontSize }">Dynamic Coupling Modularity (Q(DC)): {{ nonNullDynamicCouplingModularity }}</p>
        <p :style="{ fontSize: fontSize }">Semantic Coupling Modularity (Q(SC)): {{ nonNullSemanticCouplingModularity }}</p>
        <p :style="{ fontSize: fontSize }">Logical Coupling Modularity (Q(LC)): {{ nonNullLogicalCouplingModularity }}</p>
    </div>
</template>

<script>
    const NotAvailableLabel = 'N/A';

    export default {
        computed: {
            nonNullAmountOfClusters: function () {
                if (!this.amountOfClusters) return NotAvailableLabel;
                return this.amountOfClusters;
            },
            nonNullAmountOfInterClusterEdges: function () {
                if (!this.amountOfInterClusterEdges) return NotAvailableLabel;
                return this.amountOfInterClusterEdges;
            },
            nonNullAccumulatedInterClusterEdgeWeights: function () {
                if (!this.accumulatedInterClusterEdgeWeights) return NotAvailableLabel;
                return this.accumulatedInterClusterEdgeWeights;
            },
            nonNullPercentageInterClusterEdgeWeights: function () {
                if (!this.percentageInterClusterEdgeWeights) return NotAvailableLabel;
                return this.percentageInterClusterEdgeWeights;
            },
            nonNullGraphModularity: function () {
                if (!this.graphModularity) return NotAvailableLabel;
                return Number(this.graphModularity).toFixed(2);
            },
            nonNullTotalCouplingModularity: function () {
                if (!this.graphModularity) return NotAvailableLabel;
                return Number(this.totalCouplingModularity).toFixed(2);
            },
            nonNullAverageCouplingModularity: function () {
                const couplingModularityValues = [this.nonNullDynamicCouplingModularity, this.nonNullSemanticCouplingModularity, this.nonNullLogicalCouplingModularity];
                if (couplingModularityValues.includes(NotAvailableLabel)) return NotAvailableLabel;
                return Number(this.calculateAverage(couplingModularityValues)).toFixed(2);
            },
            nonNullDynamicCouplingModularity: function () {
                if (!this.graphModularity) return NotAvailableLabel;
                return Number(this.dynamicCouplingModularity).toFixed(2);
            },
            nonNullSemanticCouplingModularity: function () {
                if (!this.graphModularity) return NotAvailableLabel;
                return Number(this.semanticCouplingModularity).toFixed(2);
            },
            nonNullLogicalCouplingModularity: function () {
                if (!this.graphModularity) return NotAvailableLabel;
                return Number(this.logicalCouplingModularity).toFixed(2);
            },
        },
        methods: {
            calculateAverage(couplingModularityValues) {
                const numberValues = couplingModularityValues.map(Number);
                return (numberValues.reduce((a, b) => a + b, 0) / numberValues.length);
            },
        },
        props: {
            fontSize: String,
            clusteringAlgorithm: String,
            amountOfClusters: Number,
            amountOfInterClusterEdges: Number,
            accumulatedInterClusterEdgeWeights: Number,
            percentageInterClusterEdgeWeights: Number,
            graphModularity: Number,
            dynamicCouplingModularity: Number,
            semanticCouplingModularity: Number,
            logicalCouplingModularity: Number,
            totalCouplingModularity: Number,
        },
    }
</script>

<style scoped>
    .title {
        margin: 0 !important;
    }
</style>
