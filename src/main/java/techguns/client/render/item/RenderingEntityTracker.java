package techguns.client.render.item;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/**
 * Remembers which living entity is currently being rendered, because item renderers don't get the holder.
 * First person: the local player. Third person: the entity between {@link RenderLivingEvent.Pre} and Post.
 */
// MIGRATION(26.x): entity render states carry the held item state; this tracker can then go away.
public final class RenderingEntityTracker {
    private static final Deque<LivingEntity> RENDERING = new ArrayDeque<>();

    private RenderingEntityTracker() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener((RenderLivingEvent.Pre<?, ?> event) -> RENDERING.push(event.getEntity()));
        gameBus.addListener((RenderLivingEvent.Post<?, ?> event) -> RENDERING.poll());
    }

    @Nullable
    static LivingEntity holder(ItemDisplayContext context) {
        return switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> Minecraft.getInstance().player;
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> RENDERING.peek();
            default -> null;
        };
    }
}
