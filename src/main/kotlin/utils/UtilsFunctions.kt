package com.example.utils

import org.bson.BsonObjectId
import org.bson.BsonValue

fun BsonValue.toStringX(): String {
    return BsonObjectId(this.asObjectId().value).value.toHexString()
}