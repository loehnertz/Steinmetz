<template>
    <div class="level">
        <range-slider
                class="level-item slider"
                min="1"
                max="10"
                step="0.1"
                v-model="boundValue"
                :disabled="disabled"
                @change="$emit('value-change', boundValue)"
        >
        </range-slider>
        <p class="level-item">{{ boundValue }}</p>
    </div>
</template>

<script>
    import RangeSlider from 'vue-range-slider';

    export default {
        components: {
            RangeSlider,
        },
        computed: {
            boundValue: {
                get: function () {
                    return this.roundedBoundValue;
                },
                set: function (newBoundValue) {
                    this.roundedBoundValue = parseFloat(newBoundValue).toFixed(1);
                }
            },
        },
        data() {
            return {
                roundedBoundValue: this.value,
            }
        },
        props: {
            disabled: Boolean,
            value: Number,
        },
    }
</script>

<style scoped>
    @import "~vue-range-slider/dist/vue-range-slider.css";

    .slider {
        width: 255px;
    }
</style>
