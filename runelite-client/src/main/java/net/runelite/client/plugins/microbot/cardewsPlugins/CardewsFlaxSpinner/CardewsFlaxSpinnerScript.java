package net.runelite.client.plugins.microbot.cardewsPlugins.CardewsFlaxSpinner;

import net.runelite.api.GameObject;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectQueryable;
import net.runelite.client.plugins.microbot.cardewsPlugins.CardewsFlaxSpinner.enums.BotState;
import net.runelite.client.plugins.microbot.cardewsPlugins.CardewsFlaxSpinner.enums.SpinningLocation;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spellbook;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.concurrent.TimeUnit;

public class CardewsFlaxSpinnerScript extends Script {
    static private BotState botState = BotState.MOVING_TO_BANK;

    public boolean run(CardewsFlaxSpinnerConfig config) {
        Initialise();
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Rs2AntibanSettings.microBreakActive) return;
                //long startTime = System.currentTimeMillis();
                if (config.SpinningLocation() == SpinningLocation.SPIN_FLAX_SPELL)
                {
                    if (!ValidateSpellbook())
                    {
                        Microbot.showMessage("Not on Lunar Spellbook! Shutting down...");
                        Microbot.stopPlugin(CardewsFlaxSpinnerPlugin.class);
                        return;
                    }
                }

                switch(botState)
                {
                    case MOVING_TO_WHEEL:
                        if (config.SpinningLocation() == SpinningLocation.SPIN_FLAX_SPELL)
                        {
                            botState = BotState.SPIN_FLAX;
                            return;
                        }

                        MoveToSpinningWheel(config);
                        break;

                    case SPIN_FLAX:
                        if (config.SpinningLocation() == SpinningLocation.SPIN_FLAX_SPELL)
                        {
                            LunarSpinFlax();
                            return;
                        }

                        SpinFlax();
                        break;

                    case MOVING_TO_BANK:
                        if (config.SpinningLocation() == SpinningLocation.SPIN_FLAX_SPELL)
                        {
                            botState = BotState.BANKING;
                            return;
                        }

                        MoveToBank();
                        break;

                    case BANKING:
                        if (config.SpinningLocation() == SpinningLocation.SPIN_FLAX_SPELL)
                        {
                            HandleBankingWithLunars(config);
                            return;
                        }

                        HandleBanking();
                        break;
                }


                //long endTime = System.currentTimeMillis();
                //long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    private void MoveToSpinningWheel(CardewsFlaxSpinnerConfig config)
    {
        if (Rs2Walker.walkTo(config.SpinningLocation().getLocation()))
        {
            botState = BotState.SPIN_FLAX;
        }
    }

    private void SpinFlax()
    {
        if (Rs2Player.isAnimating()) return;

        if (!Rs2Inventory.hasItem(ItemID.FLAX))
        {
            botState = BotState.MOVING_TO_BANK;
            return;
        }

        Rs2TileObjectQueryable spinningWheelObjectQueryable = new Rs2TileObjectQueryable();
        spinningWheelObjectQueryable.interact(ObjectID.SPINNINGWHEEL, "Spin");
        Rs2Widget.sleepUntilHasWidget("would you like to spin");
        Rs2Keyboard.keyPress('2');
        Rs2Player.waitForAnimation();

        Rs2Antiban.actionCooldown();
    }

    private void LunarSpinFlax()
    {
        if (!Rs2Inventory.hasItem(ItemID.FLAX))
        {
            botState = BotState.BANKING;
            return;
        }

        if (Rs2Magic.canCast(MagicAction.SPIN_FLAX))
        {
            Rs2Magic.cast(MagicAction.SPIN_FLAX);
            sleepUntil(() -> !Rs2Player.isAnimating());
        }
        else
        {
            Microbot.showMessage("Unable to cast spell! Shutting down...");
            Microbot.stopPlugin(CardewsFlaxSpinnerPlugin.class);
        }
    }

