package net.vannername.qol.schemes

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.vannername.qol.utils.ConfigUtils.ConfigProperties.*
import net.vannername.qol.utils.ConfigUtils.defaultPlayerConfig
import net.vannername.qol.utils.Location

class PlayerNavigationData(
    var isNavigating: Boolean = defaultPlayerConfig[IS_NAVIGATING] as Boolean,
    var location: Location = Location(
        defaultPlayerConfig[NAV_X] as Int,
        defaultPlayerConfig[NAV_Y] as Int,
        defaultPlayerConfig[NAV_Z] as Int,
        defaultPlayerConfig[NAV_WORLD] as String
    ),
    var isDirect: Boolean = defaultPlayerConfig[NAV_IS_DIRECT] as Boolean
) {

}
