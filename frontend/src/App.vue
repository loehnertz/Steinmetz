<template>
    <div id="app">
        <notifications/>
        <div id="throbber" v-show="isLoading">
            <Throbber :is-loading="isLoading"/>
        </div>
        <section class="section">
            <div class="container box" ref="uploading-controls">
                <div class="level">
                    <div class="level-left">
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control has-icons-left tooltip is-tooltip-bottom"
                                        data-tooltip="A unique project identifier for the project to add"
                                >
                                    <input
                                            class="input"
                                            type="text"
                                            placeholder="Project Name"
                                            v-model="uploadProjectName"
                                    >
                                    <span class="icon is-small is-left">
                                        <i class="fas fa-tag"></i>
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control has-icons-left tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="The common package identifier of the project (utilized to exclude depencencies from the analysis)"
                                >
                                    <input
                                            class="input"
                                            type="text"
                                            placeholder="Base Package Identifier"
                                            v-model="uploadBasePackageIdentifier"
                                    >
                                    <span class="icon is-small is-left">
                                        <i class="fas fa-box"></i>
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control has-icons-left tooltip is-tooltip-bottom"
                                        data-tooltip="Sets the project platform of the project to be uploaded"
                                >
                                    <span class="select">
                                        <label>
                                            <select v-model="uploadProjectPlatform">
                                                <option value="java">Java</option>
                                            </select>
                                        </label>
                                    </span>
                                    <span class="icon is-small is-left">
                                        <i class="fas fa-hdd"></i>
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control has-icons-left tooltip is-tooltip-bottom"
                                        data-tooltip="Sets the VCS system of the project to be uploaded"
                                >
                                    <span class="select">
                                        <label>
                                            <select v-model="uploadVcsSystem">
                                                <option value="git2">Git</option>
                                                <option value="hg">Mercurial</option>
                                                <option value="svn">SVN</option>
                                                <option value="p4">Perforce</option>
                                                <option value="tfs">Team Foundation Server </option>
                                            </select>
                                        </label>
                                    </span>
                                    <span class="icon is-small is-left">
                                        <i class="fas fa-code-branch"></i>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="level">
                    <div class="level-left">
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="Sets the static program analysis file according to the selected platform for the upload"
                                >
                                    <div class="file">
                                        <label class="file-label">
                                            <input
                                                    class="file-input"
                                                    type="file"
                                                    placeholder="Static Analysis File"
                                                    @change="onStaticAnalysisUploadFileChange"
                                            >
                                            <span class="file-cta">
                                                <span class="file-icon">
                                                    <i class="fas fa-upload"></i>
                                                </span>
                                                <span class="file-label">
                                                    {{ staticProgramAnalyisUploadLabel }}
                                                </span>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="Sets the dynamic program analysis file according to the selected platform for the upload"
                                >
                                    <div class="file">
                                        <label class="file-label">
                                            <input
                                                    class="file-input"
                                                    type="file"
                                                    placeholder="Dynamic Coupling Analysis File"
                                                    @change="onDynamicAnalysisUploadFileChange"
                                            >
                                            <span class="file-cta">
                                                <span class="file-icon">
                                                    <i class="fas fa-upload"></i>
                                                </span>
                                                <span class="file-label">
                                                    {{ dynamicProgramAnalyisUploadLabel }}
                                                </span>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="Sets the semantic coupling analysis file according to the selected platform for the upload"
                                >
                                    <div class="file">
                                        <label class="file-label">
                                            <input
                                                    class="file-input"
                                                    type="file"
                                                    placeholder="Semantic Coupling Analysis File"
                                                    @change="onSemanticAnalysisUploadFileChange"
                                            >
                                            <span class="file-cta">
                                                <span class="file-icon">
                                                    <i class="fas fa-upload"></i>
                                                </span>
                                                <span class="file-label">
                                                    {{ semanticProgramAnalyisUploadLabel }}
                                                </span>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="Sets the VCS log file according to the selected VCS system for the upload"
                                >
                                    <div class="file">
                                        <label class="file-label">
                                            <input
                                                    class="file-input"
                                                    type="file"
                                                    placeholder="Evolutionary Coupling Analysis File"
                                                    @change="onEvolutionaryAnalysisUploadFileChange"
                                            >
                                            <span class="file-cta">
                                                <span class="file-icon">
                                                    <i class="fas fa-upload"></i>
                                                </span>
                                                <span class="file-label">
                                                    {{ evolutionaryAnalyisUploadLabel }}
                                                </span>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="level-right">
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="Uploads the selected analysis files and starts the server-sided analysis"
                                >
                                    <button class="button is-primary" @click="uploadNewProjectData">Upload</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="container box" ref="retrieval-controls">
                <div class="level">
                    <div class="level-left">
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control has-icons-left tooltip"
                                        data-tooltip="The project identifier of a previously added project"
                                >
                                    <basic-select
                                            class="project-name-search"
                                            placeholder="Project Name"
                                            :options="projectNamesOptions"
                                            @select="onProjectNameChange"
                                    />
                                    <span class="icon is-small is-left" style="padding-top: 0.2em;">
                                        <i class="fas fa-tag"></i>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="level-right">
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-bottom"
                                        data-tooltip="Toggles the graph view"
                                >
                                    <input
                                            class="switch"
                                            id="graphViewEnabled"
                                            type="checkbox"
                                            v-model="graphEnabled"
                                            :checked="graphEnabled"
                                    >
                                    <label for="graphViewEnabled">
                                        Graph View
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-bottom"
                                        data-tooltip="Toggles the textual view"
                                >
                                    <input
                                            class="switch"
                                            id="textualViewEnabled"
                                            type="checkbox"
                                            v-model="overviewEnabled"
                                            :checked="overviewEnabled"
                                    >
                                    <label for="textualViewEnabled">
                                        Textual View
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-bottom"
                                        data-tooltip="Toggles the metrics view"
                                >
                                    <input
                                            class="switch"
                                            id="metricsViewEnabled"
                                            type="checkbox"
                                            v-model="metricsEnabled"
                                            :checked="metricsEnabled"
                                    >
                                    <label for="metricsViewEnabled">
                                        Metrics View
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">&nbsp;</div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip"
                                        data-tooltip="Retrieves the analysis data of a previously added project"
                                >
                                    <button class="button is-primary" @click="fetchAnalysis">Retrieve</button>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline"
                                        data-tooltip="Runs the selected graph clustering algorithm on the analysis data of a previously added project"
                                >
                                    <button class="button is-primary" @click="fetchClusteredGraph">Cluster</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="container box" ref="clustering-controls">
                <div class="level">
                    <div class="level-right">
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control has-icons-left tooltip is-tooltip-bottom"
                                        data-tooltip="Selects the graph clustering algorithm that will be utilized"
                                >
                                    <span class="select">
                                        <label>
                                            <select
                                                    v-model="selectedClusteringAlgorithm"
                                                    :disabled="!selectedProjectId"
                                            >
                                                <option
                                                        v-for="algorithm in graphClusteringAlgorithms"
                                                        :value="algorithm"
                                                        :key="algorithm"
                                                >
                                                    {{ convertClusteringAlgorithmIdentifierToLabel(algorithm) }}
                                                </option>
                                            </select>
                                        </label>
                                    </span>
                                    <span class="icon is-small is-left">
                                        <i class="fas fa-receipt"></i>
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="Sets how many iterations should be run for the clustering algorithms"
                                >
                                    <Slider
                                            :disabled="!selectedProjectId"
                                            :value="maxClusteringIterations"
                                            :min="iterationsClusteringParameterMin"
                                            :max="iterationsClusteringParameterMax"
                                            :step="1"
                                            @value-change="handleIterationsClusteringParameterChange"
                                    />
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control has-icons-left tooltip is-tooltip-bottom"
                                        data-tooltip="Selects the metric to optimize toward"
                                >
                                    <span class="select">
                                        <label>
                                            <select
                                                    v-model="selectedClusteringMetric"
                                                    :disabled="!selectedProjectId"
                                            >
                                                <option
                                                        v-for="metric in graphClusteringMetrics"
                                                        :value="metric"
                                                        :key="metric"
                                                >
                                                    {{ convertClusteringMetricIdentifierToLabel(metric) }}
                                                </option>
                                            </select>
                                        </label>
                                    </span>
                                    <span class="icon is-small is-left">
                                        <i class="fas fa-sort-numeric-up"></i>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="level-left">
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="Toggles that the unit nodes are gravitally drawn towards their respective clusters"
                                >
                                    <input
                                            class="switch"
                                            id="clusteredViewEnabled"
                                            type="checkbox"
                                            v-model="clusteredViewEnabled"
                                            :checked="clusteredViewEnabled"
                                            :disabled="!selectedProjectId || !clusteringAvailable"
                                    >
                                    <label for="clusteredViewEnabled">
                                        Clustered View
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="Toggles that the cluster nodes will be displayed while hiding the inter-cluster edges"
                                >
                                    <input
                                            class="switch"
                                            id="showClusterNodes"
                                            type="checkbox"
                                            v-model="showClusterNodes"
                                            :checked="showClusterNodes"
                                            :disabled="!selectedProjectId || (!clusteredViewEnabled && !clusteringAvailable)"
                                    >
                                    <label for="showClusterNodes">
                                        Hide Inter-Cluster Edges
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="container box" ref="edge-weighting-formula-controls">
                <div class="level">
                    <div class="level-left">
                        <div class="level-item">
                            <p>Coupling Score</p>
                        </div>
                        <div class="level-item">
                            <p>=</p>
                        </div>
                        <div class="level-item">
                            <p>(</p>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-bottom edge-weighting-formula-factor"
                                        data-tooltip="Sets the factor of the dynamic coupling score for edge weighting"
                                >
                                    <label>
                                        <input class="input" type="number" step="1"
                                               v-model="dynamicCouplingScoreFactor">
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <p>&times;</p>
                        </div>
                        <div class="level-item">
                            <p>Dynamic Coupling Score</p>
                        </div>
                        <div class="level-item">
                            <p>+</p>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-bottom edge-weighting-formula-factor"
                                        data-tooltip="Sets the factor of the semantic coupling score for edge weighting"
                                >
                                    <label>
                                        <input class="input" type="number" step="1"
                                               v-model="semanticCouplingScoreFactor">
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <p>&times;</p>
                        </div>
                        <div class="level-item">
                            <p>Semantic Coupling Score</p>
                        </div>
                        <div class="level-item">
                            <p>+</p>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-bottom edge-weighting-formula-factor"
                                        data-tooltip="Sets the factor of the semantic coupling score for edge weighting"
                                >
                                    <label>
                                        <input class="input" type="number" step="1"
                                               v-model="evolutionaryCouplingScoreFactor">
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <p>&times;</p>
                        </div>
                        <div class="level-item">
                            <p>Evolutionary Coupling Score</p>
                        </div>
                        <div class="level-item">
                            <p>)</p>
                        </div>
                        <div class="level-item">
                            <p>&divide;</p>
                        </div>
                        <div class="level-item">
                            <p>{{ couplingScoreFactorSum }}</p>
                        </div>
                    </div>
                    <div class="level-right">
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline is-tooltip-bottom"
                                        data-tooltip="Optimizes the parameters automatically toward the selected metric"
                                >
                                    <button class="button is-primary" @click="fetchOptimizedClusteringParameters">
                                        Optimize
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <section class="section">
            <div class="container box" id="graph-container" v-if="graphData && graphEnabled">
                <Graph
                        id="graph"
                        :graph-data="graphData"
                        :cluster-ids="clusterIds"
                        :is-clustered="clusteredViewEnabled"
                        :show-cluster-nodes="showClusterNodes"
                />
            </div>
        </section>
        <section class="section" v-if="clusteringAvailable && overviewEnabled">
            <div class="container box" id="overview-container">
                <ClusteringOverview
                        :graph-data="graphData"
                        :cluster-ids="clusterIds"
                />
            </div>
        </section>
        <section class="section" id="metrics-container" v-if="clusteringAvailable && metricsEnabled">
            <div class="container">
                <div class="box">
                    <h1 class="title">Metrics</h1>
                    <p>Dynamic Analysis Fidelity: {{ dynamicAnalysisQuality }}%</p>
                    <p>Semantic Analysis Fidelity: {{ semanticAnalysisQuality }}%</p>
                    <p>Evolutionary Analysis Fidelity: {{ evolutionaryAnalysisQuality }}%</p>
                </div>
                <ClusteringMetrics
                        class="box"
                        :font-size="1.0"
                        :highlight-background="selectedClusteringAlgorithm === bestScoringClusteringAlgorithm"
                        :clustering-algorithm="convertClusteringAlgorithmIdentifierToLabel(selectedClusteringAlgorithm)"
                        :amount-of-clusters="clusteringQuality['amountClusters']"
                        :amount-of-inter-cluster-edges="clusteringQuality['amountInterfaceEdges']"
                        :accumulated-inter-cluster-edge-weights="accumulatedInterClusterEdgeWeights"
                        :percentage-inter-cluster-edge-weights="percentageInterClusterEdgeWeights"
                        :dynamic-coupling-modularity="clusteringQuality['dynamicCouplingModularity']"
                        :semantic-coupling-modularity="clusteringQuality['semanticCouplingModularity']"
                        :evolutionary-coupling-modularity="clusteringQuality['evolutionaryCouplingModularity']"
                        :average-coupling-modularity="clusteringQuality['averageCouplingModularity']"
                        :total-coupling-modularity="clusteringQuality['totalCouplingModularity']"
                        :dynamic-modularization-quality="clusteringQuality['dynamicModularizationQuality']"
                        :semantic-modularization-quality="clusteringQuality['semanticModularizationQuality']"
                        :evolutionary-modularization-quality="clusteringQuality['evolutionaryModularizationQuality']"
                        :average-modularization-quality="clusteringQuality['averageModularizationQuality']"
                        :total-modularization-quality="clusteringQuality['totalModularizationQuality']"
                />
                <div class="box level">
                    <div
                            class="level-item"
                            v-for="(metrics, clusteringAlgorithm) in clusteringAlgorithmMetrics"
                            :key="clusteringAlgorithm"
                    >
                        <ClusteringMetrics
                                class="box"
                                :font-size="0.7"
                                :highlight-background="clusteringAlgorithm === bestScoringClusteringAlgorithm"
                                :clustering-algorithm="convertClusteringAlgorithmIdentifierToLabel(clusteringAlgorithm)"
                                :amount-of-clusters="metrics['clusteringQuality']['amountClusters']"
                                :amount-of-inter-cluster-edges="metrics['clusteringQuality']['amountInterfaceEdges']"
                                :accumulated-inter-cluster-edge-weights="metrics['clusteringQuality']['accumulatedInterfaceEdgeWeights']"
                                :percentage-inter-cluster-edge-weights="calculatePercentageRatioBetweenTwoNumbers(metrics['clusteringQuality']['accumulatedInterfaceEdgeWeights'], accumulatedEdgeWeights)"
                                :dynamic-coupling-modularity="metrics['clusteringQuality']['dynamicCouplingModularity']"
                                :semantic-coupling-modularity="metrics['clusteringQuality']['semanticCouplingModularity']"
                                :evolutionary-coupling-modularity="metrics['clusteringQuality']['evolutionaryCouplingModularity']"
                                :average-coupling-modularity="metrics['clusteringQuality']['averageCouplingModularity']"
                                :total-coupling-modularity="metrics['clusteringQuality']['totalCouplingModularity']"
                                :dynamic-modularization-quality="metrics['clusteringQuality']['dynamicModularizationQuality']"
                                :semantic-modularization-quality="metrics['clusteringQuality']['semanticModularizationQuality']"
                                :evolutionary-modularization-quality="metrics['clusteringQuality']['evolutionaryModularizationQuality']"
                                :average-modularization-quality="metrics['clusteringQuality']['averageModularizationQuality']"
                                :total-modularization-quality="metrics['clusteringQuality']['totalModularizationQuality']"
                        />
                    </div>
                </div>
            </div>
        </section>
    </div>
