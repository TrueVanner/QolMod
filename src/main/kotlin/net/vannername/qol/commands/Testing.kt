package net.vannername.qol.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LodestoneTrackerComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
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