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
            </div>
        </div>
        <div id="graph__container">
            <Graph id="graph" :graph-data="graphData" :is-clustered="isClustered"/>
        </div>
    </div>
</template>

<script>
    import Graph from './components/Graph.vue';
    import ScaleLoader from 'vue-spinner/src/ScaleLoader.vue';
    import axios from 'axios';

    export default {
        name: 'app',
        components: {
            ScaleLoader,
            Graph,
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
                isClustered: true,
            }
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
                        this.isClustered = false;
                        this.graphData = response.data;
                        this.isLoading = false;
                    })
                    .catch((error) => {
                        console.error(error);
                        this.isLoading = false;
                    });
            },
            fetchClusteredGraph() {
                axios
                    .get(`http://localhost:5656/analysis/${this.selectedProjectId}/cluster`)
                    .then((response) => {
                        this.isClustered = true;
                        this.graphData = response.data;
                        this.isLoading = false;
                    })
                    .catch((error) => {
                        console.error(error);
                        this.isLoading = false;
                    });
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
        display: flex;
        flex-direction: column;
        justify-content: space-evenly;
        font-family: 'Avenir', Helvetica, Arial, sans-serif;
        color: #2c3e50;
        text-align: center;
    }

    #controls__container {
        flex-basis: 13%;
        margin-bottom: 2%;
    }

    #graph__container {
        flex-basis: 85%;
        border: 2px solid gray;
    }

    #graph {
        height: 100%;
    }
</style>
