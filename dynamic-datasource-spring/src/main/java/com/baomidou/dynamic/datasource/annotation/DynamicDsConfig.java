package com.baomidou.dynamic.datasource.annotation;

import com.baomidou.dynamic.datasource.aop.DynamicDataSourcePackageRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 *  example:
 *
 *  @DynamicDsConfig(config = {
 *         @DynamicDsConfig.PackageConfig(packages = {"xxx.mapper1"}, ds = "master"),
 *         @DynamicDsConfig.PackageConfig(packages = {"xxx.mapper2"}, ds = "slave")})
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DynamicDataSourcePackageRegistrar.class)
public @interface DynamicDsConfig {

    /**
     * package ds config
     *
     * @return
     */
    PackageConfig[] config() default {};

    /**
     * BaseClass ,for example Proxy.class only Mapper be proxy
     *
     * @return
     */
    Class<?> rootClass() default Object.class;

    //
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface PackageConfig {

        /**
         * packages
         *
         * @return
         */
        String[] packages() default {};

        /**
         * datasource
         *
         * @return
         */
        String ds() default "";
    }
}
