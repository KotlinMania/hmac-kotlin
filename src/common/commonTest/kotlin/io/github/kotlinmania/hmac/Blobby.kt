// port-lint: tests tests/mod.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.Mac
import io.github.kotlinmania.digest.dev.macTest
import io.github.kotlinmania.digest.dev.resettableMacTest
import kotlin.test.assertNull

internal data class TestVector(
    val key: ByteArray,
    val input: ByteArray,
    val tag: ByteArray,
)

internal class BlobbyReader(
    private val data: ByteArray,
) {
    private var pos: Int = 0

    private fun readVarint(): Int {
        val b = data[pos++].toInt() and 0xFF
        return if (b < 128) {
            b
        } else {
            val b2 = data[pos++].toInt() and 0xFF
            (b - 127) * 128 + b2
        }
    }

    private fun readElem(shared: List<ByteArray>): ByteArray {
        val tagOrLen = readVarint()
        return if ((tagOrLen and 1) == 1) {
            shared[tagOrLen shr 1]
        } else {
            val len = tagOrLen shr 1
            val slice = data.copyOfRange(pos, pos + len)
            pos += len
            slice
        }
    }

    fun parse(): List<TestVector> {
        val numShared = readVarint()
        val shared = mutableListOf<ByteArray>()
        for (i in 0 until numShared) {
            val len = readVarint()
            shared.add(data.copyOfRange(pos, pos + len))
            pos += len
        }

        val rows = mutableListOf<TestVector>()
        while (pos < data.size) {
            val key = readElem(shared)
            val input = readElem(shared)
            val tag = readElem(shared)
            rows.add(TestVector(key, input, tag))
        }
        return rows
    }
}

internal fun <M : Mac> runMacSuite(
    blobData: ByteArray,
    create: (ByteArray) -> M,
    truncSide: String = "",
) {
    val vectors = BlobbyReader(blobData).parse()
    for ((index, vector) in vectors.withIndex()) {
        val macErr = macTest(vector.key, vector.input, vector.tag, { create(it) }, truncSide)
        assertNull(macErr, "macTest failed at vector index $index ($macErr)")

        val resetErr = resettableMacTest(vector.key, vector.input, vector.tag, { create(it) }, truncSide)
        assertNull(resetErr, "resettableMacTest failed at vector index $index ($resetErr)")
    }
}
