package miku.bai_ze_li.genesis.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenesisItemShader {
    int useType();

    float scale() default 0.6F;

    float red() default 0.0F;

    float green() default 0.02F;

    float blue() default 0.03F;

    float alpha() default 1.0F;
}
