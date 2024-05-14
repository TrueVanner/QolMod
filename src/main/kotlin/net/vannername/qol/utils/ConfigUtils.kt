package net.vannername.qol.utils

import me.fzzyhmstrs.fzzy_config.validation.ValidatedField
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedColor
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import java.awt.Color

object ConfigUtils {

    class TrValidatedBoolean(value: Boolean, val trText: MutableText) : ValidatedBoolean(value) {
        constructor(value: Boolean, trString: String) : this(value, Text.literal(trString))
        override fun translation(fallback: String?): MutableText {
            return trText
        }
    }

    class TrValidatedInt(value: Int, val trText: MutableText) : ValidatedInt(value) {
        constructor(value: Int, trString: String) : this(value, Text.literal(trString))
        override fun translation(fallback: String?): MutableText {
            return trText
        }
    }

    class TrValidatedColor(value: Utils.Colors, val trText: MutableText) : ValidatedColor(value.c, false) {
        constructor(value: Utils.Colors, trString: String) : this(value, Text.literal(trString))
        override fun translation(fallback: String?): MutableText {
            return trText
        }
    }

    class TrValidatedAny<T: Any> (value: T, val trText: MutableText) : ValidatedAny<T>(value) {
        constructor(value: T, trString: String) : this(value, Text.literal(trString))
        override fun translation(fallback: String?): MutableText {
            return trText
        }
    }
}