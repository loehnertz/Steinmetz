<template>
    <div :class="{ 'green-background': highlightBackground }">
        <h2 class="title" :style="{ fontSize: (fontSize * 2) + 'em' }">
            {{ clusteringAlgorithm }}
        </h2>
        <br>
        <p :style="{ fontSize: fontSize + 'em' }">
            Amount Clusters: {{ nonNullAmountOfClusters }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Amount Inter-Cluster Edges: {{ nonNullAmountOfInterClusterEdges }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Ratio Inter-Cluster Edge Weights: {{ nonNullPercentageInterClusterEdgeWeights }}%
        </p>
        <br>
        <br>
        <p :style="{ fontSize: fontSize + 'em' }">
            Total Coupling Modularity: {{ nonNullTotalCouplingModularity }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Average Coupling Modularity: {{ nonNullAverageCouplingModularity }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Dynamic Coupling Modularity: {{ nonNullDynamicCouplingModularity }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Semantic Coupling Modularity: {{ nonNullSemanticCouplingModularity }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Evolutionary Coupling Modularity: {{ nonNullEvolutionaryCouplingModularity }}
        </p>
        <br>
        <p :style="{ fontSize: fontSize + 'em' }">
            Total Mean Cluster Factor: {{ nonNullTotalMeanClusterFactor }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Average Mean Cluster Factor: {{ nonNullAverageMeanClusterFactor }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Dynamic Mean Cluster Factor: {{ nonNullDynamicMeanClusterFactor }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Semantic Mean Cluster Factor: {{ nonNullSemanticMeanClusterFactor }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Evolutionary Mean Cluster Factor: {{ nonNullEvolutionaryMeanClusterFactor }}
        </p>
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
            nonNullDynamicMeanClusterFactor: function () {
                if (!this.dynamicMeanClusterFactor) return NotAvailableLabel;
                return Number(this.dynamicMeanClusterFactor).toFixed(2);
            },
            nonNullSemanticMeanClusterFactor: function () {
                if (!this.semanticMeanClusterFactor) return NotAvailableLabel;
                return Number(this.semanticMeanClusterFactor).toFixed(2);
            },
            nonNullEvolutionaryMeanClusterFactor: function () {
                if (!this.evolutionaryMeanClusterFactor) return NotAvailableLabel;
                return Number(this.evolutionaryMeanClusterFactor).toFixed(2);
            },
            nonNullAverageMeanClusterFactor: function () {
                if (!this.averageMeanClusterFactor) return NotAvailableLabel;
                return Number(this.averageMeanClusterFactor).toFixed(2);
            },
            nonNullTotalMeanClusterFactor: function () {
                if (!this.totalMeanClusterFactor) return NotAvailableLabel;
                return Number(this.totalMeanClusterFactor).toFixed(2);
            },
            nonNullTotalCouplingModularity: function () {
                if (!this.totalCouplingModularity) return NotAvailableLabel;
                return Number(this.totalCouplingModularity).toFixed(2);
            },
            nonNullAverageCouplingModularity: function () {
                if (!this.averageCouplingModularity) return NotAvailableLabel;
                return Number(this.averageCouplingModularity).toFixed(2);
            },
            nonNullDynamicCouplingModularity: function () {
                if (!this.totalCouplingModularity) return NotAvailableLabel;
                return Number(this.dynamicCouplingModularity).toFixed(2);
            },
            nonNullSemanticCouplingModularity: function () {
                if (!this.totalCouplingModularity) return NotAvailableLabel;
                return Number(this.semanticCouplingModularity).toFixed(2);
            },
            nonNullEvolutionaryCouplingModularity: function () {
                if (!this.totalCouplingModularity) return NotAvailableLabel;
                return Number(this.evolutionaryCouplingModularity).toFixed(2);
            },
        },
        props: {
            fontSize: Number,
            clusteringAlgorithm: String,
            highlightBackground: Boolean,
            amountOfClusters: Number,
            amountOfInterClusterEdges: Number,
            accumulatedInterClusterEdgeWeights: Number,
            percentageInterClusterEdgeWeights: Number,
            dynamicCouplingModularity: Number,
            semanticCouplingModularity: Number,
            evolutionaryCouplingModularity: Number,
            averageCouplingModularity: Number,
            totalCouplingModularity: Number,
            dynamicMeanClusterFactor: Number,
            semanticMeanClusterFactor: Number,
            evolutionaryMeanClusterFactor: Number,
            averageMeanClusterFactor: Number,
            totalMeanClusterFactor: Number,
        },
    }
</script>

<style scoped>
    .title {
        margin: 0 !important;
    }

    .green-background {
        background-color: #00d1b2;
    }
</style>
