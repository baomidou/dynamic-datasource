package com.baomidou.dynamic.datasource.aop;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DynamicMethodMatcher;
import org.springframework.aop.support.RootClassFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 包名前缀pointcut
 */

public class DynamicDataSourcePackagePrefixPointcut implements Pointcut {

    private Class baseClass;

    private PackagePrefixClassFilter packagePrefixClassFilter;
    private PackagePrefixMethodMatcher packagePrefixMethodMatcher;

    public DynamicDataSourcePackagePrefixPointcut(Set<String> packages, Class baseClass) {
        this.baseClass = baseClass;
        packagePrefixClassFilter = new PackagePrefixClassFilter(packages);
        packagePrefixMethodMatcher = new PackagePrefixMethodMatcher(packages);

    }

    @Override
    public ClassFilter getClassFilter() {
        return Objects.equals(Object.class, baseClass) ? packagePrefixClassFilter : new RootClassFilter(baseClass);
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return packagePrefixMethodMatcher;
    }


    public static class PackagePrefixClassFilter implements ClassFilter {

        private Set<String> packages;

        public PackagePrefixClassFilter(Set<String> packages) {
            this.packages = packages;
        }

        @Override
        public boolean matches(Class<?> clazz) {
            return isMatch(findAllSuperClass(clazz), packages);
        }
    }

    public static class PackagePrefixMethodMatcher extends DynamicMethodMatcher {

        private Set<String> packages;

        public PackagePrefixMethodMatcher(Set<String> packages) {
            this.packages = packages;
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass, Object... args) {
            return isMatch(findAllSuperClass(targetClass), packages);
        }
    }

    /**
     * 获取所有父类及实现接口
     *
     * @param clazz
     * @return
     */
    public static Set<String> findAllSuperClass(Class clazz) {
        Set<Class<?>> allInterfacesForClassAsSet = ClassUtils.getAllInterfacesForClassAsSet(clazz);
        allInterfacesForClassAsSet.add(clazz);
        allInterfacesForClassAsSet.addAll(getAllSuperForClassAsSet(clazz));
        return allInterfacesForClassAsSet.stream().map(c -> c.getName()).collect(Collectors.toSet());
    }

    public static Set<Class<?>> getAllSuperForClassAsSet(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            return Collections.singleton(clazz);
        }
        Set<Class<?>> superClazz = new LinkedHashSet<>();
        Class<?> current = clazz;
        while (!superClazz.contains(current)) {
            Class cls = current.getSuperclass();
            superClazz.add(cls);
            current = current.getSuperclass();
        }
        return superClazz;
    }

    public static boolean isMatch(Set<String> packages, Set<String> targetPacks) {
        if (!CollectionUtils.isEmpty(targetPacks) && !CollectionUtils.isEmpty(packages)) {
            for (String aPackage : packages) {
                for (String targetPack : targetPacks) {
                    if (aPackage.startsWith(targetPack)) {
                        return Boolean.TRUE;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }
}
