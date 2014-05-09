package com.demigodsrpg.stoa;

import com.demigodsrpg.stoa.data.StoaWorld;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.entity.player.StoaPlayer;
import com.demigodsrpg.stoa.mythos.Mythos;
import org.bukkit.conversations.ConversationFactory;

import java.util.Collection;

/**
 * Utility class for all of Demigods.
 */
public class Stoa
{
	// -- CONSTANTS -- //
	private static final StoaServer DEMIGODS_SERVER = new StoaServer();
	private static final ConversationFactory CONVERSATION_FACTORY = new ConversationFactory(StoaPlugin.getInst());

	// -- CONSTRUCTOR -- //

	private Stoa()
	{
	}

	// -- GETTERS FOR OTHER MANAGERS/HANDLERS/HOLDERS -- //

	public static StoaServer getServer()
	{
		return DEMIGODS_SERVER;
	}

	public static ConversationFactory getConversationFactory()
	{
		return CONVERSATION_FACTORY;
	}

	public static Mythos getMythos()
	{
		return getServer().getMythos();
	}

	// -- PASS UP DATA FROM DEMIGODS SERVER CLASS -- //

	public static Collection<StoaPlayer> getOnlinePlayers()
	{
		return getServer().getOnlinePlayers();
	}

	public static Collection<StoaCharacter> getOnlineCharacters()
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
