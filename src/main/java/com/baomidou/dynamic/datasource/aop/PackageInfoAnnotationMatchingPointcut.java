/*
 * Copyright © 2018 organization baomidou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baomidou.dynamic.datasource.aop;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * 支持package-info
 *
 * @author lqb
 * @see AnnotationMatchingPointcut
 * @since 3.5.2
 */
public class PackageInfoAnnotationMatchingPointcut implements Pointcut {

    private final ClassFilter classFilter;

    private final MethodMatcher methodMatcher;

    /**
     * Create a new PackageInfoAnnotationMatchingPointcut for the given annotation type.
     *
     * @param classAnnotationType the annotation type to look for at the class level
     */
    public PackageInfoAnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType, boolean checkInherited) {
        this.classFilter = new PackageAnnotationClassFilter(classAnnotationType, checkInherited);
        this.methodMatcher = MethodMatcher.TRUE;
    }

    @Override
    public ClassFilter getClassFilter() {
        return this.classFilter;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this.methodMatcher;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PackageInfoAnnotationMatchingPointcut)) {
            return false;
        }
        PackageInfoAnnotationMatchingPointcut that = (PackageInfoAnnotationMatchingPointcut) other;
        return ObjectUtils.nullSafeEquals(that.classFilter, this.classFilter) &&
                ObjectUtils.nullSafeEquals(that.methodMatcher, this.methodMatcher);
    }

    @Override
    public int hashCode() {
        int code = 17;
        if (this.classFilter != null) {
            code = 37 * code + this.classFilter.hashCode();
        }
        if (this.methodMatcher != null) {
            code = 37 * code + this.methodMatcher.hashCode();
        }
        return code;
    }

    @Override
    public String toString() {
        return "PackageInfoAnnotationMatchingPointcut: " + this.classFilter + ", " + this.methodMatcher;
    }

    private static class PackageAnnotationClassFilter extends AnnotationClassFilter {

        private final Class<? extends Annotation> annotationType;

        public PackageAnnotationClassFilter(Class<? extends Annotation> annotationType, boolean checkInherited) {
            super(annotationType, checkInherited);
            this.annotationType = annotationType;
        }

        @Override
        public boolean matches(Class<?> clazz) {
            boolean matches = super.matches(clazz);
            if (!matches) {
                matches = clazz.getPackage() != null && clazz.getPackage().isAnnotationPresent(annotationType);
                if (!matches) {
                    Set<Class<?>> interfacesForClassAsSet = ClassUtils.getAllInterfacesForClassAsSet(ClassUtils.getUserClass(clazz));
                    for (Class<?> aClass : interfacesForClassAsSet) {
                        matches = aClass.getPackage() != null && aClass.getPackage().isAnnotationPresent(annotationType);
                        if (matches) {
                            return true;
                        }
                    }
                }
            }
            return matches;
        }
    }
}
