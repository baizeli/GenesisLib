package miku.bai_ze_li.genesis.core.api.attribute;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class EntityAttribute
{
	public static AttributeInstance require(LivingEntity entity, Attribute attr)
	{
		AttributeInstance inst = entity.getAttribute(attr);
		if (inst == null)
			throw new NullPointerException("Entity: " + entity.getType() + ", Attribute: " + attr);
		return inst;
	}
}

