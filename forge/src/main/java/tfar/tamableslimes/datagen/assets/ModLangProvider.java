package tfar.tamableslimes.datagen.assets;

import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import org.codehaus.plexus.util.StringUtils;
import tfar.tamableslimes.TamableSlimes;
import tfar.tamableslimes.init.ModEntityTypes;
import tfar.tamableslimes.init.ModItems;

import java.util.function.Supplier;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput gen) {
        super(gen, TamableSlimes.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addDefaultItem(() -> ModItems.TAMABLE_SLIME_SPAWN_EGG);

        addDefaultEntityType(() -> ModEntityTypes.TAMABLE_SLIME);
    }

    protected void addDesc(ItemLike item, String desc) {
        add(item.asItem().getDescriptionId()+".desc",desc);
    }

    protected void addTab(CreativeModeTab tab, String translation) {
        Component display  = tab.getDisplayName();
        ComponentContents contents = display.getContents();
        if (contents instanceof TranslatableContents translatableContents) {
            add(translatableContents.getKey(), translation);
        } else {
            throw new RuntimeException("Not translatable: "+tab);
        }
    }

    protected void addDefaultItem(Supplier<? extends Item> supplier) {
        addItem(supplier,getNameFromItem(supplier.get()));
    }

    protected void addDefaultBlock(Supplier<? extends Block> supplier) {
        addBlock(supplier,getNameFromBlock(supplier.get()));
    }

    protected void addDefaultEnchantment(Supplier<? extends Enchantment> supplier) {
        addEnchantment(supplier,getNameFromEnchantment(supplier.get()));
    }

    protected void addDefaultEntityType(Supplier<? extends EntityType<?>> supplier) {
        addEntityType(supplier,getNameFromEntity(supplier.get()));
    }


    public static String getNameFromItem(Item item) {
        return StringUtils.capitaliseAllWords(item.getDescriptionId().split("\\.")[2].replace("_", " "));
    }

    public static String getNameFromBlock(Block block) {
        return StringUtils.capitaliseAllWords(block.getDescriptionId().split("\\.")[2].replace("_", " "));
    }

    public static String getNameFromEnchantment(Enchantment enchantment) {
        return StringUtils.capitaliseAllWords(enchantment.getDescriptionId().split("\\.")[2].replace("_", " "));
    }

    public static String getNameFromEntity(EntityType<?> type) {
        return StringUtils.capitaliseAllWords(type.getDescriptionId().split("\\.")[2].replace("_", " "));
    }


}
