package miku.bai_ze_li.genesis.core.api.mixin;

import net.minecraft.world.entity.Entity;

public interface EntityMarker
{
	public static final long ENTITY_DATA_HEALTH = 1;

	void mark(long mark);
	long mark();

	public static void mark(Entity entity, long mark)
	{
		EntityMarker marker = (EntityMarker) entity;
		marker.mark(marker.mark() | mark);
	}

	public static void unmark(Entity entity, long mark)
	{
		mark = ~mark;
		EntityMarker marker = (EntityMarker) entity;
		marker.mark(marker.mark() & mark);
	}

	public static boolean has(Entity entity, long mark)
	{
		return (((EntityMarker) entity).mark() & mark) != 0;
	}
}
