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

package green.sailor.kython.interpreter.objects

import green.sailor.kython.interpreter.objects.python.PyException
import green.sailor.kython.interpreter.objects.python.PyString

/**
 * A nice list of exceptions.
 */
object Exceptions {
    // BaseException and its special subclasses
    val BASE_EXCEPTION = PyException.makeExceptionType("BaseException", listOf())

    // Root of all other exceptions
    val EXCEPTION = BASE_EXCEPTION.typeSubclassOf("Exception")

    // main errors
    val NAME_ERROR = EXCEPTION.typeSubclassOf("NameError")

    // runtimeerror and its children
    val RUNTIME_ERROR = EXCEPTION.typeSubclassOf("RuntimeError")
    val NOT_IMPLEMENTED_ERROR = RUNTIME_ERROR.typeSubclassOf("NotImplementedError")

    val EXCEPTION_MAP = mapOf(
        PyString("BaseException") to EXCEPTION,
        PyString("Exception") to EXCEPTION,
        PyString("NameError") to NAME_ERROR,
        PyString("RuntimeError") to RUNTIME_ERROR,
        PyString("NotImplementedError") to NOT_IMPLEMENTED_ERROR
    )
}