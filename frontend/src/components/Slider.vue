<template>
    <div class="level">
        <range-slider
                class="level-item slider"
                :min="min"
                :max="max"
                :step="step"
                v-model="boundValue"
                :disabled="disabled"
                @change="$emit('value-change', boundValue)"
        >
        </range-slider>
        <p class="level-item" v-show="!disabled">{{ boundValue }}</p>
    </div>
</template>

<script>
    import RangeSlider from 'vue-range-slider';


    export default {
        components: {
            RangeSlider,
        },
        data() {
            return {
                boundValue: this.value,
            }
        },
        props: {
            disabled: Boolean,
            value: Number,
            min: Number,
            max: Number,
            step: Number,
        },
    }
</script>

<style scoped>
    @import '~vue-range-slider/dist/vue-range-slider.css';

    .slider {
        width: 255px;
    }
</style>
