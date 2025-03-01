/*
 * This file is part of kython.
 *
 * kython is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kython is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with kython.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package green.sailor.kython.kyc

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path

/**
 * Object that supports decoding kyc files.
 */
open class UnKyc(private val buf: ByteBuffer) {
    companion object {
        /**
         * Parses a kyc file from a Path.
         */
        fun parseKycFile(path: Path): KycFile {
            val bytes = Files.readAllBytes(path)
            val buf = ByteBuffer.wrap(bytes)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            return parseKycFile(buf)
        }

        /**
         * Parses a kyc file from a ByteBuffer.
         */
        fun parseKycFile(data: ByteBuffer): KycFile {
            val magicNumber = String(listOf(data.get(), data.get(), data.get()).toByteArray())
            check(magicNumber == "KYC") { "Magic number is not KYC" }
            val kycVer = data.get().toChar()
            check(kycVer == 'A') { "KYC is not version 1" }

            val pyVer = data.get()

            val marshaller = UnKyc(data)
            return marshaller.readObject() as KycFile
        }
    }

    /**
     * Reads an object from the stream.
     */
    fun readObject(): BaseKycType {
        val byte = buf.get()
        val typeChar = byte.toChar()
        val type = KycType.get(typeChar)

        val result = when (type) {
            // simple types...
            KycType.FALSE -> KycBoolean.FALSE
            KycType.TRUE -> KycBoolean.TRUE
            KycType.NONE -> KycNone
            // MarshalType.ELLIPSIS -> MarshalEllipsis
            // MarshalType.NULL -> MarshalNull

            // string types
            KycType.UNICODE_STRING -> readString()

            // byte types
            KycType.BYTESTRING -> readByteString()

            // number types
            KycType.INT -> readInt()
            KycType.LONG -> readLong()
            KycType.FLOAT -> readFloat()

            // container types
            KycType.TUPLE -> readTuple()
            KycType.LIST -> readList()
            KycType.DICT -> readDict()

            // code type
            KycType.CODE -> readCode()

            KycType.KY_FILE -> readKycFile()

            else -> error("Unknown kyc type: $type")
        }

        return result
    }

    /**
     * Reads an int from the stream.
     */
    fun readInt(): KycInt = KycInt(buf.int)

    /**
     * Reads a long from the stream.
     */
    fun readLong(): KycLong = KycLong(buf.long)

    /**
     * Reads a float from the stream.
     */
    fun readFloat(): KycFloat = KycFloat(buf.double)

    /**
     * Reads a string from the stream.
     *
     * @param short: If this is a short string (TYPE_SHORT_ASCII).
     */
    fun readString(): KycUnicodeString {
        val size = buf.int

        val ca = ByteArray(size)
        for (x in 0 until size) {
            val b = buf.get()
            ca[x] = b
        }

        return KycUnicodeString(ca.toString(Charsets.UTF_8))
    }

    /**
     * Reads a byte string from the stream.
     */
    fun readByteString(): KycString {
        val size = buf.int

        val ca = ByteArray(size)
        for (x in 0 until size) {
            val b = buf.get()
            ca[x] = b
        }

        return KycString(ca)
    }

    /**
     * Gets a sized container (tuple, list, set, etc) from the stream.
     *
     * @param small: If this is a "small" container (TYPE_SMALL_TUPLE).
     */
    fun getSizedContainer(small: Boolean = false): List<BaseKycType> {
        val size = buf.int
        val arr = arrayOfNulls<BaseKycType>(size)

        // loop over and read a new object off
        for (i in 0 until size) {
            arr[i] = readObject()
        }

        // it shouldn't be a problem, but just in case...
        val filtered = arr.filterNotNull()
        check(filtered.size == size) {
            "Encoded container didn't have $size elements but ${filtered.size}"
        }
        return filtered
    }

    /**
     * Reads a tuple from the stream.
     */
    fun readTuple(): KycTuple = KycTuple(getSizedContainer())

    /**
     * Reads a list from the stream.
     */
    fun readList(): KycList = KycList(getSizedContainer().toList()) // inefficient, but, oh well.

    /**
     * Reads a dict from the stream.
     */
    fun readDict(): KycDict {
        val map = hashMapOf<BaseKycType, BaseKycType>()
        val size = buf.int
        for (x in 0 until size) {
            map[this.readObject()] = this.readObject()
        }
        return KycDict(map)
    }

    // the fun one...
    /**
     * Reads a code object from the stream.
     */
    @Suppress("LocalVariableName")
    fun readCode(): KycCodeObject {
        // simple int values
        val co_argcount = readObject() as KycInt
        // TODO: co_posonlyargcount
        val co_posonlyargcount = readObject() as KycInt
        val co_kwonlyargcount = readObject() as KycInt
        val co_nlocals = readObject() as KycInt
        val co_stacksize = readObject() as KycInt
        val co_flags = readObject() as KycInt

        // more complex values
        val co_code = readObject() as KycString
        val co_consts = readObject().ensureTuple()
        val co_names = readObject().ensureTuple()
        val co_varnames = readObject().ensureTuple()
        val co_freevars = readObject().ensureTuple()
        val co_cellvars = readObject().ensureTuple()
        val co_filename = readObject() as KycUnicodeString
        val co_name = readObject() as KycUnicodeString
        val co_firstlineno = readObject() as KycInt
        val lnotab = readObject() as KycString

        return KycCodeObject(
            co_argcount, co_posonlyargcount, co_kwonlyargcount, co_nlocals, co_stacksize, co_flags,

            co_code, co_consts, co_names,
            co_varnames, co_freevars, co_cellvars,

            co_filename, co_name,
            co_firstlineno, lnotab
        )
    }

    /**
     * Reads the root Kyc object.
     */
    private fun readKycFile(): KycFile {
        val hash = (readObject() as KycLong).wrapped
        val comment = readObject() as KycUnicodeString
        val codeObj = readObject() as KycCodeObject
        return KycFile(pyHash = hash, comment = comment, code = codeObj)
    }
}
