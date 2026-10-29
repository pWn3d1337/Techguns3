package techguns.client.render.block;

import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import techguns.TGIds;
import techguns.client.model.machine.MachineModel;
import techguns.client.model.machine.MetalPressModel;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGBlocks;

/** Block entity and item renderers of the machines and the 3x3 doors. */
public final class TGMachineRenderers {
    private static final TGMachineRenderer<?> METAL_PRESS = new TGMachineRenderer<>(lazy(MetalPressModel::bake),
            TGIds.id("textures/block/metal_press.png"));
    private static final TGMachineRenderer<?> AMMO_PRESS = new TGMachineRenderer<>(lazy(techguns.client.model.machine.AmmoPressModel::bake),
            TGIds.id("textures/block/ammo_press.png"));
    private static final TGMachineRenderer<?> CHEM_LAB = new TGMachineRenderer<>(lazy(techguns.client.model.machine.ChemLabModel::bake),
            TGIds.id("textures/block/chem_lab.png"));

    private TGMachineRenderers() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(TGMachineRenderers::registerRenderers);
        modEventBus.addListener(TGMachineRenderers::registerItems);
        modEventBus.addListener(Door3x3Renderer::registerAdditional);
    }

    @SuppressWarnings("unchecked")
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TGBlockEntities.METAL_PRESS.get(), context -> (TGMachineRenderer<techguns.machine.MetalPressBlockEntity>) METAL_PRESS);
        event.registerBlockEntityRenderer(TGBlockEntities.AMMO_PRESS.get(), context -> (TGMachineRenderer<techguns.machine.AmmoPressBlockEntity>) AMMO_PRESS);
        event.registerBlockEntityRenderer(TGBlockEntities.GRINDER.get(), GrinderRenderer::new);
        event.registerBlockEntityRenderer(TGBlockEntities.CHEM_LAB.get(), context -> (TGMachineRenderer<techguns.machine.ChemLabBlockEntity>) CHEM_LAB);
        event.registerBlockEntityRenderer(TGBlockEntities.CHARGING_STATION.get(), ChargingStationRenderer::new);
        event.registerBlockEntityRenderer(TGBlockEntities.BLAST_FURNACE.get(), BlastFurnaceRenderer::new);
        event.registerBlockEntityRenderer(TGBlockEntities.FABRICATOR.get(), FabricatorRenderer::new);
        event.registerBlockEntityRenderer(TGBlockEntities.REACTION_CHAMBER.get(), ReactionChamberRenderer::new);
        event.registerBlockEntityRenderer(TGBlockEntities.ORE_DRILL.get(), OreDrillRenderer::new);
        event.registerBlockEntityRenderer(TGBlockEntities.TURRET.get(), TurretBaseRenderer::new);
        event.registerBlockEntityRenderer(TGBlockEntities.DOOR_3X3.get(), Door3x3Renderer::new);
    }

    private static void registerItems(RegisterClientExtensionsEvent event) {
        TGMachineItemRenderer.register(event, TGBlocks.METAL_PRESS.get(), METAL_PRESS);
        TGMachineItemRenderer.register(event, TGBlocks.AMMO_PRESS.get(), AMMO_PRESS);
        TGMachineItemRenderer.register(event, TGBlocks.CHEM_LAB.get(), CHEM_LAB);
        TGMachineItemRenderer.register(event, TGBlocks.TURRET.get(),
                (poseStack, buffer, light, overlay) -> TurretBaseRenderer.renderBase(poseStack, buffer, light, overlay, false, 0));
    }

    /** Bakes on first use (after resource loading). */
    private static <M extends MachineModel> Supplier<M> lazy(Supplier<M> bake) {
        return new Supplier<>() {
            private M baked;

            @Override
            public M get() {
                if (baked == null) {
                    baked = bake.get();
                }
                return baked;
            }
        };
    }
}