    private void MoveToBank()
    {
        if (Rs2Walker.walkTo(Rs2Bank.getNearestBank().getWorldPoint()))
        {
            botState = BotState.BANKING;
        }
    }

    private void HandleBanking()
    {
        if (!Rs2Bank.isOpen())
        {
            Rs2Bank.openBank();
            return;
        }

        Rs2Bank.depositAll();
        sleepGaussian(100, 33);
        if (!Rs2Bank.withdrawAll(ItemID.FLAX))
        {
            Microbot.showMessage("Missing flax! Shutting down...");
            Microbot.stopPlugin(CardewsFlaxSpinnerPlugin.class);
            return;
        }
        Rs2Inventory.waitForInventoryChanges(1200);

        if (Rs2Inventory.hasItemAmount(ItemID.FLAX, 28))
        {
            Rs2Bank.closeBank();
            sleepGaussian(600, 150);

            botState = BotState.MOVING_TO_WHEEL;
        }
    }

    private void HandleBankingWithLunars(CardewsFlaxSpinnerConfig config)
    {
        if (!Rs2Bank.isOpen())
        {
            Rs2Bank.openBank();
            return;
        }

        if (!Rs2Inventory.hasItem(ItemID.NATURERUNE))
        {
            if (!Rs2Bank.withdrawAll(ItemID.NATURERUNE))
            {
                Microbot.showMessage("Missing nature runes! Shutting down...");
                Microbot.stopPlugin(CardewsFlaxSpinnerPlugin.class);
                return;
            }
            Rs2Inventory.waitForInventoryChanges(600);
        }
        if (!Rs2Inventory.hasItem(ItemID.ASTRALRUNE))
        {
            if (!Rs2Bank.withdrawAll(ItemID.ASTRALRUNE))
            {
                Microbot.showMessage("Missing astral runes! Shutting down...");
                Microbot.stopPlugin(CardewsFlaxSpinnerPlugin.class);
                return;
            }
            Rs2Inventory.waitForInventoryChanges(600);
        }
        if (!config.IsUsingAirStaff())
        {
            if (!Rs2Inventory.hasItem(ItemID.AIRRUNE))
            {
                if (!Rs2Bank.withdrawAll(ItemID.AIRRUNE))
                {
                    Microbot.showMessage("Missing air runes! Shutting down...");
                    Microbot.stopPlugin(CardewsFlaxSpinnerPlugin.class);
                    return;
                }
                Rs2Inventory.waitForInventoryChanges(600);
            }
        }

        Rs2Bank.depositAll(ItemID.BOW_STRING);
        Rs2Inventory.waitForInventoryChanges(600);

        if (!Rs2Bank.withdrawX(ItemID.FLAX, 25))
        {
            Microbot.showMessage("Missing flax! Shutting down...");
            Microbot.stopPlugin(CardewsFlaxSpinnerPlugin.class);
            return;
        }
        Rs2Inventory.waitForInventoryChanges(1200);

        if (Rs2Inventory.hasItemAmount(ItemID.FLAX, 25))
        {
            Rs2Bank.closeBank();
            sleepGaussian(600, 150);

            botState = BotState.SPIN_FLAX;
        }
    }

    public static BotState GetBotState()
    {
        return botState;
    }

    private void Initialise()
    {
        Rs2AntibanSettings.actionCooldownChance = 1.0;
        Rs2AntibanSettings.naturalMouse = true;
        Rs2AntibanSettings.simulateMistakes = true;
        Rs2AntibanSettings.moveMouseOffScreen = true;
        Rs2AntibanSettings.moveMouseOffScreenChance = 0.1;
        Rs2AntibanSettings.moveMouseRandomly = true;
        Rs2AntibanSettings.moveMouseRandomlyChance = 0.1;
    }

    private boolean ValidateSpellbook()
    {
        return Rs2Magic.isSpellbook(Rs2Spellbook.LUNAR);
    }
}
