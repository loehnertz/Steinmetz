<template>
    <div id="throbber-container">
        <ScaleLoader :loading="isLoading"/>
    </div>
</template>

<script>
    import ScaleLoader from 'vue-spinner/src/ScaleLoader.vue';

    export default {
        components: {
            ScaleLoader,
        },
        props: {
            isLoading: Boolean,
        },
    }
</script>

<style scoped>
    #throbber-container {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        transform: scale(1.55);
    }
</style>
