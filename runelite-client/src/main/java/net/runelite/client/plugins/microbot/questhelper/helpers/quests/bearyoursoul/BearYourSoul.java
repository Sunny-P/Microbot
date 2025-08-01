/*
 * Copyright (c) 2020, Zoinkwiz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.bearyoursoul;

import net.runelite.client.plugins.microbot.questhelper.collections.KeyringCollection;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.KeyringRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class BearYourSoul extends BasicQuestHelper
{
	//Items Required
	ItemRequirement spade, dustyKeyOr70AgilOrKeyMasterTeleport, damagedSoulBearer;

	Requirement inTaverleyDungeon, inKeyMaster;

	QuestStep findSoulJourneyAndRead, talkToAretha, arceuusChurchDig, goToTaverleyDungeon, enterCaveToKeyMaster, speakKeyMaster;

	//Zones
	Zone inTaverleyDungeonZone, inKeyMasterZone;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		steps.put(0, findSoulJourneyAndRead);
		steps.put(1, talkToAretha);

		ConditionalStep repairSoulBearer = new ConditionalStep(this, arceuusChurchDig);
		repairSoulBearer.addStep(inKeyMaster, speakKeyMaster);
		repairSoulBearer.addStep(inTaverleyDungeon, enterCaveToKeyMaster);
		repairSoulBearer.addStep(damagedSoulBearer, goToTaverleyDungeon);

		steps.put(2, repairSoulBearer);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		dustyKeyOr70AgilOrKeyMasterTeleport =
		new KeyringRequirement("Dusty key, or another way to get into the deep Taverley Dungeon",
			configManager, KeyringCollection.DUSTY_KEY).isNotConsumed();
		spade = new ItemRequirement("Spade", ItemID.SPADE).isNotConsumed();
		damagedSoulBearer = new ItemRequirement("Damaged soul bearer", ItemID.ARCEUUS_SOULBEARER_DAMAGED);
	}

	@Override
	protected void setupZones()
	{
		inTaverleyDungeonZone = new Zone(new WorldPoint(2816, 9668, 0), new WorldPoint(2973, 9855, 0));
		inKeyMasterZone = new Zone(new WorldPoint(1289, 1236, 0), new WorldPoint(1333, 1274, 0));
	}

	public void setupConditions()
	{
		inTaverleyDungeon = new ZoneRequirement(inTaverleyDungeonZone);
		inKeyMaster = new ZoneRequirement(inKeyMasterZone);
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Collections.singletonList(new ItemReward("A Soul Bearer", ItemID.ARCEUUS_SOULBEARER, 1));
	}

	public void setupSteps()
	{
		findSoulJourneyAndRead = new DetailedQuestStep(this, new WorldPoint(1632, 3808, 0), "Go to the Arceuus library and find The Soul journey book in one of the bookcases, then read it. You can ask Biblia for help locating it, or make use of the Runelite Kourend Library plugin.");

		talkToAretha = new NpcStep(this, NpcID.ARCEUUS_SOULGUARDIAN, new WorldPoint(1814, 3851, 0),
			"Talk to Aretha at the Soul Altar.");
		talkToAretha.addDialogStep("I've been reading your book...");
		talkToAretha.addDialogStep("Yes please.");

		arceuusChurchDig = new DigStep(this, new WorldPoint(1699, 3794, 0), "Go to the Arceuus church and dig for the Damaged soul bearer.");

		goToTaverleyDungeon = new ObjectStep(this, ObjectID.LADDER_OUTSIDE_TO_UNDERGROUND, new WorldPoint(2884, 3397, 0), "Go to Taverley Dungeon, or teleport to the Key Master directly.", damagedSoulBearer, dustyKeyOr70AgilOrKeyMasterTeleport);

		enterCaveToKeyMaster = new ObjectStep(this, ObjectID.HELLHOUND_CAVE_ENTRANCE_A_01, new WorldPoint(2874, 9846, 0), "Enter the cave to the Key Master.", damagedSoulBearer, dustyKeyOr70AgilOrKeyMasterTeleport);

		speakKeyMaster = new NpcStep(this, NpcID.KEEPER_OF_KEYS, new WorldPoint(1310, 1251, 0),
			"Talk to Key Master in the Cerberus' Lair.", damagedSoulBearer);
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		List<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(spade);
		reqs.add(dustyKeyOr70AgilOrKeyMasterTeleport);
		return reqs;
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Find the Soul journey book", Collections.singletonList(findSoulJourneyAndRead)));
		allSteps.add(new PanelDetails("Talk to Aretha", Collections.singletonList(talkToAretha)));
		allSteps.add(new PanelDetails("Dig up the Soul Bearer", Collections.singletonList(arceuusChurchDig), spade));
		allSteps.add(new PanelDetails("Have the Soul Bearer repaired", Arrays.asList(goToTaverleyDungeon, enterCaveToKeyMaster, speakKeyMaster), dustyKeyOr70AgilOrKeyMasterTeleport));
		return allSteps;
	}
}
