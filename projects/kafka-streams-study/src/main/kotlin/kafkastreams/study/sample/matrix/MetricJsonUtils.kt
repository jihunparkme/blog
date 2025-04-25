package kafkastreams.study.sample.matrix

import com.google.gson.JsonParser

object MetricJsonUtils {
    /**
     * 전체 CPU 사용량 퍼센티지
     * system > cpu > total > norm > pct
     */
    fun getTotalCpuPercent(value: String?): Double {
        return JsonParser.parseString(value).getAsJsonObject().get("system").getAsJsonObject().get("cpu")
            .getAsJsonObject().get("total").getAsJsonObject().get("norm").getAsJsonObject().get("pct").getAsDouble()
    }

    /**
     * 메트릭 종류 추출
     * metricset > name
     */
    fun getMetricName(value: String?): String {
        return JsonParser.parseString(value).getAsJsonObject().get("metricset").getAsJsonObject().get("name")
            .getAsString()
    }

    /**
     * 호스트 이름과 timestamp 값 조합
     * hostname: host > name
     * timestamp : @timestamp
     */
    fun getHostTimestamp(value: String?): String {
        val objectValue = JsonParser.parseString(value).getAsJsonObject()
        val result = objectValue.getAsJsonObject("host")
        result.add("timestamp", objectValue.get("@timestamp"))
        return result.toString()
    }
}