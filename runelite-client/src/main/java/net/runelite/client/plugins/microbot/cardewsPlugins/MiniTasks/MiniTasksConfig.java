package net.runelite.client.plugins.microbot.cardewsPlugins.MiniTasks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;

import java.awt.event.KeyEvent;

@ConfigGroup("MiniTasks")
public interface MiniTasksConfig extends Config {
    @ConfigSection(
            name = "Use on Object",
            description = "Uses inventory object on a game object via specified IDs.",
            position = 0,
            closedByDefault = true
    )
    String useOnObjectSection = "Uses inventory object on a game object via specified IDs.";

    @ConfigItem(
            keyName = "enableUseOnObject",
            name = "Enable Use On Object",
            description = "Toggles this function.",
            position = 0,
            section = useOnObjectSection
    )
    default boolean IsUseOnObjectEnabled() { return false; }
    @ConfigItem(
            keyName = "gameObjectIdentifier",
            name = "Game Object ID",
            description = "The ID of the object to use an item on.",
            position = 1,
            section = useOnObjectSection
    )
    default int GetGameObjectIdentifier() { return -1; }
    @ConfigItem(
            keyName = "inventoryIdentifier",
            name = "Inventory Identifier",
            description = "The inventory ID to use on the game object.",
            position = 2,
            section = useOnObjectSection
    )
    default int GetInventoryIdentifier() { return -1; }

    @ConfigSection(
            name = "Build on Hotspot",
            description = "Builds on construction hotspots with desired furniture built with key input and via hotspot ID.",
            position = 1,
            closedByDefault = true
    )
    String buildOnHotspotSection = "Builds on construction hotspots with desired furniture built with key input and via hotspot ID.";

    @ConfigItem(
            keyName = "enableBuildHotspot",
            name = "Enable Build Hotspot",
            description = "Toggles this function.",
            position = 0,
            section = buildOnHotspotSection
    )
    default boolean IsBuildHotspotEnabled() { return false; }
    @ConfigItem(
            keyName = "buildHotspotID",
            name = "Build Hotspot ID",
            description = "The GameObject ID for the desired hotspot to build on.",
            position = 1,
            section = buildOnHotspotSection
    )
    default int GetBuildHotspotID() { return 0; }
    @ConfigItem(
            keyName = "hotkey",
            name = "Hotkey",
            description = "The hotkey which will be used to select the furniture to build.",
            position = 2,
            section = buildOnHotspotSection
    )
    default String GetBuildHotkey() { return "3"; }
    @ConfigItem(
            keyName = "furnitureRemovalID",
            name = "Furniture Removal ID",
            description = "The ID of the game object furniture piece to be removed.",
            position = 3,
            section = buildOnHotspotSection
    )
    default int GetFurnitureRemovalID() { return 0; }
    @ConfigItem(
            keyName = "sleepTimeoutAfterRemoval",
            name = "Sleep Timeout After Removal",
            description = "How long to timeout if at all after removing a piece of furniture.",
            position = 4,
            section = buildOnHotspotSection
    )
    default int GetTimeoutAfterRemoval() { return 0; }
    @ConfigItem(
            keyName = "clickOnInventoryItem",
            name = "Click On Inventory Item",
            description = "Whether to left click an inventory item after building X pieces.",
            position = 5,
            section = buildOnHotspotSection
    )
    default boolean ShouldClickOnInventoryItem() { return false; }
    @ConfigItem(
            keyName = "inventoryItemID",
            name = "Inventory Item ID",
            description = "The ID to use to click on an object in the inventory.",
            position = 6,
            section = buildOnHotspotSection
    )
    default int GetInventoryItemID() { return 0; }
    @ConfigItem(
            keyName = "inventoryAction",
            name = "Inventory Action",
            description = "The name of the action on the inventory item.",
            position = 7,
            section = buildOnHotspotSection
    )
    default String GetInventoryAction() { return ""; }
    @ConfigItem(
            keyName = "furnitureToBuildBeforeClickingInventory",
            name = "Furniture No. To Build Before Click",
            description = "The number of furniture to be built before clicking inventory item.",
            position = 8,
            section = buildOnHotspotSection
    )
    default int GetFurnitureIteratorMax() { return 0; }

    @ConfigSection(
            name = "Enter Bank Pin",
            description = "Enters your bank pin, stored in your profile.",
            position = 2,
            closedByDefault = true
    )
    String enterBankPinSection = "Handle the automatic entry of your stored bank pin.";
    @ConfigItem(
            keyName = "enterBankPin",
            name = "Enter Bank Pin",
            description = "Enable or disable the automatic entry of your bank pin.",
            position = 0,
            section = enterBankPinSection
    )
    default boolean EnterBankPin() { return false; }

    @ConfigSection(
            name = "Use Item On Item",
            description = "Uses an item from the inventory on another item in your inventory via IDs",
            position = 3,
            closedByDefault = true
    )
    String itemOnItemSection = "Automates using an item on another item.";
    @ConfigItem(
            keyName = "enableItemOnItem",
            name = "Enable Using Item On Item",
            description = "Enables the automated process.",
            position = 0,
            section = itemOnItemSection
    )
    default boolean EnableItemOnItem() { return false; }
    @ConfigItem(
            keyName = "firstItemID",
            name = "First Item ID",
            description = "The item ID of the first object.",
            position = 1,
            section = itemOnItemSection
    )
    default int FirstItemID() { return 0; }
    @ConfigItem(
            keyName = "secondItemID",
            name = "Second Item ID",
            description = "The item ID of the second object.",
            position = 2,
            section = itemOnItemSection
    )
    default int SecondItemID() { return 0; }
    @ConfigItem(
            keyName = "sleepMean",
            name = "Sleep Mean",
            description = "The mean value the script will sleep before clicking the second item.",
            position = 3,
            section = itemOnItemSection
    )
    default int SleepMean() { return 80; }
    @ConfigItem(
            keyName = "sleepStdDev",
            name = "Sleep Std Dev",
            description = "The standard deviation the sleep value will deviate by.",
            position = 4,
            section = itemOnItemSection
    )
    default int SleepStdDev() { return 20; }
}
