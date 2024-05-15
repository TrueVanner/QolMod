package net.vannername.qol.utils

import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import net.vannername.qol.commands.SkipDayNight

class ServerConfig : Config(Utils.MyIdentifier("server_config")) {
    var skippingSettings = SkipDayNightConfig()
    @ClientModifiable
    class SkipDayNightConfig : ConfigSection() {
        @ClientModifiable
        var daysToSkip = ValidatedAny(SkipDayNight.SkipPeriod(0, false))
        var nightsToSkip = ValidatedAny(SkipDayNight.SkipPeriod(0, false))
    }
}