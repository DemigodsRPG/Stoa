package com.demigodsrpg.stoa.deity;

import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.item.DivineItem;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.PlayerModel;
import com.demigodsrpg.stoa.model.SkillModel;
import com.demigodsrpg.stoa.model.TameableModel;
import com.demigodsrpg.stoa.util.BattleUtil;
import com.demigodsrpg.stoa.util.CharacterUtil;
import com.demigodsrpg.stoa.util.PlayerUtil;
import com.demigodsrpg.stoa.util.TameableUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockIterator;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

public interface Ability {
    String getDeity();

    String getName();

    String getCommand();

    int getCost();

    int getDelay();

    int getRepeat();

    List<String> getDetails();

    SkillModel.Type getType();

    DivineItem getItem();

    boolean hasItem();

    boolean use(CharacterModel character);

    Listener getListener();

    Runnable getRunnable();

    public static class Util {
        public static final int TARGET_OFFSET = 5;

        public static boolean preProcessAbility(Player player, Ability ability) {
            // Define variables
            CharacterModel character = CharacterUtil.currentFromPlayer(player);

            if (!BattleUtil.canTarget(character)) {
                player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                return false;
            } else if (character.getFavor() < ability.getCost()) {
                player.sendMessage(ChatColor.YELLOW + "You do not have enough favor.");
                return false;
            } else return CharacterUtil.isCooledDown(character, ability.getName());
        }

        /**
         * Returns true if the <code>target</code> can be attacked by the <code>player</code> with the defined <code>ability</code>.
         *
         * @param player  the Player doing the ability
         * @param target  the LivingEntity being targeted
         * @param ability the ability itself
         * @return true/false depending on if all pre-process tests have passed
         */
        public static boolean preProcessAbility(Player player, LivingEntity target, Ability ability) {
            // Define variables
            CharacterModel character = CharacterUtil.currentFromPlayer(player);

            if (preProcessAbility(player, ability)) {
                if (target == null) {
                    player.sendMessage(ChatColor.YELLOW + "No target found.");
                    return false;
                } else if (BattleUtil.canParticipate(target) && !BattleUtil.canTarget(BattleUtil.defineParticipant(target))) {
                    player.sendMessage(ChatColor.YELLOW + "Target is in a no-PVP zone.");
                    return false;
                }

                Participant attacked = BattleUtil.defineParticipant(target);
                return !(attacked != null && character.alliedTo(attacked));
            }
            return false;
        }

        public static Set<LivingEntity> preProcessAbility(Player player, Collection<Entity> targets, Ability ability) {
            // Define variables
            CharacterModel character = CharacterUtil.currentFromPlayer(player);
            Set<LivingEntity> set = Sets.newHashSet();

            if (preProcessAbility(player, ability)) {
                for (Entity target : targets) {
                    if (target == null) continue;
                    if (!(target instanceof LivingEntity)) continue;
                    else if (BattleUtil.canParticipate(target) && !BattleUtil.canTarget(BattleUtil.defineParticipant(target)))
                        continue;
                    else if (target instanceof Player) {
                        Participant attacked = BattleUtil.defineParticipant(target);
                        if (character.alliedTo(attacked)) continue;
                        if (BattleUtil.isInBattle(character) && !BattleUtil.isInBattle(attacked)) continue;
                    }
                    set.add((LivingEntity) target);
                }
            }
            if (set.isEmpty()) player.sendMessage(ChatColor.YELLOW + "No target found.");
            return set;
        }

        /**
         * Processes the ability by removing its cost from the <code>character</code>'s
         * current favor and then setting the players cooldown.
         *
         * @param character the character to manipulate.
         * @param ability   the ability whose details to use.
         */
        public static void postProcessAbility(CharacterModel character, Ability ability) {
            if (ability.getDelay() > 0) CharacterUtil.setCooldown(character, ability.getName(), ability.getDelay());
            character.removeFavor(ability.getCost());
        }

        /**
         * Returns true if the event <code>event</code> is caused by a left click.
         *
         * @param event the interact event
         * @return true/false depending on if the event is caused by a left click or not
         */
        public static boolean isLeftClick(PlayerInteractEvent event) {
            Action action = event.getAction();
            return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        }

