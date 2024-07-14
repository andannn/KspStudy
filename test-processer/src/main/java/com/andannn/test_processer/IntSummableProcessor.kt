package com.andannn.test_processer

import com.andannn.annotation.IntSummable
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class IntSummableProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private lateinit var intType: KSType
    override fun process(resolver: Resolver): List<KSAnnotated> {
        intType = resolver.builtIns.intType

        resolver.getSymbolsWithAnnotation(IntSummable::class.qualifiedName!!)
            .forEach { it.accept(Visitor(), Unit) }

        return  emptyList()
    }


    private inner class Visitor : KSVisitorVoid() {
        @OptIn(KotlinPoetKspPreview::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if (classDeclaration.modifiers.none { it == Modifier.DATA}) {
                logger.error("@IntSummable must target an data class", classDeclaration)
            }

            val summables = classDeclaration.getAllProperties()
                .filter {  it.extensionReceiver == null }
                .filter { it.type.resolve().isAssignableFrom(intType) }
                .toList()

            if (summables.isEmpty()) {
                return
            }

            FileSpec.builder(classDeclaration.packageName.asString(), "${classDeclaration.simpleName.asString()}Ext")
                .addFunction(
                    FunSpec.builder("sumInt")
                        .returns(Int::class)
                        .receiver(classDeclaration.asType(emptyList()).toTypeName())
                        .addStatement("return %L", summables.joinToString("+"))
                        .build()
                )
                .build()
                .writeTo(codeGenerator, false)
        }
    }
}