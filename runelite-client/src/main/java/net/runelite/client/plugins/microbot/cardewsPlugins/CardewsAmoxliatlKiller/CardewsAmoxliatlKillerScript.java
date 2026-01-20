package net.runelite.client.plugins.microbot.cardewsPlugins.CardewsAmoxliatlKiller;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;

import java.util.concurrent.TimeUnit;

public class CardewsAmoxliatlKillerScript extends Script {


    public boolean run(CardewsAmoxliatlKillerConfig config) {

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Rs2AntibanSettings.microBreakActive) return;
                //long startTime = System.currentTimeMillis();



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
}
