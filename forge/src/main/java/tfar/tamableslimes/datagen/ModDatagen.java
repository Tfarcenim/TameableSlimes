package tfar.tamableslimes.datagen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tfar.tamableslimes.TamableSlimes;
import tfar.tamableslimes.datagen.assets.ModLangProvider;
import tfar.tamableslimes.datagen.assets.ModModelProvider;
import tfar.tamableslimes.datagen.data.ModBlockTagsProvider;
import tfar.tamableslimes.datagen.data.ModItemTagsProvider;
import tfar.tamableslimes.datagen.data.ModLootTableProvider;
import tfar.tamableslimes.datagen.data.ModRecipeProvider;
import tfar.tamableslimes.init.ModEntityTypes;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModDatagen {

    public static void start(GatherDataEvent e) {
        DataGenerator generator = e.getGenerator();
        ExistingFileHelper helper = e.getExistingFileHelper();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = e.getLookupProvider();


        if (e.includeClient()) {
            generator.addProvider(true,new ModLangProvider(output));
            generator.addProvider(true,new ModModelProvider(output));
        }
        if (e.includeServer()) {
            generator.addProvider(true, ModLootTableProvider.create(output));
            generator.addProvider(true,new ModRecipeProvider(output));

            ModBlockTagsProvider blockTags = new ModBlockTagsProvider(output,lookupProvider, helper);
            generator.addProvider(true,blockTags);

            generator.addProvider(true,new ModItemTagsProvider(output,lookupProvider,blockTags.contentsGetter(),helper));
            generator.addProvider(true, new DatapackBuiltinEntriesProvider(
                    output, CompletableFuture.supplyAsync(ModDatagen::getProvider), Set.of(TamableSlimes.MOD_ID)));
        }
    }

    private static HolderLookup.Provider getProvider() {
        final RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
        // We need the BIOME registry to be present so we can use a biome tag, doesn't matter that it's empty
        registryBuilder.add(Registries.BIOME, context -> {
        });
        registryBuilder.add(ForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
            final HolderGetter<Biome> biomeHolderGetter = context.lookup(Registries.BIOME);
            final BiomeModifier addSpawn = ForgeBiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                    biomeHolderGetter.getOrThrow(BiomeTags.HAS_SWAMP_HUT),
                    new MobSpawnSettings.SpawnerData(ModEntityTypes.TAMABLE_SLIME, 5, 1, 4));
            context.register(createModifierKey("add_tamable_slime_spawn"), addSpawn);
        });
        RegistryAccess.Frozen regAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        return registryBuilder.buildPatch(regAccess, VanillaRegistries.createLookup());
    }

    private static ResourceKey<BiomeModifier> createModifierKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(TamableSlimes.MOD_ID, name));
    }

    public static Stream<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.stream()
                .filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(TamableSlimes.MOD_ID));
    }
}