</template>

<script>
    import ClusteringMetrics from './components/ClusteringMetrics.vue';
    import Graph from './components/Graph.vue';
    import ClusteringOverview from './components/ClusteringOverview.vue';
    import Slider from './components/Slider.vue';
    import Throbber from './components/Throbber.vue';

    import {BasicSelect} from 'vue-search-select';
    import axios from 'axios';

    const NotAvailableLabel = 'N/A';
    const MclIdentifier = 'mcl';
    const InfomapIdentifier = 'infomap';
    const LouvainIdentifier = 'louvain';
    const ClausetNewmanMooreIdentifier = 'clauset_newman_moore';
    const WalktrapIdentifier = 'walktrap';
    const ChineseWhispersIdentifier = 'chinese_whispers';
    const LabelPropagationIdentifier = 'label_propagation';
    const WeaklyConnectedComponentsIdentifier = 'weakly_connected_components';
    const GraphClusteringAlgorithms = [MclIdentifier, InfomapIdentifier, LouvainIdentifier, ClausetNewmanMooreIdentifier, WalktrapIdentifier, ChineseWhispersIdentifier, LabelPropagationIdentifier];
    const MetricAmountClusters = 'amountClusters';
    const MetricInterClusterEdges = 'amountInterfaceEdges';
    const MetricTotalCouplingModularity = 'totalCouplingModularity';
    const MetricAverageCouplingModularity = 'averageCouplingModularity';
    const MetricDynamicCouplingModularity = 'dynamicCouplingModularity';
    const MetricSemanticCouplingModularity = 'semanticCouplingModularity';
    const MetricEvolutionaryCouplingModularity = 'evolutionaryCouplingModularity';
    const MetricTotalModularizationQuality = 'totalModularizationQuality';
    const MetricAverageModularizationQuality = 'averageModularizationQuality';
    const MetricDynamicModularizationQuality = 'dynamicModularizationQuality';
    const MetricSemanticModularizationQuality = 'semanticModularizationQuality';
    const MetricEvolutionaryModularizationQuality = 'evolutionaryModularizationQuality';
    const GraphClusteringMetrics = [MetricTotalCouplingModularity, MetricAverageCouplingModularity, MetricDynamicCouplingModularity, MetricSemanticCouplingModularity, MetricEvolutionaryCouplingModularity, MetricTotalModularizationQuality, MetricAverageModularizationQuality, MetricDynamicModularizationQuality, MetricSemanticModularizationQuality, MetricEvolutionaryModularizationQuality];
    const DefaultMaxClusteringIterations = 100;
    const DefaultIterationsClusteringParameterMin = 1;
    const DefaultIterationsClusteringParameterMax = 100;
    const DefaultDynamicCouplingScoreFactor = 1;
    const DefaultSemanticCouplingScoreFactor = 1;
    const DefaultEvolutionaryCouplingScoreFactor = 1;


    export default {
        name: 'app',
        components: {
            BasicSelect,
            ClusteringMetrics,
            ClusteringOverview,
            Graph,
            Slider,
            Throbber,
        },
        computed: {
            dynamicCouplingScoreFactor: {
                get: function () {
                    return this.dynamicCouplingScoreWeightAsInteger;
                },
                set: function (newValue) {
                    if (!newValue) return;
                    this.dynamicCouplingScoreWeightAsInteger = parseInt(newValue);
                },
            },
            semanticCouplingScoreFactor: {
                get: function () {
                    return this.semanticCouplingScoreWeightAsInteger;
                },
                set: function (newValue) {
                    if (!newValue) return;
                    this.semanticCouplingScoreWeightAsInteger = parseInt(newValue);
                },
            },
            evolutionaryCouplingScoreFactor: {
                get: function () {
                    return this.evolutionaryCouplingScoreWeightAsInteger;
                },
                set: function (newValue) {
                    if (!newValue) return;
                    this.evolutionaryCouplingScoreWeightAsInteger = parseInt(newValue);
                },
            },
            couplingScoreFactorSum: function () {
                return (this.dynamicCouplingScoreFactor + this.semanticCouplingScoreFactor + this.evolutionaryCouplingScoreFactor);
            },
            clusterIds: function () {
                if (!this.clusteringAvailable || !this.graphData["nodes"]) return new Set();
                return new Set(this.graphData["nodes"].map((node) => {
                    return node["attributes"]["cluster"];
                }));
            },
            dynamicAnalysisQuality: function () {
                if (!this.metricsData["inputQuality"]) return NotAvailableLabel;
                return this.metricsData["inputQuality"]["dynamicAnalysis"];
            },
            semanticAnalysisQuality: function () {
                if (!this.metricsData["inputQuality"]) return NotAvailableLabel;
                return this.metricsData["inputQuality"]["semanticAnalysis"];
            },
            evolutionaryAnalysisQuality: function () {
                if (!this.metricsData["inputQuality"]) return NotAvailableLabel;
                return this.metricsData["inputQuality"]["evolutionaryAnalysis"];
            },
            clusteringQuality: function () {
                if (!this.metricsData["clusteringQuality"]) return {};
                return this.metricsData["clusteringQuality"];
            },
            accumulatedEdgeWeights: function () {
                if (!this.clusteringQuality) return null;
                return this.clusteringQuality["accumulatedEdgeWeights"];
            },
            accumulatedInterClusterEdgeWeights: function () {
                if (!this.clusteringQuality) return null;
                return this.clusteringQuality["accumulatedInterfaceEdgeWeights"];
            },
            percentageInterClusterEdgeWeights: function () {
                if (!this.clusteringQuality) return null;
                return this.calculatePercentageRatioBetweenTwoNumbers(this.accumulatedInterClusterEdgeWeights, this.accumulatedEdgeWeights);
            },
        },
        data() {
            return {
                uploadProjectName: '',
                uploadProjectPlatform: 'java',
                uploadVcsSystem: 'git2',
                uploadBasePackageIdentifier: '',
                uploadStaticAnalysisFile: null,
                uploadDynamicAnalysisFile: null,
                uploadSemanticAnalysisFile: null,
                uploadEvolutionaryAnalysisFile: null,
                selectedProjectId: '',
                projectNamesOptions: [],
                graphData: {},
                metricsData: {},
                staticProgramAnalyisUploadLabel: 'Static Analysis',
                dynamicProgramAnalyisUploadLabel: 'Dynamic Analysis',
                semanticProgramAnalyisUploadLabel: 'Semantic Analysis',
                evolutionaryAnalyisUploadLabel: 'Evolutionary Analysis',
                graphClusteringAlgorithms: GraphClusteringAlgorithms,
                graphClusteringMetrics: GraphClusteringMetrics,
                selectedClusteringAlgorithm: ClausetNewmanMooreIdentifier,
                selectedClusteringMetric: MetricTotalCouplingModularity,
                bestScoringClusteringAlgorithm: null,
                clusteringAlgorithmMetrics: {},
                clusteringAvailable: false,
                clusteredViewEnabled: false,
                showClusterNodes: false,
                sliderFloatingPointStepEnabled: true,
                graphEnabled: true,
                overviewEnabled: false,
                metricsEnabled: true,
                dynamicCouplingScoreWeightAsInteger: DefaultDynamicCouplingScoreFactor,
                semanticCouplingScoreWeightAsInteger: DefaultSemanticCouplingScoreFactor,
                evolutionaryCouplingScoreWeightAsInteger: DefaultEvolutionaryCouplingScoreFactor,
                maxClusteringIterations: DefaultMaxClusteringIterations,
                iterationsClusteringParameterMin: DefaultIterationsClusteringParameterMin,
                iterationsClusteringParameterMax: DefaultIterationsClusteringParameterMax,
                isLoading: false,
            }
        },
        watch: {
            uploadProjectName: function (uploadProjectName) {
                this.selectedProjectId = uploadProjectName;
            },
            clusteredViewEnabled: function (clusteredViewEnabled) {
                if (!clusteredViewEnabled) {
                    this.showClusterNodes = false;
                }
            },
        },
        mounted() {
            this.fetchProjectNames();
        },
        methods: {
            uploadNewProjectData() {
                this.isLoading = true;

                if (!this.uploadProjectName) {
                    this.notifyError("No project name was chosen");
                    return;
                }

                const data = new FormData();
                data.append('projectName', this.uploadProjectName);
                data.append('projectPlatform', this.uploadProjectPlatform);
                data.append('vcsSystem', this.uploadVcsSystem);
                data.append('basePackageIdentifier', this.uploadBasePackageIdentifier);
                data.append('staticAnalysisFile', this.uploadStaticAnalysisFile);
                data.append('dynamicAnalysisFile', this.uploadDynamicAnalysisFile);
                data.append('semanticAnalysisFile', this.uploadSemanticAnalysisFile);
                data.append('evolutionaryAnalysisFile', this.uploadEvolutionaryAnalysisFile);

                axios
                    .post(`http://${this.$backendHost}/analysis/`, data)
                    .then((response) => {
                        this.graphData = response.data["graph"];
                        this.metricsData = response.data["metrics"];
                        this.fetchProjectNames();
                        this.isLoading = false;
                    })
                    .catch((error) => {
                        console.error(error.response);
                        this.isLoading = false;
                        this.notifyError(error.response.data);
                    });
            },
            fetchProjectNames() {
                axios
                    .get(`http://${this.$backendHost}/analysis/`)
                    .then((response) => {
                        this.projectNamesOptions = response.data.map((projectName) => ({
                            value: projectName,
                            text: projectName
                        }));
                    })
                    .catch((error) => {
                        console.error(error.response);
                        this.notifyError(error.response.data);
                    });
            },
            fetchAnalysis() {
                if (!this.selectedProjectId) {
                    this.notifyError("No project name was filled in");
                    return;
                }

                axios
                    .get(`http://${this.$backendHost}/analysis/${this.selectedProjectId}`)
                    .then((response) => {
                        this.clusteredViewEnabled = false;
                        this.graphData = response.data["graph"];
                        this.metricsData = response.data["metrics"];
                        this.isLoading = false;
                    })
                    .catch((error) => {
                        console.error(error.response);
                        this.isLoading = false;
                        this.notifyError(error.response.data);
                    });
            },
            fetchClusteredGraph() {
                this.isLoading = true;

                if (!this.selectedProjectId) {
                    this.notifyError("No project name was filled in");
                    return;
                }

                const parameters = {
                    'clusteringAlgorithm': this.selectedClusteringAlgorithm,
                    'clusteringMetric': this.selectedClusteringMetric,
                    'dynamicCouplingScoreWeight': this.dynamicCouplingScoreWeightAsInteger,
                    'semanticCouplingScoreWeight': this.semanticCouplingScoreWeightAsInteger,
                    'evolutionaryCouplingScoreWeight': this.evolutionaryCouplingScoreWeightAsInteger,
                    'maxClusteringIterations': this.maxClusteringIterations,
                };

                axios
                    .get(
                        `http://${this.$backendHost}/analysis/${this.selectedProjectId}/cluster`,
                        {
                            params: parameters,
                        },
                    )
                    .then((response) => {
                        this.clusteringAvailable = true;
                        this.clusteredViewEnabled = true;
                        this.showClusterNodes = true;
                        this.graphData = response.data["graph"];
                        this.metricsData = response.data["metrics"];
                        this.isLoading = false;
                        this.scrollToRefAnchor('clustering-controls');
                        this.$notify({title: `Clustering with '${this.convertClusteringAlgorithmIdentifierToLabel(this.selectedClusteringAlgorithm)}' was successful`});
                    })
                    .catch((error) => {
                        console.error(error.response);
                        this.isLoading = false;
                        this.$notify({title: `Clustering with '${this.convertClusteringAlgorithmIdentifierToLabel(this.selectedClusteringAlgorithm)}' failed`});
                    });

                this.fetchClusteredGraphOfEveryGraphClusteringAlgorithm();
            },
            fetchClusteredGraphOfEveryGraphClusteringAlgorithm() {
                const clusteringAlgorithms = this.graphClusteringAlgorithms.filter((algo) => algo !== this.selectedClusteringAlgorithm);

                this.clusteringAlgorithmMetrics = {};

                for (let clusteringAlgorithm of clusteringAlgorithms) {
                    const parameters = {
                        'clusteringAlgorithm': clusteringAlgorithm,
                        'clusteringMetric': this.selectedClusteringMetric,
                        'dynamicCouplingScoreWeight': this.dynamicCouplingScoreWeightAsInteger,
                        'semanticCouplingScoreWeight': this.semanticCouplingScoreWeightAsInteger,
                        'evolutionaryCouplingScoreWeight': this.evolutionaryCouplingScoreWeightAsInteger,
                        'maxClusteringIterations': this.maxClusteringIterations,
                    };

                    axios
                        .get(
                            `http://${this.$backendHost}/analysis/${this.selectedProjectId}/cluster`,
                            {
                                params: parameters,
                            },
                        )
                        .then((response) => {
                            this.clusteringAvailable = true;
                            this.clusteredViewEnabled = true;
                            this.clusteringAlgorithmMetrics[clusteringAlgorithm] = response.data["metrics"];
                            this.reevaluateMetrics();
                            this.$forceUpdate();
                            this.$notify({title: `Clustering with '${this.convertClusteringAlgorithmIdentifierToLabel(clusteringAlgorithm)}' was successful`});
                        })
                        .catch((error) => {
                            console.error(error.response);
                            this.isLoading = false;
                            this.$notify({title: `Clustering with '${this.convertClusteringAlgorithmIdentifierToLabel(clusteringAlgorithm)}' failed`});
                        });
                }

                this.scrollToRefAnchor('clustering-controls');
            },
            fetchOptimizedClusteringParameters() {
                this.isLoading = true;

                const parameters = {
                    'clusteringAlgorithm': this.selectedClusteringAlgorithm,
                    'clusteringMetric': this.selectedClusteringMetric,
                    'maxClusteringIterations': this.maxClusteringIterations,
                };

                axios
                    .get(
                        `http://localhost:5656/analysis/${this.selectedProjectId}/optimize`,
                        {
                            params: parameters,
                        },
                    )
                    .then((response) => {
                        this.dynamicCouplingScoreFactor = response.data["edgeAttributeWeights"]["dynamicCouplingScoreWeight"];
                        this.semanticCouplingScoreFactor = response.data["edgeAttributeWeights"]["semanticCouplingScoreWeight"];
                        this.logicalCouplingScoreFactor = response.data["edgeAttributeWeights"]["logicalCouplingScoreWeight"];
                        this.isLoading = false;
                    })
                    .catch((error) => {
                        console.error(error);
                        this.isLoading = false;
                    });
            },
            reevaluateMetrics() {
                let currentBestScore = 0;

                const clusteringAlgorithmMetrics = {...{[this.selectedClusteringAlgorithm]: this.metricsData}, ...this.clusteringAlgorithmMetrics};
                for (let clusteringAlgorithm of Object.keys(clusteringAlgorithmMetrics)) {
                    if (!clusteringAlgorithmMetrics[clusteringAlgorithm]["clusteringQuality"]) continue;
                    const score = clusteringAlgorithmMetrics[clusteringAlgorithm]["clusteringQuality"][this.selectedClusteringMetric];

                    if (score >= currentBestScore) {
                        currentBestScore = score;
                        this.bestScoringClusteringAlgorithm = clusteringAlgorithm;
                    }
                }
            },
            handleIterationsClusteringParameterChange(value) {
                this.maxClusteringIterations = parseInt(value);
            },
            convertClusteringAlgorithmIdentifierToLabel(clusteringAlgorithm) {
                switch (clusteringAlgorithm) {
                    case MclIdentifier:
                        return 'MCL';
                    case InfomapIdentifier:
                        return 'Infomap';
                    case LouvainIdentifier:
                        return 'Louvain';
                    case ClausetNewmanMooreIdentifier:
                        return 'Clauset et al.';
                    case WalktrapIdentifier:
                        return 'Walktrap';
                    case ChineseWhispersIdentifier:
                        return 'Chinese Whispers';
                    case LabelPropagationIdentifier:
                        return 'Label Propagation';
                    case WeaklyConnectedComponentsIdentifier:
                        return 'WCC';
                    default:
                        return undefined
                }
            },
            convertClusteringMetricIdentifierToLabel(clusteringMetric) {
                switch (clusteringMetric) {
                    case MetricAmountClusters:
                        return 'Amount Clusters';
                    case MetricInterClusterEdges:
                        return 'Amount Inter-Cluster Edges';
                    case MetricTotalCouplingModularity:
                        return 'Total Coupling Modularity';
                    case MetricAverageCouplingModularity:
                        return 'Average Coupling Modularity';
                    case MetricDynamicCouplingModularity:
                        return 'Dynamic Coupling Modularity';
                    case MetricSemanticCouplingModularity:
                        return 'Semantic Coupling Modularity';
                    case MetricEvolutionaryCouplingModularity:
                        return 'Evolutionary Coupling Modularity';
                    case MetricTotalModularizationQuality:
                        return 'Total Modularization Quality';
                    case MetricAverageModularizationQuality:
                        return 'Average Modularization Quality';
                    case MetricDynamicModularizationQuality:
                        return 'Dynamic Modularization Quality';
                    case MetricSemanticModularizationQuality:
                        return 'Semantic Modularization Quality';
                    case MetricEvolutionaryModularizationQuality:
                        return 'Evolutionary Modularization Quality';
                    default:
                        return undefined
                }
            },
            onStaticAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) {
                    this.uploadStaticAnalysisFile = files[0];
                    this.staticProgramAnalyisUploadLabel = this.shortenText(files[0].name, 15);
                }
            },
            onDynamicAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) {
                    this.uploadDynamicAnalysisFile = files[0];
                    this.dynamicProgramAnalyisUploadLabel = this.shortenText(files[0].name, 15);
                }
            },
            onSemanticAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) {
                    this.uploadSemanticAnalysisFile = files[0];
                    this.semanticProgramAnalyisUploadLabel = this.shortenText(files[0].name, 15);
                }
            },
            onEvolutionaryAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) {
                    this.uploadEvolutionaryAnalysisFile = files[0];
                    this.evolutionaryAnalyisUploadLabel = this.shortenText(files[0].name, 15);
                }
            },
            onProjectNameChange(projectName) {
                this.uploadProjectName = projectName.value;
            },
            notifyError(errorMessage) {
                this.$notify({title: `An error occurred: '${errorMessage}'`});
            },
            calculatePercentageRatioBetweenTwoNumbers(first, second) {
                return parseInt((first / second) * 100);
            },
            scrollToRefAnchor(refAnchorName) {
                const element = this.$refs[refAnchorName];
                window.scrollTo(0, element.offsetTop - 11);
            },
            shortenText(input, maximumLength) {
                if (input.length > maximumLength) {
                    return `${input.substring(0, maximumLength - 1)}…`;
                } else {
                    return input;
                }
            },
        },
    }
