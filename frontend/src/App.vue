<template>
    <div id="app">
        <div id="controls__container">
            <div>
                <ScaleLoader :loading="isLoading"></ScaleLoader>
            </div>
            <br>
            <div>
                <input type="text" placeholder="Project Name" v-model="uploadProjectName">
                <label>
                    <select v-model="uploadProjectPlatform">
                        <option value="jvm">JVM</option>
                    </select>
                </label>
                <input type="text" placeholder="Base Package Identifier" v-model="uploadBasePackageIdentifier">
                <input type="file" placeholder="Static Analysis Archive" @change="onStaticAnalysisUploadFileChange">
                <input type="file" placeholder="Dynamic Analysis Archive" @change="onDynamicAnalysisUploadFileChange">
                <button @click="uploadNewProjectData">Upload</button>
            </div>
            <br>
            <div>
                <input type="text" placeholder="Project Identifier" v-model="selectedProjectId">
                <button @click="fetchAnalysis">Retrieve</button>
            </div>
            <br>
            <div>
                <button @click="fetchClusteredGraph">Cluster</button>
                <br>
                <input
                        id="enable-clustering"
                        type="checkbox"
                        v-model="clusteredViewEnabled"
                >
                <label for="enable-clustering">Clustered View</label>
                <input
                        id="show-cluster-nodes"
                        type="checkbox"
                        v-model="showClusterNodes"
                        :disabled="!clusteredViewEnabled"
                >
                <label for="show-cluster-nodes">Show Cluster Nodes</label>
            </div>
        </div>
        <div>
            <Slider :value="clusteringInflationValue" @value-change="handleClusteringInflationValueChange"/>
        </div>
        <div id="graph__container">
            <Graph
                    id="graph"
                    :graph-data="graphData"
                    :is-clustered="clusteredViewEnabled"
                    :show-cluster-nodes="showClusterNodes"
            />
        </div>
    </div>
</template>

<script>
    import Graph from './components/Graph.vue';
    import Slider from './components/Slider.vue';
    import ScaleLoader from 'vue-spinner/src/ScaleLoader.vue';
    import axios from 'axios';

    export default {
        name: 'app',
        components: {
            Graph,
            Slider,
            ScaleLoader,
        },
        data() {
            return {
                isLoading: false,
                uploadProjectName: '',
                uploadProjectPlatform: 'jvm',
                uploadBasePackageIdentifier: '',
                uploadStaticAnalysisArchive: null,
                uploadDynamicAnalysisArchive: null,
                selectedProjectId: '',
                graphData: {},
                clusteredViewEnabled: false,
                showClusterNodes: false,
                clusteringInflationValue: 2.0,
            }
        },
        watch: {
            clusteredViewEnabled: function (clusteredViewEnabled) {
                if (!clusteredViewEnabled) this.showClusterNodes = false;
            },
            clusteringInflationValue: function (clusteringInflationValue) {
                if (clusteringInflationValue) this.fetchClusteredGraph(clusteringInflationValue);
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
            fetchClusteredGraph(clusteringInflationValue) {
                axios
                    .get(
                        `http://localhost:5656/analysis/${this.selectedProjectId}/cluster`,
                        {
                            params: {
                                'clusteringInflationValue': clusteringInflationValue,
                            },
                        },
                    )
                    .then((response) => {
                        this.clusteredViewEnabled = true;
                        this.graphData = response.data;
                        this.isLoading = false;
                    })
                    .catch((error) => {
                        console.error(error);
                        this.isLoading = false;
                    });
            },
            handleClusteringInflationValueChange(value) {
                this.clusteringInflationValue = parseFloat(value);
            },
            onStaticAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) this.uploadStaticAnalysisArchive = files[0]
            },
            onDynamicAnalysisUploadFileChange(e) {
                const files = e.target.files || e.dataTransfer.files;
                if (files.length > 0) this.uploadDynamicAnalysisArchive = files[0]
            },
        },
    }
</script>

<style>
    body {
        display: flex;
    }

    #app {
        width: 100%;
        font-family: 'Avenir', Helvetica, Arial, sans-serif;
        color: #2c3e50;
        text-align: center;
    }

    #controls__container {
        height: 10vh;
        margin-bottom: 5vh;
    }

    #graph__container {
        height: 80vh;
        border: 2px solid gray;
        margin: 0 1vw;
    }

    #graph {
        height: 100%;
    }

    #enable-clustering {
        margin-top: 5px;
    }
</style>
