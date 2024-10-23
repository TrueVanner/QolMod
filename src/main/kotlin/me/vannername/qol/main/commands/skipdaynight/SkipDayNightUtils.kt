package me.vannername.qol.main.commands.skipdaynight

import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.util.Walkable
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import me.vannername.qol.main.QoLMod
import me.vannername.qol.main.QoLMod.serverConfig
import me.vannername.qol.main.config.ServerConfig
import me.vannername.qol.main.utils.Utils
import me.vannername.qol.main.utils.Utils.sentenceCase
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World
import kotlin.ranges.contains

object SkipDayNightUtils {
    /**
     * A class that represents the mode (day or night) to skip.
     */
    enum class Mode(
        val color: Utils.Colors,
        var associatedEntry: SkipPeriod,
        var opposite: Mode? = null,
        var skipForce: Boolean = false
    ) {
        DAY(Utils.Colors.YELLOW, serverConfig.skippingSettings.daysToSkip),
        NIGHT(Utils.Colors.GRAY, serverConfig.skippingSettings.nightsToSkip)
    }

    /**
     * A class that represents the number of days or nights to skip.
     *
     * @param period The number of days or nights to skip.
     * @param isInfinite Whether the period is infinite or not.
     */
    class SkipPeriod(
        @ClientModifiable @property:ValidatedInt.Restrict(min = 0, max = 100) var period: Int,
        @ClientModifiable var isInfinite: Boolean
    ) : Walkable {

        override fun toString(): String {
            return if (isInfinite) "Infinite" else period.toString()
        }

        fun isSet(): Boolean {
            return period > 0 || isInfinite
        }

        /**
         * Get the current period and decrement it by 1.
         *
         * @return The current period.
         * @throws IllegalStateException If the period is already 0.
         */
        fun getAndUpdate(force: Boolean = false): Int {
            if (isInfinite || force) return period

            if (period <= 0) throw IllegalStateException("Period is already 0")

            period -= 1
            return period
        }
    }

    /**
     * The mode received is the current mode. This function changes the time
     * such that the mode is inverted (day -> add time to make it night, same with night).
     */
    @Throws(IllegalStateException::class)
    fun performSkip(mode: Mode) {
        // i usually do skipday force if skipnight is on ->
        // mode.force is true when mode.opposite.value > 0.
        // then, the day is skipped ->
        // mode becomes mode.opposite, so now we have that mode.opposite.force is true
        // then, I want the night to not be skipped ->
        // if mode.opposite.force = true, do not skip mode
        try {
            // known problem - if the server is stopped (cache unloaded) the force state is lost,
            // meaning that as soon as the world starts again, the skip will be performed.

            if (mode.skipForce || !mode.opposite!!.skipForce) {
                val world = QoLMod.defaultWorld!!
                val fullDays = world.timeOfDay.toInt() / 24000
                // math
                val newTime = (fullDays + 1) * 24000L + if (mode == Mode.DAY) 13000 else 0
                world.timeOfDay = newTime

                if (!mode.skipForce && !mode.opposite!!.skipForce) {
                    val currentValue = mode.associatedEntry.getAndUpdate()
                    world.players.forEach {
                        it.sendMessage(
                            Text.literal("${mode.name.sentenceCase()} successfully skipped!").formatted(Formatting.AQUA)
                        )
                        if (currentValue == 0) {
                            it.sendMessage(
                                Text.literal("Warning: this was the last ${mode.name.lowercase()} skip.")
                                    .formatted(Formatting.YELLOW)
                            )
                        }
                    }
                    forceUpdateConfig()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Detect the time change and skip the day/night if necessary.
     * This method is called every tick.
     */
    fun detectTimeChange() {
        // 12000 (10:00) Beginning of the Minecraft sunset.
        // Villagers go to their beds and sleep.
        // 23460 (19:33) In clear weather, beds can no longer be used.
        // In clear weather, bees leave the nest/hive.
        // In clear weather, undead mobs begin to burn.

        ServerTickEvents.END_WORLD_TICK.register { world ->
            try {
                val currentMode = worldTimeToMode(world)
                if (currentMode.associatedEntry.isSet()) {
                    currentMode.skipForce = false
                    performSkip(currentMode)
                }
            } catch (e: IllegalStateException) {
                // Should only be thrown if the period is 0 and skipping is
                // impossible; Do nothing
            }
        }
    }

    fun worldTimeToMode(world: World): Mode {
        return if (world.timeOfDay % 24000 in 12000..23459) Mode.NIGHT else Mode.DAY
    }

    fun forceUpdateConfig() {
        serverConfig.skippingSettings.daysToSkip = Mode.DAY.associatedEntry
        serverConfig.skippingSettings.nightsToSkip = Mode.NIGHT.associatedEntry

        ConfigApi.save<ServerConfig>(serverConfig)
    }
}