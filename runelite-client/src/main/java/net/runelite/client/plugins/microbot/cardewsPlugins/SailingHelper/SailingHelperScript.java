package net.runelite.client.plugins.microbot.cardewsPlugins.SailingHelper;

import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.misc.Rs2UiHelper;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SailingHelperScript extends Script {
    // **********   AUTO PORT TASK PROPERTIES   ****************************************
    public enum AutoPortTaskStates{
        NULL,
        DETERMINE_CURRENT_PORT,
        GETTING_TASKS_FROM_BOARD,
        GO_TO_CARGO_PICKUP_DESTINATION,
        ACTIVE_TASK_BEGIN,
        ACTIVE_TASK_SAIL_TO_DESTINATION,
        ACTIVE_TASK_FINISH,
        FINISHED_TASK_DECIDING_NEXT_STATE
    }
    public static AutoPortTaskStates currentAutoPortTaskState = AutoPortTaskStates.DETERMINE_CURRENT_PORT;

    //PortLocation currentPortLocation = null;
    //PortTaskData activePortTasks = null;

    int cargoLoaded = 0;
    //BoatPathFollower boatPather = null;
    //PortPaths pathToTake = null;

    // ********** END AUTO PORT TASK PROPERTIES ****************************************

    // **********   AUTO TRIM SAILS PROPERTIES   ****************************************
    public static AtomicBoolean receivedGustOfWind = new AtomicBoolean(false);
    // ********** END AUTO TRIM SAILS PROPERTIES ****************************************

    public boolean run(SailingHelperConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Rs2AntibanSettings.microBreakActive) return;
                //long startTime = System.currentTimeMillis();

                //if (config.ShouldAutoTrimSails())
                //{
                //    if (receivedGustOfWind.get())
                //    {
                //        //Microbot.log("We are able to Trim our Sails!");
                //        Rs2Sailing.trimSails();
                //        receivedGustOfWind.set(false);
                //    }
                //}

                // Auto complete port courier tasks
                //switch (currentAutoPortTaskState)
                //{
                //    case DETERMINE_CURRENT_PORT:
                //        PortLocation nearbyPort = GetNearbyPortLocation();
                //        if (currentPortLocation != nearbyPort)
                //        {
                //            currentPortLocation = nearbyPort;
                //        }
//
                //        if (currentPortLocation != PortLocation.EMPTY)
                //        {
                //            // We are at a port currently.
                //            if (activePortTasks != null)
                //            {
                //                if (currentPortLocation.equals(activePortTasks.getCargoLocation()))
                //                {
                //                    currentAutoPortTaskState = AutoPortTaskStates.ACTIVE_TASK_BEGIN;
                //                }
                //                else
                //                {
                //                    currentAutoPortTaskState = AutoPortTaskStates.GO_TO_CARGO_PICKUP_DESTINATION;
                //                }
                //            }
                //            else
                //            {
                //                currentAutoPortTaskState = AutoPortTaskStates.GETTING_TASKS_FROM_BOARD;
                //            }
                //        }
                //        break;
//
                //    case GETTING_TASKS_FROM_BOARD:
                //        if (!Rs2Player.getSkillRequirement(Skill.SAILING, currentPortLocation.getSailingLevelRequired()))
                //        {
                //            Microbot.showMessage("Sailing level requirement for this Port not met.");
                //            Microbot.stopPlugin(SailingHelperPlugin.class);
                //            return;
                //        }
                //        // Do we have an active task
                //        if (activePortTasks == null)
                //        {
                //            // We have no port tasks currently
                //            // If the notice board is not open
                //            if (!Rs2Widget.isWidgetVisible(941, 1))
                //            {
                //                // Open notice board to view tasks
                //                Rs2GameObject.interact(currentPortLocation.getNoticeboardObject(), "inspect");
                //                sleepUntil(() -> Rs2Widget.isWidgetVisible(941, 1));
                //                // Port Task Container Widget ID: 941, 3
                //                // Some of the children widgets under 941, 3, have text in the Name property providing the task names.
                //                //
                //                Widget portTaskContainerWidget = Rs2Widget.getWidget(941, 3);
                //                Widget[] portTaskContainerWidgetChildren = portTaskContainerWidget.getChildren();
                //                assert portTaskContainerWidgetChildren != null;
                //                List<PortTaskData> availablePossibleTasks = new ArrayList<>();
                //                Map<PortTaskData, Widget> availablePossibleTasksMap = new HashMap<>();
                //                for (Widget widget : portTaskContainerWidgetChildren)
                //                {
                //                    if (!widget.getName().isEmpty())
                //                    {
                //                        for (PortTaskData taskData : PortTaskData.values())
                //                        {
                //                            if (taskData.taskName.equalsIgnoreCase(Text.removeTags(widget.getName()))
                //                            && taskData.getNoticeBoard().equals(currentPortLocation)
                //                            && Rs2Player.getSkillRequirement(Skill.SAILING, taskData.getLevelRequired()))
                //                            {
                //                                Microbot.log("Widget Name: " + widget.getName());
                //                                Microbot.log("TaskName matches widget Name, is from your port's notice board, & Player has level required!");
                //                                availablePossibleTasks.add(taskData);
                //                                availablePossibleTasksMap.put(taskData, widget);
                //                                break;
                //                            }
                //                        }
                //                    }
                //                }
                //                // Get highest level tasks available
                //                List<PortTaskData> highestLevelAvailableTasks = new ArrayList<>();
                //                PortTaskData highestLevelTaskData = null;
                //                for (PortTaskData taskData : availablePossibleTasks)
                //                {
                //                    if (highestLevelTaskData == null)
                //                    {
                //                        highestLevelTaskData = taskData;
                //                    }
                //                    if (taskData.getLevelRequired() > highestLevelTaskData.getLevelRequired())
                //                    {
                //                        highestLevelAvailableTasks.clear();
                //                        highestLevelTaskData = taskData;
                //                        highestLevelAvailableTasks.add(highestLevelTaskData);
                //                        break;
                //                    }
                //                    if (taskData.getLevelRequired() == highestLevelTaskData.getLevelRequired())
                //                    {
                //                        highestLevelTaskData = taskData;
                //                        highestLevelAvailableTasks.add(highestLevelTaskData);
                //                    }
                //                }
                //                if (!highestLevelAvailableTasks.isEmpty())
                //                {
                //                    activePortTasks = highestLevelAvailableTasks.get(Rs2Random.betweenInclusive(0, highestLevelAvailableTasks.size() - 1));
//
                //                    Microbot.log("Found a task to do: " + activePortTasks.taskName);
                //                    Microbot.log("This task needs level " + activePortTasks.getLevelRequired() + " to do.");
                //                    Microbot.log("Requires #" + activePortTasks.getCargoAmount() + " cargo!");
                //                    Microbot.log("Shipping from Port: " + activePortTasks.getCargoLocation().getName() + ", delivering at Port: " + activePortTasks.getDeliveryLocation().getName());
//
                //                    // NEED TO CLICK ON THE FRICKING PORT TASK TO SELECT IT
                //                    Widget widgetToClick = availablePossibleTasksMap.get(activePortTasks);
                //                    Rs2Widget.clickWidget(widgetToClick);
                //                    // Sleep until we have the task pop-up to accept task
                //                    //sleepUntil(() -> Rs2Widget.isWidgetVisible(941, 5));
                //                    sleepGaussian(1200, 200);
                //                    // Click accept
                //                    Widget acceptWidget = Rs2Widget.getWidget(61734922);
                //                    acceptWidget = Rs2Widget.findWidget("Accept task", false);
                //                    if (acceptWidget != null)
                //                    {
                //                        Microbot.log("Accept Widget is not null!");
                //                    }
                //                    else
                //                    {
                //                        Microbot.log("Accept Widget is null!");
                //                    }
                //                    Rs2Widget.clickWidget(acceptWidget);
                //                    sleepGaussian(600, 75);
//
                //                    currentAutoPortTaskState = AutoPortTaskStates.ACTIVE_TASK_BEGIN;
                //                    break;
                //                }
                //                else
                //                {
                //                    Microbot.showMessage("No available port task apparently?!");
                //                    Microbot.stopPlugin(SailingHelperPlugin.class);
                //                }
                //            }
                //        }
                //        break;
//
                //    case ACTIVE_TASK_BEGIN:
                //        if (currentPortLocation != GetNearbyPortLocation())
                //        {
                //            currentPortLocation = GetNearbyPortLocation();
                //        }
                //        // TODO: Check if we are at the correct port to collect cargo from.
                //        // Player Pose #'s when carrying cargo: 4193 (Carrying idle), 4194 (Carrying walk), 7274 (carrying run)
                //        if (currentPortLocation != null)
                //        {
                //            // Are we at the correct location to pickup cargo from?
                //            if (currentPortLocation.equals(activePortTasks.getCargoLocation()))
                //            {
                //                // WE ARE AT THE CORRECT PORT TO PICKUP CARGO FROM
                //                // If cargoLoaded is less than activePortTasks.cargoAmount
                //                if (cargoLoaded < activePortTasks.getCargoAmount())
                //                {
                //                    if (Rs2Player.getPoseAnimation() != 4193
                //                    && Rs2Player.getPoseAnimation() != 4194
                //                    && Rs2Player.getPoseAnimation() != 7274)
                //                    {
                //                        // If we are not in a pose indicating we are carrying cargo
                //                        GameObject ledger = Rs2GameObject.getGameObject(LedgerID.getObjectIdByName(currentPortLocation.getName()));
                //                        if (ledger != null)
                //                        {
                //                            if (Rs2Player.distanceTo(ledger.getWorldLocation()) > 5)
                //                            {
                //                                Rs2Walker.walkTo(ledger.getWorldLocation());
                //                            }
                //                            else
                //                            {
                //                                Rs2GameObject.interact(ledger, "Take-cargo");
                //                                if (sleepUntil(() -> Rs2Player.getPoseAnimation() == 4193))
                //                                {
                //                                    Microbot.log("Player has PoseAnimation 4193");
                //                                }
                //                            }
                //                        }
                //                    }
                //                    else    // Should be carrying cargo if in here
                //                    {
                //                        Rs2Sailing.boardBoat();
                //                        sleepUntil(Rs2Sailing::isOnBoat, 10000);
                //                        sleepGaussian(600, 150);
                //                        // Interact with ship cargo hold to deposit cargo. Increment cargoLoaded
                //                        // If we still have less cargoLoaded than cargoAmount; disembark and let it loop to load more cargo
                //                        Rs2Sailing.openCargo();
                //                        sleepGaussian(1200, 200);
                //                        cargoLoaded++;
                //                        Microbot.log("Cargo Loaded: " + cargoLoaded + " | Cargo Needed: " + activePortTasks.getCargoAmount());
                //                        if (cargoLoaded < activePortTasks.getCargoAmount())
                //                        {
                //                            // Disembark
                //                            Rs2Sailing.disembarkBoat();
                //                            sleepUntil(() -> !Rs2Sailing.isOnBoat());
                //                        }
                //                        else
                //                        {
                //                            currentAutoPortTaskState = AutoPortTaskStates.ACTIVE_TASK_SAIL_TO_DESTINATION;
                //                            break;
                //                        }
                //                    }
                //                }
                //                else
                //                {
                //                    currentAutoPortTaskState = AutoPortTaskStates.ACTIVE_TASK_SAIL_TO_DESTINATION;
                //                    break;
                //                }
                //            }
                //            else
                //            {
                //                // We are not at our cargo pickup location.
                //                currentAutoPortTaskState = AutoPortTaskStates.GO_TO_CARGO_PICKUP_DESTINATION;
                //                break;
                //            }
//
                //        }
                //        break;
//
                //    case GO_TO_CARGO_PICKUP_DESTINATION:
                //        if (!Rs2Sailing.isOnBoat())
                //        {
                //            if (currentPortLocation != GetNearbyPortLocation())
                //            {
                //                currentPortLocation = GetNearbyPortLocation();
                //            }
//
                //            Rs2Sailing.boardBoat();
                //            sleepUntil(Rs2Sailing::isOnBoat, 10000);
                //            sleepGaussian(1000, 200);
                //        }
                //        else
                //        {
                //            if (pathToTake == null)
                //            {
                //                for (PortPaths path : PortPaths.values())
                //                {
                //                    if (currentPortLocation.equals(path.getStart()))
                //                    {
                //                        if (activePortTasks.getCargoLocation().equals(path.getEnd()))
                //                        {
                //                            pathToTake = path;
                //                        }
                //                    }
                //                }
                //            }
                //            if (pathToTake != null)
                //            {
                //                if (boatPather != null)
                //                {
                //                    if (boatPather.loop())
                //                    {
                //                        boatPather = null;
                //                        pathToTake = null;
                //                        // We have finished this pathing.
                //                        Rs2Sailing.disembarkBoat();
                //                        currentAutoPortTaskState = AutoPortTaskStates.ACTIVE_TASK_BEGIN;
                //                        sleepUntil(() -> !Rs2Sailing.isOnBoat());
                //                        sleepGaussian(1000, 200);
                //                        break;
                //                    }
                //                }
                //                else
                //                {
                //                    boatPather = new BoatPathFollower(pathToTake.getFullPath(false));
                //                }
                //            }
                //        }
                //        break;
//
                //    case ACTIVE_TASK_SAIL_TO_DESTINATION:
                //        if (!Rs2Sailing.isNavigating())
                //        {
                //            Rs2Sailing.navigate();
                //            break;
                //        }
//
                //        if (boatPather != null)
                //        {
                //            if (boatPather.loop())
                //            {
                //                boatPather = null;
                //                currentAutoPortTaskState = AutoPortTaskStates.ACTIVE_TASK_FINISH;
                //                break;
                //            }
                //        }
                //        else
                //        {
                //            boatPather = new BoatPathFollower(activePortTasks.getDockMarkers().getFullPath(activePortTasks.isReversePath()));
                //        }
                //        break;
//
                //    case ACTIVE_TASK_FINISH:
                //        // Widget 943, 10 is widgetContainer for CargoHold Items
                //        // Iterate through the children to match item ID in the cargo hold with current port task cargo item ID
                //        // TODO: When unloading cargo, use the above comment info to get your cargo item
                //        // Cargo Hold close widget is the 12th child of 943, 1. Index 11 (0...11)
                //        if (cargoLoaded > 0)
                //        {
                //            if (Rs2Sailing.isOnBoat())
                //            {
                //                Rs2Sailing.openCargo();
                //                sleepGaussian(2000, 400);
                //                Widget cargoHoldContainerWidget = Rs2Widget.getWidget(943, 10);
                //                //sleepUntil(() -> Rs2Widget.isWidgetVisible(cargoHoldContainerWidget.getId()));
                //                //sleepUntil(() -> !cargoHoldContainerWidget.isHidden());
                //                Widget[] cargoHoldItemWidgets = cargoHoldContainerWidget.getChildren();
                //                assert cargoHoldItemWidgets != null;
                //                for (Widget itemWidget : cargoHoldItemWidgets)
                //                {
                //                    if (itemWidget.getItemId() == activePortTasks.cargo)
                //                    {
                //                        Rs2Widget.clickWidget(itemWidget);
                //                        sleepGaussian(1000, 400);
                //                        Rs2Sailing.disembarkBoat();
                //                        sleepUntil(() -> !Rs2Sailing.isOnBoat());
                //                        sleepGaussian(1800, 200);
                //                        break;
                //                    }
                //                }
                //            }
                //            else
                //            {
                //                if (currentPortLocation != GetNearbyPortLocation())
                //                {
                //                    if (!GetNearbyPortLocation().equals(PortLocation.EMPTY))
                //                    {
                //                        currentPortLocation = GetNearbyPortLocation();
                //                    }
                //                }
//
                //                GameObject ledger = Rs2GameObject.getGameObject(LedgerID.getObjectIdByName(currentPortLocation.getName()));
                //                if (ledger != null)
                //                {
                //                    Rs2GameObject.interact(ledger, "Deposit-cargo");
                //                    sleepUntil(() -> Rs2Player.getPoseAnimation() == 808);
                //                    cargoLoaded--;
                //                }
                //                else
                //                {
                //                    Microbot.log("Ledger is null.");
                //                }
                //            }
                //        }
                //        else
                //        {
                //            // We have unloaded all of our cargo, completing the task.
                //            currentAutoPortTaskState = AutoPortTaskStates.DETERMINE_CURRENT_PORT;
                //            activePortTasks = null;
                //            pathToTake = null;
                //            cargoLoaded = 0;
                //        }
                //        break;
//
                //    case FINISHED_TASK_DECIDING_NEXT_STATE:
//
                //        break;
                //}

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

    //PortLocation GetCurrentPortLocation()
    //{
    //    return currentPortLocation;
    //}

    //PortLocation GetNearbyPortLocation()
    //{
    //    for (PortLocation location : PortLocation.values())
    //    {
    //        // If the notice board object is within 50 tiles of the player
    //        // return this PortLocation
    //        GameObject portNoticeboard = Rs2GameObject.getGameObject(location.getNoticeboardObject());
    //        if (portNoticeboard != null)
    //        {
    //            // Enters statement if the Player is within 50 tiles of this ports notice board
    //            if (Rs2WorldPoint.quickDistance(Rs2Player.getWorldLocation(), portNoticeboard.getWorldLocation()) < 50)
    //            {
    //                return location;
    //            }
    //        }
    //    }
    //    return PortLocation.EMPTY;
    //}

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
