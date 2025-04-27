package com.numplates.nomera3.presentation.utils


import com.meera.db.models.message.UniquenameEntity
import com.meera.db.models.message.UniquenameSpanData
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import org.junit.Test


internal class UniquenameUtilsKtTest {

    @Test
    fun parseUniquenameTest() {
        val inputString = "Привет <tag:253ee3d7-e8b0-4f1a-92d6-0a67b632b436>, как дела? Это второе имя <tag:253m7d57-eр30-4f1a-92d6-0a67bfq65777> Repeat <tag:253ee3d7-e8b0-4f1a-92d6-83gd53jsnxh63>"
        val tags = mutableListOf<UniquenameEntity>()
        tags.add(
                UniquenameEntity(
                        id = "253ee3d7-e8b0-4f1a-92d6-0a67b632b436",
                        type = UniquenameType.NICK.value,
                        options = null,
                        text = "@vasya"
                )
        )
        tags.add(
                UniquenameEntity(
                        id = "253m7d57-eр30-4f1a-92d6-0a67bfq65777",
                        type = UniquenameType.NICK.name,
                        options = null,
                        text = "@hermes_test"
                )
        )
        tags.add(
                UniquenameEntity(
                        id = "253ee3d7-e8b0-4f1a-92d6-83gd53jsnxh63",
                        type = UniquenameType.NICK.value,
                        options = null,
                        text = "@vasya"
                )
        )

        val (text, spanData) = parseUniquename(inputString, tags)
        println("OUTPUT:$text Span:$spanData")
    }

    private data class SpanDataContainer(
            val id: String?,
            val text: String?,
            val startSpanPos: Int?,
            val endSpanPos: Int?,
            val type: String?,
            val userId: Long?,
            val groupId: Long?
    )

    @Test
    fun parseUniquenameRegexTest() {
        val inputString = "Привет <tag:d3949939-22f7-4a7a-ad4a-f1086f118efc>, как дела? Это второе имя <tag:1554d601-eaaf-4b54-a97e-1e15d633323e> Repeat <tag:1554d601-eaaf-4b54-a97e-1e15d6333999>"
        //val inputString = "@nomit/((-);@nomurad <tag:d3949939-22f7-4a7a-ad4a-f1086f118efc>@nomurad <tag:1554d601-eaaf-4b54-a97e-1e15d633323e>"

        // Приходит с бэка
        val tags = mutableListOf<UniquenameEntity>()
        tags.add(
                UniquenameEntity(
                        id = "d3949939-22f7-4a7a-ad4a-f1086f118efc",
                        type = UniquenameType.NICK.value,
                        options = null,
                        text = "@vasya-keks"
                )
        )
        tags.add(
                UniquenameEntity(
                        id = "1554d601-eaaf-4b54-a97e-1e15d633323e",
                        type = UniquenameType.NICK.name,
                        options = null,
                        text = "@hermes_test"
                )
        )
        tags.add(
                UniquenameEntity(
                        id = "1554d601-eaaf-4b54-a97e-1e15d6333999",
                        type = UniquenameType.NICK.value,
                        options = null,
                        text = "@vasya-keks"
                )
        )

        /*tags.add(
                UniquenameEntity(
                        id = "d3949939-22f7-4a7a-ad4a-f1086f118efc",
                        type = UniquenameType.NICK.value,
                        options = null,
                        text = "@nomit"
                )
        )
        tags.add(
                UniquenameEntity(
                        id = "1554d601-eaaf-4b54-a97e-1e15d633323e",
                        type = UniquenameType.NICK.name,
                        options = null,
                        text = "@nomit"
                )
        )*/


        val spanData = mutableListOf<SpanDataContainer?>()
        var offset = 0

        val regex = "<tag:(.*?)>".toRegex()
        val parsedText = inputString.replace(regex) { matchResult ->
            tags.forEach { tag ->

                if (matchResult.groupValues[1] == tag.id) {
                    val range = matchResult.range
                    println("RNG:$range")   // Блок уникального имени
                    val nameLength = tag.text?.length ?: 0

                    val start = range.first - offset
                    val end = range.last.plus(1) - 42 + nameLength - offset
                    println("Start:$start => END:$end OFFSET:$offset")
                    spanData.add(SpanDataContainer(
                            id = tag.id,
                            text = tag.text,
                            startSpanPos = start,
                            endSpanPos = end,
                            type = tag.type,
                            userId = tag.options?.userId,
                            groupId = tag.options?.groupId
                    ))
                    offset += 42 - nameLength           // предидущие офсеты накапл

                    return@replace tag.text ?: ""
                }
            }
            ""      // stub
        }

        val listSpDat = spanData.map { UniquenameSpanData(
                tag = it?.text,
                type = it?.type,
                startSpanPos = it?.startSpanPos,
                endSpanPos = it?.endSpanPos,
                userId = it?.userId,
                groupId = it?.groupId,
            id = it?.id,
            symbol = null
        ) }

        // 36-...
        listSpDat.forEach {
            println("NEW-LIST Span dat:$it")
        }


        val regOnlyTags = "(@[A-Za-z0-9-_.]+)(?:@[A-Za-z0-9-_.]+)*".toRegex()
        //val match = regOnlyTags.findAll(parsedText)
        /* match.forEachIndexed { index, matchResult ->
             val range = matchResult.range
             listSpanData.add(UniquenameSpanData(
                     tag = spanData[index]?.text,
                     type = spanData[index]?.type,
                     startSpanPos = range.first,
                     endSpanPos = range.last.plus(1),
                     userId = spanData[index]?.userId,
                     groupId = spanData[index]?.groupId
             ))
         }*/

        println("INPUT:$inputString")
        println("Parsed:$parsedText")
    }


    @Test
    fun parseUniquenameWithoutTagDataTest() {
        val input = "Hello @terminator parse text and @testtag account @vasya hey"
        val regex = "(@[A-Za-z0-9-_]+)(?:@[A-Za-z0-9-_]+)*".toRegex()
        val matches = regex.findAll(input)
        matches.forEach {
            println("Match:${it.groupValues[1]} Range:${it.range}")
        }
    }
}
