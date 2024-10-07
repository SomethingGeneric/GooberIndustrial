package cloud.goober.industrial;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.SpawnGroup;

import static net.minecraft.component.type.AttributeModifierSlot.ARMOR;

public class GoobotEntity extends PathAwareEntity implements Inventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> handItems = DefaultedList.ofSize(2, ItemStack.EMPTY);

    public GoobotEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createCustomPlayerAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25D));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.85D));
        this.goalSelector.add(6, new LookAroundGoal(this));
    }

    // Inventory methods
    @Override
    public int size() {
        return 41; // 36 inventory + 4 armor + 1 offhand
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        for (ItemStack stack : this.armorItems) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return this.handItems.get(0).isEmpty() && this.handItems.get(1).isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= 0 && slot < 36) {
            return this.inventory.get(slot);
        } else if (slot >= 36 && slot < 40) {
            return this.armorItems.get(slot - 36);
        } else if (slot == 40) {
            return this.handItems.get(1); // Offhand
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.getInventoryStacks(), slot, amount);
        if (!result.isEmpty()) {
            this.markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.getInventoryStacks(), slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 36) {
            this.inventory.set(slot, stack);
        } else if (slot >= 36 && slot < 40) {
            this.armorItems.set(slot - 36, stack);
        } else if (slot == 40) {
            this.handItems.set(1, stack); // Offhand
        }
        this.markDirty();
    }

    @Override
    public void markDirty() {
        // what should this be doing?
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true; // Or implement your own logic
    }

    @Override
    public void clear() {
        this.inventory.clear();
        this.armorItems.clear();
        this.handItems.clear();
    }

    private DefaultedList<ItemStack> getInventoryStacks() {
        DefaultedList<ItemStack> list = DefaultedList.of();
        list.addAll(this.inventory);
        list.addAll(this.armorItems);
        list.addAll(this.handItems);
        return list;
    }

    // NBT
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        // Write hand items to NBT
        nbt.put("HandItems", Inventories.writeNbt(new NbtCompound(), this.handItems, BuiltinRegistries.createWrapperLookup()));

        // Write armor items to NBT
        nbt.put("ArmorItems", Inventories.writeNbt(new NbtCompound(), this.armorItems, BuiltinRegistries.createWrapperLookup()));

        // Write inventory items to NBT (if you have a custom inventory)
        NbtCompound inventoryNbt = new NbtCompound();
        Inventories.writeNbt(inventoryNbt, this.inventory, BuiltinRegistries.createWrapperLookup());
        nbt.put("Inventory", inventoryNbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt)   {
        super.readCustomDataFromNbt(nbt);

        // Read hand items from NBT
        NbtCompound handItemsNbt = nbt.getCompound("HandItems");
        Inventories.readNbt(handItemsNbt, this.handItems, BuiltinRegistries.createWrapperLookup());

        // Read armor items from NBT
        NbtCompound armorItemsNbt = nbt.getCompound("ArmorItems");
        Inventories.readNbt(armorItemsNbt, this.armorItems, BuiltinRegistries.createWrapperLookup());

        // Read inventory items from NBT (if you have a custom inventory)
        if (nbt.contains("Inventory", 10)) {
            NbtCompound inventoryNbt = nbt.getCompound("Inventory");
            Inventories.readNbt(inventoryNbt, this.inventory, BuiltinRegistries.createWrapperLookup());
        }
    }


    // Equipment methods
    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.armorItems;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> this.handItems.get(slot.getEntitySlotId());
            case HUMANOID_ARMOR -> this.armorItems.get(slot.getEntitySlotId());
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        switch (slot.getType()) {
            case HAND:
                this.handItems.set(slot.getEntitySlotId(), stack);
                break;
            case HUMANOID_ARMOR:
                this.armorItems.set(slot.getEntitySlotId(), stack);
                break;
        }
    }

    public static final EntityType<GoobotEntity> GOOBOT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(GooberIndustrial.MOD_ID, "goobot"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GoobotEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
                    .build()
    );
}