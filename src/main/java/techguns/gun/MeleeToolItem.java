package techguns.gun;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import techguns.core.damage.DeathType;
import techguns.registry.TGComponents;

/**
 * Powered melee tool (Techguns 2 {@code GenericGunMeleeCharge}): chainsaw, mining drill, power hammer, shishkebap.
 * Left click attacks and mines like a tool while it has fuel; every hit and every mined block uses one round. Without
 * fuel it is a weak club that mines like a hand. Mining heads (an item data component) raise the mining level, speed
 * and damage. The use key does what the tool's {@link UseAction} says.
 */
public class MeleeToolItem extends ChargeGunItem implements techguns.item.TGMeleeWeapon {
    /** Techguns 2 used the sword attack speed for all melee tools (Fabric's positive values were a port mistake). */
    private static final double ATTACK_SPEED = -2.4;

    /** What the use key does. */
    public enum UseAction {
        /** Shoot the tool's short-range projectile (chainsaw, drill). */
        SHOOT,
        /** Charge and release a charged projectile (power hammer). */
        CHARGE,
        /** Nothing (shishkebap). */
        NONE
    }

    /**
     * A mining head: the item installed on the tool and its bonuses over the base head.
     *
     * @param levelBonus mining level bonus (1 = one tier higher)
     */
    public record MiningHead(Supplier<? extends Item> item, int levelBonus, double damageBonus, float speedBonus, ChatFormatting color) {
        public MiningHead(Supplier<? extends Item> item, ChatFormatting color) {
            this(item, 0, 0.0, 0.0f, color);
        }
    }

    private final float meleeDamage;
    private final float meleeDamageUnpowered;
    private final float miningSpeed;
    private final List<MiningHead> heads;
    private final List<Pair<TagKey<Block>, Integer>> toolLevels = new ArrayList<>();
    private final UseAction useAction;
    private int miningRadius;
    private DeathType meleeDeathType = DeathType.GORE;
    private float meleeGoreChance = 1.0f;
    private boolean meleeIgnites;
    /** Knockback of a powered hit (Fabric {@code PowerHammer}: 1.5). */
    private float meleeKnockback = 1.0f;
    @Nullable
    private Supplier<SoundEvent> hitSound;

    public MeleeToolItem(Properties properties, GunStats stats, GunSounds sounds, ProjectileSelector projectiles, float fullChargeTicks,
                         int ammoPerFullCharge, float meleeDamage, float meleeDamageUnpowered, float miningSpeed, List<MiningHead> heads,
                         UseAction useAction) {
        super(properties, stats, sounds, projectiles, fullChargeTicks, ammoPerFullCharge);
        this.meleeDamage = meleeDamage;
        this.meleeDamageUnpowered = meleeDamageUnpowered;
        this.miningSpeed = miningSpeed;
        this.heads = heads;
        this.useAction = useAction;
    }

    /** Mines {@code tag} blocks up to the vanilla tool tier {@code level} (0 wood, 1 stone, 2 iron, 3 diamond, 4 netherite). */
    public MeleeToolItem toolLevel(TagKey<Block> tag, int level) {
        toolLevels.add(Pair.of(tag, level));
        return this;
    }

    /** Mines a (2r+1)² plane around the target block unless sneaking (mining drill). */
    public MeleeToolItem miningRadius(int radius) {
        this.miningRadius = radius;
        return this;
    }

    public MeleeToolItem meleeDeath(DeathType deathType, float goreChance) {
        this.meleeDeathType = deathType;
        this.meleeGoreChance = goreChance;
        return this;
    }

    /** Powered hits set the target on fire (shishkebap). */
    public MeleeToolItem meleeKnockback(float multiplier) {
        this.meleeKnockback = multiplier;
        return this;
    }

    /** Knockback multiplier of a hit with this stack (unpowered: normal). */
    public float meleeKnockback(ItemStack stack) {
        return isPowered(stack) ? meleeKnockback : 1.0f;
    }

    public MeleeToolItem meleeIgnites() {
        this.meleeIgnites = true;
        return this;
    }

    public MeleeToolItem withHitSound(Supplier<SoundEvent> sound) {
        this.hitSound = sound;
        return this;
    }

    // --- Fuel and heads ---

    public boolean isPowered(ItemStack stack) {
        return GunData.loaded(stack) > 0;
    }

    public List<MiningHead> heads() {
        return heads;
    }

    public int headLevel(ItemStack stack) {
        int level = stack.getOrDefault(TGComponents.MINING_HEAD.get(), 0);
        return level >= 0 && level < heads.size() ? level : 0;
    }

    public void setHeadLevel(ItemStack stack, int level) {
        stack.set(TGComponents.MINING_HEAD.get(), level);
    }

    /** Head level of a head item for this tool, or -1. */
    public int headLevelFor(ItemStack headItem) {
        for (int i = 0; i < heads.size(); i++) {
            if (headItem.is(heads.get(i).item().get())) {
                return i;
            }
        }
        return -1;
    }

