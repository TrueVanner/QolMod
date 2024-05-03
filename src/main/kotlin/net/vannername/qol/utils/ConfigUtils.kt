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
import net.vannername.qol.utils.ConfigUtils.ConfigProperty.*
import net.vannername.qol.utils.Utils.setPlayerData
import java.awt.Color

object ConfigUtils {

    val boolean: String = Boolean::class.simpleName!!
    val string: String = String::class.simpleName!!
    val int: String = Int::class.simpleName!!

    enum class ConfigProperty(val text: String, val type: String) {
        SEND_DEATH_COORDS("send_death_coords", boolean),
        SEND_ACTIONBAR_COORDS("send_actionbar_coords", boolean),
        IS_NAVIGATING("is_navigating", boolean),
        IS_AFK("is_afk", boolean),
        IS_SITTING("is_sitting", boolean),
        NAV_X("nav_x", int),
        NAV_Y("nav_y", int),
        NAV_Z("nav_z", int),
        NAV_WORLD("nav_world", string),
        NAV_IS_DIRECT("nav_is_direct", boolean),
        ACTIONBAR_COORDS_COLOR_TEXT("actionbar_coords_color_text", int),
        ACTIONBAR_COORDS_COLOR_COORDS("actionbar_coords_color_coords", int),
        // ...
    }
    // list of all params that can be changed by the player using /setproperty
    val configurableParams = ConfigProperty.entries.filter { elem -> elem.type in listOf(boolean, string, int) }

    val propertyNames = ConfigProperty.entries.map { elem -> elem.text }

    @JvmStatic
    fun ServerPlayerEntity.getConfig(param: ConfigProperty): Any {
        return when (val elem = PlayerDataApi.getGlobalDataFor(this, Identifier("qolmod:${param.text}"))) {
            is NbtByte -> elem.byteValue()
            is NbtInt -> elem.intValue()
            else -> elem.toString()
        }
    }

    @JvmStatic
    fun ServerPlayerEntity.setConfig(param: ConfigProperty, value: Any) {
        val nbtElem: NbtElement = when (value) {
            is Boolean -> NbtByte.of(value)
            is Int -> NbtInt.of(value)
            else -> NbtString.of(value.toString())
        }

        PlayerDataApi.setGlobalDataFor(this, Identifier("qolmod:${param.text}"), nbtElem)
        Utils.debug("${param.text} is set to $value resulting in $nbtElem")
    }

    @JvmStatic
    fun toBoolean(b: Byte): Boolean {
        return b.toInt() == 1
    }

    @Throws(Exception::class)
    fun createAndLoadCustomData(p: ServerPlayerEntity) {
        val data = PlayerData(
            toBoolean(p.getConfig(SEND_DEATH_COORDS) as Byte),
            toBoolean(p.getConfig(SEND_ACTIONBAR_COORDS) as Byte),
            toBoolean(p.getConfig(IS_AFK) as Byte),
            toBoolean(p.getConfig(IS_SITTING) as Byte),
            PlayerNavigationData(
                toBoolean(p.getConfig(IS_NAVIGATING) as Byte),
                Location(
                    p.getConfig(NAV_X) as Int,
                    p.getConfig(NAV_Y) as Int,
                    p.getConfig(NAV_Z) as Int,
                    p.getConfig(NAV_WORLD) as String
                ),
                toBoolean(p.getConfig(NAV_IS_DIRECT) as Byte),
            ),
            PlayerActionbarCoordsColors(
                p.getConfig(ACTIONBAR_COORDS_COLOR_TEXT) as Int,
                p.getConfig(ACTIONBAR_COORDS_COLOR_COORDS) as Int
            ),
            //...
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
        p.setConfig(SEND_DEATH_COORDS, data.sendDeathCoords)
        p.setConfig(SEND_ACTIONBAR_COORDS, data.sendActionbarCoords)
        p.setConfig(IS_AFK, data.isAFK)
        p.setConfig(IS_SITTING, data.isSitting)
        p.setConfig(IS_NAVIGATING, data.navData.isNavigating)
        p.setConfig(NAV_X, data.navData.location.x)
        p.setConfig(NAV_Y, data.navData.location.y)
        p.setConfig(NAV_Z, data.navData.location.z)
        p.setConfig(NAV_WORLD, data.navData.location.worldName)
        p.setConfig(NAV_IS_DIRECT, data.navData.isDirect)
        p.setConfig(ACTIONBAR_COORDS_COLOR_TEXT, data.actionbarCoordsColors.text)
        p.setConfig(ACTIONBAR_COORDS_COLOR_COORDS, data.actionbarCoordsColors.coords)
        p.setConfig(ACTIONBAR_COORDS_COLOR_COORDS, data.actionbarCoordsColors.coords)
        // ...
    }

    val defaultPlayerConfig: Map<ConfigProperty, Any> = mapOf(
        SEND_DEATH_COORDS to true,
        SEND_ACTIONBAR_COORDS to true,
        IS_AFK to false,
        IS_SITTING to false,
        IS_NAVIGATING to false,
        NAV_X to 0,
        NAV_Y to 0,
        NAV_Z to 0,
        NAV_WORLD to "overworld",
        NAV_IS_DIRECT to false,
        ACTIONBAR_COORDS_COLOR_TEXT to Color.YELLOW.rgb,
        ACTIONBAR_COORDS_COLOR_COORDS to Color.GREEN.rgb,
    )

    @JvmStatic
    fun setDefaultPlayerValues(p: ServerPlayerEntity) {
        for(pair in defaultPlayerConfig) {
            p.setConfig(pair.key, pair.value)
        }
    }
}