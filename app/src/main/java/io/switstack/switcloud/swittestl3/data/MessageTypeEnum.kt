package io.switstack.switcloud.swittestl3.data

enum class MessageTypeEnum(val code: Int) {
    ALERT(0),
    REQUEST_GET_POI_ID(1001),
    RESPONSE_GET_POI_ID(2001),
    REQUEST_SET_CREDENTIALS(1002),
    RESPONSE_SET_CREDENTIALS(2002),
    REQUEST_START_PAYMENT(1003),
    RESPONSE_START_PAYMENT(2003)
}