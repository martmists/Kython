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
package green.sailor.kython.interpreter.pyobject

import green.sailor.kython.interpreter.kyobject.KyCodeObject

/**
 * Represents a code object. Wraps a KyCodeObject, but exposes it to Python.
 */
class PyCodeObject(val wrappedCodeObject: KyCodeObject) : PyObject() {
    override fun getPyStr(): PyString {
        return PyString("<code object ${wrappedCodeObject.codename}>")
    }

    override fun getPyRepr(): PyString = getPyStr()

    override var type: PyType
        get() = TODO("not implemented")
        set(_) {}
}
