package tfar.tameableslimes.datagen.assets;

import com.google.gson.JsonElement;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import tfar.tameableslimes.init.ModItems;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ModItemModelProvider extends ItemModelGenerators {

    public ModItemModelProvider(BiConsumer<ResourceLocation, Supplier<JsonElement>> pOutput) {
        super(pOutput);
    }

    @Override
    public void run() {
        generateFlatItem(ModItems.TAMEABLE_SLIME_SPAWN_EGG,SPAWN_EGG);
    }

    public void generateFlatItems(Item... items) {
        for (Item item : items) {
            generateFlatItem(item,ModelTemplates.FLAT_ITEM);
        }
    }

    public void generateFlatHandheldItems(Item... items) {
        for (Item item : items) {
            generateFlatItem(item,ModelTemplates.FLAT_HANDHELD_ITEM);
        }
    }


    public static final ModelTemplate SPAWN_EGG = createItem("template_spawn_egg");

    public static final ModelTemplate TWO_LAYERED_HANDHELD_ITEM = createItem("handheld", TextureSlot.LAYER0, TextureSlot.LAYER1);

    private static ModelTemplate createItem(String pItemModelLocation, TextureSlot... pRequiredSlots) {
        return new ModelTemplate(Optional.of(new ResourceLocation("minecraft", "item/" + pItemModelLocation)), Optional.empty(), pRequiredSlots);
    }

    private static ModelTemplate createItem(String domain, String pItemModelLocation, TextureSlot... pRequiredSlots) {
        return new ModelTemplate(Optional.of(new ResourceLocation(domain, "item/" + pItemModelLocation)), Optional.empty(), pRequiredSlots);
    }


}
