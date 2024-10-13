package me.vannername.qol.clientutils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;

public abstract class GlobalMixinVariables {
    private static boolean b_playerEnteredServer = false;
    private static PlayerEntity player = null;
//    private static PlayerConfig playerConfig = null;

    public static PlayerEntity getClientPlayer() throws NullPointerException {
        if (player == null) {
            player = MinecraftClient.getInstance().player;
        }
        return player;
    }

//    public static PlayerConfig getPlayerConfig() throws NullPointerException {
//        if (playerConfig == null) {
//            playerConfig = QoLMod.playerConfigs.get(getClientPlayer().getUuid());
//        }
//        return playerConfig;
//    }

    public static Screen currentScreen() {
        return MinecraftClient.getInstance().currentScreen;
    }

    public static boolean playerEnteredServer() {
        return b_playerEnteredServer;
    }

    public static void setPlayerEnteredServer(boolean playerEnteredServer) {
        GlobalMixinVariables.b_playerEnteredServer = playerEnteredServer;
    }
}
