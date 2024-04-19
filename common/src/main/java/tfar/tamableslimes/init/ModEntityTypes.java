package tfar.tamableslimes.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import tfar.tamableslimes.TamableSlime;

import java.util.HashMap;
import java.util.Map;

public class ModEntityTypes {

    public static Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributes = new HashMap<>();
    public static final EntityType<TamableSlime> TAMABLE_SLIME = EntityType.Builder.of(TamableSlime::new, MobCategory.CREATURE).sized(2.04F, 2.04F)
            .clientTrackingRange(10).build("");

    public static void initAttributes() {
        attributes.put(TAMABLE_SLIME, TamableSlime.createAttributes().build());
    }
}
