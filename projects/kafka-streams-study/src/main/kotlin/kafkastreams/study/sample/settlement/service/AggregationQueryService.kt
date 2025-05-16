package kafkastreams.study.sample.settlement.service

import kafkastreams.study.common.logger
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregateValue
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregationKey
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.stereotype.Service

@Service
class AggregationQueryService(
    private val kafkaStreams: KafkaStreams
) {
    companion object {
        private val log by logger()
        const val STATISTICS_STORE_NAME = "statistics-store"
    }

    fun getAggregatedValue(key: BaseAggregationKey): BaseAggregateValue? {
        return try {
            val store: ReadOnlyKeyValueStore<BaseAggregationKey, BaseAggregateValue> =
                kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(
                        STATISTICS_STORE_NAME,
                        QueryableStoreTypes.keyValueStore()
                    )
                )
            store.get(key)
        } catch (e: InvalidStateStoreException) {
            // Kafka Streams 애플리케이션이 아직 실행 중이지 않거나, 해당 인스턴스에 상태 저장소가 없거나, 리밸런싱 중일 수 있음
            println("Error querying state store '$STATISTICS_STORE_NAME': ${e.message}")
            null
        } catch (e: Exception) {
            println("An unexpected error occurred while querying store '$STATISTICS_STORE_NAME': ${e.message}")
            null
        }
    }

    fun getAllAggregatedValues(): List<AggregatedDataEntry> {
        val results = mutableListOf<AggregatedDataEntry>()
        try {
            val store: ReadOnlyKeyValueStore<BaseAggregationKey, BaseAggregateValue> =
                kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(
                        STATISTICS_STORE_NAME,
                        QueryableStoreTypes.keyValueStore()
                    )
                )
            store.all().use { iterator ->
                while (iterator.hasNext()) {
                    val keyValue = iterator.next()
                    results.add(AggregatedDataEntry(keyValue.key, keyValue.value))
                }
            }
        } catch (e: InvalidStateStoreException) {
            // Kafka Streams 애플리케이션이 아직 실행 중이지 않거나, 해당 인스턴스에 상태 저장소가 없거나, 리밸런싱 중일 수 있음
            log.error("Error querying all state from store '$STATISTICS_STORE_NAME': ${e.message}")
        } catch (e: Exception) {
            log.error("An unexpected error occurred while querying all state from store '$STATISTICS_STORE_NAME': ${e.message}")
        }
        return results
    }
}

data class AggregatedDataEntry(
    val key: BaseAggregationKey,
    val value: BaseAggregateValue
)