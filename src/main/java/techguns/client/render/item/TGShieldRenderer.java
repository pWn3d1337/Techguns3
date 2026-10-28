package techguns.client.render.item;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import techguns.TGIds;
import techguns.client.model.shield.TGShieldModel;
import techguns.client.render.CamoTextures;
import techguns.registry.TGArmors;

/**
 * Draws the Techguns shields (Techguns 2 TileEntityItemRendererTGShield); the item model is the vanilla
 * item/shield (builtin/entity), so display transforms and the blocking pose come from there.
 */
// MIGRATION(26.x): BlockEntityWithoutLevelRenderer / IClientItemExtensions#getCustomRenderer are replaced by special item model renderers in 1.21.4.
public final class TGShieldRenderer extends BlockEntityWithoutLevelRenderer {
    private final Supplier<TGShieldModel> model;
    private final ResourceLocation defaultTexture;

    private TGShieldRenderer(Supplier<TGShieldModel> model, String texture) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.model = model;
        this.defaultTexture = TGIds.id("textures/armors/" + texture + ".png");
    }

    public static void register(RegisterClientExtensionsEvent event) {
        register(event, TGArmors.RIOT_SHIELD.get(), new TGShieldRenderer(TGShieldModel.lazy(TGShieldModel::riot), "riot_shield"));
        register(event, TGArmors.BALLISTIC_SHIELD.get(), new TGShieldRenderer(TGShieldModel.lazy(TGShieldModel::ballistic), "ballistic_shield"));
        register(event, TGArmors.ADVANCED_SHIELD.get(), new TGShieldRenderer(TGShieldModel.lazy(TGShieldModel::advanced), "advanced_shield"));
        register(event, TGArmors.HEAVY_SHIELD.get(), new TGShieldRenderer(TGShieldModel.lazy(TGShieldModel::heavy), "heavy_shield"));
    }

    private static void register(RegisterClientExtensionsEvent event, Item item, TGShieldRenderer renderer) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, item);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        ResourceLocation texture = CamoTextures.of(stack).orElse(defaultTexture);
        TGShieldModel m = model.get();
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffer, m.renderType(texture), true, stack.hasFoil());
        m.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, -1);
        poseStack.popPose();
    }
}
