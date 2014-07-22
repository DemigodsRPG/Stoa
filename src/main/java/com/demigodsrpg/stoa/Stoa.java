package com.demigodsrpg.stoa;

import com.demigodsrpg.stoa.item.ItemRegistry;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.PlayerModel;
import com.demigodsrpg.stoa.mythos.Mythos;
import org.bukkit.conversations.ConversationFactory;

import java.util.Collection;

/**
 * Utility class for all of Demigods.
 */
public class Stoa {
    // -- CONSTANTS -- //

    private static final StoaServer STOA_SERVER = new StoaServer();
    private static final ConversationFactory CONVERSATION_FACTORY = new ConversationFactory(StoaPlugin.getInst());

    // -- CONSTRUCTOR -- //

    private Stoa() {
    }

    // -- GETTERS FOR OTHER MANAGERS/HANDLERS/HOLDERS -- //

    public static StoaServer getServer() {
        return STOA_SERVER;
    }

    public static ConversationFactory getConversationFactory() {
        return CONVERSATION_FACTORY;
    }

    public static Mythos getMythos() {
        return getServer().getMythos();
    }

    public static ItemRegistry getItemRegistry() {
        return getServer().getItemRegistry();
    }

    // -- PASS UP DATA FROM STOA SERVER CLASS -- //

    public static Collection<PlayerModel> getOnlinePlayers() {
        return getServer().getOnlinePlayers();
    }

    public static Collection<CharacterModel> getOnlineCharacters() {
        return getServer().getOnlineCharacters();
    }
}
