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
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.*;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.SpawnGroup;

public class GoobotEntity extends PathAwareEntity implements Inventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> handItems = DefaultedList.ofSize(2, ItemStack.EMPTY);

    public PlayerEntity manager = null;

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
        //this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25D));
        this.goalSelector.add(2, new PickupNearbyItemsGoal(this));
        this.goalSelector.add(3, new MineOreGoal(this));
        //this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.85D));
        //this.goalSelector.add(6, new LookAroundGoal(this));
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
        } else if (slot == 41) {
            return this.handItems.getFirst(); // Main hand??
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 36) {
            this.inventory.set(slot, stack);
        } else if (slot >= 36 && slot < 40) {
            this.armorItems.set(slot - 36, stack);
        } else if (slot == 40) {
            this.handItems.set(1, stack); // Offhand
        } else if (slot == 41) {
            this.handItems.set(0, stack);
        }
        this.markDirty();
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

    private int getEmptyStack() {
        for (int c = 0; c < 37; c++) {
            ItemStack foo = getStack(c);
            if (foo.isEmpty()) {
                return c;
            }
        }

        return 0; // TODO: What do if all in 0-36 are full?
    }

    @Override
    public void markDirty() {
        // What??
        return;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true; // Or implement your own logic
    }

    public boolean canPickupItem(ItemStack stack) {
        // Determine whether the entity can pick up the item (optional filtering logic)
        return true; // For now, always true
    }

    public boolean tryPickupItem(ItemStack stack) {
        if (this.canPickupItem(stack)) {
            int slot = getEmptyStack();
            this.setStack(slot, stack);
            return true;
        }
        return false;
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

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        // Get the item in the player's hand
        ItemStack playerItemStack = player.getStackInHand(hand);

        if (!playerItemStack.isEmpty()) {
            if (!getStack(41).isEmpty()) { // am I holding something
                ItemStack myCurrentItem = getStack(41);
                player.setStackInHand(hand, myCurrentItem.copy());
                setStack(41, playerItemStack);
            } else { // I am not holding anything
                setStack(41, playerItemStack);
                player.setStackInHand(hand, ItemStack.EMPTY);
            }
        } else {
            ItemStack foo = getStack(41);
            player.setStackInHand(hand, foo.copy());
            setStack(41, ItemStack.EMPTY);
        }

        manager = player;

        return ActionResult.SUCCESS; // Return SUCCESS if the interaction was handled

        // Return PASS to allow other interactions (like opening GUI or riding)
        //return super.interactMob(player, hand);
    }

    @Override
    public void tick() {
        super.tick();

        // Forced Chunk Loading
        // Ensure the entity is in a ServerWorld (as client worlds don't handle chunk loading)
        if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld serverWorld) {
            // Get the chunk coordinates where the entity is currently located
            ChunkPos chunkPos = new ChunkPos(this.getBlockPos());

            // Create a ticket for this chunk to force it to stay loaded
            serverWorld.getChunkManager().addTicket(ChunkTicketType.FORCED, chunkPos, 1, ChunkPos.ORIGIN);
        }

    }

    @Override
    public void remove(RemovalReason reason) {
        // When the entity is removed, also release the chunk
        if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld serverWorld) {
            ChunkPos chunkPos = new ChunkPos(this.getBlockPos());
            serverWorld.getChunkManager().removeTicket(ChunkTicketType.FORCED, chunkPos, 1, ChunkPos.ORIGIN);
        }

        super.remove(reason);
    }

    // END METHODS
    public static final EntityType<GoobotEntity> GOOBOT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(GooberIndustrial.MOD_ID, "goobot"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GoobotEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
                    .build()
    );
}