        /**
         * Returns the LivingEntity that <code>player</code> is target.
         *
         * @param player the player
         * @return the targeted LivingEntity
         */
        public static LivingEntity autoTarget(Player player) {
            // Define variables
            int range = StoaPlugin.config().getInt("caps.target_range") > 140 ? 140 : StoaPlugin.config().getInt("caps.target_range");
            final int correction = 3;
            Location target = null;
            try {
                target = player.getTargetBlock(null, range).getLocation();
            } catch (Exception ignored) {
            }
            if (target == null) return null;
            BlockIterator iterator = new BlockIterator(player, range);
            List<Entity> targets = Lists.newArrayList();
            final CharacterModel looking = CharacterUtil.currentFromPlayer(player);

            // Iterate through the blocks and find the target
            while (iterator.hasNext()) {
                final Block block = iterator.next();

                targets.addAll(Collections2.filter(player.getNearbyEntities(range, range, range), new Predicate<Entity>() {
                    @Override
                    public boolean apply(Entity entity) {
                        if (entity instanceof LivingEntity && entity.getLocation().distance(block.getLocation()) <= correction) {
                            if (entity instanceof Tameable && ((Tameable) entity).isTamed() && TameableUtil.fromEntity((LivingEntity) entity) != null) {
                                TameableModel wrapper = TameableUtil.fromEntity((LivingEntity) entity);
                                if (looking.alliedTo(wrapper)) return false;
                            } else if (entity instanceof Player && PlayerUtil.fromPlayer(((Player) entity)).hasCharacter()) {
                                CharacterModel character = CharacterUtil.currentFromPlayer((Player) entity);
                                if (looking.alliedTo(character) || ((Player) entity).getGameMode().equals(GameMode.CREATIVE))
                                    return false;
                            }
                            return true;
                        }
                        return false;
                    }
                }));
            }

            // Attempt to return the closest entity to the cursor
            for (Entity entity : targets)
                if (entity.getLocation().distance(target) <= correction) return (LivingEntity) entity;

            // If it failed to do that then just return the first entity
            try {
                return (LivingEntity) targets.get(0);
            } catch (Exception ignored) {
            }

            return null;
        }

        public static Location directTarget(Player player) {
            return player.getTargetBlock(null, StoaPlugin.config().getInt("caps.target_range")).getLocation();
        }

        /**
         * Returns true if the <code>player</code> ability hits <code>target</code>.
         *
         * @param player the player using the ability
         * @param target the targeted LivingEntity
         * @return true/false depending on if the ability hits or misses
         */
        public static boolean target(Player player, Location target, boolean notify) {
            CharacterModel character = CharacterUtil.currentFromPlayer(player);
            Location toHit = adjustedAimLocation(character, target);
            if (isHit(target, toHit)) return true;
            if (notify) player.sendMessage(ChatColor.RED + "Missed..."); // TODO Better message.
            return false;
        }

        /**
         * Returns the location that <code>character</code> is actually aiming
         * at when target <code>target</code>.
         *
         * @param character the character triggering the ability callAbilityEvent
         * @param target    the location the character is target at
         * @return the aimed at location
         */
        public static Location adjustedAimLocation(CharacterModel character, Location target) {
            // FIXME: This needs major work.

            int accuracy = character.getDeity().getAccuracy();
            if (accuracy < 3) accuracy = 3;

            int offset = (int) (TARGET_OFFSET + character.getOfflinePlayer().getPlayer().getLocation().distance(target));
            int adjustedOffset = offset / accuracy;
            if (adjustedOffset < 1) adjustedOffset = 1;
            Random random = new Random();
            World world = target.getWorld();

            int randomInt = random.nextInt(adjustedOffset);
            int sampleSpace = random.nextInt(3);

            double X = target.getX();
            double Z = target.getZ();
            double Y = target.getY();

            if (sampleSpace == 0) {
                X += randomInt;
                Z += randomInt;
            } else if (sampleSpace == 1) {
                X -= randomInt;
                Z -= randomInt;
            } else if (sampleSpace == 2) {
                X -= randomInt;
                Z += randomInt;
            } else if (sampleSpace == 3) {
                X += randomInt;
                Z -= randomInt;
            }

            return new Location(world, X, Y, Z);
        }

        /**
         * Returns true if <code>target</code> is hit at <code>hit</code>.
         *
         * @param target the LivingEntity being targeted
         * @param hit    the location actually hit
         * @return true/false if <code>target</code> is hit
         */
        public static boolean isHit(Location target, Location hit) {
            return hit.distance(target) <= 2;
        }

