package com.demigodsrpg.stoa;

import com.demigodsrpg.stoa.controller.CharacterController;
import com.demigodsrpg.stoa.controller.PlayerController;
import com.demigodsrpg.stoa.data.StoaWorld;
import com.demigodsrpg.stoa.mythos.Mythos;
import org.bukkit.conversations.ConversationFactory;

import java.util.Collection;

/**
 * Utility class for all of Demigods.
 */
public class Stoa
{
	// -- CONSTANTS -- //

	private static final StoaServer STOA_SERVER = new StoaServer();
	private static final ConversationFactory CONVERSATION_FACTORY = new ConversationFactory(StoaPlugin.getInst());

	// -- CONSTRUCTOR -- //

	private Stoa()
	{
	}

	// -- GETTERS FOR OTHER MANAGERS/HANDLERS/HOLDERS -- //

	public static StoaServer getServer()
	{
		return STOA_SERVER;
	}

	public static ConversationFactory getConversationFactory()
	{
		return CONVERSATION_FACTORY;
	}

	public static Mythos getMythos()
	{
		return getServer().getMythos();
	}

	// -- PASS UP DATA FROM STOA SERVER CLASS -- //

	public static Collection<PlayerController> getOnlinePlayers()
	{
		return getServer().getOnlinePlayers();
	}

	public static Collection<CharacterController> getOnlineCharacters()
	{
		return getServer().getOnlineCharacters();
	}

	public static StoaWorld getWorld(String name)
	{
		return getServer().getWorld(name);
	}

	public static Collection<StoaWorld> getWorlds()
	{
		return getServer().getWorlds();
	}
}
