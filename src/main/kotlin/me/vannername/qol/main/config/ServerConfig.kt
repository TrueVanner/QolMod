package me.vannername.qol.main.config

import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.vannername.qol.main.commands.SkipDayNight
import me.vannername.qol.main.utils.Utils

class ServerConfig : Config(Utils.MyIdentifier("server_config")) {
    @ClientModifiable
    var skippingSettings = SkipDayNightConfig()

    class SkipDayNightConfig : ConfigSection() {
        @ClientModifiable
        var daysToSkip = ValidatedAny(SkipDayNight.SkipPeriod(0, false))

        @ClientModifiable
        var nightsToSkip = ValidatedAny(SkipDayNight.SkipPeriod(0, false))
    }
}