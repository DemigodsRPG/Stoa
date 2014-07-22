package com.demigodsrpg.stoa.deity.template;

import com.demigodsrpg.stoa.deity.Ability;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.SkillModel;
import com.google.common.collect.Lists;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class TemplateAbility implements Ability {
    private static final String NAME = "Test", COMMAND = "test";
    private static final int COST = 170, DELAY = 1, REPEAT = 0;
    private static final List<String> DETAILS = Lists.newArrayList("Test your target.");
    private static final SkillModel.Type TYPE = SkillModel.Type.OFFENSE;
    private final String deity;

    public TemplateAbility(String deity) {
        this.deity = deity;
    }

    @Override
    public String getDeity() {
        return deity;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public int getCost() {
        return COST;
    }

    @Override
    public int getDelay() {
        return DELAY;
    }

    @Override
    public int getRepeat() {
        return REPEAT;
    }

    @Override
    public List<String> getDetails() {
        return DETAILS;
    }

    @Override
    public SkillModel.Type getType() {
        return TYPE;
    }

    @Override
    public MaterialData getWeapon() {
        return null;
    }

    @Override
    public boolean hasWeapon() {
        return getWeapon() != null;
    }

    @Override
    public boolean use(CharacterModel model) {
        // Define variables
        LivingEntity target = Ability.Util.autoTarget(model.getEntity());

        if (!Ability.Util.target(model.getEntity(), target.getLocation(), true)) return false;

        if (target instanceof Player) {
            Player victim = (Player) target;
            victim.sendMessage("Test!");
            model.getEntity().sendMessage("Tested " + victim.getName() + "!");
        }

        return true;
    }

    @Override
    public Listener getListener() {
        return null;
    }

    @Override
    public BukkitRunnable getRunnable() {
        return null;
    }
}
