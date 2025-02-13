package com.example.utils

import org.bson.BsonObjectId
import org.bson.BsonValue
import org.bson.types.ObjectId

fun BsonValue.toStringX(): String {
    return BsonObjectId(this.asObjectId().value).value.toHexString()
}

fun stringToObjectId(string: String?): ObjectId? {
    return if(string.isNullOrEmpty() || string.length != 24 || !isHexString(string)) {
        null
    }
    else {
        ObjectId(string)
    }
}

fun isHexString(input: String): Boolean {
    val hexRegex = "^[0-9a-fA-F]+$" // Matches strings with only hex characters
    return input.matches(hexRegex.toRegex())
}