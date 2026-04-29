package io.switstack.switcloud.swittestl3.common

fun String?.nullIfBlank() = if (this.isNullOrBlank()) null else this