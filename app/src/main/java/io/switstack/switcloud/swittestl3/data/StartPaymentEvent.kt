package io.switstack.switcloud.swittestl3.data

// class to raise a start payment event in a sharedflow collector
data class StartPaymentEvent(val timeStamp: Long = System.currentTimeMillis())
