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
                                <div class="control">
                                    <input class="input" type="text" placeholder="Project Name"
                                           v-model="uploadProjectName">
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div class="control">
                                    <input class="input" type="text" placeholder="Base Package Identifier"
                                           v-model="uploadBasePackageIdentifier">
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div class="control">
                                    <div class="select">
                                        <label>
                                            <select v-model="uploadProjectPlatform">
                                                <option value="jvm">JVM</option>
                                            </select>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div class="file has-name">
                                    <label class="file-label">
                                        <input class="file-input" type="file" name="resume"
                                               placeholder="Static Analysis Archive"
                                               @change="onStaticAnalysisUploadFileChange">
                                        <span class="file-cta">
                                    <span class="file-icon">
                                        <i class="fas fa-upload"></i>
                                    </span>
                                    <span class="file-label">
                                        Static Analysis
                                    </span>
                                </span>
                                        <!--<span class="file-name">-->
                                        <!--filename.txt-->
                                        <!--</span>-->
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div class="file has-name">
                                    <label class="file-label">
                                        <input class="file-input" type="file" name="resume"
                                               placeholder="Dynamic Analysis Archive"
                                               @change="onDynamicAnalysisUploadFileChange">
                                        <span class="file-cta">
                                    <span class="file-icon">
                                        <i class="fas fa-upload"></i>
                                    </span>
                                    <span class="file-label">
                                        Dynamic Analysis
                                    </span>
                                </span>
                                        <!--<span class="file-name">-->
                                        <!--filename.txt-->
                                        <!--</span>-->
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="level-right">
                        <div class="level-item">
                            <div class="field">
                                <div class="control">
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
                                <div class="control">
                                    <input class="input" type="text" placeholder="Project Name"
                                           v-model="selectedProjectId">
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="level-right">
                        <div class="level-item">
                            <div class="field">
                                <div class="control">
                                    <button class="button is-primary" @click="fetchAnalysis">
                                        Retrieve
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div class="control">
                                    <button class="button is-primary" @click="fetchClusteredGraph">
                                        Cluster
                                    </button>
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
                                <div class="control">
                                    <div class="select">
                                        <label>
                                            <select v-model="chosenClusteringAlgorithm" :disabled="!clusterAvailable">
                                                <option value="mcl">MCL</option>
                                                <option value="infomap">Infomap</option>
                                            </select>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div class="control">
                                    <Slider
                                            :disabled="!clusterAvailable"
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
                                <div class="control">
                                    <input
                                            class="switch"
                                            id="clusteredViewEnabled"
                                            type="checkbox"
                                            v-model="clusteredViewEnabled"
                                            :checked="clusteredViewEnabled"
                                            :disabled="!clusterAvailable"
                                    >
                                    <label for="clusteredViewEnabled">
                                        Clustered View
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="level-item">
                            <div class="field">
                                <div class="control">
                                    <input
                                            class="switch"
                                            id="showClusterNodes"
                                            type="checkbox"
                                            v-model="showClusterNodes"
                                            :checked="showClusterNodes"
                                            :disabled="!clusteredViewEnabled && !clusterAvailable"
                                    >
                                    <label for="showClusterNodes">
                                        Show Cluster Nodes &amp; Hide Inter-Cluster Edges
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
                        :is-clustered="clusteredViewEnabled"
                        :show-cluster-nodes="showClusterNodes"
                />
            </div>
        </section>
    </div>
</template>

<script>
    import Graph from './components/Graph.vue';
    import Slider from './components/Slider.vue';
    import Throbber from './components/Throbber.vue';
    import axios from 'axios';

    export default {
        name: 'app',
        components: {
            Graph,
            Slider,
            Throbber,
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
                chosenClusteringAlgorithm: 'mcl',
                clusterAvailable: false,
                clusteredViewEnabled: false,
                showClusterNodes: false,
                tunableClusteringParameter: 2.0,
                isLoading: false,
            }
        },
        watch: {
            chosenClusteringAlgorithm: function (chosenClusteringAlgorithm) {
                if (chosenClusteringAlgorithm) this.fetchClusteredGraph();
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
                        this.graphData = response.data;
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
                        this.graphData = response.data;
                        this.isLoading = false;
                    })
                    .catch((error) => {
                        console.error(error);
                        this.isLoading = false;
                    });
            },
            fetchClusteredGraph() {
                const params = {
                    'clusteringAlgorithm': this.chosenClusteringAlgorithm,
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
                        this.graphData = response.data;
                        this.isLoading = false;
                        this.scrollToAnchorRef('clustering-controls');
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
                if (files.length > 0) this.uploadStaticAnalysisArchive = files[0]
            },
            onDynamicAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) this.uploadDynamicAnalysisArchive = files[0]
            },
            scrollToAnchorRef(anchorRefName) {
                const element = this.$refs[anchorRefName];
                window.scrollTo(0, element.offsetTop - 11);
            }
        },
    }
</script>

<style>
    @import "~bulma/css/bulma.min.css";
    @import "~bulma-switch/dist/css/bulma-switch.min.css";

    #app {
        width: 100%;
        color: #2c3e50;
        text-align: center;
        padding: 1em;
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

    .vis-button.vis-edit.vis-edit-mode {
        border: none !important;
    }
</style>
