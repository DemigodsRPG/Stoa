package com.demigodsrpg.stoa.structure;

import com.censoredsoftware.library.schematic.Schematic;
import com.demigodsrpg.stoa.data.StoaWorld;
import com.demigodsrpg.stoa.data.WorldDataManager;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.location.StoaLocation;
import com.demigodsrpg.stoa.location.StoaRegion;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import javax.annotation.Nullable;
import java.util.*;

public interface StoaStructureType
{
	String getName();

	Design getDesign(final String name);

	Collection<Design> getDesigns();

	Collection<Flag> getFlags();

	Listener getUniqueListener();

	boolean sanctify(StoaStructure data, StoaCharacter character);

	boolean corrupt(StoaStructure data, StoaCharacter character);

	boolean birth(StoaStructure data, StoaCharacter character);

	boolean kill(StoaStructure data, StoaCharacter character);

	float getDefSanctity();

	float getSanctityRegen();

	int getRadius();

	int getRequiredGenerationCoords();

	boolean isAllowed(@Nullable StoaStructure data, Player sender);

	StoaStructure createNew(boolean generate, @Nullable String design, Location... reference);

	public interface Design
	{
		String getName();

		Set<Location> getClickableBlocks(Location reference);

		Schematic getSchematic(@Nullable StoaStructure data);
	}

	public interface InteractFunction<T>
	{
		T apply(@Nullable StoaStructure data, @Nullable StoaCharacter character);
	}

	public enum Flag
	{
		DELETE_WITH_OWNER, DESTRUCT_ON_BREAK, PROTECTED_BLOCKS, NO_GRIEFING, NO_PVP, PRAYER_LOCATION, OBELISK_LOCATION, TRIBUTE_LOCATION, RESTRICTED_AREA, NO_OVERLAP, STRUCTURE_WAND_GENERABLE
	}

	public static class Util
	{
		public static StoaStructure getStructureRegional(final Location location)
		{
			try
			{
				return Iterables.find(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
				{
					@Override
					public boolean apply(StoaStructure save)
					{
						return save.getBukkitLocations().contains(location);
					}
				});
			}
			catch(NoSuchElementException ignored)
			{
			}
			return null;
		}

		public static StoaStructure getStructureGlobal(final Location location)
		{
			try
			{
				return Iterables.find(StoaStructure.all(StoaWorld.of(location)), new Predicate<StoaStructure>()
				{
					@Override
					public boolean apply(StoaStructure save)
					{
						return save.getBukkitLocations().contains(location);
					}
				});
			}
			catch(NoSuchElementException ignored)
			{
			}
			return null;
		}

		public static Set<StoaStructure> getStructuresInRegionalArea(Location location)
		{
			final StoaRegion center = StoaRegion.at(location);
			Set<StoaStructure> set = new HashSet<>();
			for(StoaRegion region : center.getSurroundingRegions())
				set.addAll(getStructuresInSingleRegion(region));
			return set;
		}

