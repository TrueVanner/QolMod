import net.fabricmc.fabric.impl.gametest.FabricGameTestHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.test.GameTest
import net.vannername.qol.schemes.PlayerData
import net.vannername.qol.utils.ConfigUtils
import net.vannername.qol.utils.Utils.getPlayerData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PropertiesTest {
    @Test
    fun `All default properties defined`() {
        assertThat(PlayerData()).hasNoNullFieldsOrProperties()
        assertThat(ConfigUtils.defaultPlayerConfig.keys).containsExactlyInAnyOrderElementsOf(ConfigUtils.ConfigProperty.entries)
    }
}