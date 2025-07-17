package net.runelite.client.plugins.microbot.cardewsPlugins.LineFiremaker;

import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.cardewsPlugins.CUtil;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.TimeUnit;


public class LineFiremakerScript extends Script {

    WorldPoint firemakeLocation = null;
    WorldPoint lastFmLocation = null;
    WorldPoint fmLocation = null;
    boolean burnLogs = true;

    public boolean run(LineFiremakerConfig config) {
        Microbot.enableAutoRunOn = false;

        firemakeLocation = null;
        burnLogs = true;
        CUtil.SetMyAntiban(0.06);

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Microbot.bankPinBeingHandled) return;
                if (Rs2AntibanSettings.microBreakActive) return;
                if (Microbot.handlingRandomEvent) return;
                long startTime = System.currentTimeMillis();

                if (firemakeLocation == null)
                {
                    firemakeLocation = Rs2Player.getWorldLocation();
                    fmLocation = firemakeLocation;
                    lastFmLocation = fmLocation;
                }
                if (!config.GetSelectedLog().HasRequiredLevel())
                {
                    Microbot.showMessage("You don't have the required Firemaking level to burn this log!");
                    shutdown();
                }
//
                if (!burnLogs)
                {
                    if (!Rs2Walker.isInArea(firemakeLocation, 3))
                    {
                        Rs2Walker.walkTo(firemakeLocation);
                    }
                    else
                    {
                        if (Rs2Player.isMoving())
                        {
                            return;
                        }

                        if (fmLocation == lastFmLocation)
                        {
                            //Microbot.log("New location rolled");
                            fmLocation = new WorldPoint(firemakeLocation.getX(),
                                    (int)Rs2Random.normalRange(firemakeLocation.getY() - 1, firemakeLocation.getY() + 1, 0.0), firemakeLocation.getPlane());
                            //Microbot.log("fmLocation: " +fmLocation);
                            //Microbot.log("lastFmLocation: " +lastFmLocation);
                            return;
                        }
                        else
                        {
                            if (Rs2Player.getWorldLocation().getX() != fmLocation.getX() && Rs2Player.getWorldLocation().getY() != fmLocation.getY())
                            {
                                Rs2Walker.walkFastCanvas(fmLocation);
                                Rs2Player.waitForWalking();
                            }
                            else
                            {
                                lastFmLocation = fmLocation;
                                burnLogs = true;
                                Rs2Antiban.takeMicroBreakByChance();
                            }
                        }
                    }
                    return;
                }

                if (Rs2Inventory.hasItem(config.GetSelectedLog().GetLogName()))
                {
                    if (burnLogs)
                    {
                        Rs2Inventory.use("Tinderbox");
                        sleep(80, 500);
                        Rs2Inventory.use(config.GetSelectedLog().GetLogName());

                        sleepUntilOnClientThread(() -> Rs2Player.getPoseAnimation() == 819 || Rs2Player.getPoseAnimation() == 820 || Rs2Player.getPoseAnimation() == 822 || Rs2Player.getPoseAnimation() == 823);
                        //Microbot.log("Waiting for pose finished");
                    }
                    return;
                }
                else
                {
                    if (Rs2Walker.isInArea(Rs2Bank.getNearestBank().getWorldPoint(), 8))
                    {
                        if (Rs2Bank.isOpen())
                        {
                            if (Rs2Bank.hasItem(config.GetSelectedLog().GetLogName()))
                            {
                                //Microbot.log("Getting logs out. Has item in bank.");
                                Rs2Bank.withdrawX(config.GetSelectedLog().GetLogName(), 27);
                                sleep(300, 900);
                                Rs2Bank.closeBank();
                                burnLogs = false;
                                Rs2Antiban.takeMicroBreakByChance();
                            }
                            else
                            {
                                Microbot.showMessage("No more logs of type in bank.");
                                shutdown();
                            }
                        }
                        else
                        {
                            Rs2Bank.openBank();
                        }
                    }
                    else
                    {
                        Rs2Walker.walkTo(Rs2Bank.getNearestBank().getWorldPoint());
                    }
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}