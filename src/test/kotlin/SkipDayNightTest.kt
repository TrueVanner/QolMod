class SkipDayNightTest {
    /* @Nested
    inner class SkipPeriodTest {
        @Test
        fun `SkipPeriod should return correct period and isInfinite`() {
            val skipPeriod = SkipDayNight.SkipPeriod(5, true)
            assertEquals(5, skipPeriod.period)
            assertTrue(skipPeriod.isInfinite)
        }

        @Test
        fun `getAndUpdate should decrement period by 1 if not infinite`() {
            val skipPeriod = SkipDayNight.SkipPeriod(5, false)
            assertEquals(5, skipPeriod.getAndUpdate())
            assertEquals(4, skipPeriod.period)
        }

        @Test
        fun `getAndUpdate should not decrement period if infinite`() {
            val skipPeriod = SkipDayNight.SkipPeriod(5, true)
            assertEquals(5, skipPeriod.getAndUpdate())
            assertEquals(5, skipPeriod.period)
        }

        @Test
        fun `getAndUpdate should throw IllegalStateException if period is 0 and not infinite`() {
            val skipPeriod = SkipDayNight.SkipPeriod(0, false)
            assertThrows(IllegalStateException::class.java) {
                skipPeriod.getAndUpdate()
            }
        }

        @Test
        fun `getAndUpdate should not throw IllegalStateException if period is 0 but is infinite`() {
            val skipPeriod = SkipDayNight.SkipPeriod(0, true)
            assertDoesNotThrow {
                skipPeriod.getAndUpdate()
            }

            assertEquals(skipPeriod.getAndUpdate(), 0)
        }
    }

    @Nested
    inner class ModeTest {
        @Test
        fun `Mode DAY should have YELLOW color and daysToSkip entry`() {
            val mode = SkipDayNight.Mode.DAY
            assertEquals(Utils.Colors.YELLOW, mode.color)
            assertEquals(serverConfig.skippingSettings.daysToSkip, mode.associatedEntry)
        }

        @Test
        fun `Mode NIGHT should have GRAY color and nightsToSkip entry`() {
            val mode = SkipDayNight.Mode.NIGHT
            assertEquals(Utils.Colors.GRAY, mode.color)
            assertEquals(serverConfig.skippingSettings.nightsToSkip, mode.associatedEntry)
        }

        @Test
        fun `Associated entry in Mode should always be up-to-date`() {
            val mode = SkipDayNight.Mode.DAY

            serverConfig.skippingSettings.daysToSkip.validateAndSet(SkipDayNight.SkipPeriod(5, false))
            mode.associatedEntry.get().getAndUpdate()
            mode.associatedEntry.get().getAndUpdate()
            assertEquals(3, mode.associatedEntry.get().period)

            serverConfig.skippingSettings.daysToSkip.validateAndSet(SkipDayNight.SkipPeriod(10, true))
            assertEquals(10, mode.associatedEntry.get().period)

            serverConfig.skippingSettings.daysToSkip.validateAndSet(SkipDayNight.SkipPeriod(0, false))
            assertEquals(0, mode.associatedEntry.get().period)
            assertThrows(IllegalStateException::class.java) {
                mode.associatedEntry.get().getAndUpdate()
            }

            serverConfig.skippingSettings.daysToSkip.validateAndSet(SkipDayNight.SkipPeriod(0, true))
            mode.associatedEntry.get().getAndUpdate()
            assertEquals(0, mode.associatedEntry.get().period)
        }
    } */
}