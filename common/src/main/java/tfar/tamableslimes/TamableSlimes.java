package tfar.tamableslimes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ServerLevelAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tfar.tamableslimes.init.ModEntityTypes;
import tfar.tamableslimes.init.ModItems;
import tfar.tamableslimes.platform.Services;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class TamableSlimes {

    public static final String MOD_ID = "tameableslimes";
    public static final String MOD_NAME = "TamableSlimes";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        Services.PLATFORM.registerAll(ModEntityTypes.class, BuiltInRegistries.ENTITY_TYPE, EntityType.class);
        Services.PLATFORM.registerAll(ModItems.class, BuiltInRegistries.ITEM, Item.class);//this class MUST come after entitytypes!
    }

    public static boolean spawnConditions(EntityType<TamableSlime> pType, ServerLevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
        boolean b = Mob.checkMobSpawnRules(pType,pLevel,pSpawnType,pPos,pRandom);
        return b;
    }

    public static void onSlimeSplit(Slime oldSlime,Slime newSlime) {
        if (oldSlime instanceof TamableSlime oldTamableSlime && newSlime instanceof TamableSlime newTamableSlime) {
            newTamableSlime.setTame(oldTamableSlime.isTame());
            newTamableSlime.setOwnerUUID(oldTamableSlime.getOwnerUUID());
        }
    }
}