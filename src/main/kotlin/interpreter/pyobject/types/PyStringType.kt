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
package green.sailor.kython.interpreter.pyobject.types

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.functions.PyBuiltinFunction
import green.sailor.kython.interpreter.iface.ArgType
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyInt
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.pyobject.PyString
import green.sailor.kython.interpreter.pyobject.PyType
import green.sailor.kython.interpreter.throwKy

object PyStringType : PyType("str") {
    override fun newInstance(kwargs: Map<String, PyObject>): PyObject {
        val arg = kwargs["x"]!!
        return arg.getPyStr()
    }

    override val signature: PyCallableSignature by lazy {
        PyCallableSignature(
            "x" to ArgType.POSITIONAL
        )
    }

    /** str.upper() */
    val pyUpper = PyBuiltinFunction.wrap("upper", PyCallableSignature.EMPTY_METHOD) {
        val self = it["self"]!!.cast<PyString>()
        PyString(self.wrappedString.toUpperCase())
    }

    // there *is* special casing
    /** str.__int__() */
    val pyStrInt = PyBuiltinFunction.wrap("__int__", PyCallableSignature.EMPTY_METHOD) {
        val self = it["self"]!!.cast<PyString>()
        try {
            PyInt(self.wrappedString.toInt().toLong())
        } catch (e: NumberFormatException) {
            Exceptions.VALUE_ERROR("Cannot convert '${self.wrappedString}' to int").throwKy()
        }
    }

    override val initialDict: Map<String, PyObject> by lazy {
        mapOf(
            // magic method impls
            "__int__" to pyStrInt,

            // regular method impls
            "upper" to pyUpper
        )
    }
}
