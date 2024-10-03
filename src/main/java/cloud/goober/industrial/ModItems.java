package cloud.goober.industrial;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {

    // END STATIC DEFS

    // START ITEMS

    public static final Item PANKONIUM_INGOT = register(
            new Item(new Item.Settings()),
            "pankonium_ingot"
    );

    // END ITEMS

    public static Item register(Item item, String id) {
        // Create the identifier for the item.
        Identifier itemID = Identifier.of(GooberIndustrial.MOD_ID, id);
        // Register the item.
        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);
        // Return the registered item!
        return registeredItem;
    }

    public static void initialize() {
    }
}
