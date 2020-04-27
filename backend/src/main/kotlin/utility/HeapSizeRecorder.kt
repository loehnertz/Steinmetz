package utility

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration


/**
 * Records datapoints regarding the current size of the allocated heap.
 * The recording runs in an asynchronous coroutine which is automatically canceled,
 * once the recording is stopped.
 *
 * @param tickRate The duration between two datapoints.
 */
class HeapSizeRecorder(private val tickRate: Duration) {
    private lateinit var job: Job
    private val datapoints: ArrayList<Int> = arrayListOf()

    fun start() {
        job = GlobalScope.launch {
            while (true) recordDatapoint()
        }.apply { start() }
    }

    fun stopAndRetrieveMax(): Int {
        job.cancel()
        return datapoints.max() ?: throw IllegalStateException("No datapoint was recorded so far")
    }

    private suspend fun recordDatapoint() = datapoints.add(Utilities.heapSizeInMb()).also { delay(tickRate.toMillis()) }
}
