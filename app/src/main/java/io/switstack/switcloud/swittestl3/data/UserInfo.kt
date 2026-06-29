package io.switstack.switcloud.swittestl3.data

import io.switstack.switcloud.switcloudclt.data.OutcomeParameterSet
import io.switstack.switcloud.switcloudclt.data.UserInterfaceRequestData

sealed class UserInfo {
    data class LedLevel(val level: Int) : UserInfo()
    data class UserMessage(
        val uird: UserInterfaceRequestData?,
        val opsStatus: OutcomeParameterSet.Status? = null,
        val defaultMessageStatus: String? = null
    ) : UserInfo() {
        val status: String? = uird?.status?.let { "${it.name} (${it.value.toHexString()})" }
        val message: String = uird?.messageIdentifier?.let { "${it.name} (${it.value.toHexString()})" } ?: ""
        val ops: String? = defaultMessageStatus ?: opsStatus?.let { "${it.name} (${it.value.toHexString()})" }
    }
}
