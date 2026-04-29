package io.switstack.switcloud.swittestl3.common

fun String.sanitize(): String {
    return this
        .replace('\u00A0', ' ') // Convert nbsp to normal space
        .replace("\n", "") // Remove line returns
        .replace("\r", "") // Remove carriage returns
        .trim() // Remove leading/trailing
}