    /** No bonuses, for tools without mining heads (shishkebap). */
    private static final MiningHead NO_HEAD = new MiningHead(() -> Items.AIR, ChatFormatting.GRAY);

    private MiningHead head(ItemStack stack) {
        return heads.isEmpty() ? NO_HEAD : heads.get(headLevel(stack));
    }

    // --- Melee ---

    @Override
    public boolean shootsWithLeftClick() {
        return false;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        double damage = isPowered(stack) ? meleeDamage + head(stack).damageBonus() : meleeDamageUnpowered;
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (isPowered(stack)) {
            if (hitSound != null) {
                attacker.level().playSound(null, target.getX(), target.getY(), target.getZ(), hitSound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            if (meleeIgnites && !target.fireImmune()) {
                target.igniteForSeconds(3);
            }
            useFuel(stack, attacker);
        }
        return true;
    }

    /** Death effect of a powered melee kill (Techguns 2 {@code getMeleeDamageSource}); see {@code TGDamageEvents}. */
    public DeathType meleeDeathType(ItemStack stack) {
        return isPowered(stack) ? meleeDeathType : DeathType.DEFAULT;
    }

    public float meleeGoreChance() {
        return meleeGoreChance;
    }

    // --- Mining ---

    private boolean isEffectiveOn(BlockState state) {
        return toolLevels.stream().anyMatch(entry -> state.is(entry.getFirst()));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isPowered(stack) && isEffectiveOn(state) ? miningSpeed + head(stack).speedBonus() : 1.0f;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (!isPowered(stack)) {
            return false;
        }
        int bonus = head(stack).levelBonus();
        return toolLevels.stream().anyMatch(entry -> state.is(entry.getFirst()) && !state.is(incorrectFor(entry.getSecond() + bonus)));
    }

    /** Vanilla "incorrect for tier" tag of a mining level. */
    private static TagKey<Block> incorrectFor(int level) {
        return switch (Math.max(0, level)) {
            case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            default -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        };
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (isPowered(stack) && state.getDestroySpeed(level, pos) != 0.0f) {
            useFuel(stack, miner);
        }
        return true;
    }

    /** Radius of the plane mined around the target; 0 = single block. */
    public int miningRadius(Player player) {
        return player.isShiftKeyDown() ? 0 : miningRadius;
    }

    private static void useFuel(ItemStack stack, LivingEntity user) {
        if (!(user instanceof Player player) || !player.isCreative()) {
            GunData.consume(stack, 1);
        }
    }

    // --- Use key ---

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return switch (useAction) {
            case CHARGE -> super.use(level, player, hand);
            case SHOOT -> {
                // shoots (or reloads) every time the held use key repeats
                shoot(stack, level, player, hand, false, null);
                yield InteractionResultHolder.fail(stack);
            }
            default -> InteractionResultHolder.pass(stack);
        };
    }

    /**
     * Techguns 2 {@code GenericGunMeleeCharge.addMiningTooltip}: the tool classes; with shift the mining head, the mining
     * level per tool class, the break speed and the mining area.
     */
    @Override
    protected void appendToolTooltip(ItemStack stack, List<Component> tooltip, boolean detailed) {
        if (!detailed) {
            MutableComponent classes = Component.empty();
            for (int i = 0; i < toolLevels.size(); i++) {
                classes.append(i > 0 ? Component.literal(", ") : Component.empty()).append(toolClassName(toolLevels.get(i).getFirst()));
            }
            tooltip.add(GunTooltip.line("tooltip.techguns.tool.classes", classes));
            return;
        }
        MiningHead head = head(stack);
        if (!heads.isEmpty()) {
            tooltip.add(GunTooltip.line("tooltip.techguns.tool.mining_head", head.item().get().getDescription().copy().withStyle(head.color())));
        }
        tooltip.add(GunTooltip.line("tooltip.techguns.tool.classes", Component.empty()));
        for (Pair<TagKey<Block>, Integer> entry : toolLevels) {
            tooltip.add(GunTooltip.line("tooltip.techguns.tool.class_level", toolClassName(entry.getFirst()), entry.getSecond() + head.levelBonus()));
        }
        tooltip.add(GunTooltip.line("tooltip.techguns.tool.break_speed", GunTooltip.number(miningSpeed + head.speedBonus())));
        if (miningRadius > 0) {
            int size = miningRadius * 2 + 1;
            tooltip.add(GunTooltip.line("tooltip.techguns.tool.mining_radius", size, size));
        }
    }

    /** Techguns 2 tool class names for the mineable tags (sword for the sword-efficient blocks). */
    private static Component toolClassName(TagKey<Block> tag) {
        String path = tag.location().getPath();
        return Component.translatable("techguns.tool_class." + path.substring(path.lastIndexOf('/') + 1).replace("_efficient", ""));
    }
}
