package net.vannername.qol.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import eu.pb4.playerdata.api.PlayerDataApi
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LodestoneTrackerComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtByte
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.vannername.qol.QoLMod.DATA_STORAGE
import net.vannername.qol.schemes.PlayerData
import net.vannername.qol.utils.ConfigUtils
import net.vannername.qol.utils.ConfigUtils.getConfig
import net.vannername.qol.utils.Utils
import net.vannername.qol.utils.Utils.getPlayerData
import net.vannername.qol.utils.Utils.multiColored
import java.awt.Color
import java.util.*

class Testing {
    init {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
            CommandManager.literal("vn_test")
                .executes(this::run)) }
    }

    @Throws(CommandSyntaxException::class)
    private fun run(context: CommandContext<ServerCommandSource>): Int {
        val p = context.source.playerOrThrow

        val compass = ItemStack(Items.COMPASS)
        compass.set(DataComponentTypes.LODESTONE_TRACKER, LodestoneTrackerComponent(Optional.of(GlobalPos.create(p.world.registryKey, BlockPos(0, 50, 0))), false))
        p.giveItemStack(compass)

        return 1
    }
}