package io.switstack.switcloud.swittestl3.data

// class to raise a reset in a sharedflow collector
data class ResetEvent(val timeStamp: Long = System.currentTimeMillis())
