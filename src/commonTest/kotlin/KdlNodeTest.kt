import kotlin.test.Test
import kotlin.test.assertEquals

class KdlNodeTest {

    @Test
    fun lets_try_making_a_document_with_KdlNode() {
        val nodes = KdlDocument(
            KdlNode(
                "package",
                children = listOf(
                    KdlNode("name", arguments = listOf("kdl".kdlValue())),
                    KdlNode("version", arguments = listOf("0.0.0".kdlValue())),
                    KdlNode("description", arguments = listOf("kat's document language".kdlValue())),
                    KdlNode("authors", arguments = listOf("Kat Marchán <kzm@zkat.tech>".kdlValue())),
                    KdlNode("license-file", arguments = listOf("LICENSE.md".kdlValue())),
                    KdlNode("edition", arguments = listOf("2018".kdlValue()))
                )
            ),
            KdlNode(
                "dependencies",
                children = listOf(
                    KdlNode("nom", arguments = listOf("6.0.1".kdlValue())),
                    KdlNode("thiserror", arguments = listOf("1.0.22".kdlValue()))
                )
            )
        )

        val x = 1
    }

    @Test
    fun idiomatic_builder() {
        val doc = KdlDocument {
            node("foo", "bar")
            node("parent") {
                node("plainChild")
                node("childWithArgs", "a", null, 1, true)
                node("childWithArgs2", "a", null, 1, true) {
                    node("x")
                }
                node("childWithProps", "a" to "z", "b" to 2, "c" to false, "d" to null)
                node("childWithProps2", "a" to "z", "b" to 2, "c" to false, "d" to null) {
                    node("y")
                }
            }

            // this isn't ordinary usage, but tests `infix to` builds KdlProp from plain types
            val x: KdlProp<KdlStr> = "foo" to "x"
            val y: KdlProp<KdlNum> = "foo" to 2
            val z: KdlProp<KdlBool> = "foo" to true
            val zz: KdlProp<KdlNull> = "foo" to null
        }
    }

    @Test
    fun parse_cargo_example() {
        val kdlString = """
            package {
                name "kdl"
                version "0.0.0"
                description "kat's document language"
                authors "Kat Marchán <kzm@zkat.tech>"
                license-file "LICENSE.md"
                edition "2018"
            }

            dependencies {
                nom "6.0.1"
                thiserror "1.0.22"
            }
        """.trimIndent()
        val expected = KdlDocument {
            node("package") {
                node("name", "kdl")
                node("version", "0.0.0")
                node("description", "kat's document language")
                node("authors", "Kat Marchán <kzm@zkat.tech>")
                node("license-file", "LICENSE.md")
                node("edition", "2018")
            }
            node("dependencies") {
                node("nom", "6.0.1")
                node("thiserror", "1.0.22")
            }
        }

        assertEquals(kdlString, expected.print().trimEnd())
        TODO("think of a convenient reader/mapper/query api. model after DOM/CSS selectors?")
        (expected.getNode("name")?.properties?.get("z") as KdlNum).value
    }
}
