package tfar.tamableslimes;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;
import tfar.tamableslimes.client.TameableSlimesClientForge;
import tfar.tamableslimes.datagen.ModDatagen;
import tfar.tamableslimes.init.ModEntityTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mod(TamableSlimes.MOD_ID)
public class TameableSlimesForge {

    public static Map<Registry<?>, List<Pair<ResourceLocation, Supplier<?>>>> registerLater = new HashMap<>();

    public TameableSlimesForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);
        bus.addListener(this::register);
        bus.addListener(this::attributes);
        bus.addListener(this::spawnPlacement);
        bus.addListener(ModDatagen::start);
        if (FMLEnvironment.dist.isClient()) {
            bus.addListener(TameableSlimesClientForge::renderers);
        }
        TamableSlimes.init();
    }

    private void register(RegisterEvent e) {
        for (Map.Entry<Registry<?>,List<Pair<ResourceLocation, Supplier<?>>>> entry : registerLater.entrySet()) {
            Registry<?> registry = entry.getKey();
            List<Pair<ResourceLocation, Supplier<?>>> toRegister = entry.getValue();
            for (Pair<ResourceLocation,Supplier<?>> pair : toRegister) {
                e.register((ResourceKey<? extends Registry<Object>>)registry.key(),pair.getLeft(),(Supplier<Object>)pair.getValue());
            }
        }
    }

    private void spawnPlacement(SpawnPlacementRegisterEvent e) {
        e.register(ModEntityTypes.TAMABLE_SLIME, SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TamableSlimes::spawnConditions,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private void attributes(EntityAttributeCreationEvent e) {
        ModEntityTypes.initAttributes();
        ModEntityTypes.attributes.forEach(e::put);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        registerLater.clear();
        SlimeInteractions.bootstrap();
    }
}