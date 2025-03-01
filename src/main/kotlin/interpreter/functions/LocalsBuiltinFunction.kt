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
package green.sailor.kython.interpreter.functions

import green.sailor.kython.interpreter.Exceptions
import green.sailor.kython.interpreter.KythonInterpreter
import green.sailor.kython.interpreter.iface.PyCallableSignature
import green.sailor.kython.interpreter.pyobject.PyDict
import green.sailor.kython.interpreter.pyobject.PyObject
import green.sailor.kython.interpreter.stack.UserCodeStackFrame
import green.sailor.kython.interpreter.throwKy

/**
 * The implementation for the locals() function. Returns a PyDict wrapping the locals.
 */
class LocalsBuiltinFunction : PyBuiltinFunction("locals") {
    override val signature: PyCallableSignature = PyCallableSignature.EMPTY

    override fun callFunction(kwargs: Map<String, PyObject>): PyObject {
        val frame = KythonInterpreter.getCurrentFrameForThisThread().parentFrame
            ?: error("Parent frame was null!")
        if (frame !is UserCodeStackFrame) {
            Exceptions.RUNTIME_ERROR("Built-in frames do not have locals").throwKy()
        }
        return PyDict.fromAnyMap(frame.locals)
    }
}
