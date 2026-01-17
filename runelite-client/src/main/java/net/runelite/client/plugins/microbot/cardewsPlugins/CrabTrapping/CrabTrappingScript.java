package net.runelite.client.plugins.microbot.cardewsPlugins.CrabTrapping;

import net.runelite.api.GameObject;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CrabTrappingScript extends Script {
    int CRAB_TRAP1 = 58885;
    int CRAB_TRAP2 = 58886;
    ArrayList<Integer> crabTraps = new ArrayList<>();
    int CRAB_TRAP_EMPTY = 58905;    // Action: "Bait"
    int CRAB_TRAP_BAITED = 58906;
    int CRAB_TRAP_FULL = 58908;     // Action: "Empty"

    int FISH_OFFCUTS = 11334;
    int FINE_FISH_OFFCUTS = 32307;

    int RED_CRAB = 31671;
    int RAW_RED_CRAB = 31686;

    int maxActiveTraps = -1;
    // Retain either a list of active baited traps, or the number of baited traps, depending on how it will be executed.
    List<Rs2GameObject> baitedTrapsList = new ArrayList<Rs2GameObject>();
    int currentNumberBaitedTraps = 0;

    public boolean run(CrabTrappingConfig config) {
        maxActiveTraps = GetMaxActiveTraps();
        crabTraps.add(58885);
        crabTraps.add(58886);

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Rs2AntibanSettings.microBreakActive) return;
                //long startTime = System.currentTimeMillis();
                if (Rs2Player.isAnimating() || Rs2Player.isMoving()) return;

                if (Rs2Inventory.hasItemAmount(RED_CRAB, 2))
                {
                    for (int i = 0; i < 2; i++)
                    {
                        Rs2Inventory.use("Knife");
                        sleepGaussian(50, 10);
                        Rs2Inventory.use(RED_CRAB);
                        Rs2Inventory.waitForInventoryChanges(600);
                    }
                    Rs2Inventory.dropAll(RAW_RED_CRAB);
                    return;
                }


                Optional<GameObject> caughtCrabTrap = Rs2GameObject.getGameObjects()
                        .stream()
                        .filter(obj -> crabTraps.contains(obj.getId()))
                        .collect(Collectors.toList()).stream().findAny();
                if (caughtCrabTrap.isPresent())
                {
                    WorldPoint caughtCrabLocation = caughtCrabTrap.get().getWorldLocation();

                    Rs2GameObject.interact(caughtCrabTrap.get(), "Empty");
                    Rs2Inventory.waitForInventoryChanges(800);
                    Rs2GameObject.interact(caughtCrabLocation, "Bait");
                }

                // Do we have bait to hunt
                if (Rs2Inventory.hasItem(FISH_OFFCUTS, FINE_FISH_OFFCUTS))
                {
                    Microbot.log("Attempting to bait trap");
                    Rs2GameObject.interact("Crab trap (empty)", "Bait");
                }
                else
                {
                    // NO OFFCUTS TO BE HAD
                    Microbot.log("Stopping plugin.. No fish offcuts.");
                    Microbot.stopPlugin(CrabTrappingPlugin.class);
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

    int GetMaxActiveTraps()
    {
        if (Rs2Player.getRealSkillLevel(Skill.HUNTER) >= 80)
        {
            return 5;
        }
        else if (Rs2Player.getRealSkillLevel(Skill.HUNTER) >= 60)
        {
            return 4;
        }
        else if (Rs2Player.getRealSkillLevel(Skill.HUNTER) >= 40)
        {
            return 3;
        }
        else if (Rs2Player.getRealSkillLevel(Skill.HUNTER) >= 20)
        {
            return 2;
        }
        return 1;
    }
}
