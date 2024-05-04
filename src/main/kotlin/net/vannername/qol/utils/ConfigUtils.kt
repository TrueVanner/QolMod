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
import kotlin.reflect.KClass

object ConfigUtils {

    val bool = Boolean::class
    val string = String::class
    val int = Int::class
    val double = Double::class
    val color = Utils.Colors::class

    class ConfigBadTypeException(prop: ConfigProperty, usedType: KClass<*>) :
        Exception("Property ${prop.text} stored values of type ${prop.type}, but $usedType was specified")

    enum class ConfigProperty(val text: String, val type: KClass<*>) {
        SEND_DEATH_COORDS("send_death_coords", bool),
        SEND_ACTIONBAR_COORDS("send_actionbar_coords", bool),
        IS_NAVIGATING("is_navigating", bool),
        IS_AFK("is_afk", bool),
        IS_SITTING("is_sitting", bool),
        NAV_X("nav_x", int),
        NAV_Y("nav_y", int),
        NAV_Z("nav_z", int),
        NAV_WORLD("nav_world", string),
        NAV_IS_DIRECT("nav_is_direct", bool),
        ACTIONBAR_COORDS_COLOR_TEXT("actionbar_coords_color_text", color),
        ACTIONBAR_COORDS_COLOR_COORDS("actionbar_coords_color_coords", color);
        // ...

        companion object {
            fun typeOf(value: String): KClass<*> {
                return ConfigProperty.valueOf(value.uppercase()).type
            }
        }
    }

    // list of all params that can be changed by the player using /setproperty
    val configurableProps = ConfigProperty.entries.filter { elem -> elem.type in listOf(bool, int, string, color) }

    // to get property without type
    @Deprecated("Replaced by a more advanced getConfig()")
    @JvmStatic
    fun ServerPlayerEntity.getConfig(prop: ConfigProperty): Any {
        return when (val elem = PlayerDataApi.getGlobalDataFor(this, Identifier("qolmod:${prop.text}"))) {
            is NbtByte -> elem.byteValue()
            is NbtInt -> elem.intValue()
            else -> elem.toString()
        }
    }

    // to get property with type
    @JvmStatic
    fun <T : Any> ServerPlayerEntity.getConfig(prop: ConfigProperty, expectedType: KClass<T>): T {
        try {
            val result = when (val elem = PlayerDataApi.getGlobalDataFor(this, Identifier("qolmod:${prop.text}"))) {
                is NbtByte -> if(expectedType == Boolean::class) toBoolean(elem.byteValue()) else elem.byteValue()
                is NbtInt -> elem.intValue()
                is NbtString -> when(prop.type) {
                    Utils.Colors::class -> Utils.Colors.valueOf(elem.asString())
                    else -> elem.asString()
                }
                else -> elem.toString()
            } as T
            return result
        } catch(e: ClassCastException) {
            throw ConfigBadTypeException(prop, expectedType)
        }
    }

    @JvmStatic
    fun ServerPlayerEntity.setConfig(prop: ConfigProperty, value: Any) {
        if(value::class != prop.type) {
            throw ConfigBadTypeException(prop, value::class)
        }
        val nbtElem: NbtElement = when (value) {
            is Boolean -> NbtByte.of(value)
            is Int -> NbtInt.of(value)
            else -> NbtString.of(value.toString())
        }

        PlayerDataApi.setGlobalDataFor(this, Identifier("qolmod:${prop.text}"), nbtElem)
        Utils.debug("${prop.text} is set to $value resulting in $nbtElem")
    }

    @JvmStatic
    fun toBoolean(b: Byte): Boolean {
        return b.toInt() == 1
    }

    @Throws(Exception::class)
    fun createAndLoadCustomData(p: ServerPlayerEntity) {
        val data = PlayerData(
            p.getConfig(SEND_DEATH_COORDS, bool),
            p.getConfig(SEND_ACTIONBAR_COORDS, bool),
            p.getConfig(IS_AFK, bool),
            p.getConfig(IS_SITTING, bool),
            PlayerNavigationData(
                p.getConfig(IS_NAVIGATING, bool),
                Location(
                    p.getConfig(NAV_X, int),
                    p.getConfig(NAV_Y, int),
                    p.getConfig(NAV_Z, int),
                    p.getConfig(NAV_WORLD, string)
                ),
                p.getConfig(NAV_IS_DIRECT, bool)
            ),
            PlayerActionbarCoordsColors(
                p.getConfig(ACTIONBAR_COORDS_COLOR_TEXT, color),
                p.getConfig(ACTIONBAR_COORDS_COLOR_COORDS, color)
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

            setDefaultConfig(p)
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
        ACTIONBAR_COORDS_COLOR_TEXT to Utils.Colors.YELLOW,
        ACTIONBAR_COORDS_COLOR_COORDS to Utils.Colors.GREEN,
    )

    @JvmStatic
    fun setDefaultConfig(p: ServerPlayerEntity) {
        for(pair in defaultPlayerConfig) {
            p.setConfig(pair.key, pair.value)
        }
    }
}