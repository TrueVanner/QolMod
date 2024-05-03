package net.vannername.qol.utils

import eu.pb4.playerdata.api.PlayerDataApi
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtString
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.vannername.qol.QoLMod
import net.vannername.qol.schemes.PlayerActionbarCoordsColors
import net.vannername.qol.schemes.PlayerData
import net.vannername.qol.schemes.PlayerNavigationData
import net.vannername.qol.utils.ConfigUtils.ConfigProperties.*
import net.vannername.qol.utils.Utils.setPlayerData
import java.awt.Color

object ConfigUtils {

    class ConfigUnknownParamException(override val message: String = "") : Exception("Attempted to set undefined property")

    val boolean: String = Boolean::class.simpleName!!
    val string: String = String::class.simpleName!!
    val int: String = Int::class.simpleName!!

    val configurableTypes = listOf(boolean, string, int)

    enum class ConfigProperties(val n: String, val type: String) {
        SEND_DEATH_COORDS("send_death_coords", boolean),
        SEND_ACTIONBAR_COORDS("send_actionbar_coords", boolean),
        IS_NAVIGATING("is_navigating", boolean),
        NAV_X("nav_x", int),
        NAV_Y("nav_y", int),
        NAV_Z("nav_z", int),
        NAV_WORLD("nav_world", string),
        NAV_IS_DIRECT("nav_is_direct", boolean),
        ACTIONBAR_COORDS_COLOR_TEXT("actionbar_coords_color_text", int),
        ACTIONBAR_COORDS_COLOR_COORDS("actionbar_coords_color_coords", int)
    }

    val propertyNames = ConfigProperties.entries.map { elem -> elem.n }

    @JvmStatic
    fun getConfig(p: ServerPlayerEntity, param: String): Any {
        if(param !in propertyNames) throw ConfigUnknownParamException()

        return when (val elem = PlayerDataApi.getGlobalDataFor(p, Identifier("qolmod:$param"))) {
            is NbtByte -> elem.byteValue()
            is NbtInt -> elem.intValue()
            else -> elem.toString()
        }
    }

    @JvmStatic
    fun setConfig(p: ServerPlayerEntity, param: String, value: Any) {
        if(param !in propertyNames) throw ConfigUnknownParamException()

        val nbtElem: NbtElement = when (value) {
            is Boolean -> NbtByte.of(value)
            is Int -> NbtInt.of(value)
            else -> NbtString.of(value.toString())
        }

        PlayerDataApi.setGlobalDataFor(p, Identifier("qolmod:$param"), nbtElem)
        Utils.debug("$param is set to $value resulting in $nbtElem")
    }

    @JvmStatic
    fun toBoolean(b: Byte): Boolean {
        return b.toInt() == 1
    }

    @Throws(Exception::class)
    fun createAndLoadCustomData(p: ServerPlayerEntity) {
        val data = PlayerData(
            toBoolean(getConfig(p, SEND_DEATH_COORDS.n) as Byte),
            toBoolean(getConfig(p, SEND_ACTIONBAR_COORDS.n) as Byte),
            PlayerNavigationData(
                toBoolean(getConfig(p, IS_NAVIGATING.n) as Byte),
                Location(
                    getConfig(p, NAV_X.n) as Int,
                    getConfig(p, NAV_Y.n) as Int,
                    getConfig(p, NAV_Z.n) as Int,
                    getConfig(p, NAV_WORLD.n) as String
                ),
                toBoolean(getConfig(p, NAV_IS_DIRECT.n) as Byte),
            ),
            PlayerActionbarCoordsColors(
                getConfig(p, ACTIONBAR_COORDS_COLOR_TEXT.n) as Int,
                getConfig(p, ACTIONBAR_COORDS_COLOR_COORDS.n) as Int
            )
        )
        p.setPlayerData(data)
    }

    /**
     * Sets the custom data for the player from config, if the config is present,
     * or assigns default values to config params and to the custom data.
     */
    @JvmStatic
    fun loadCustomDataFromConfig(p: ServerPlayerEntity) {
        try {
            createAndLoadCustomData(p)
        } catch(e: Exception) {
            QoLMod.logger.info("Config was not set up for player ${p.name.string}, not anymore")
            // TODO: some message informing the player about their defaults

            setDefaultPlayerValues(p)
            p.setPlayerData(PlayerData())
        }
    }

    @JvmStatic
    fun setConfigFromCustomData(p: ServerPlayerEntity, data: PlayerData) {
        setConfig(p, SEND_DEATH_COORDS.n, data.sendDeathCoords)
        setConfig(p, SEND_ACTIONBAR_COORDS.n, data.sendActionbarCoords)
        setConfig(p, IS_NAVIGATING.n, data.navData.isNavigating)
        setConfig(p, NAV_X.n, data.navData.location.x)
        setConfig(p, NAV_Y.n, data.navData.location.y)
        setConfig(p, NAV_Z.n, data.navData.location.z)
        setConfig(p, NAV_WORLD.n, data.navData.location.worldName)
        setConfig(p, NAV_IS_DIRECT.n, data.navData.isDirect)
        setConfig(p, ACTIONBAR_COORDS_COLOR_TEXT.n, data.actionbarCoordsColors.text)
        setConfig(p, ACTIONBAR_COORDS_COLOR_COORDS.n, data.actionbarCoordsColors.coords)
        // ...
    }

    val defaultPlayerConfig: Map<ConfigProperties, Any> = mapOf(
        SEND_DEATH_COORDS to true,
        SEND_ACTIONBAR_COORDS to true,
        IS_NAVIGATING to false,
        NAV_X to 0,
        NAV_Y to 0,
        NAV_Z to 0,
        NAV_WORLD to "overworld",
        NAV_IS_DIRECT to false,
        ACTIONBAR_COORDS_COLOR_TEXT to Color.YELLOW.rgb,
        ACTIONBAR_COORDS_COLOR_COORDS to Color.GREEN.rgb
    )

    @JvmStatic
    fun setDefaultPlayerValues(p: ServerPlayerEntity) {
        for(pair in defaultPlayerConfig) {
            setConfig(p, pair.key.n, pair.value)
        }
    }
}