		/* public static boolean bindAbility(Player player, String command)
        {
			// Define character and ability
			CharacterModel character = CharacterUtil.currentFromPlayer(player);
			Ability ability = getAbilityByCommand(character.getDeity().getName(), command);

			// Return if it isn't an ability
			if(ability == null) return false;

			// Handle enabling the command
			String abilityName = ability.getName();
			ItemStack itemInHand = player.getItemInHand();

			if(!character.isBound(ability))
			{
				if(itemInHand == null || itemInHand.getType().equals(Material.AIR))
				{
					// Slot must not be empty
					player.sendMessage(ChatColor.RED + English.ERROR_EMPTY_SLOT.getLine());
					return true;
				}
				else if(character.getMeta().isBound(itemInHand.getType()))
				{
					// Material already bound
					player.sendMessage(ChatColor.RED + English.ERROR_MATERIAL_BOUND.getLine());
					return true;
				}
				else if(ability.hasWeapon() && !itemInHand.getData().equals(ability.getWeapon()))
				{
					// Weapon required
					player.sendMessage(ChatColor.RED + English.ERROR_BIND_WEAPON_REQUIRED.getLine().replace("{weapon}", Strings.beautify(ability.getWeapon().getItemType().name()).toLowerCase()).replace("{ability}", abilityName));
					return true;
				}

				// Save the bind
				character.getMeta().setBind(ability, itemInHand.getType());

				// Let them know
				player.sendMessage(ChatColor.GREEN + English.SUCCESS_ABILITY_BOUND.getLine().replace("{ability}", StringUtils.capitalize(abilityName)).replace("{material}", (Strings.beginsWithVowel(itemInHand.getType().name()) ? "an " : "a ") + Strings.beautify(itemInHand.getType().name()).toLowerCase()));

				return true;
			}
			else
			{
				// Remove the bind
				character.getMeta().removeBind(ability);

				// Let them know
				player.sendMessage(ChatColor.GREEN + English.SUCCESS_ABILITY_UNBOUND.getLine().replace("{ability}", StringUtils.capitalize(abilityName)));

				return true;
			}
		} */

        public static void dealDamage(LivingEntity source, LivingEntity target, double amount, EntityDamageEvent.DamageCause cause) {
            if (source instanceof Player) {
                PlayerModel owner = PlayerUtil.fromPlayer(((Player) source));
                if (owner != null) {
                    Participant participant = BattleUtil.defineParticipant(target);
                    if (participant != null && owner.getCharacter().alliedTo(participant)) return;
                }
            }

            /**
             * This code below MAY cause issues, it should be watched whenever something is changed.
             */

            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(source, target, cause, amount);
            Bukkit.getPluginManager().callEvent(event);
            if (amount >= 1 && !event.isCancelled()) {
                target.setLastDamageCause(event);
                target.damage(amount);
            }
        }

        /**
         * Returns the instance of an ability with a deity matching <code>deityName</code> and command matching <code>commandName</code>.
         *
         * @param deityName   the deity to look for.
         * @param commandName the command name to look for.
         * @return the ability found
         */
        public static Ability getAbilityByCommand(final String deityName, final String commandName) {
            try {
                return Iterables.find(getLoadedAbilities(), new Predicate<Ability>() {
                    @Override
                    public boolean apply(Ability ability) {
                        return ability.getCommand() != null && ability.getCommand().equalsIgnoreCase(commandName) && ability.getDeity().equalsIgnoreCase(deityName);
                    }
                });
            } catch (Exception ignored) {
                // ignored
            }

            return null;
        }

        /**
         * Returns the instance of an ability with a deity matching <code>deityName</code> and name matching <code>abilityName</code>.
         *
         * @param deityName   the deity to look for.
         * @param abilityName the ability name to look for.
         * @return the ability found
         */
        public static Ability getAbilityByName(final String deityName, final String abilityName) {
            try {
                return Iterables.find(getLoadedAbilities(), new Predicate<Ability>() {
                    @Override
                    public boolean apply(Ability ability) {
                        return ability.getCommand() != null && ability.getName().equalsIgnoreCase(abilityName) && ability.getDeity().equalsIgnoreCase(deityName);
                    }
                });
            } catch (Exception ignored) {
                // ignored
            }

            return null;
        }

        /**
         * Finds all ability instances for the currently loaded deities.
         *
         * @return a collection of abilities.
         */
        public static Collection<Ability> getLoadedAbilities() {
            Set<Ability> abilities = Sets.newHashSet();

            for (Deity deity : Stoa.getMythos().getDeities()) {
                abilities.addAll(deity.getAbilities());
            }

            return abilities;
        }
    }
}
