package br.com.brforgers.mods.ducts;

import alexiil.mc.lib.attributes.CombinableAttribute;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import alexiil.mc.lib.attributes.item.ItemExtractable;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import alexiil.mc.lib.attributes.item.ItemInvUtil;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import alexiil.mc.lib.attributes.item.impl.RejectingItemInsertable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.Collections;

public class DuctBlockEntity extends BlockEntity implements Inventory {
    public static final BlockEntityType<DuctBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(DuctBlockEntity::new, Ducts.DUCT_BLOCK).build(null);
    public Inventory inventory = new SimpleInventory(1);
    public DuctBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return inventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return inventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.setStack(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    public static <E extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState state, E e) {
    }
    int transferCooldown = -1;
    Text customName = null;

        private static boolean attemptInsert(World world, BlockPos pos, BlockState state, DuctBlockEntity blockEntity){
            ItemStack stack = blockEntity.getStack(0);
            if (stack.isEmpty()) return false;

            Direction outputDir = blockEntity.getCachedState().get(DuctBlock.OUTPUT);
            Inventory outputInv = HopperBlockEntity.getInventoryAt(world, pos.offset(outputDir));

            if(outputInv != null){
                ItemStack stackCopy = blockEntity.getStack(0).copy();
                ItemStack ret = HopperBlockEntity.transfer(blockEntity, outputInv, blockEntity.removeStack(0, 1), outputDir.getOpposite());
                if (ret.isEmpty()) {
                    if (outputInv instanceof DuctBlockEntity){
                        ((DuctBlockEntity) outputInv).transferCooldown = 8;
                    }
                    outputInv.markDirty();
                    return true;
                }
                blockEntity.setStack(0, stackCopy);
            } else {
                ItemInsertable insertable = ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(outputDir));
                if (insertable == RejectingItemInsertable.NULL) {
                    return false;
                }
                ItemExtractable extractable = new FixedInventoryVanillaWrapper(blockEntity).getExtractable();

                return ItemInvUtil.move(extractable, insertable, 1) > 0;
            }
            return false;
        }

        public static void tick(World world, BlockPos pos, BlockState state, DuctBlockEntity blockEntity) {
            if(world == null || world.isClient) return ;
            blockEntity.transferCooldown--;
            if (blockEntity.transferCooldown > 0) return;
                    blockEntity.transferCooldown = 0;

            if (attemptInsert(world, pos, state, blockEntity)) {
                blockEntity.transferCooldown = 8;
                blockEntity.markDirty();
            }
        }

    @Override
        public NbtCompound writeNbt(NbtCompound tag){
            super.writeNbt(tag);
            Inventories.writeNbt(tag, DefaultedList.copyOf(ItemStack.EMPTY, inventory.getStack(0)));
            tag.putInt("TransferCooldown", transferCooldown);
            if (customName != null) {
                tag.putString("CustomName", Text.Serializer.toJson(customName));
            }
            return tag;
        }

        @Override
        public void readNbt(NbtCompound tag) {
            super.readNbt(tag);
        DefaultedList<ItemStack> savedContent = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        Inventories.readNbt(tag, savedContent);
        inventory.setStack(0, savedContent.get(0));
            transferCooldown = tag.getInt("TransferCooldown");
            if (tag.contains("CustomName", 8)) {
                customName = Text.Serializer.fromJson(tag.getString("CustomName"));
            }
        }
        @Override
        public void markDirty() {
            super.markDirty();
            inventory.markDirty();
        }
}

