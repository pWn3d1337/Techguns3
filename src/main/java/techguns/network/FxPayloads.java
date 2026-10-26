package techguns.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import techguns.TGIds;
import techguns.core.damage.DeathType;
import techguns.core.fx.EntityCondition;

/**
 * Payloads of the FX system (particle systems, bullet impacts). All server to client.
 */
public final class FxPayloads {
    private FxPayloads() {
    }

    /** Spawn the particle system {@code name} at a position. */
    public record SpawnFx(String name, double x, double y, double z, double motionX, double motionY, double motionZ,
                          float pitch, float yaw, float scale) implements CustomPacketPayload {
        public static final Type<SpawnFx> TYPE = new Type<>(TGIds.id("spawn_fx"));
        public static final StreamCodec<ByteBuf, SpawnFx> STREAM_CODEC = StreamCodec.of(SpawnFx::write, SpawnFx::read);

        public SpawnFx(String name, double x, double y, double z) {
            this(name, x, y, z, 0, 0, 0, 0f, 0f, 1.0f);
        }

        public SpawnFx(String name, double x, double y, double z, double motionX, double motionY, double motionZ) {
            this(name, x, y, z, motionX, motionY, motionZ, 0f, 0f, 1.0f);
        }

        private static void write(ByteBuf buf, SpawnFx p) {
            ByteBufCodecs.STRING_UTF8.encode(buf, p.name);
            buf.writeDouble(p.x).writeDouble(p.y).writeDouble(p.z);
            buf.writeDouble(p.motionX).writeDouble(p.motionY).writeDouble(p.motionZ);
            buf.writeFloat(p.pitch).writeFloat(p.yaw).writeFloat(p.scale);
        }

        private static SpawnFx read(ByteBuf buf) {
            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            double mx = buf.readDouble();
            double my = buf.readDouble();
            double mz = buf.readDouble();
            float pitch = buf.readFloat();
            float yaw = buf.readFloat();
            float scale = buf.readFloat();
            return new SpawnFx(name, x, y, z, mx, my, mz, pitch, yaw, scale);
        }

        @Override
        public Type<SpawnFx> type() {
            return TYPE;
        }
    }

    /** Spawn the particle system {@code name} attached to an entity. */
    public record SpawnFxOnEntity(String name, int entityId, float offsetX, float offsetY, float offsetZ, boolean attachToHead,
                                  byte condition) implements CustomPacketPayload {
        public static final Type<SpawnFxOnEntity> TYPE = new Type<>(TGIds.id("spawn_fx_on_entity"));
        public static final StreamCodec<ByteBuf, SpawnFxOnEntity> STREAM_CODEC = StreamCodec.of(SpawnFxOnEntity::write, SpawnFxOnEntity::read);

        public SpawnFxOnEntity(String name, int entityId) {
            this(name, entityId, 0f, 0f, 0f, false, EntityCondition.NONE);
        }

        public SpawnFxOnEntity(String name, int entityId, float offsetX, float offsetY, float offsetZ, boolean attachToHead,
                               EntityCondition condition) {
            this(name, entityId, offsetX, offsetY, offsetZ, attachToHead, condition.id);
        }

        public EntityCondition entityCondition() {
            return EntityCondition.fromByte(condition);
        }

        private static void write(ByteBuf buf, SpawnFxOnEntity p) {
            ByteBufCodecs.STRING_UTF8.encode(buf, p.name);
            ByteBufCodecs.VAR_INT.encode(buf, p.entityId);
            buf.writeFloat(p.offsetX).writeFloat(p.offsetY).writeFloat(p.offsetZ);
            buf.writeBoolean(p.attachToHead);
            buf.writeByte(p.condition);
        }

        private static SpawnFxOnEntity read(ByteBuf buf) {
            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
            int entityId = ByteBufCodecs.VAR_INT.decode(buf);
            float ox = buf.readFloat();
            float oy = buf.readFloat();
            float oz = buf.readFloat();
            boolean head = buf.readBoolean();
            byte condition = buf.readByte();
            return new SpawnFxOnEntity(name, entityId, ox, oy, oz, head, condition);
        }

        @Override
        public Type<SpawnFxOnEntity> type() {
            return TYPE;
        }
    }

    /**
     * A living entity died with a special death effect; {@code motion} is its velocity at death
     * (Techguns 2 / Fabric {@code PacketEntityDeathType}).
     */
    public record EntityDeathFx(int entityId, DeathType deathType, float motionX, float motionY, float motionZ)
            implements CustomPacketPayload {
        public static final Type<EntityDeathFx> TYPE = new Type<>(TGIds.id("entity_death_fx"));
        public static final StreamCodec<ByteBuf, EntityDeathFx> STREAM_CODEC = StreamCodec.of(EntityDeathFx::write, EntityDeathFx::read);

        private static void write(ByteBuf buf, EntityDeathFx p) {
            ByteBufCodecs.VAR_INT.encode(buf, p.entityId);
            buf.writeByte(p.deathType.ordinal());
            buf.writeFloat(p.motionX).writeFloat(p.motionY).writeFloat(p.motionZ);
        }

        private static EntityDeathFx read(ByteBuf buf) {
            int entityId = ByteBufCodecs.VAR_INT.decode(buf);
            int type = buf.readByte();
            DeathType deathType = type >= 0 && type < DeathType.values().length ? DeathType.values()[type] : DeathType.DEFAULT;
            float mx = buf.readFloat();
            float my = buf.readFloat();
            float mz = buf.readFloat();
            return new EntityDeathFx(entityId, deathType, mx, my, mz);
        }

        @Override
        public Type<EntityDeathFx> type() {
            return TYPE;
        }
    }

    /**
     * A bullet hit a block: {@code soundType} selects sound and particles (0 stone, 1 wood, 2 glass, 3 metal,
     * 4 gravel/sand, 5 other = sound only, -1 default), pitch and yaw orient the effect along the hit face.
     */
    public record GunImpactFx(short soundType, double x, double y, double z, float pitch, float yaw, boolean incendiary, boolean blue)
            implements CustomPacketPayload {
        public static final Type<GunImpactFx> TYPE = new Type<>(TGIds.id("gun_impact_fx"));
        public static final StreamCodec<ByteBuf, GunImpactFx> STREAM_CODEC = StreamCodec.of(GunImpactFx::write, GunImpactFx::read);

        private static void write(ByteBuf buf, GunImpactFx p) {
            buf.writeShort(p.soundType);
            buf.writeDouble(p.x).writeDouble(p.y).writeDouble(p.z);
            buf.writeFloat(p.pitch).writeFloat(p.yaw);
            buf.writeBoolean(p.incendiary);
            buf.writeBoolean(p.blue);
        }

        private static GunImpactFx read(ByteBuf buf) {
            short soundType = buf.readShort();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            float pitch = buf.readFloat();
            float yaw = buf.readFloat();
            boolean incendiary = buf.readBoolean();
            boolean blue = buf.readBoolean();
            return new GunImpactFx(soundType, x, y, z, pitch, yaw, incendiary, blue);
        }

        @Override
        public Type<GunImpactFx> type() {
            return TYPE;
        }
    }
}
