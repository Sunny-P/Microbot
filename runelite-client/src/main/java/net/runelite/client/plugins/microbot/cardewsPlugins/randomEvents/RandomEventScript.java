package net.runelite.client.plugins.microbot.cardewsPlugins.randomEvents;

import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.cardewsPlugins.CUtil;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.reflection.Rs2Reflection;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;


public class RandomEventScript extends Script {
    enum RandomEvent{
        NONE,
        BEEKEEPER,
        GENIE,
        COUNT_CHECK,
        DRUNKEN_DWARF,
        GRAVEDIGGER,
        DUNCE,
        SANDWICH_LADY,
        DRILL_SERGEANT,
        FREAKY_FORESTER
    }
    RandomEvent activeEvent = RandomEvent.NONE;

    boolean talkedToEvent = false;
    boolean xpRewardChosen = false;

    public boolean run(RandomEventConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.bankPinBeingHandled) return;
                if (Microbot.pauseAllScripts.get()) return;
                long startTime = System.currentTimeMillis();

                if (HandleXPLamp(config)) return;

                HandleRandomEvents(config);

                if (!Microbot.handlingRandomEvent) return;
                // Dialogue will only be continued if we do not disable the option
                if (Rs2Dialogue.hasContinue())
                {
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                }

                //Microbot.log("Random Event State: " + activeEvent);

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

