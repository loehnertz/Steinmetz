<template>
    <div id="app">
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
                                        class="control has-icons-left tooltip"
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
                                        class="control has-icons-left tooltip is-tooltip-multiline"
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
                                        class="control has-icons-left tooltip"
                                        data-tooltip="Sets the project platform of project to be uploaded"
                                >
                                    <span class="select">
                                        <label>
                                            <select v-model="uploadProjectPlatform">
                                                <option value="jvm">JVM</option>
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
                                        class="control tooltip is-tooltip-multiline"
                                        data-tooltip="Sets the static program analysis file according to the selected platform for the upload"
                                >
                                    <div class="file">
                                        <label class="file-label">
                                            <input
                                                    class="file-input"
                                                    type="file"
                                                    placeholder="Static Analysis Archive"
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
                                        class="control tooltip is-tooltip-multiline"
                                        data-tooltip="Sets the dynamic program analysis file according to the selected platform for the upload"
                                >
                                    <div class="file">
                                        <label class="file-label">
                                            <input
                                                    class="file-input"
                                                    type="file"
                                                    placeholder="Dynamic Analysis Archive"
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
                    </div>
                    <div class="level-right">
                        <div class="level-item">
                            <div class="field">
                                <div
                                        class="control tooltip is-tooltip-multiline"
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
                                                <option :value="mclAlgorithm">MCL</option>
                                                <option :value="infomapAlgorithm">Infomap</option>
                                                <option :value="louvainAlgorithm">Louvain</option>
                                                <option :value="clausetNewmanMooreAlgorithm">Clauset-Newman-Moore</option>
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
                                            :disabled="!selectedProjectId || !clusterAvailable"
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
                                            :disabled="!selectedProjectId || (!clusteredViewEnabled && !clusterAvailable)"
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
        </section>
        <section class="section">
            <div class="container box" id="graph-container">
                <Graph
                        id="graph"
                        :graph-data="graphData"
                        :cluster-ids="clusterIds"
                        :is-clustered="clusteredViewEnabled"
                        :show-cluster-nodes="showClusterNodes"
                />
            </div>
        </section>
        <section class="section">
            <div class="container box">
                <p>Dynamic Analysis Quality: {{ dynamicAnalysisQuality }}</p>
                <p>Amount of Clusters: {{ amountOfClusters }}</p>
                <p>Amount of Inter-Cluster Edges: {{ amountOfInterClusterEdges }}</p>
            </div>
        </section>
    </div>
</template>

<script>
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
            Graph,
            Slider,
            Throbber,
        },
        computed: {
            clusterIds: function () {
                if (!this.clusterAvailable) return new Set();
                return new Set(this.graphData["nodes"].map((node) => {
                    return node["attributes"]["cluster"];
                }));
            },
            dynamicAnalysisQuality: function () {
                if (!this.metricsData["inputQuality"] || !this.metricsData["inputQuality"].hasOwnProperty('dynamicAnalysis')) {
                    return NotAvailableLabel;
                } else {
                    return `${this.metricsData["inputQuality"]["dynamicAnalysis"]}%`;
                }
            },
            amountOfClusters: function () {
                if (!this.metricsData["clusteringQuality"] || !this.metricsData["clusteringQuality"].hasOwnProperty('amountClusters')) {
                    return NotAvailableLabel;
                } else {
                    return this.metricsData["clusteringQuality"]["amountClusters"];
                }
            },
            amountOfInterClusterEdges: function () {
                if (!this.metricsData["clusteringQuality"] || !this.metricsData["clusteringQuality"].hasOwnProperty('amountInterfaceEdges')) {
                    return NotAvailableLabel;
                } else {
                    return this.metricsData["clusteringQuality"]["amountInterfaceEdges"];
                }
            },
        },
        data() {
            return {
                uploadProjectName: '',
                uploadProjectPlatform: 'jvm',
                uploadBasePackageIdentifier: '',
                uploadStaticAnalysisArchive: null,
                uploadDynamicAnalysisArchive: null,
                selectedProjectId: '',
                graphData: {},
                metricsData: {},
                staticProgramAnalyisUploadLabel: 'Static Analysis',
                dynamicProgramAnalyisUploadLabel: 'Dynamic Analysis',
                mclAlgorithm: MclIdentifier,
                infomapAlgorithm: InfomapIdentifier,
                louvainAlgorithm: LouvainIdentifier,
                clausetNewmanMooreAlgorithm: ClausetNewmanMooreIdentifier,
                selectedClusteringAlgorithm: 'mcl',
                clusterAvailable: false,
                clusteredViewEnabled: false,
                showClusterNodes: false,
                tunableClusteringParameter: 2.0,
                tunableClusteringParameterDisabled: false,
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
                    this.fetchClusteredGraph();
                }
            },
            clusteredViewEnabled: function (clusteredViewEnabled) {
                if (!clusteredViewEnabled) this.showClusterNodes = false;
            },
            tunableClusteringParameter: function (tunableClusteringParameter) {
                if (tunableClusteringParameter) this.fetchClusteredGraph();
            },
        },
        methods: {
            uploadNewProjectData() {
                this.isLoading = true;

                const data = new FormData();

                data.append('projectName', this.uploadProjectName);
                data.append('projectPlatform', this.uploadProjectPlatform);
                data.append('basePackageIdentifier', this.uploadBasePackageIdentifier);
                data.append('staticAnalysisArchive', this.uploadStaticAnalysisArchive);
                data.append('dynamicAnalysisArchive', this.uploadDynamicAnalysisArchive);

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
                        this.isLoading = false;
                    });
            },
            fetchClusteredGraph() {
                const params = {
                    'clusteringAlgorithm': this.selectedClusteringAlgorithm,
                    'tunableClusteringParameter': this.tunableClusteringParameter,
                };

                axios
                    .get(
                        `http://localhost:5656/analysis/${this.selectedProjectId}/cluster`,
                        {
                            params: params,
                        },
                    )
                    .then((response) => {
                        this.clusterAvailable = true;
                        this.clusteredViewEnabled = true;
                        this.graphData = response.data["graph"];
                        this.metricsData = response.data["metrics"];
                        this.isLoading = false;
                        this.scrollToRefAnchor('clustering-controls');
                    })
                    .catch((error) => {
                        console.error(error);
                        this.isLoading = false;
                    });
            },
            handleTunableClusteringParameterChange(value) {
                this.tunableClusteringParameter = parseFloat(value);
            },
            onStaticAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) {
                    this.uploadStaticAnalysisArchive = files[0];
                    this.staticProgramAnalyisUploadLabel = this.shortenText(files[0].name, 15);
                }
            },
            onDynamicAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) {
                    this.uploadDynamicAnalysisArchive = files[0];
                    this.dynamicProgramAnalyisUploadLabel = this.shortenText(files[0].name, 15);
                }
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
