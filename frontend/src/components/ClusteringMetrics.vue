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
            Total Modularization Quality: {{ nonNullTotalModularizationQuality }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Average Modularization Quality: {{ nonNullAverageModularizationQuality }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Dynamic Modularization Quality: {{ nonNullDynamicModularizationQuality }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Semantic Modularization Quality: {{ nonNullSemanticModularizationQuality }}
        </p>
        <p :style="{ fontSize: fontSize + 'em' }">
            Evolutionary Modularization Quality: {{ nonNullEvolutionaryModularizationQuality }}
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
            nonNullDynamicModularizationQuality: function () {
                if (!this.dynamicModularizationQuality) return NotAvailableLabel;
                return Number(this.dynamicModularizationQuality).toFixed(2);
            },
            nonNullSemanticModularizationQuality: function () {
                if (!this.semanticModularizationQuality) return NotAvailableLabel;
                return Number(this.semanticModularizationQuality).toFixed(2);
            },
            nonNullEvolutionaryModularizationQuality: function () {
                if (!this.evolutionaryModularizationQuality) return NotAvailableLabel;
                return Number(this.evolutionaryModularizationQuality).toFixed(2);
            },
            nonNullAverageModularizationQuality: function () {
                if (!this.averageModularizationQuality) return NotAvailableLabel;
                return Number(this.averageModularizationQuality).toFixed(2);
            },
            nonNullTotalModularizationQuality: function () {
                if (!this.totalModularizationQuality) return NotAvailableLabel;
                return Number(this.totalModularizationQuality).toFixed(2);
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
            dynamicModularizationQuality: Number,
            semanticModularizationQuality: Number,
            evolutionaryModularizationQuality: Number,
            averageModularizationQuality: Number,
            totalModularizationQuality: Number,
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
