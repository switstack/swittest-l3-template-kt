package io.switstack.switcloud.swittestl3.data

enum class StatusEnum(val code: UInt) {
    Ok(0x0u),
    ErrFormat(0x10u),
    ErrRuntime(0x20u),
    ErrInternal(UInt.MAX_VALUE)
}