package tfar.tameableslimes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tfar.tameableslimes.init.ModEntityTypes;
import tfar.tameableslimes.platform.Services;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class TameableSlimes {

    public static final String MOD_ID = "tameableslimes";
    public static final String MOD_NAME = "TameableSlimes";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        Services.PLATFORM.registerAll(ModEntityTypes.class, BuiltInRegistries.ENTITY_TYPE, EntityType.class);
    }
}