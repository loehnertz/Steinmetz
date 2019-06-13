<template>
    <div id="app">
        <div id="throbber" v-show="isLoading && !liveRerenderModeActive">
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
                                                    placeholder="Logical Coupling Analysis File"
                                                    @change="onLogicalAnalysisUploadFileChange"
                                            >
                                            <span class="file-cta">
                                                <span class="file-icon">
                                                    <i class="fas fa-upload"></i>
                                                </span>
                                                <span class="file-label">
                                                    {{ logicalAnalyisUploadLabel }}
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
                                    <input
                                            class="input"
                                            type="text"
                                            placeholder="Project Name"
                                            v-model="selectedProjectId"
                                    >
                                    <span class="icon is-small is-left">
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
                                                <option :value="mclAlgorithm">{{ convertClusteringAlgorithmIdentifierToLabel(mclAlgorithm) }}</option>
                                                <option :value="infomapAlgorithm">{{ convertClusteringAlgorithmIdentifierToLabel(infomapAlgorithm) }}</option>
                                                <option :value="louvainAlgorithm">{{ convertClusteringAlgorithmIdentifierToLabel(louvainAlgorithm) }}</option>
                                                <option :value="clausetNewmanMooreAlgorithm">{{ convertClusteringAlgorithmIdentifierToLabel(clausetNewmanMooreAlgorithm) }}</option>
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
                                        data-tooltip="Sets the tunable parameter of the selected graph clustering algorithm"
                                >
                                    <Slider
                                            :disabled="!selectedProjectId || tunableClusteringParameterDisabled"
                                            :value="tunableClusteringParameter"
                                            @value-change="handleTunableClusteringParameterChange"
                                    />
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
                    <div class="level-right">
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
                                               v-model="logicalCouplingScoreFactor">
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <p>&times;</p>
                        </div>
                        <div class="level-item">
                            <p>Logical Coupling Score</p>
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
                </div>
            </div>
        </section>
        <section class="section">
            <div class="container box" id="graph-container">
                <Graph
                        id="graph"
                        :graph-data="graphData"
                        :cluster-ids="clusterIds"
                        :is-clustered="clusteredViewEnabled"
                        :show-cluster-nodes="showClusterNodes"
                        :live-rerender-mode-active="liveRerenderModeActive"
                />
            </div>
        </section>
        <section class="section" v-if="clusteringAvailable">
            <div class="container">
                <div class="box">
                    <h1 class="title">Metrics</h1>
                    <p>Dynamic Analysis Quality: {{ dynamicAnalysisQuality }}%</p>
                    <p>Semantic Analysis Quality: {{ semanticAnalysisQuality }}%</p>
                    <p>Logical Analysis Quality: {{ logicalAnalysisQuality }}%</p>
                    <br>
                    <p>Accumulated Edge Weights: {{ accumulatedEdgeWeights }}</p>
                </div>
                <ClusteringMetrics
                        :clustering-algorithm="convertClusteringAlgorithmIdentifierToLabel(selectedClusteringAlgorithm)"
                        :amount-of-clusters="metricsData['clusteringQuality']['amountClusters']"
                        :amount-of-inter-cluster-edges="metricsData['clusteringQuality']['amountInterfaceEdges']"
                        :accumulated-inter-cluster-edge-weights="accumulatedInterClusterEdgeWeights"
                        :percentage-inter-cluster-edge-weights="percentageInterClusterEdgeWeights"
                        :graph-modularity="metricsData['clusteringQuality']['graphModularity']"
                        :dynamic-coupling-modularity="metricsData['clusteringQuality']['dynamicCouplingModularity']"
                        :semantic-coupling-modularity="metricsData['clusteringQuality']['semanticCouplingModularity']"
                        :logical-coupling-modularity="metricsData['clusteringQuality']['logicalCouplingModularity']"
                        :total-coupling-modularity="metricsData['clusteringQuality']['totalCouplingModularity']"
                />
                <div class="box level">
                    <div
                            class="level-item"
                            v-for="(metrics, clusteringAlgorithm) in clusteringAlgorithmMetrics"
                            :key="clusteringAlgorithm"
                    >
                        <ClusteringMetrics
                                :clustering-algorithm="convertClusteringAlgorithmIdentifierToLabel(clusteringAlgorithm)"
                                :amount-of-clusters="metrics['clusteringQuality']['amountClusters']"
                                :amount-of-inter-cluster-edges="metrics['clusteringQuality']['amountInterfaceEdges']"
                                :accumulated-inter-cluster-edge-weights="metrics['clusteringQuality']['accumulatedInterfaceEdgeWeights']"
                                :percentage-inter-cluster-edge-weights="calculatePercentageRatioBetweenTwoNumbers(metrics['clusteringQuality']['accumulatedInterfaceEdgeWeights'], accumulatedEdgeWeights)"
                                :graph-modularity="metrics['clusteringQuality']['graphModularity']"
                                :dynamic-coupling-modularity="metrics['clusteringQuality']['dynamicCouplingModularity']"
                                :semantic-coupling-modularity="metrics['clusteringQuality']['semanticCouplingModularity']"
                                :logical-coupling-modularity="metrics['clusteringQuality']['logicalCouplingModularity']"
                                :total-coupling-modularity="metrics['clusteringQuality']['totalCouplingModularity']"
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
    import Slider from './components/Slider.vue';
    import Throbber from './components/Throbber.vue';

    import axios from 'axios';

    const MclIdentifier = 'mcl';
    const InfomapIdentifier = 'infomap';
    const LouvainIdentifier = 'louvain';
    const ClausetNewmanMooreIdentifier = 'clauset_newman_moore';
    const NotAvailableLabel = 'N/A';

    export default {
        name: 'app',
        components: {
            ClusteringMetrics,
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
            logicalCouplingScoreFactor: {
                get: function () {
                    return this.logicalCouplingScoreWeightAsInteger;
                },
                set: function (newValue) {
                    if (!newValue) return;
                    this.logicalCouplingScoreWeightAsInteger = parseInt(newValue);
                },
            },
            couplingScoreFactorSum: function () {
                return (this.dynamicCouplingScoreFactor + this.semanticCouplingScoreFactor + this.logicalCouplingScoreFactor);
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
            logicalAnalysisQuality: function () {
                if (!this.metricsData["inputQuality"]) return NotAvailableLabel;
                return this.metricsData["inputQuality"]["logicalAnalysis"];
            },
            accumulatedEdgeWeights: function () {
                if (!this.metricsData["clusteringQuality"]) return NotAvailableLabel;
                return this.metricsData["clusteringQuality"]["accumulatedEdgeWeights"];
            },
            accumulatedInterClusterEdgeWeights: function () {
                if (!this.metricsData["clusteringQuality"]) return null;
                return this.metricsData["clusteringQuality"]["accumulatedInterfaceEdgeWeights"];
            },
            percentageInterClusterEdgeWeights: function () {
                if (!this.metricsData["clusteringQuality"]) return null;
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
                uploadLogicalAnalysisFile: null,
                selectedProjectId: '',
                graphData: {},
                metricsData: {},
                staticProgramAnalyisUploadLabel: 'Static Analysis',
                dynamicProgramAnalyisUploadLabel: 'Dynamic Analysis',
                semanticProgramAnalyisUploadLabel: 'Semantic Analysis',
                logicalAnalyisUploadLabel: 'Logical Analysis',
                mclAlgorithm: MclIdentifier,
                infomapAlgorithm: InfomapIdentifier,
                louvainAlgorithm: LouvainIdentifier,
                clausetNewmanMooreAlgorithm: ClausetNewmanMooreIdentifier,
                selectedClusteringAlgorithm: MclIdentifier,
                clusteringAlgorithmMetrics: {},
                clusteringAvailable: false,
                clusteredViewEnabled: false,
                showClusterNodes: false,
                dynamicCouplingScoreWeightAsInteger: 1,
                semanticCouplingScoreWeightAsInteger: 1,
                logicalCouplingScoreWeightAsInteger: 1,
                tunableClusteringParameter: 2.0,
                tunableClusteringParameterDisabled: false,
                liveRerenderModeActive: false,
                isLoading: false,
            }
        },
        watch: {
            uploadProjectName: function (uploadProjectName) {
                this.selectedProjectId = uploadProjectName;
            },
            selectedClusteringAlgorithm: function (selectedClusteringAlgorithm) {
                if (selectedClusteringAlgorithm) {
                    this.tunableClusteringParameterDisabled = selectedClusteringAlgorithm === LouvainIdentifier || selectedClusteringAlgorithm === ClausetNewmanMooreIdentifier;
                    this.liveRerenderModeActive = false;
                    this.fetchClusteredGraph();
                }
            },
            clusteredViewEnabled: function (clusteredViewEnabled) {
                if (!clusteredViewEnabled) {
                    this.liveRerenderModeActive = false;
                    this.showClusterNodes = false;
                }
            },
            dynamicCouplingScoreWeightAsInteger: function (dynamicCouplingScoreWeightAsInteger) {
                if (dynamicCouplingScoreWeightAsInteger) {
                    this.liveRerenderModeActive = true;
                    this.fetchClusteredGraph();
                }
            },
            semanticCouplingScoreWeightAsInteger: function (semanticCouplingScoreWeightAsInteger) {
                if (semanticCouplingScoreWeightAsInteger) {
                    this.liveRerenderModeActive = true;
                    this.fetchClusteredGraph();
                }
            },
            logicalCouplingScoreWeightAsInteger: function (logicalCouplingScoreWeightAsInteger) {
                if (logicalCouplingScoreWeightAsInteger) {
                    this.liveRerenderModeActive = true;
                    this.fetchClusteredGraph();
                }
            },
            tunableClusteringParameter: function (tunableClusteringParameter) {
                if (tunableClusteringParameter) {
                    this.liveRerenderModeActive = true;
                    this.fetchClusteredGraph();
                }
            },
        },
        methods: {
            uploadNewProjectData() {
                this.isLoading = true;

                const data = new FormData();

                data.append('projectName', this.uploadProjectName);
                data.append('projectPlatform', this.uploadProjectPlatform);
                data.append('vcsSystem', this.uploadVcsSystem);
                data.append('basePackageIdentifier', this.uploadBasePackageIdentifier);
                data.append('staticAnalysisFile', this.uploadStaticAnalysisFile);
                data.append('dynamicAnalysisFile', this.uploadDynamicAnalysisFile);
                data.append('semanticAnalysisFile', this.uploadSemanticAnalysisFile);
                data.append('logicalAnalysisFile', this.uploadLogicalAnalysisFile);

                axios
                    .post(`http://localhost:5656/analysis/`, data)
                    .then((response) => {
                        this.graphData = response.data["graph"];
                        this.metricsData = response.data["metrics"];
                        this.isLoading = false;
                    })
                    .catch((error) => {
                        console.error(error);
                        this.isLoading = false;
                    });
            },
            fetchAnalysis() {
                axios
                    .get(`http://localhost:5656/analysis/${this.selectedProjectId}`)
                    .then((response) => {
                        this.clusteredViewEnabled = false;
                        this.graphData = response.data["graph"];
                        this.metricsData = response.data["metrics"];
                        this.isLoading = false;
                    })
                    .catch((error) => {
                        console.error(error);
                        this.liveRerenderModeActive = false;
                        this.isLoading = false;
                    });
            },
            fetchClusteredGraph() {
                this.isLoading = true;

                const parameters = {
                    'clusteringAlgorithm': this.selectedClusteringAlgorithm,
                    'dynamicCouplingScoreWeight': this.dynamicCouplingScoreWeightAsInteger,
                    'semanticCouplingScoreWeight': this.semanticCouplingScoreWeightAsInteger,
                    'logicalCouplingScoreWeight': this.logicalCouplingScoreWeightAsInteger,
                    'tunableClusteringParameter': this.tunableClusteringParameter,
                };

                axios
                    .get(
                        `http://localhost:5656/analysis/${this.selectedProjectId}/cluster`,
                        {
                            params: parameters,
                        },
                    )
                    .then((response) => {
                        this.clusteringAvailable = true;
                        this.clusteredViewEnabled = true;
                        this.graphData = response.data["graph"];
                        this.metricsData = response.data["metrics"];
                        this.isLoading = false;
                        this.scrollToRefAnchor('clustering-controls');
                    })
                    .catch((error) => {
                        console.error(error);
                        this.liveRerenderModeActive = false;
                        this.isLoading = false;
                    });

                this.fetchClusteredGraphOfEveryGraphClusteringAlgorithm();
            },
            fetchClusteredGraphOfEveryGraphClusteringAlgorithm() {
                const clusteringAlgorithms = [MclIdentifier, InfomapIdentifier, LouvainIdentifier, ClausetNewmanMooreIdentifier].filter((algo) => algo !== this.selectedClusteringAlgorithm);

                this.clusteringAlgorithmMetrics = {};

                for (let clusteringAlgorithm of clusteringAlgorithms) {
                    const parameters = {
                        'clusteringAlgorithm': clusteringAlgorithm,
                        'dynamicCouplingScoreWeight': this.dynamicCouplingScoreWeightAsInteger,
                        'semanticCouplingScoreWeight': this.semanticCouplingScoreWeightAsInteger,
                        'logicalCouplingScoreWeight': this.logicalCouplingScoreWeightAsInteger,
                        'tunableClusteringParameter': this.tunableClusteringParameter,
                    };

                    axios
                        .get(
                            `http://localhost:5656/analysis/${this.selectedProjectId}/cluster`,
                            {
                                params: parameters,
                            },
                        )
                        .then((response) => {
                            this.clusteringAvailable = true;
                            this.clusteredViewEnabled = true;
                            this.clusteringAlgorithmMetrics[clusteringAlgorithm] = response.data["metrics"];
                            this.$forceUpdate();
                        })
                        .catch((error) => {
                            console.error(error);
                            this.liveRerenderModeActive = false;
                            this.isLoading = false;
                        });
                }

                this.scrollToRefAnchor('clustering-controls');
            },
            handleTunableClusteringParameterChange(value) {
                this.tunableClusteringParameter = parseFloat(value);
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
                        return 'Clauset-Newman-Moore';
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
            onLogicalAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) {
                    this.uploadLogicalAnalysisFile = files[0];
                    this.logicalAnalyisUploadLabel = this.shortenText(files[0].name, 15);
                }
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

    .vis-button.vis-edit.vis-edit-mode {
        border: none !important;
    }

    .vis-tooltip {
        background-color: transparent !important;
        border: none !important;
        padding: 0 !important;
    }
</style>
