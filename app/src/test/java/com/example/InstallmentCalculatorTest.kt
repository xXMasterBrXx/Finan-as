package com.example

import com.example.util.InstallmentCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToLong

class InstallmentCalculatorTest {

    @Test
    fun testExactDivision100In3x() {
        // R$ 100,00 divided by 3:
        // base = 33.33, remainder adjusted on last = 33.34
        val schedule = InstallmentCalculator.calculateSchedule(100.0, 3)
        assertEquals(3, schedule.size)
        assertEquals(33.33, schedule[0], 0.0001)
        assertEquals(33.33, schedule[1], 0.0001)
        assertEquals(33.34, schedule[2], 0.0001)

        val totalCents = schedule.sumOf { (it * 100.0).roundToLong() }
        assertEquals(10000L, totalCents)
    }

    @Test
    fun testExactDivision50In3x() {
        // R$ 50,00 divided by 3:
        // base = 16.67, last = 16.66
        val schedule = InstallmentCalculator.calculateSchedule(50.0, 3)
        assertEquals(3, schedule.size)
        assertEquals(16.67, schedule[0], 0.0001)
        assertEquals(16.67, schedule[1], 0.0001)
        assertEquals(16.66, schedule[2], 0.0001)

        val totalCents = schedule.sumOf { (it * 100.0).roundToLong() }
        assertEquals(5000L, totalCents)
    }

    @Test
    fun testExactDivision10In6x() {
        // R$ 10,00 in 6x:
        // base = 1.67 x 5 = 8.35, last = 1.65 (total 10.00)
        val schedule = InstallmentCalculator.calculateSchedule(10.0, 6)
        assertEquals(6, schedule.size)
        for (i in 0..4) {
            assertEquals(1.67, schedule[i], 0.0001)
        }
        assertEquals(1.65, schedule[5], 0.0001)

        val totalCents = schedule.sumOf { (it * 100.0).roundToLong() }
        assertEquals(1000L, totalCents)
    }

    @Test
    fun testNoRemainderDivision90In3x() {
        // R$ 90,00 in 3x: exactly 30.00 each
        val schedule = InstallmentCalculator.calculateSchedule(90.0, 3)
        assertEquals(3, schedule.size)
        assertEquals(30.00, schedule[0], 0.0001)
        assertEquals(30.00, schedule[1], 0.0001)
        assertEquals(30.00, schedule[2], 0.0001)

        val preview = InstallmentCalculator.getPreview(90.0, 3)
        assertEquals(false, preview.hasDifference)
    }

    @Test
    fun testPreviewContainsDifferenceNotice() {
        val preview = InstallmentCalculator.getPreview(100.0, 3)
        assertTrue(preview.hasDifference)
        assertTrue(preview.previewText.contains("2x de"))
        assertTrue(preview.previewText.contains("1x de"))
    }
}
