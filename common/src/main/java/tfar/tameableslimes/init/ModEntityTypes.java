package tfar.tameableslimes.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import tfar.tameableslimes.TameableSlime;

import java.util.HashMap;
import java.util.Map;

public class ModEntityTypes {

    public static Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributes = new HashMap<>();
    public static final EntityType<TameableSlime> TAMEABLE_SLIME = EntityType.Builder.of(TameableSlime::new, MobCategory.CREATURE).sized(2.04F, 2.04F)
            .clientTrackingRange(10).build("");

    static {
        attributes.put(TAMEABLE_SLIME, TameableSlime.createAttributes().build());
    }

}