</script>

<style>
    @import '~bulma/css/bulma.min.css';
    @import '~bulma-tooltip/dist/css/bulma-tooltip.min.css';
    @import '~bulma-switch/dist/css/bulma-switch.min.css';
    @import '~vue-search-select/dist/VueSearchSelect.css';

    #app {
        width: 100%;
        text-align: center;
    }

    #throbber {
        position: fixed;
        height: 100vh;
        width: 100vw;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        z-index: 99;
        background-color: rgba(0, 0, 0, 0.55);
        padding: 0;
        margin: 0;
    }

    #graph-container {
        height: 80vh;
        padding: 0 !important;
    }

    #graph {
        height: 100%;
    }

    .section {
        padding-top: 1rem !important;
        padding-bottom: 1rem !important;
    }

    .file-cta {
        background-color: #ffffff;
    }

    .file-icon {
        color: #dbdbdb;
    }

    .edge-weighting-formula-factor {
        width: 4em;
    }

    .edge-weighting-formula-factor > input {
        text-align: center;
    }

    .range-slider-fill {
        background-color: #00d1b2;
    }

    .tooltip.is-tooltip-multiline::before {
        text-align: center !important;
    }

    .project-name-search {
        width: 15em !important;
    }

    .project-name-search .text, .project-name-search .search {
        font-size: 1.15em !important;
        padding-left: 1.4em !important;
    }

    .project-name-search .search {
        font-size: 1.15em !important;
        padding-top: 0.5em !important;
        padding-left: 2.4em !important;
    }

    .vis-button.vis-edit.vis-edit-mode {
        border: none !important;
    }

    .vis-tooltip {
        background-color: transparent !important;
        border: none !important;
        padding: 0 !important;
    }

    #metrics-container .level {
        flex-wrap: wrap;
    }

    #metrics-container .level > .level-item {
        margin: 1em;
    }
</style>
