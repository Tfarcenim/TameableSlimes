package tfar.tamableslimes.datagen.data;

import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import tfar.tamableslimes.datagen.ModDatagen;

import java.util.stream.Collectors;

public class ModBlockLoot extends VanillaBlockLoot {

    @Override
    protected void generate() {
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModDatagen.getKnownBlocks().collect(Collectors.toList());
    }
}
