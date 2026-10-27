package techguns.client.render.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import techguns.TGIds;
import techguns.client.particle.ClientFx;
import techguns.machine.MachineClientEffects;
import techguns.registry.TGFluids;

/** Client side of the Techguns fluids (textures, translucent layer) and the machine effects hook. */
public final class TGFluidClient {
    private static final ResourceLocation ACID_STILL = TGIds.id("block/acid_still");
    private static final ResourceLocation ACID_FLOW = TGIds.id("block/acid_flow");

    private TGFluidClient() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(TGFluidClient::registerExtensions);
        modEventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            // MIGRATION(26.x): fluid render layers move to the fluid model / chunk section layer API.
            ItemBlockRenderTypes.setRenderLayer(TGFluids.ACID.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TGFluids.ACID_FLOWING.get(), RenderType.translucent());
        }));
        MachineClientEffects.install(new MachineClientEffects() {
            @Override
            public void spawnFx(String name, net.minecraft.world.level.Level level, double x, double y, double z) {
                if (level instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel) {
                    ClientFx.createFX(name, clientLevel, x, y, z, 0, 0, 0, 0f, 0f, 1.0f);
                }
            }
        });
    }

    private static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ACID_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ACID_FLOW;
            }
        }, TGFluids.ACID_TYPE.get());
    }
}
