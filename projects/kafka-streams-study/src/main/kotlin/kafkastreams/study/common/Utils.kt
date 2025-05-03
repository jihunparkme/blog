package kafkastreams.study.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import kotlin.random.Random

fun <A : Any> A.logger(): Lazy<Logger> = lazy { getLogger(this.javaClass) }

inline fun <reified T : Enum<T>> randomEnum(): T {
    val enumValues = enumValues<T>()
    return enumValues[Random.nextInt(enumValues.size)]
}