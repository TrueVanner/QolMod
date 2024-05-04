package net.vannername.qol.tests

import jdk.incubator.vector.VectorOperators.Test
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import net.vannername.qol.schemes.PlayerData
import net.vannername.qol.utils.ConfigUtils
import net.vannername.qol.utils.ConfigUtils.getConfig
import net.vannername.qol.utils.Location
import net.vannername.qol.utils.Utils
import net.vannername.qol.utils.Utils.getPlayerData
import org.apache.logging.log4j.core.jmx.Server
import org.assertj.core.api.Assertions.assertThat

class GameTests {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun test_config(ctx: TestContext) {
        val p = ctx.world.players[0]

        setDefaultPlayerConfig(ctx, p)
        loadCustomData(ctx, p)
        setConfigFromCustomData(ctx, p)

        // restore after tests
        ConfigUtils.setDefaultConfig(p)

        ctx.complete()
    }

    private fun setDefaultPlayerConfig(ctx: TestContext, p: ServerPlayerEntity) {
        ConfigUtils.setDefaultConfig(p)
        for(prop in ConfigUtils.defaultPlayerConfig) {
            ctx.assertTrue(p.getConfig(prop.key, prop.key.type) == prop.value, "Property ${prop.key.text} is not equal to default value ${prop.value}")
        }
    }

    private fun loadCustomData(ctx: TestContext, p: ServerPlayerEntity) {
        ConfigUtils.loadCustomDataFromConfig(p)
        assertThat(p.getPlayerData()).hasNoNullFieldsOrProperties()
        assertThat(p.getPlayerData()).usingRecursiveComparison().isEqualTo(PlayerData())
    }

    private fun setConfigFromCustomData(ctx: TestContext, p: ServerPlayerEntity) {
        val data = p.getPlayerData()

        data.sendDeathCoords = !data.sendDeathCoords
        data.navData.location = Location(Math.random() * 150, Math.random() * 150, Math.random() * 150, "the_nether")
        data.actionbarCoordsColors.text = Utils.Colors.entries[(Math.random() * Utils.Colors.entries.size).toInt()]

        ConfigUtils.setConfigFromCustomData(p, p.getPlayerData())
        ConfigUtils.createAndLoadCustomData(p)

        assertThat(p.getPlayerData()).usingRecursiveComparison().isEqualTo(data)
    }
}