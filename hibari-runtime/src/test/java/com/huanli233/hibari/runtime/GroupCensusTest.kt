package com.huanli233.hibari.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The census is the evidence for or against building group level skipping, so the one way it can lie
 * is worth pinning: a group that read a state which moved must never be counted clean, and a change
 * nobody inside it read must not mark it dirty either.
 */
class GroupCensusTest {

    private val changedA = Any()
    private val changedB = Any()
    private val untouched = Any()

    @Test
    fun `a group that read nothing is clean`() {
        val census = GroupCensus(setOf(changedA))
        census.noteGroup("3")

        assertEquals(1, census.groupsSeen())
        assertEquals(1, census.cleanGroupCount())
    }

    @Test
    fun `a group that read a changed state is not clean`() {
        val census = GroupCensus(setOf(changedA))
        census.noteGroup("7")
        census.noteRead("7-2", changedA)

        assertEquals(0, census.cleanGroupCount())
    }

    @Test
    fun `a change no group read leaves the group clean`() {
        val census = GroupCensus(setOf(changedA, changedB))
        census.noteGroup("7")
        census.noteRead("7-2", untouched)

        assertEquals(1, census.cleanGroupCount())
    }

    @Test
    fun `one dirty sibling does not mark another`() {
        val census = GroupCensus(setOf(changedA))
        census.noteGroup("4")
        census.noteGroup("9")
        census.noteRead("9-1", changedA)

        assertEquals(2, census.groupsSeen())
        assertEquals(1, census.cleanGroupCount())
    }

    @Test
    fun `a read before any group belongs to nobody`() {
        val census = GroupCensus(setOf(changedA))
        census.noteGroup("5")
        census.noteRead("", changedA)

        assertEquals(1, census.groupsSeen())
        assertEquals(1, census.cleanGroupCount())
    }

    @Test
    fun `a round that entered no group says so`() {
        val census = GroupCensus(emptySet())

        assertTrue(census.isEmpty)
        assertEquals(0, census.cleanGroupCount())
    }

    @Test
    fun `loop siblings fold into the group they repeat`() {
        val census = GroupCensus(setOf(changedA))
        census.noteGroup("12#3-1")
        census.noteGroup("12")
        census.noteRead("12#3-2", changedA)

        // One group, dirtied through its second iteration: counting them apart would overstate what
        // a skip pass could cover, because a skip decision is made per group entry.
        assertEquals(1, census.groupsSeen())
        assertEquals(0, census.cleanGroupCount())
    }
}
