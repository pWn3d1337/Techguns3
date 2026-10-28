package techguns.radiation;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import techguns.registry.TGMobEffects;

/**
 * Techguns 2 {@code ItemRadAway} / {@code ItemRadpills}: used like food (always, also when full), gives its radiation
 * effects without particles and leaves its container behind.
 */
public class RadMedicineItem extends Item {
    private static final int USE_DURATION = 32;

    private final UseAnim animation;
    private final Supplier<List<MobEffectInstance>> effects;
    private final Supplier<ItemStack> container;

    public RadMedicineItem(Properties properties, UseAnim animation, Supplier<List<MobEffectInstance>> effects, Supplier<ItemStack> container) {
        super(properties);
        this.animation = animation;
        this.effects = effects;
        this.container = container;
    }

    /** Techguns 2 rad away: rad regeneration XV for 20 s (-300 radiation), leaves an infusion bag. */
    public static List<MobEffectInstance> radAwayEffects() {
        return List.of(new MobEffectInstance(TGMobEffects.RAD_REGENERATION, 400, 14, false, false, true));
    }

    /** Techguns 2 rad pills: rad regeneration II for 25 s (-50) and rad resistance III for 3 minutes, leave a glass bottle. */
    public static List<MobEffectInstance> radPillsEffects() {
        return List.of(new MobEffectInstance(TGMobEffects.RAD_REGENERATION, 500, 1, false, false, true),
                new MobEffectInstance(TGMobEffects.RAD_RESISTANCE, 3600, 2, false, false, true));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return animation;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 0.5f,
                level.random.nextFloat() * 0.1f + 0.9f);
        if (!level.isClientSide) {
            for (MobEffectInstance effect : effects.get()) {
                entity.addEffect(new MobEffectInstance(effect));
            }
        }
        if (entity instanceof Player player) {
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
            if (player.getAbilities().instabuild) {
                return stack;
            }
            stack.shrink(1);
            ItemStack rest = container.get();
            if (stack.isEmpty()) {
                return rest;
            }
            if (!player.getInventory().add(rest)) {
                player.drop(rest, false);
            }
            return stack;
        }
        stack.shrink(1);
        return stack;
    }

    /** Level numerals (vanilla has potency names only up to VI). */
    public static String roman(int level) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] numerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (level >= values[i]) {
                out.append(numerals[i]);
                level -= values[i];
            }
        }
        return out.toString();
    }

    /** Techguns 2: each effect with its level and what it does (radiation removed, or the duration). */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        for (MobEffectInstance effect : effects.get()) {
            Component name = Component.translatable(effect.getDescriptionId()).append(" " + roman(effect.getAmplifier() + 1));
            String detail = effect.is(TGMobEffects.RAD_REGENERATION)
                    ? "-" + (effect.getAmplifier() + 1) * (effect.getDuration() / 20)
                    : effect.getDuration() / 20 + "s";
            tooltip.add(Component.empty().append(name).append(" (" + detail + ")").withStyle(ChatFormatting.BLUE));
        }
    }
}
