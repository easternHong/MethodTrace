package revert


import java.util.StringTokenizer

/**
 * @author [Marko Luksa](mailto:mluksa@redhat.com)
 * @author [Ales Justin](mailto:ales.justin@jboss.org)
 */
object SignatureConverter {

    fun convertMethodSignature(methodName: String, descriptor: String): String {
        return convertToMethodSignature(methodName, descriptor, false)
    }

    fun convertFullMethodSignature(methodName: String, descriptor: String): String {
        return convertToMethodSignature(methodName, descriptor, true)
    }

    private fun convertToMethodSignature(methodName: String, descriptor: String, full: Boolean): String {
        if (descriptor[0] != '(') {
            throw IllegalArgumentException("Can't convert $descriptor")
        }

        val p = descriptor.lastIndexOf(')')
        val params = descriptor.substring(1, p)

        val tokenizer = StringTokenizer(params, ";")
        val sb = StringBuilder()

        if (full) {
            var retParam = descriptor.substring(p + 1)
            if (retParam.endsWith(";")) {
                retParam = retParam.substring(0, retParam.length - 1)
            }
            var ret = convertParam(retParam)
            // only use simple name for return type
            val r = ret.lastIndexOf(".")
            if (r > 0) {
                ret = ret.substring(r + 1)
            }
            sb.append(ret).append("  ")
        }

        sb.append(methodName).append("(")
        while (tokenizer.hasMoreTokens()) {
            val param = tokenizer.nextToken()
            sb.append(convertParam(param))
            if (tokenizer.hasMoreTokens()) {
                sb.append(", ")
            }
        }
        sb.append(")")
        return sb.toString()
    }

    private fun convertParam(param: String): String {
        var i = 0
        val appendix = StringBuilder()
        while (param[i] == '[') {
            appendix.append("[]")
            i++
        }

        val subparam = param.substring(i)
        val result = convertNonArrayParam(subparam) + appendix
        return if (isPrimitive(subparam) && subparam.length > 1) {
            result + ", " + convertParam(subparam.substring(1))
        } else {
            result
        }
    }

    private fun convertNonArrayParam(param: String): String {
        when (param[0]) {
            'B' -> return "byte"
            'C' -> return "char"
            'D' -> return "double"
            'F' -> return "float"
            'I' -> return "int"
            'J' -> return "long"
            'S' -> return "short"
            'Z' -> return "boolean"
            'V' -> return "void"
        //            case 'L':
        //                int lastDotIndex = param.lastIndexOf('/');
        //                return lastDotIndex == -1 ? param.substring(1) : param.substring(lastDotIndex+1);
            'L' -> return param.substring(1).replace('/', '.')
            else -> throw IllegalArgumentException("Unknown param type $param")
        }
    }

    private fun isPrimitive(param: String): Boolean {
        return when (param[0]) {
            'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 'V' -> true
            else -> false
        }
    }


}