package net.runelite.client.plugins.microbot.cardewsPlugins.MiniTasks;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

public class MiniTasksScript extends Script {
    int numberOfFurnitureBuilt = 0;

    public boolean run(MiniTasksConfig config) {
        numberOfFurnitureBuilt = 0;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Rs2AntibanSettings.microBreakActive) return;
                //long startTime = System.currentTimeMillis();

                UseOnObject(config);
                BuildHotspot(config);
                HandleBankPin(config);
                UseItemOnItem(config);

                //long endTime = System.currentTimeMillis();
                //long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    void UseOnObject(MiniTasksConfig config)
    {
        if (!config.IsUseOnObjectEnabled())
        {
            return;
        }

        if (config.GetGameObjectIdentifier() != -1 && config.GetInventoryIdentifier() != -1)
        {
            //Rs2Inventory.useItemOnObject(config.GetInventoryIdentifier(), config.GetGameObjectIdentifier());
            Rs2ItemModel lastItem = Rs2Inventory.getLast(config.GetInventoryIdentifier());
            assert lastItem != null;
            //Rs2Inventory.useItemOnObject(lastItem.getId(), config.GetGameObjectIdentifier());
            Rs2Inventory.slotInteract(lastItem.getSlot(), "use");
            sleepUntil(Rs2Inventory::isItemSelected);
            Rs2GameObject.interact(config.GetGameObjectIdentifier());
        }
    }

    void BuildHotspot(MiniTasksConfig config)
    {
        if (!config.IsBuildHotspotEnabled()) { return; }

        Rs2GameObject.interact(config.GetBuildHotspotID(), "Build");
        sleepUntil(() -> Rs2Widget.isWidgetVisible(458, 1));
        sleepGaussian(150, 50);

        Rs2Keyboard.typeString(config.GetBuildHotkey());
        if (config.ShouldClickOnInventoryItem())
        {
            numberOfFurnitureBuilt += 1;
            sleepGaussian(600 * 2, 50);
            if (numberOfFurnitureBuilt >= config.GetFurnitureIteratorMax())
            {
                if (Rs2Inventory.interact(config.GetInventoryItemID(), config.GetInventoryAction()))
                {
                    numberOfFurnitureBuilt = 0;
                }
            }
            sleepGaussian(600 * 2, 50);
        }
        else
        {
            sleepGaussian(600 * 4, 100);
        }
        //Rs2Keyboard.keyPress(config.GetBuildHotkey());

        Rs2GameObject.interact(config.GetFurnitureRemovalID(), "Remove");
        Rs2Widget.sleepUntilHasWidget("Really remove it?");

        Rs2Keyboard.keyPress('1');
        sleepGaussian(600 * 1, 100);

        sleepGaussian(config.GetTimeoutAfterRemoval(), 100);
    }

    void HandleBankPin(MiniTasksConfig config)
    {
        if (!config.EnterBankPin()) { return; }
        if (!Rs2Bank.isBankPinWidgetVisible()) { return; }
        Rs2Bank.handleBankPin();
    }

    void UseItemOnItem(MiniTasksConfig config)
    {
        if (!config.EnableItemOnItem()) { return; }
        //Rs2Inventory.combine(config.FirstItemID(), config.SecondItemID());

        Rs2ItemModel firstItem = Rs2Inventory.get(config.FirstItemID());
        Rs2ItemModel secondItem = Rs2Inventory.get(config.SecondItemID());

        Rs2Inventory.use(firstItem);
        sleepGaussian(80, 20);
        Rs2Inventory.use(secondItem);
    }
}
