package net.runelite.client.plugins.microbot.cardewsPlugins.TwoTickCooking;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.tileitem.models.Rs2TileItemModel;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import org.slf4j.event.Level;

import java.util.concurrent.TimeUnit;

public class TwoTickCookingScript extends Script {
    State state;

    WorldPoint standLocation = new WorldPoint(3159, 2841, 0);

    public boolean run(TwoTickCookingConfig config) {
        state = State.BANK;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Rs2AntibanSettings.microBreakActive) return;
                //long startTime = System.currentTimeMillis();

                if (Rs2Antiban.getActivity() != Activity.TWO_TICK_COOKING)
                {
                    Rs2Antiban.setActivity(Activity.TWO_TICK_COOKING);
                }

                switch (state)
                {
                    case BANK:
                        if (!Rs2Bank.isOpen())
                        {
                            Rs2Bank.openBank();
                            return;
                        }

                        if (Rs2Inventory.count() > 0)
                        {
                            Rs2Bank.depositAll();
                        }

                        if (!Rs2Bank.withdrawAll(config.RawItemID()))
                        {
                            Microbot.showMessage("Couldn't find any more item with ID: " + config.RawItemID() + "! Shutting down...");
                            Microbot.stopPlugin(TwoTickCookingPlugin.class);
                            return;
                        }
                        Rs2Inventory.waitForInventoryChanges(1000);
                        if (Rs2Inventory.hasItem(config.RawItemID()))
                        {
                            Rs2Bank.closeBank();
                            state = State.DROP;
                        }
                        break;

                    case DROP:
                        if (Rs2Player.isMoving()) return;

                        if (Rs2Player.getWorldLocation().equals(standLocation))
                        {
                            Rs2Inventory.dropAll(config.RawItemID());
                        }
                        else
                        {
                            Rs2Walker.walkFastCanvas(standLocation);
                            return;
                        }

                        if (Rs2Inventory.count() == 0)
                        {
                            state = State.COOK;
                        }
                        break;

                    case COOK:
                        // Pick-up 1 dropped fish.
                        Rs2TileItemModel pickedFish = Microbot.getRs2TileItemCache().query()
                                .withId(config.RawItemID())
                                .where(Rs2TileItemModel::isOwned)
                                .first();
                        if (pickedFish == null)
                        {
                            //Microbot.log("TwoTickCooking: pickedFish is null!", Level.DEBUG);
                            state = State.BANK;
                            return;
                        }
                        pickedFish.click("Take");
                        Rs2Inventory.waitForInventoryChanges(600);

                        Rs2TileObjectModel cookingPot = Microbot.getRs2TileObjectCache().query()
                                .withId(41316).first();
                        if (cookingPot == null)
                        {
                            Microbot.log("TwoTickCooking: cookingPot is null!", Level.DEBUG);
                            return;
                        }
                        cookingPot.click("Cook");
                        Rs2Inventory.waitForInventoryChanges(600);
                        break;
                }

                //long endTime = System.currentTimeMillis();
                //long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
