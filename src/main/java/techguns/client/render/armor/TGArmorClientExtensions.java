package techguns.client.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import techguns.armor.TGArmorItem;
import techguns.client.model.armor.TGArmorModels;
import techguns.registry.TGArmors;

/** Swaps in the custom armor model of Techguns armor pieces that have one (NeoForge copies the pose onto it). */
// MIGRATION(26.x): getHumanoidArmorModel is replaced by equipment-asset based armor rendering in 1.21.2+.
public final class TGArmorClientExtensions implements IClientItemExtensions {
    private static final TGArmorClientExtensions INSTANCE = new TGArmorClientExtensions();

    private TGArmorClientExtensions() {
    }

    public static void register(RegisterClientExtensionsEvent event) {
        TGArmors.armors().forEach(item -> {
            if (item.get().armorModel() != null) {
                event.registerItem(INSTANCE, item.get());
            }
        });
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        if (stack.getItem() instanceof TGArmorItem armor && armor.armorModel() != null) {
            HumanoidModel<LivingEntity> model = TGArmorModels.get(armor.armorModel());
            if (model != null) {
                return model;
            }
        }
        return original;
    }
}
