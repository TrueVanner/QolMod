package net.vannername.qol.tests

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import net.vannername.qol.utils.Utils.getPlayerData

class GameTests {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun playerTest(ctx: TestContext) {
        val p = ctx.world.players[0]
        p.getPlayerData()
        ctx.complete()
    }
}