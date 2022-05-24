package foundation.e.apps.manager.download.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

class DownloadProgressFlow : Flow<DownloadProgress> {
    override suspend fun collect(collector: FlowCollector<DownloadProgress>) {

    }
}