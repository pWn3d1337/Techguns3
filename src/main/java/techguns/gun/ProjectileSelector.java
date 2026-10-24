package techguns.gun;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import techguns.gun.ammo.AmmoType;

/**
 * Ammo type of a gun plus the projectile factory for each of its variants (in variant order).
 */
public final class ProjectileSelector {
    private final AmmoType ammoType;
    private final Map<String, ProjectileFactory<?>> factories = new HashMap<>();

    public ProjectileSelector(AmmoType ammoType, ProjectileFactory<?>... factoriesInVariantOrder) {
        this.ammoType = ammoType;
        List<AmmoType.Variant> variants = ammoType.variants();
        if (factoriesInVariantOrder.length < variants.size()) {
            throw new IllegalArgumentException("Need one projectile factory per ammo variant: " + variants.size());
        }
        for (int i = 0; i < variants.size(); i++) {
            factories.put(variants.get(i).key(), factoriesInVariantOrder[i]);
        }
    }

    public AmmoType ammoType() {
        return ammoType;
    }

    public ProjectileFactory<?> factory(String variantKey) {
        ProjectileFactory<?> factory = factories.get(variantKey);
        return factory != null ? factory : factories.get(AmmoType.DEFAULT);
    }
}