    private boolean HandleXPLamp(RandomEventConfig _config)
    {
        // If there is a lamp in the players inventory, handle lamp with chosen skill
        // Then return. I don't want potential script in-fighting for control.
        if (Rs2Inventory.hasItem("Lamp"))
        {
            Microbot.handlingRandomEvent = true;
            if (Rs2Widget.isWidgetVisible(240, 0))
            {
                if (xpRewardChosen)
                {
                    // Our skill to put xp in has been chosen,
                    // Click the confirm widget
                    if (Rs2Widget.clickWidget(240, 26))
                    {
                        xpRewardChosen = false;
                        Microbot.handlingRandomEvent = false;
                        return true;
                    }
                }

                switch (_config.skillXPReward())
                {
                    case ATTACK:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 2);
                        break;
                    case STRENGTH:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 3);
                        break;
                    case RANGED:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 4);
                        break;
                    case MAGIC:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 5);
                        break;
                    case DEFENCE:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 6);
                        break;
                    case HITPOINTS:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 7);
                        break;
                    case PRAYER:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 8);
                        break;
                    case AGILITY:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 9);
                        break;
                    case HERBLORE:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 10);
                        break;
                    case THIEVING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 11);
                        break;
                    case CRAFTING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 12);
                        break;
                    case RUNECRAFT:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 13);
                        break;
                    case SLAYER:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 14);
                        break;
                    case FARMING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 15);
                        break;
                    case MINING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 16);
                        break;
                    case SMITHING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 17);
                        break;
                    case FISHING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 18);
                        break;
                    case COOKING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 19);
                        break;
                    case FIREMAKING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 20);
                        break;
                    case WOODCUTTING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 21);
                        break;
                    case FLETCHING:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 22);
                        break;
                    case CONSTRUCTION:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 23);
                        break;
                    case HUNTER:
                        xpRewardChosen = Rs2Widget.clickWidget(240, 24);
                        break;
                }
            }
            else
            {
                Rs2Inventory.interact("Lamp", "Rub");
            }

            return true;
        }
        return false;
    }

    private boolean shouldDismissNpc(NPC _npc, RandomEventConfig _config)
    {
        if (_npc.getName() == null) { return false; }
        switch (_npc.getName()) {
            case "Bee keeper":
                if (!_config.dismissBeekeeper()) {
                    activeEvent = RandomEvent.BEEKEEPER;
                }
                return _config.dismissBeekeeper();
            case "Capt' Arnav":
                if (!_config.dismissArnav()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissArnav();
            case "Niles":
            case "Miles":
            case "Giles":
                if (!_config.dismissCerters()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissCerters();
            case "Count Check":
                if (!_config.dismissCountCheck()) {
                    activeEvent = RandomEvent.COUNT_CHECK;
                }
                return _config.dismissCountCheck();
            case "Sergeant Damien":
                if (!_config.dismissDrillDemon()) {
                    activeEvent = RandomEvent.DRILL_SERGEANT;
                }
                return _config.dismissDrillDemon();
            case "Drunken Dwarf":
                if (!_config.dismissDrunkenDwarf()) {
                    activeEvent = RandomEvent.DRUNKEN_DWARF;
                }
                return _config.dismissDrunkenDwarf();
            case "Evil Bob":
                // IS THIS ISLAND BOB or PRISON PETE BOB?
                // Talk to Evil Bob
                // Look at the conversation text to determine if he's taking you to Prison Pete or not
                // return as necessary
                if (!_config.dismissEvilBob()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissEvilBob();
            case "Postie Pete":
                if (!_config.dismissEvilTwin()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissEvilTwin();
            case "Freaky Forester":
                if (!_config.dismissFreakyForester()) {
                    activeEvent = RandomEvent.FREAKY_FORESTER;
                }
                return _config.dismissFreakyForester();
            case "Genie":
                if (!_config.dismissGenie()) {
                    activeEvent = RandomEvent.GENIE;
                }
                return _config.dismissGenie();
            case "Leo":
                if (!_config.dismissGravedigger()) {
                    activeEvent = RandomEvent.GRAVEDIGGER;
                }
                return _config.dismissGravedigger();
            case "Dr Jekyll":
                if (!_config.dismissJekyllAndHyde()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissJekyllAndHyde();
            case "Frog":
                if (!_config.dismissKissTheFrog()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissKissTheFrog();
            case "Mysterious Old Man":
                // THIS NPC HAS THE MAZE AND MIME VARIATIONS
                if (!_config.dismissMysteriousOldMan()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissMysteriousOldMan();
            case "Pillory Guard":
                if (!_config.dismissPillory()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissPillory();
            case "Flippa":
            case "Tilt":
                if (!_config.dismissPinball()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissPinball();
            case "Quiz Master":
                if (!_config.dismissQuizMaster()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissQuizMaster();
            case "Rick Turpentine":
                if (!_config.dismissRickTurpentine()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissRickTurpentine();
            case "Sandwich lady":
                if (!_config.dismissSandwichLady()) {
                    activeEvent = RandomEvent.SANDWICH_LADY;
                }
                return _config.dismissSandwichLady();
            case "Strange plant":
                if (!_config.dismissStrangePlant()) {
                    activeEvent = RandomEvent.NONE;
                }
                return _config.dismissStrangePlant();
            case "Dunce":
                if (!_config.dismissSurpriseExam()) {
                    activeEvent = RandomEvent.DUNCE;
                }
                return _config.dismissSurpriseExam();
            default:
                return false;
        }
    }

    private void HandleRandomEvents(RandomEventConfig _config)
    {
        switch (activeEvent)
        {
            case NONE:
                Rs2NpcModel npc = Rs2Npc.getRandomEventNPC();
                if (npc != null)
                {
                    if (shouldDismissNpc(npc, _config))
                    {
                        // Dismiss the NPC if we don't want to do the NPC
                        Rs2Npc.interact(npc, "Dismiss");
                    }
                    else
                    {
                        Microbot.handlingRandomEvent = true;
                        Rs2Npc.interact(npc, "Talk-to");
                    }
                    // Otherwise, Talk to the NPC and continue to complete the event
                    // Check which event we are doing
                    // Complete that event
                }
                break;
            case BEEKEEPER:
                HandleBeekeeperEvent();
                break;
            case GENIE:
                HandleDialogueOnlyEvent("Genie");
                break;
            case COUNT_CHECK:
                HandleDialogueOnlyEvent("Count Check");
                break;
            case DRUNKEN_DWARF:
                HandleDialogueOnlyEvent("Drunken Dwarf");
                break;
            case GRAVEDIGGER:
                HandleGravediggerEvent();
                break;
            case DUNCE:
                HandleDunceEvent();
                break;
            case SANDWICH_LADY:
                HandleSandwichLadyEvent();
                break;
            case DRILL_SERGEANT:
                HandleDrillSergeant();
                break;
            case FREAKY_FORESTER:

                break;
        }
    }

    private void HandleDialogueOnlyEvent(String npcName)
    {
        // Checking if dialogue is open
        if (Rs2Dialogue.isInDialogue())
        {
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
        }
        else
        {
            if (talkedToEvent)
            {
                activeEvent = RandomEvent.NONE;
                talkedToEvent = false;
                Microbot.handlingRandomEvent = false;
            }
            else
            {
                Rs2NpcModel npc = Rs2Npc.getNpc(npcName);
                Rs2Npc.interact(npc, "Talk-to");
                talkedToEvent = true;
            }
        }
    }

    private void HandleDunceEvent()
    {

    }

    enum DrillExercise{
        NULL("null"),
        SIT_UPS("sit ups"),
        STAR_JUMPS("star jumps"),
        PUSH_UPS("push ups"),
        JOG("jog");

        final String exerciseText;

        DrillExercise(String _text) {
            exerciseText = _text;
        }
    }
    DrillExercise exerciseToDo = DrillExercise.NULL;

    private void HandleDrillSergeant()
    {
        if (Rs2Widget.hasWidget("Sir, yes sir!"))
        {
            Rs2Widget.clickWidget("Sir, yes sir!");
            return;
        }

        // Find out how we can tell the event is ending. Text saying good job?

        if (Rs2Widget.hasWidget("Go to this mat"))
        {
            Widget infoWidget = Rs2Widget.findWidget("Go to this mat");
            if (infoWidget.getText().contains("sit ups"))
            {
                exerciseToDo = DrillExercise.SIT_UPS;
            }
            else if (infoWidget.getText().contains("push ups"))
            {
                exerciseToDo = DrillExercise.PUSH_UPS;
            }
            else if (infoWidget.getText().contains("star jumps"))
            {
                exerciseToDo = DrillExercise.STAR_JUMPS;
            }
            else if (infoWidget.getText().contains("jog"))
            {
                exerciseToDo = DrillExercise.JOG;
            }
        }
        else if (Rs2Widget.hasWidget("Get on that mat"))
        {
            Widget infoWidget = Rs2Widget.findWidget("Get on that mat");
            if (infoWidget.getText().contains("sit ups"))
            {
                exerciseToDo = DrillExercise.SIT_UPS;
            }
            else if (infoWidget.getText().contains("push ups"))
            {
                exerciseToDo = DrillExercise.PUSH_UPS;
            }
            else if (infoWidget.getText().contains("star jumps"))
            {
                exerciseToDo = DrillExercise.STAR_JUMPS;
            }
            else if (infoWidget.getText().contains("jog"))
            {
                exerciseToDo = DrillExercise.JOG;
            }
        }
        else if (Rs2Widget.hasWidget("I want to see"))
        {
            Widget infoWidget = Rs2Widget.findWidget("I want to see");
            if (infoWidget.getText().contains("sit ups"))
            {
                exerciseToDo = DrillExercise.SIT_UPS;
            }
            else if (infoWidget.getText().contains("push ups"))
            {
                exerciseToDo = DrillExercise.PUSH_UPS;
            }
            else if (infoWidget.getText().contains("star jumps"))
            {
                exerciseToDo = DrillExercise.STAR_JUMPS;
            }
            else if (infoWidget.getText().contains("jog"))
            {
                exerciseToDo = DrillExercise.JOG;
            }
        }
        else if (Rs2Widget.hasWidget("Wrong exercise"))
        {
            Widget infoWidget = Rs2Widget.findWidget("Wrong exercise");
            if (infoWidget.getText().contains("sit ups"))
            {
                exerciseToDo = DrillExercise.SIT_UPS;
            }
            else if (infoWidget.getText().contains("push ups"))
            {
                exerciseToDo = DrillExercise.PUSH_UPS;
            }
            else if (infoWidget.getText().contains("star jumps"))
            {
                exerciseToDo = DrillExercise.STAR_JUMPS;
            }
            else if (infoWidget.getText().contains("jog"))
            {
                exerciseToDo = DrillExercise.JOG;
            }
        }
        else if (Rs2Widget.hasWidget("Get yourself over there"))
        {
            Widget infoWidget = Rs2Widget.findWidget("Get yourself over there");
            if (infoWidget.getText().contains("sit ups"))
            {
                exerciseToDo = DrillExercise.SIT_UPS;
            }
            else if (infoWidget.getText().contains("push ups"))
            {
                exerciseToDo = DrillExercise.PUSH_UPS;
            }
            else if (infoWidget.getText().contains("star jumps"))
            {
                exerciseToDo = DrillExercise.STAR_JUMPS;
            }
            else if (infoWidget.getText().contains("jog"))
            {
                exerciseToDo = DrillExercise.JOG;
            }
        }
        else if (Rs2Widget.hasWidget("Drop and give me"))
        {
            Widget infoWidget = Rs2Widget.findWidget("Drop and give me");
            if (infoWidget.getText().contains("sit ups"))
            {
                exerciseToDo = DrillExercise.SIT_UPS;
            }
            else if (infoWidget.getText().contains("push ups"))
            {
                exerciseToDo = DrillExercise.PUSH_UPS;
            }
            else if (infoWidget.getText().contains("star jumps"))
            {
                exerciseToDo = DrillExercise.STAR_JUMPS;
            }
            else if (infoWidget.getText().contains("jog"))
            {
                exerciseToDo = DrillExercise.JOG;
            }
        }
        else if (Rs2Widget.hasWidget("you actually did it"))
        {
            activeEvent = RandomEvent.NONE;
            Microbot.handlingRandomEvent = false;
        }
        Microbot.log("Exercise to do: " + exerciseToDo);

        if (Rs2Player.isAnimating(3000))
        {
            return;
        }
        if (Rs2Player.isMoving())
        {
            return;
        }

        switch (exerciseToDo)
        {
            case JOG:
                if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_1) == 1)
                {
                    Rs2GameObject.interact(20810, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_2) == 1)
                {
                    Rs2GameObject.interact(16508, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_3) == 1)
                {
                    Rs2GameObject.interact(9313, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_4) == 1)
                {
                    Rs2GameObject.interact(20801, "Use");
                }
                else
                {
                    Microbot.log("Couldn't identify exercise mat to use!");
                }
                break;
            case SIT_UPS:
                if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_1) == 2)
                {
                    Rs2GameObject.interact(20810, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_2) == 2)
                {
                    Rs2GameObject.interact(16508, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_3) == 2)
                {
                    Rs2GameObject.interact(9313, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_4) == 2)
                {
                    Rs2GameObject.interact(20801, "Use");
                }
                else
                {
                    Microbot.log("Couldn't identify exercise mat to use!");
                }
                break;
            case PUSH_UPS:
                if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_1) == 3)
                {
                    Rs2GameObject.interact(20810, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_2) == 3)
                {
                    Rs2GameObject.interact(16508, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_3) == 3)
                {
                    Rs2GameObject.interact(9313, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_4) == 3)
                {
                    Rs2GameObject.interact(20801, "Use");
                }
                else
                {
                    Microbot.log("Couldn't identify exercise mat to use!");
                }
                break;
            case STAR_JUMPS:
                if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_1) == 4)
                {
                    Rs2GameObject.interact(20810, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_2) == 4)
                {
                    Rs2GameObject.interact(16508, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_3) == 4)
                {
                    Rs2GameObject.interact(9313, "Use");
                }
                else if (Microbot.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_4) == 4)
                {
                    Rs2GameObject.interact(20801, "Use");
                }
                else
                {
                    Microbot.log("Couldn't identify exercise mat to use!");
                }
                break;
            case NULL:
                Microbot.log("No exercise to do!");
                break;
        }
    }

    enum SandwichLadyFoodItem {
        BAGUETTE("baguette", 10726),
        BREAD_ROLL("bread roll", 10727),
        CHOCOLATE_BAR("chocolate bar", 10728),
        KEBAB("kebab", 10729),
        PIE("meat pie", 10730),
        SQUARE_SANDWICH("square sandwich", 10731),
        TRIANGLE_SANDWICH("triangle sandwich", 10732);

        final String foodText;
        final int modelID;

        SandwichLadyFoodItem(String _foodText, int _modelID) {
            this.foodText = _foodText;
            this.modelID = _modelID;
        }
    }

    private void HandleSandwichLadyEvent()
    {
        // Selectable food widgets: 297 6->12
        // ModelIDs
        // Pie: 10730
        // Triangle sandwich: 10732
        // Bread/roll: 10727
        // Baguette: 10726
        // Chocolate bar: 10728
        // Kebab: 10729
        // Square sandwich: 10731
        // 297 3 :: Exit
        // 297 5 :: Sandwich lady window open
        // 297 2 Text: contains food item to select "chocolate bar"
        boolean chosenFoodItem = false;
        if (Rs2Widget.isWidgetVisible(297, 5))
        {
            Widget infoTextWidget = Rs2Widget.getWidget(297, 2);
            SandwichLadyFoodItem targetFoodItem = null;
            for (SandwichLadyFoodItem foodItem : SandwichLadyFoodItem.values())
            {
                if (infoTextWidget.getText().contains(foodItem.foodText))
                {
                    targetFoodItem = foodItem;
                }
            }
            if (targetFoodItem != null)
            {
                for (int i = 0; i < 6; i++)
                {
                    Widget foodWidget = Rs2Widget.getWidget(297, 6 + i);
                    // compare this widget with what food we need to get.
                    if (foodWidget.getModelId() == targetFoodItem.modelID)
                    {
                        Rs2Widget.clickWidget(foodWidget);
                        chosenFoodItem = true;
                        break;
                    }
                }
            }
        }
        if (chosenFoodItem)
        {
            activeEvent = RandomEvent.NONE;
            Microbot.handlingRandomEvent = false;
        }
    }

    private void HandleBeekeeperEvent()
    {
        // Bee Keeper
        // Talk to beekeeper, continue once, press 1.
        // Skip chat (4 dialogues)
        // Beekeeper Event WidgetID: N 420.0 | 27525120
        // Beehive part 1 ID: S 420.10 | 27525130
        // Beehive part 2 ID: S 420.11 | 27525131
        // Beehive part 3 ID: S 420.12 | 27525132
        // Beehind part 4 ID: S 420.13 | 27525133
        // BeehiveLid       ModelID: 28806
        // BeehiveBody      ModelID: 28428
        // BeehiveEntrance  ModelID: 28803
        // BeehiveLegs      ModelID: 28808
        // Beehive Lid Area ID: S 420.15 | 27525135
        // Beehive Body Area ID: S 420.17 | 27525137
        // Beehive Entrance Area ID: S 420.19 | 27525139
        // Beehive Legs Area ID: S 420.21 | 27525141
        if (Rs2Widget.isWidgetVisible(420, 0))
        {
            //Microbot.log("Beekeeper Widget is visible");
            Widget lidDropWidget = Rs2Widget.getWidget(420, 15);
            Widget bodyDropWidget = Rs2Widget.getWidget(420, 17);
            Widget entranceDropWidget = Rs2Widget.getWidget(420, 19);
            Widget legsDropWidget = Rs2Widget.getWidget(420, 21);

            Widget lidWidget = null, bodyWidget = null, entranceWidget = null, legsWidget = null;

            Point hiveLidLocation = null, hiveBodyLocation = null, hiveEntranceLocation = null, hiveLegsLocation = null;
            Point lidDestination = lidDropWidget.getCanvasLocation();
            Point bodyDestination = bodyDropWidget.getCanvasLocation();
            Point entranceDestination = entranceDropWidget.getCanvasLocation();
            Point legsDestination = legsDropWidget.getCanvasLocation();

            for (int i = 0; i < 4; i++)
            {
                Widget widget = Rs2Widget.getWidget(420, (10 + i));
                if (widget.getModelId() == 28806)
                {
                    lidWidget = widget;
                    hiveLidLocation = widget.getCanvasLocation();
                }
                else if (widget.getModelId() == 28428)
                {
                    bodyWidget = widget;
                    hiveBodyLocation = widget.getCanvasLocation();
                }
                else if (widget.getModelId() == 28803)
                {
                    entranceWidget = widget;
                    hiveEntranceLocation = widget.getCanvasLocation();
                }
                else if (widget.getModelId() == 28808)
                {
                    legsWidget = widget;
                    hiveLegsLocation = widget.getCanvasLocation();
                }
            }

            if (hiveLidLocation != null)
            {
                Rectangle rect = new Rectangle(lidWidget.getCanvasLocation().getX(), lidWidget.getCanvasLocation().getY(),
                        lidWidget.getWidth(), lidWidget.getHeight());
                Point randPoint1 = CUtil.GetRandomPointInRectangle(rect);

                Rectangle rect2 = new Rectangle(lidDropWidget.getCanvasLocation().getX(), lidDropWidget.getCanvasLocation().getY(),
                        lidDropWidget.getWidth(), lidDropWidget.getHeight());
                Point randPoint2 = CUtil.GetRandomPointInRectangle(rect2);
                Microbot.getMouse().drag(randPoint1, randPoint2);
            }
            if (hiveBodyLocation != null)
            {
                Rectangle rect = new Rectangle(bodyWidget.getCanvasLocation().getX(), bodyWidget.getCanvasLocation().getY(),
                        bodyWidget.getWidth(), bodyWidget.getHeight());
                Point randPoint1 = CUtil.GetRandomPointInRectangle(rect);

                Rectangle rect2 = new Rectangle(bodyDropWidget.getCanvasLocation().getX(), bodyDropWidget.getCanvasLocation().getY(),
                        bodyDropWidget.getWidth(), bodyDropWidget.getHeight());
                Point randPoint2 = CUtil.GetRandomPointInRectangle(rect2);
                Microbot.getMouse().drag(randPoint1, randPoint2);
            }
            if (hiveEntranceLocation != null)
            {
                Rectangle rect = new Rectangle(entranceWidget.getCanvasLocation().getX(), entranceWidget.getCanvasLocation().getY(),
                        entranceWidget.getWidth(), entranceWidget.getHeight());
                Point randPoint1 = CUtil.GetRandomPointInRectangle(rect);

                Rectangle rect2 = new Rectangle(entranceDropWidget.getCanvasLocation().getX(), entranceDropWidget.getCanvasLocation().getY(),
                        entranceDropWidget.getWidth(), entranceDropWidget.getHeight());
                Point randPoint2 = CUtil.GetRandomPointInRectangle(rect2);
                Microbot.getMouse().drag(randPoint1, randPoint2);
            }
            if (hiveLegsLocation != null)
            {
                Rectangle rect = new Rectangle(legsWidget.getCanvasLocation().getX(), legsWidget.getCanvasLocation().getY(),
                        legsWidget.getWidth(), legsWidget.getHeight());
                Point randPoint1 = CUtil.GetRandomPointInRectangle(rect);

                Rectangle rect2 = new Rectangle(legsDropWidget.getCanvasLocation().getX(), legsDropWidget.getCanvasLocation().getY(),
                        legsDropWidget.getWidth(), legsDropWidget.getHeight());
                Point randPoint2 = CUtil.GetRandomPointInRectangle(rect2);
                Microbot.getMouse().drag(randPoint1, randPoint2);
            }

            if (lidDropWidget.getModelId() == 28806
            && bodyDropWidget.getModelId() == 28428
            && entranceDropWidget.getModelId() == 28803
            && legsDropWidget.getModelId() == 28808)
            {
                Rs2Widget.clickWidget(420, 22);
                if (Rs2Dialogue.isInDialogue())
                {
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                }
                else
                {
                    activeEvent = RandomEvent.NONE;
                    Microbot.handlingRandomEvent = false;
                }
            }
        }
        else
        {
            //Microbot.log("Beekeeper Widget is NOT visible");
            // Beekeeper widget is not open

            // Checking if we have dialogue options
            if (Rs2Widget.isWidgetVisible(219, 1))
            {
                // Press key 2
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                Rs2Widget.clickWidget(Rs2Widget.getWidget(219, 1).getChild(2));
            }
            else
            {
                // Checking if dialogue is open
                if (Rs2Widget.isWidgetVisible(231, 0) || Rs2Widget.isWidgetVisible(217, 0))
                {
                    //Microbot.log("We are talking");
                    // We are talking with beekeeper
                    Rs2Dialogue.clickContinue();
                }
                else
                {
                    Rs2NpcModel npc = Rs2Npc.getNpc("Bee keeper");
                    Rs2Npc.interact(npc, "Talk-to");
                }
            }
            //NPC Chat Widget ID: S 231.6 DIALOG_NPC_TEXT Id: 15138822

        }
    }

    enum GraveProfessions{
        MINER,
        WOODCUTTER,
        POTTER,
        FARMER,
        BAKER
    }
    GraveProfessions gravestone1Profession = null;  // Stonehead ObjectID: 9360 | Grave ObjectID: 9365
    GraveProfessions gravestone2Profession = null;  // Stonehead ObjectID: 9362 | Grave ObjectID: 9367
    GraveProfessions gravestone3Profession = null;  // Stonehead ObjectID: 9359 | Grave ObjectID: 9364
    GraveProfessions gravestone4Profession = null;  // Stonehead ObjectID: 9361 | Grave ObjectID: 9366
    GraveProfessions gravestone5Profession = null;  // Stonehead ObjectID: 9363 | Grave ObjectID: 10049
    // Target WidgetID in stonehead: 175.1
    // Woodcutter ModelID: 13399
    // Miner ModelID: 16035
    // Potter ModelID: 13403
    // Farmer ModelID: 13402
    // Baker ModelID: 13404

    boolean grave1Looted = false; // ItemID: 7589 | BAKER
    boolean grave2Looted = false; // ItemID: 7588 | MINER
    boolean grave3Looted = false; // ItemID: 7587 | POTTER
    boolean grave4Looted = false; // ItemID: 7591 | WOODCUTTER
    boolean grave5Looted = false; // ItemID: 7590 | FARMER

    boolean grave1Fixed = false;
    boolean grave2Fixed = false;
    boolean grave3Fixed = false;
    boolean grave4Fixed = false;
    boolean grave5Fixed = false;
    boolean graveEventFinished = false;

    private void HandleGravediggerEvent()
    {
        if (Rs2Dialogue.hasSelectAnOption())
        {

            if (graveEventFinished)
            {
                // Replace keyboard event with Select Option "I'm done mate or whatever"
                Rs2Keyboard.keyPress('1');
                activeEvent = RandomEvent.NONE;
                graveEventFinished = false;
                Microbot.handlingRandomEvent = false;
            }
            else
            {
                // Replace keyboard event with Select Option "Yeah, let's do it. I'll fix ya grave mate"
                Rs2Keyboard.keyPress('1');
            }
        }
        else if (Rs2Dialogue.isInDialogue())
        {
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);

            if (graveEventFinished)
            {
                gravestone1Profession = null;
                gravestone2Profession = null;
                gravestone3Profession = null;
                gravestone4Profession = null;
                gravestone5Profession = null;
                grave1Looted = false;
                grave2Looted = false;
                grave3Looted = false;
                grave4Looted = false;
                grave5Looted = false;
                grave1Fixed = false;
                grave2Fixed = false;
                grave3Fixed = false;
                grave4Fixed = false;
                grave5Fixed = false;
            }
        }
        else
        {
            if (Rs2Widget.isWidgetVisible(220, 16))
            {
                Rs2Widget.clickWidget(220, 16);
            }
            if (gravestone1Profession == null)
            {
                if (Rs2Widget.isWidgetVisible(175, 1))
                {
                    gravestone1Profession = GetGravestoneWidgetProfession();
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                    Rs2Widget.clickWidget(175, 2);
                }
                else
                {
                    Rs2GameObject.interact(9360, "Read");
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                }
            }
            else if (gravestone2Profession == null)
            {
                if (Rs2Widget.isWidgetVisible(175, 1))
                {
                    gravestone2Profession = GetGravestoneWidgetProfession();
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                    Rs2Widget.clickWidget(175, 2);
                }
                else
                {
                    Rs2GameObject.interact(9362, "Read");
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                }
            }
            else if (gravestone3Profession == null)
            {
                if (Rs2Widget.isWidgetVisible(175, 1))
                {
                    gravestone3Profession = GetGravestoneWidgetProfession();
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                    Rs2Widget.clickWidget(175, 2);
                }
                else
                {
                    Rs2GameObject.interact(9359, "Read");
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                }
            }
            else if (gravestone4Profession == null)
            {
                if (Rs2Widget.isWidgetVisible(175, 1))
                {
                    gravestone4Profession = GetGravestoneWidgetProfession();
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                    Rs2Widget.clickWidget(175, 2);
                }
                else
                {
                    Rs2GameObject.interact(9361, "Read");
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                }
            }
            else if (gravestone5Profession == null)
            {
                if (Rs2Widget.isWidgetVisible(175, 1))
                {
                    gravestone5Profession = GetGravestoneWidgetProfession();
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                    Rs2Widget.clickWidget(175, 2);
                }
                else
                {
                    Rs2GameObject.interact(9363, "Read");
                    int sleepBetween = 600 + (int) (Math.random() * (2000 - 600));
                    sleep(sleepBetween);
                }
            }
            else
            {
                if (!Rs2Player.isMoving())
                {
                    // All grave stoneheads should have been identified
                    if (!grave1Looted)
                    {
                        Rs2GameObject.interact(9365, "Take-Coffin");
                        grave1Looted = true;
                    }
                    else if (!grave2Looted)
                    {
                        Rs2GameObject.interact(9367, "Take-Coffin");
                        grave2Looted = true;
                    }
                    else if (!grave3Looted)
                    {
                        Rs2GameObject.interact(9364, "Take-Coffin");
                        grave3Looted = true;
                    }
                    else if (!grave4Looted)
                    {
                        Rs2GameObject.interact(9366, "Take-Coffin");
                        grave4Looted = true;
                    }
                    else if (!grave5Looted)
                    {
                        Rs2GameObject.interact(10049, "Take-Coffin");
                        grave5Looted = true;
                    }
                    else if (!grave1Fixed)
                    {
                        if (Rs2Player.isAnimating() || Rs2Player.isMoving()) return;
                        PlaceCoffinInGrave(gravestone1Profession, 10051);
                        grave1Fixed = true;
                    }
                    else if (!grave2Fixed)
                    {
                        if (Rs2Player.isAnimating() || Rs2Player.isMoving()) return;
                        PlaceCoffinInGrave(gravestone2Profession, 10053);
                        grave2Fixed = true;
                    }
                    else if (!grave3Fixed)
                    {
                        if (Rs2Player.isAnimating() || Rs2Player.isMoving()) return;
                        PlaceCoffinInGrave(gravestone3Profession, 10050);
                        grave3Fixed = true;
                    }
                    else if (!grave4Fixed)
                    {
                        if (Rs2Player.isAnimating() || Rs2Player.isMoving()) return;
                        PlaceCoffinInGrave(gravestone4Profession, 10052);
                        grave4Fixed = true;
                    }
                    else if (!grave5Fixed)
                    {
                        if (Rs2Player.isAnimating() || Rs2Player.isMoving()) return;
                        PlaceCoffinInGrave(gravestone5Profession, 10054);
                        grave5Fixed = true;

                    }
                    else
                    {
                        Rs2Npc.interact("Leo", "Talk-to");
                        graveEventFinished = true;
                    }
                }
            }
        }
    }

    private GraveProfessions GetGravestoneWidgetProfession()
    {
        Widget widget = Rs2Widget.getWidget(175, 1);
        if (widget != null)
        {
            if (widget.getModelId() == 13399)   // Woodcutter
            {
                return GraveProfessions.WOODCUTTER;
            }
            else if (widget.getModelId() == 16035)  // Miner
            {
                return GraveProfessions.MINER;
            }
            else if (widget.getModelId() == 13403)  // Potter
            {
                return GraveProfessions.POTTER;
            }
            else if (widget.getModelId() == 13402)  // Farmer
            {
                return GraveProfessions.FARMER;
            }
            else if (widget.getModelId() == 13404)  // Baker
            {
                return GraveProfessions.BAKER;
            }
        }
        return null;
    }

    private void PlaceCoffinInGrave(GraveProfessions _graveProfession, int _graveObjectID)
    {
        int sleepBetween;
        switch (_graveProfession)
        {
            case BAKER:
                //Rs2Inventory.use(7589);
                Rs2Inventory.useItemOnObject(7589, _graveObjectID);
                //sleepBetween = 2000 + (int) (Math.random() * (4000 - 2000));
                //sleep(sleepBetween);
                break; case WOODCUTTER://Rs2Inventory.use(7591);
            Rs2Inventory.useItemOnObject(7591, _graveObjectID);
            //sleepBetween = 2000 + (int) (Math.random() * (4000 - 2000));
            //sleep(sleepBetween);
            break;
            case FARMER:
                //Rs2Inventory.use(7590);
                Rs2Inventory.useItemOnObject(7590, _graveObjectID);
                //sleepBetween = 2000 + (int) (Math.random() * (4000 - 2000));
                //sleep(sleepBetween);
                break; case POTTER://Rs2Inventory.use(7587);
            Rs2Inventory.useItemOnObject(7587, _graveObjectID);
            //sleepBetween = 2000 + (int) (Math.random() * (4000 - 2000));
            //sleep(sleepBetween);
            break;
            case MINER:
                //Rs2Inventory.use(7588);
                Rs2Inventory.useItemOnObject(7588, _graveObjectID);
                //sleepBetween = 2000 + (int) (Math.random() * (4000 - 2000));
                //sleep(sleepBetween);
                break;
        }
    }
}
