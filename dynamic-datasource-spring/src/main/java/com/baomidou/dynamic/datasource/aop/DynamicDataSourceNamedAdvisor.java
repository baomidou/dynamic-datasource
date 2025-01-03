package com.baomidou.dynamic.datasource.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PatternMatchUtils;

import java.util.List;

/**
 * @author yuhuangbin
 */
public class DynamicDataSourceNamedAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    /**
     * the advice
     */
    private final Advice advice;
    /**
     * the pointcut
     */
    private final Pointcut pointcut;


    public DynamicDataSourceNamedAdvisor(Advice advice, List<String> scanPackages) {
        this.advice = advice;
        this.pointcut = new ClassPackageNamedPointCut(scanPackages);
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }

    public static class ClassPackageNamedPointCut implements Pointcut {

        private final ClassFilter classFilter;

        public ClassPackageNamedPointCut(List<String> packagePatterns) {
            this.classFilter = clazz -> !CollectionUtils.isEmpty(packagePatterns)
                    && PatternMatchUtils.simpleMatch(packagePatterns.toArray(new String[0]), ClassUtils.getPackageName(clazz));
        }

        @Override
        public ClassFilter getClassFilter() {
            return classFilter;
        }

        @Override
        public MethodMatcher getMethodMatcher() {
            return MethodMatcher.TRUE;
        }
    }

}
