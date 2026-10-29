package techguns.client.render.slot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import techguns.armor.TGArmorItem;
import techguns.client.model.slot.SlotGearModel;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotType;

/**
 * Draws the worn extra-slot gear (face, back, hand) on players (Techguns 2 {@code TGLayerRendererer}). Added to both
 * the default and the slim player renderer.
 */
// MIGRATION(26.x): RenderLayer / AddLayers are replaced by render states and layer submit calls in 1.21.2+.
public final class SlotGearLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final boolean slim;

    private SlotGearLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, boolean slim) {
        super(parent);
        this.slim = slim;
    }

    public static void register(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            Object renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new SlotGearLayer(playerRenderer, skin == PlayerSkin.Model.SLIM));
            }
        }
    }

    /**
     * Whether the slot's gear is drawn: not when the player switched the slot's visibility off in Curios, and not when
     * the worn armor hides it: powered helmets hide face gear, chest pieces hide back gear and gloves (Techguns 2
     * {@code isSlotVisible}).
     */
    public static boolean isSlotVisible(Player player, TGSlotType slot) {
        if (!ExtraSlots.isRendered(player, slot)) {
            return false;
        }
        switch (slot) {
            case FACE:
                return !(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof TGArmorItem armor && armor.hidesFaceSlot());
            case BACK:
                return !(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TGArmorItem armor && armor.hidesBackSlot());
            case HAND:
                return !(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TGArmorItem armor && armor.hidesHandSlot());
            default:
                return true;
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isSpectator()) {
            return;
        }
        for (TGSlotType slot : TGSlotType.values()) {
            ItemStack stack = ExtraSlots.get(player, slot);
            if (stack.isEmpty() || !isSlotVisible(player, slot)) {
                continue;
            }
            SlotGearRegistry.Entry entry = SlotGearRegistry.get(stack);
            if (entry == null) {
                continue;
            }
            SlotGearModel model = entry.model(slim);
            model.follow(getParentModel());
            if (entry.prepare() != null) {
                entry.prepare().accept(model, player);
            }
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(SlotGearRegistry.texture(entry, stack, slim)));
            model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
