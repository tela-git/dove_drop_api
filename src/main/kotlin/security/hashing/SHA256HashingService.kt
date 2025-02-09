package com.example.security.hashing

import com.example.domain.model.security.SaltedHash
import com.example.domain.security.HashingService
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class SHA256HashingService: HashingService {
    override fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
        val saltAsHex = Hex.encodeHexString(salt)

        val hash = DigestUtils.sha256Hex("$saltAsHex$value")
        return SaltedHash(
            salt = saltAsHex,
            hash = hash
        )
    }

    override fun verify(value: String, saltedHash: SaltedHash): Boolean {
        val salt = DigestUtils.sha256Hex("${saltedHash.salt}$value")
        return salt == saltedHash.hash
    }
}