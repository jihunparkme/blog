package kafkastreams.study.sample.settlement.common

enum class PaymentType {
    ONLINE,
    OFFLINE,
}

enum class PaymentMethodType {
    CARD,
    MONEY,
    PAY,
}

enum class PaymentActionType {
    PAYMENT,
    CANCEL,
}