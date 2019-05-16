<template>
    <div class="box">
        <h2 class="title">{{ clusteringAlgorithm }}</h2>
        <br>
        <p>Amount Clusters: {{ nonNullAmountOfClusters }}</p>
        <p>Amount Inter-Cluster Edges: {{ nonNullAmountOfInterClusterEdges }}</p>
        <p>Graph Modularity (Q): {{ nonNullGraphModularity }}</p>
        <p>Accumulated Inter-Cluster Edge Weights: {{ nonNullAccumulatedInterClusterEdgeWeights }}</p>
        <p>Ratio Inter-Cluster Edge Weights: {{ nonNullPercentageInterClusterEdgeWeights }}%</p>
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
            nonNullGraphModularity: function () {
                if (!this.graphModularity) return NotAvailableLabel;
                return Number(this.graphModularity).toFixed(2);
            },
            nonNullAccumulatedInterClusterEdgeWeights: function () {
                if (!this.accumulatedInterClusterEdgeWeights) return NotAvailableLabel;
                return this.accumulatedInterClusterEdgeWeights;
            },
            nonNullPercentageInterClusterEdgeWeights: function () {
                if (!this.percentageInterClusterEdgeWeights) return NotAvailableLabel;
                return this.percentageInterClusterEdgeWeights;
            },
        },
        props: {
            clusteringAlgorithm: String,
            amountOfClusters: Number,
            amountOfInterClusterEdges: Number,
            graphModularity: Number,
            accumulatedInterClusterEdgeWeights: Number,
            percentageInterClusterEdgeWeights: Number,
        },
    }
</script>

<style scoped>
    .title {
        margin: 0 !important;
    }
</style>
