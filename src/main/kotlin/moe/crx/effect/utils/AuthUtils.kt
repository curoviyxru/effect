package moe.crx.effect.utils

import io.ktor.util.encodeBase64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

fun hashPassword(username: String, password: String): String {
    val combinedSalt = "effect_salt_CpCdBuhPJgceWZT7QR!XghnI6@UU8CidBJ6czeH4j^zvg5W^Qsz2P!0P@*qEkEppFKTL5TR2&^ei3#T1QU94RDGF^mIndJkouOJ78U7fJM*6?4g1G8yiP09@I@u81taj_$username".toByteArray()
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
    val spec = PBEKeySpec(password.toCharArray(), combinedSalt, 210_000, 256)
    val key = factory.generateSecret(spec)

    return key.encoded.encodeBase64()
}

fun generateToken(userId: Long): String {
    var secureRandom = SecureRandom()
    val bytes = ByteArray(60)
    secureRandom.nextBytes(bytes)

    return "effect:$userId:${bytes.encodeBase64()}"
}