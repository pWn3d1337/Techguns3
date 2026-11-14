package techgunsdev;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Dev client only: gives the local player a fixed default skin, so first-person arms are tested with a known arm width.
 * The offline profile {@code Dev} would get the slim Efe skin (3 px arms). Default {@code wide/efe} (same look, 4 px
 * arms); the system property {@code techguns_dev.skin} picks another vanilla default skin ({@code slim/alex},
 * {@code wide/steve}, ...) or {@code none} to keep the profile's own.
 */
final class DevSkin {
    private static final String SKIN = System.getProperty("techguns_dev.skin", "wide/efe");
    /** Player infos already given the skin (a new one comes with every join). */
    private static final Set<PlayerInfo> DONE = Collections.newSetFromMap(new WeakHashMap<>());

    private DevSkin() {
    }

    static void init() {
        if (!"none".equals(SKIN)) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> apply());
        }
    }

    private static void apply() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        if (info == null || !DONE.add(info)) {
            return;
        }
        PlayerSkin skin = new PlayerSkin(ResourceLocation.withDefaultNamespace("textures/entity/player/" + SKIN + ".png"), null, null, null,
                SKIN.startsWith("slim/") ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE, true);
        // the skin is a private final supplier of PlayerInfo; dev only, so reflection instead of an access transformer
        ObfuscationReflectionHelper.setPrivateValue(PlayerInfo.class, info, (Supplier<PlayerSkin>) () -> skin, "skinLookup");
        LogUtils.getLogger().info("Dev skin {} for {}: {}", SKIN, mc.player.getName().getString(), mc.player.getSkin().model());
    }
}
