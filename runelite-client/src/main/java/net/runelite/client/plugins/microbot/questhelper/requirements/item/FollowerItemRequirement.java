/*
 *
 *  * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice, this
 *  *    list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package net.runelite.client.plugins.microbot.questhelper.requirements.item;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.api.Client;

import java.util.List;
import java.util.Objects;

public class FollowerItemRequirement extends ItemRequirement
{
	private final List<Integer> followerIDs;
	private final List<Integer> itemIDs;

	public FollowerItemRequirement(String name, List<Integer> itemIDs, List<Integer> followerIDs)
	{
		super(name, itemIDs);

		assert(itemIDs.stream().noneMatch(Objects::isNull));
		assert(followerIDs.stream().noneMatch(Objects::isNull));

		this.itemIDs = itemIDs;
		this.followerIDs = followerIDs;
	}

	public FollowerItemRequirement(String name, ItemCollections itemIDs, List<Integer> followerIDs)
	{
		super(name, itemIDs);

		assert(followerIDs.stream().noneMatch(Objects::isNull));

		this.itemIDs = itemIDs.getItems();
		this.followerIDs = followerIDs;
	}

	@Override
	protected FollowerItemRequirement copyOfClass()
	{
		return new FollowerItemRequirement(getName(), itemIDs, followerIDs);
	}

	@Override
	public boolean check(Client client)
	{
		boolean match = client.getTopLevelWorldView().npcs().stream()
			.filter(npc -> npc.getInteracting() != null) // we need this check because Client#getLocalPlayer is Nullable
			.filter(npc -> npc.getInteracting() == client.getLocalPlayer())
			.anyMatch(npc -> followerIDs.contains(npc.getId()));

		if (match)
		{
			return true;
		}

		return super.check(client);
	}
}
