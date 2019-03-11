<template>
    <div id="app">
        <input type="text" placeholder="Project Identifier" v-model="selectedProjectId">
        <button @click="fetchGraph()">Retrieve</button>
        <Graph id="graph" :graph-data="graphData"/>
    </div>
</template>

<script>
    import Graph from './components/Graph.vue';
    import axios from 'axios';

    export default {
        name: 'app',
        components: {
            Graph,
        },
        data() {
            return {
                selectedProjectId: '',
                graphData: {},
            }
        },
        methods: {
            fetchGraph() {
                axios
                    .get(`http://localhost:5656/data/${this.selectedProjectId}`)
                    .then((response) => {
                        this.graphData = response.data;
                    })
                    .catch((error) => {
                        console.error(error);
                    });
            },
        },
    }
</script>

<style>
    html, body {
        height: 100vh;
        width: 100vw;
        overflow: hidden;
    }

    #app {
        height: 100vh;
        width: 100vw;
        font-family: 'Avenir', Helvetica, Arial, sans-serif;
        color: #2c3e50;
        text-align: center;
    }

    #graph {
        height: 100vh;
        width: 100vw;
    }
</style>
