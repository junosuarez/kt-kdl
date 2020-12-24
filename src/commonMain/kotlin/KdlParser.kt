// TODO(juno) ultimately I want to write a JVMish streaming parser that makes nice use of common platform types
// maybe java.nio, maybe okio. For now, for simplicity of initial parser implementation, we just take a string
// of a complete kdl document as input
// initial api tries to be similar to kdl-rs implementation, with kotlin idioms

sealed class KdlLeaf {
    abstract fun print(): String

    data class KdlProp<T : KdlVal>(val key: String, val value: T) : KdlLeaf() {
        override fun print(): String = "$key=${value.print()}"
    }

    sealed class KdlVal : KdlLeaf() {
        data class KdlStr(val value: String) : KdlVal() {
            override fun print(): String = "\"$value\""
        }
        data class KdlNum(val value: Number) : KdlVal() {
            override fun print(): String = "$value"
        }
        data class KdlBool(val value: Boolean) : KdlVal() {
            override fun print(): String = "$value"
        }
        object KdlNull : KdlVal() {
            override fun print(): String = "null"
        }
    }
}

typealias KdlProp<T> = KdlLeaf.KdlProp<T>
typealias KdlVal = KdlLeaf.KdlVal
typealias KdlStr = KdlLeaf.KdlVal.KdlStr
typealias KdlNum = KdlLeaf.KdlVal.KdlNum
typealias KdlBool = KdlLeaf.KdlVal.KdlBool
typealias KdlNull = KdlLeaf.KdlVal.KdlNull

fun Any.toKdlLeaf(): KdlLeaf = this.toKdlLeafOrNull() ?: throw IllegalArgumentException("$this must be a KdlValue, found ${this::class.simpleName}")
fun Any?.toKdlLeafOrNull(): KdlLeaf? = when (this) {
    is KdlLeaf -> this
    is String -> this.kdlValue()
    is Number -> this.kdlValue()
    is Boolean -> this.kdlValue()
    is Nothing? -> KdlNull
    else -> null
}

fun Number.kdlValue() = KdlNum(this)
fun String.kdlValue() = KdlStr(this)
fun Boolean.kdlValue() = KdlBool(this)
fun kdlValue() = KdlNull
fun Nothing?.kdlValue(): KdlNull = KdlNull

data class KdlDocument(val nodes: List<KdlNode>) {

    constructor(vararg node: KdlNode) : this(listOf(*node))
    constructor(lambda: NodeBuilder.() -> Unit) : this(NodeBuilderImpl(lambda).nodes)

    fun print(): String = nodes.joinToString("") { it.print() }

    interface NodeBuilder {
        fun node(name: String, vararg argOrProp: Any?)
        fun node(name: String, vararg argOrProp: Any?, children: NodeBuilder.() -> Unit)

        // override "to" to make KdlProps instead of Pairs
        infix fun String.to(that: String): KdlProp<KdlStr> = KdlProp(this, that.kdlValue())
        infix fun String.to(that: Int): KdlProp<KdlNum> = KdlProp(this, that.kdlValue())
        infix fun String.to(that: Boolean): KdlProp<KdlBool> = KdlProp(this, that.kdlValue())
        infix fun String.to(that: Nothing?): KdlProp<KdlNull> = KdlProp(this, KdlNull)
    }

    class NodeBuilderImpl(lambda: NodeBuilderImpl.() -> Unit) : NodeBuilder {
        var nodes: MutableList<KdlNode> = mutableListOf()

        override fun node(name: String, vararg argOrProp: Any?) {
            nodes.add(nodeOf(name, argOrProp))
        }

        override fun node(name: String, vararg argOrProp: Any?, children: NodeBuilder.() -> Unit) {
            nodes.add(nodeOf(name, argOrProp, children))
        }

        private fun nodeOf(name: String, argOrProp: Array<out Any?>, children: (NodeBuilder.() -> Unit)? = null): KdlNode {
            var args: MutableList<KdlLeaf.KdlVal> = mutableListOf()
            var props: MutableMap<String, KdlLeaf.KdlVal> = mutableMapOf()
            var children_: List<KdlNode> = listOf()

            argOrProp.mapNotNull { it.toKdlLeafOrNull() }.forEach {
                when (it) {
                    is KdlProp<*> -> props[it.key] = it.value
                    is KdlVal -> args.add(it)
                }
            }

            if (children != null) {
                children_ = NodeBuilderImpl(children).nodes
            }

            return KdlNode(name, arguments = args, properties = props, children = children_)
        }

        init {
            lambda(this)
        }
    }
}

data class KdlNode(
    val name: String = "",
    val arguments: List<KdlVal> = listOf(),
    val properties: Map<String, KdlVal> = mapOf(),
    val children: List<KdlNode> = listOf()
) {
    fun print(indent: Int = 0): String = "${"    ".repeat(indent)}$name${
    if (arguments.any()) arguments.joinToString(" ", " ") { it.print() } else ""
    }${
    if (properties.any()) properties.entries.joinToString(" ", " ") { "${it.key}=${it.value.print()}"} else ""
    }${
    if (children.any()) children.joinToString("\n${"    ".repeat(indent)}", " {\n","\n${"    ".repeat(indent)}}\n\n") { it.print(indent + 1) } else ""
    }"
}

// TODO(juno) better differentiate errors a la https://github.com/kdl-org/kdl-rs/blob/main/src/error.rs
class KdlError(message: String) : RuntimeException(message)

object KdlParser {
    fun parseDocument(input: String): KdlDocument {
        return TODO("implement parser itself")
    }
}
