package com.andannn.test_processer

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import junit.framework.TestCase.assertEquals
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class IntSummableProcessorTest {
    @Rule
    @JvmField
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `test compile ext`() {
        val sourceFile = SourceFile.kotlin(
            "Foo.kt", """
package com.example

import com.andannn.annotation.IntSummable

@IntSummable
data class Foo(
    private val a: Int,
    private val b: Int,
)
        """
        )

        val compileResult = compile(listOf(sourceFile))

        assertSourceEquals("""
package com.example

import kotlin.Int

public fun Foo.sumInt(): Int = a+b
""", compileResult.sourceFor("FooExt.kt"))
    }


    private fun KotlinCompilation.Result.sourceFor(fileName: String): String {
        return kspGeneratedSources().find { it.name == fileName }
            ?.readText()
            ?: throw IllegalArgumentException("Could not find file $fileName in ${kspGeneratedSources()}")
    }

    private val KotlinCompilation.Result.workingDir: File
        get() = checkNotNull(outputDirectory.parentFile)

    private fun KotlinCompilation.Result.kspGeneratedSources(): List<File> {
        val kspWorkingDir = workingDir.resolve("ksp")
        val kspGeneratedDir = kspWorkingDir.resolve("sources")
        val kotlinGeneratedDir = kspGeneratedDir.resolve("kotlin")
        val javaGeneratedDir = kspGeneratedDir.resolve("java")
        return kotlinGeneratedDir.walk().toList() +
                javaGeneratedDir.walk().toList()
    }

    private fun assertSourceEquals(@Language("kotlin") expected: String, actual: String) {
        assertEquals(
            expected.trimIndent(),
            // unfortunate hack needed as we cannot enter expected text with tabs rather than spaces
            actual.trimIndent().replace("\t", "    ")
        )
    }

    private fun compile(source: List<SourceFile>) = KotlinCompilation().apply {
        sources = source
        symbolProcessorProviders = listOf(IntSummableProcessorProvider())
        workingDir = temporaryFolder.root
        inheritClassPath = true
        verbose = false
    }.compile()
}