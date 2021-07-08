package br.com.brforgers.mods.ducts;

import br.com.brforgers.mods.ducts.DuctBlockEntity;
import br.com.brforgers.mods.ducts.DuctBlock;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Ducts implements ModInitializer {
    static final String MOD_ID = "ducts";
    Logger logger = LogManager.getLogger("Ducts");

    static final Identifier DUCT  = new Identifier(MOD_ID, "duct");

    static final Block DUCT_BLOCK = new DuctBlock();

    @Override
    public void onInitialize() {
        logger.info("Ducts!");
        Registry.register(Registry.BLOCK, DUCT, DUCT_BLOCK);
        Registry.register(Registry.ITEM, DUCT, new BlockItem(DUCT_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));
        Registry.register(Registry.BLOCK_ENTITY_TYPE, DUCT, DuctBlockEntity.TYPE);

    }
}
