package me.vannername.qol.main.config

import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.vannername.qol.main.commands.skipdaynight.SkipDayNightUtils
import me.vannername.qol.main.utils.Utils

class ServerConfig : Config(Utils.MyIdentifier("server_config")) {
    @ClientModifiable
    var skippingSettings = SkipDayNightConfig()

    class SkipDayNightConfig : ConfigSection() {
        @ClientModifiable
        var daysToSkip = SkipDayNightUtils.SkipPeriod(0, false)

        @ClientModifiable
        var nightsToSkip = SkipDayNightUtils.SkipPeriod(0, false)
    }
}