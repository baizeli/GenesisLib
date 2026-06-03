package miku.bai_ze_li.genesis.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(GenesisTooltipTextEffectGroup.class)
public @interface GenesisTooltipTextEffect {
    String translation();

    GenesisTextEffect.Preset preset();
}
