package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.cardewsPlugins.CUtil;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerMaster;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardewSlayerScript extends Script {
    enum States{
        MOVING_TO_SLAYER_MASTER,
        GETTING_SLAYER_TASK,
        MOVING_TO_MONSTER_LOCATION,
        MOVING_TO_NEAREST_BANK,
        BANKING
    }
    States currentState = States.MOVING_TO_SLAYER_MASTER;

    SlayerMaster currentMaster = SlayerMaster.NONE;

    int killsLeft = 0;

    public boolean run(CardewSlayerConfig config) {
        Microbot.enableAutoRunOn = false;
        CUtil.SetMyAntiban(0.0);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.bankPinBeingHandled) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Microbot.handlingRandomEvent) return;
                long startTime = System.currentTimeMillis();

                if (currentMaster == SlayerMaster.NONE) {
                    currentMaster = GetSlayerMasterFromConfig(config);
                }
                HandleStateMachine();

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    SlayerMaster GetSlayerMasterFromConfig(CardewSlayerConfig _config) {
        for (SlayerMaster master : SlayerMaster.values()) {
            if (master.getName().equalsIgnoreCase(_config.SlayerMaster())) {
                return master;
            }
        }
        // No string equal to a masters name
        return SlayerMaster.NONE;
    }

    void HandleStateMachine()
    {
        switch(currentState)
        {
            case MOVING_TO_SLAYER_MASTER:
                if (!Rs2Walker.isNear(currentMaster.getWorldPoint())) {
                    Rs2Walker.walkTo(currentMaster.getWorldPoint());
                }
                else {
                    // We are near the master
                    currentState = States.GETTING_SLAYER_TASK;
                }
                break;

            case GETTING_SLAYER_TASK:
                // Assuming we have no task
                // Check if we have chat text widget active
                if (Rs2Widget.isWidgetVisible(231, 6)) {
                    // Get the text as a string
                    String chatText = Rs2Widget.getChildWidgetText(231, 6);
                    // Get number of kills
                    // Get name of task
                    Pattern pattern = Pattern.compile("kill (\\d+) (.+)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(chatText);
                }
                else {
                    // Get task from master
                    Rs2Npc.interact(currentMaster.getName(), "Assignment");
                }
                break;

            case MOVING_TO_MONSTER_LOCATION:

                break;

            case MOVING_TO_NEAREST_BANK:

                break;

            case BANKING:

                break;
        }
    }
}
