package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.*;
import java.util.Objects;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + "Auto Slayer",
        description = "Cardews Slayer Plugin",
        tags = {"cd", "microbot", "slayer", "cardew"},
        enabledByDefault = false
)
@Slf4j
public class CardewSlayerPlugin extends Plugin {
    @Inject
    private CardewSlayerConfig config;
    @Provides
    CardewSlayerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CardewSlayerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CardewSlayerOverlay cardewSlayerOverlay;

    @Inject
    CardewSlayerScript cardewSlayerScript;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(cardewSlayerOverlay);
        }
        cardewSlayerScript.run(config);
    }

    protected void shutDown() {
        cardewSlayerScript.shutdown();
        overlayManager.remove(cardewSlayerOverlay);
    }

    boolean prayerFlickState = false;
    boolean lastFlickState = false;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (cardewSlayerScript.ShouldPrayerFlick()) {
            // THIS FIGHTS THE SCRIPT FOR TAB CONTROL
            // INSTEAD IMPLEMENT QUICK PRAYER SETTING DEPENDING ON TASK
            // AND FLICK USING QUICK PRAYERS INSTEAD
            //if (Rs2Tab.getCurrentTab() != InterfaceTab.PRAYER)
            //{
            //    Rs2Tab.switchTo(InterfaceTab.PRAYER);
            //}
            // Toggle flick state on each tick
            prayerFlickState = !prayerFlickState;

            // Sync actual game prayer state
            boolean prayerEnabled = IsAProtectionPrayerActive();
//
            if (!prayerEnabled)
            {
                Rs2Prayer.toggle(cardewSlayerScript.protectionPrayer, true);
            }
            else
            {
                Rs2Prayer.toggle(cardewSlayerScript.protectionPrayer, false);
                //lastFlickState = false;
                CardewSlayerScript.sleepGaussian(100, 50);
                Rs2Prayer.toggle(cardewSlayerScript.protectionPrayer, true);
            }
        }
        else
        {
            // Make sure prayer is OFF when not flicking
            if (IsAProtectionPrayerActive())
            {
                Rs2Prayer.toggle(cardewSlayerScript.protectionPrayer, false);
            }

            // Reset internal state
            prayerFlickState = false;
            lastFlickState = false;
        }

    }

    boolean IsAProtectionPrayerActive()
    {
        if (Rs2Prayer.getActiveProtectionPrayer() == null) return false;
        switch (Rs2Prayer.getActiveProtectionPrayer())
        {
            case PROTECT_MELEE:
            case PROTECT_MAGIC:
            case PROTECT_RANGE:
                return true;
        }
        return false;
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.ENGINE)
        {
            return;
        }

        String chatMsg = Text.removeTags(event.getMessage()); //remove color and linebreaks

        if (chatMsg.contains("You're assigned to kill"))
        {
            // Our message from checking enchanted gem
            cardewSlayerScript.UpdateSlayerTaskFromGemText(chatMsg, config);
        }
        else if (chatMsg.contains("You have completed your task!"))
        {
            cardewSlayerScript.SlayerTaskCompleted(config);
        }
        else if (chatMsg.contains("helmet repels the wall beast") && (cardewSlayerScript.GetCurrentState() == CardewSlayerScript.States.SLAYING_MONSTER))
        {
            cardewSlayerScript.WallBeastAppeared();
        }
        else if (chatMsg.contains("can't reach"))
        {
            //Microbot.log("Trying to do something you can't reach detected!<br>ChatMessageType: " + event.getType());
            cardewSlayerScript.tryForceWalkToMonsterLocation = true;
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        if (event == null) return;
        if (event.getSkill() == Skill.SLAYER)
        {
            cardewSlayerScript.CalculateKillsLeft();
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = (NPC) event.getActor();

        cardewSlayerScript.TryAddNpcToTargetList(npc, config);

    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = (NPC) event.getActor();

        cardewSlayerScript.TryRemoveNpcFromTargetList(npc, config);
    }
}