		public static Collection<StoaStructure> getStructuresInSingleRegion(final StoaRegion region)
		{
			return StoaStructure.find(WorldDataManager.getWorld(region.getWorld()), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return save.getRegion().equals(region.toString());
				}
			});
		}

		public static boolean partOfStructureWithType(final Location location, final String type)
		{
			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return save.getTypeName().equals(type) && save.getBukkitLocations().contains(location);
				}
			});
		}

		public static boolean partOfStructureWithAllFlags(final Location location, final Flag... flags)
		{
			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return save.getRawFlags() != null && save.getBukkitLocations().contains(location) && save.getRawFlags().containsAll(Collections2.transform(Sets.newHashSet(flags), new Function<Flag, String>()
					{
						@Override
						public String apply(Flag flag)
						{
							return flag.name();
						}
					}));
				}
			});
		}

		public static boolean partOfStructureWithFlag(final Location location, final Flag... flags)
		{
			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					if(save.getRawFlags() == null || !save.getBukkitLocations().contains(location)) return false;
					for(Flag flag : flags)
						if(save.getRawFlags().contains(flag.name())) return true;
					return false;
				}
			});
		}

		public static boolean partOfStructureWithFlag(final Location location, final Flag flag)
		{
			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getBukkitLocations().contains(location);
				}
			});
		}

		public static boolean isClickableBlockWithFlag(final Location location, final Flag flag)
		{
			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getClickableBlocks().contains(location);
				}
			});
		}

		public static boolean isInRadiusWithFlag(Location location, Flag flag)
		{
			return !getInRadiusWithFlag(location, flag).isEmpty();
		}

		public static Collection<StoaStructure> getInRadiusWithFlag(final Location location, final Flag... flags)
		{
			return Collections2.filter(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					if(save.getRawFlags() == null || !save.getBukkitLocation().getWorld().equals(location.getWorld()) || save.getBukkitLocation().distance(location) > save.getType().getRadius()) return false;
					for(Flag flag : flags)
						if(save.getRawFlags().contains(flag.name())) return true;
					return false;
				}
			});
		}

		public static Collection<StoaStructure> getInRadiusWithFlag(final Location location, final Flag flag)
		{
			return Collections2.filter(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getBukkitLocation().getWorld().equals(location.getWorld()) && save.getBukkitLocation().distance(location) <= save.getType().getRadius();
				}
			});
		}

		public static StoaStructure closestInRadiusWithFlag(final Location location, final Flag flag)
		{
			StoaStructure found = null;
			double nearestDistance = Double.MAX_VALUE;
			for(StoaStructure save : getStructuresInRegionalArea(location))
			{
				if(save.getRawFlags() != null && save.getRawFlags().contains(flag.name()))
				{
					double distance = save.getBukkitLocation().distance(location);
					if(distance <= save.getType().getRadius() && distance < nearestDistance)
					{
						found = save;
						nearestDistance = distance;
					}
				}
			}
			return found;
		}

		public static Set<StoaStructure> getInRadiusWithFlag(final Location location, final Flag flag, final int radius)
		{
			return Sets.filter(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getBukkitLocation().getWorld().equals(location.getWorld()) && save.getBukkitLocation().distance(location) <= radius;
				}
			});
		}

		public static void regenerateStructures()
		{
			for(StoaStructure save : StoaStructure.all())
				save.generate();
		}

		public static Collection<StoaStructure> getStructuresWithFlag(final Flag flag)
		{
			return StoaStructure.find(new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name());
				}
			});
		}

		public static Collection<StoaStructure> getStructuresWithType(final String type)
		{
			return StoaStructure.find(new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return type.equals(save.getTypeName());
				}
			});
		}

		public static boolean noOverlapStructureNearby(Location location)
		{
			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure save)
				{
					return save.getRawFlags().contains(Flag.NO_OVERLAP.name());
				}
			});
		}

		/**
		 * Strictly checks the <code>reference</code> location to validate if the area is safe
		 * for automated generation.
		 *
		 * @param reference the location to be checked
		 * @param area      how big of an area (in blocks) to validate
		 * @return Boolean
		 */
		public static boolean canGenerateStrict(Location reference, int area)
		{
			Location location = reference.clone();
			location.subtract(0, 1, 0);
			location.add((area / 3), 0, (area / 2));

			// Check ground
			for(int i = 0; i < area; i++)
			{
				if(!location.getBlock().getType().isSolid()) return false;
				location.subtract(1, 0, 0);
			}

			// Check ground adjacent
			for(int i = 0; i < area; i++)
			{
				if(!location.getBlock().getType().isSolid()) return false;
				location.subtract(0, 0, 1);
			}

			// Check ground adjacent again
			for(int i = 0; i < area; i++)
			{
				if(!location.getBlock().getType().isSolid()) return false;
				location.add(1, 0, 0);
			}

			location.add(0, 1, 0);

			// Check air diagonally
			for(int i = 0; i < area + 1; i++)
			{
				if(location.getBlock().getType().isSolid()) return false;
				location.add(0, 1, 1);
				location.subtract(1, 0, 0);
			}

			return true;
		}

		public static SimpleWeightedGraph<UUID, DefaultWeightedEdge> getGraphOfStructuresWithType(final StoaStructureType type)
		{
			return getGraphOfStructuresWithPredicate(new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure structure)
				{
					return structure.getType().equals(type);
				}
			}, type.getRadius() * 5); // FIXME Fine tune this distance.
		}

		public static SimpleWeightedGraph<UUID, DefaultWeightedEdge> getGraphOfStructuresWithFlag(final Flag flag, int distance)
		{
			return getGraphOfStructuresWithPredicate(new Predicate<StoaStructure>()
			{
				@Override
				public boolean apply(StoaStructure structure)
				{
					return structure.getType().getFlags().contains(flag);
				}
			}, distance);
		}

		public static SimpleWeightedGraph<UUID, DefaultWeightedEdge> getGraphOfStructuresWithPredicate(Predicate<StoaStructure> predicate, final int distance)
		{
			SimpleWeightedGraph<UUID, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

			for(final StoaStructure save : StoaStructure.find(predicate))
			{
				graph.addVertex(save.getId());

				for(StoaStructure found : StoaStructure.find(new Predicate<StoaStructure>()
				{
					@Override
					public boolean apply(StoaStructure given)
					{
						return !given.equals(save) && StoaLocation.distanceFlat(given.getBukkitLocation(), save.getBukkitLocation()) <= distance;
					}
				}))
				{
					graph.addVertex(found.getId());
					graph.addEdge(save.getId(), found.getId());
					graph.setEdgeWeight(graph.getEdge(save.getId(), found.getId()), StoaLocation.distanceFlat(found.getBukkitLocation(), save.getBukkitLocation()));
				}
			}

			return graph;
		}

		/**
		 * Updates favor for all structures.
		 */
		public static void updateSanctity()
		{
			for(StoaStructure data : getStructuresWithFlag(Flag.DESTRUCT_ON_BREAK))
				data.corrupt(-1F * data.getType().getSanctityRegen());
		}
	}
}
