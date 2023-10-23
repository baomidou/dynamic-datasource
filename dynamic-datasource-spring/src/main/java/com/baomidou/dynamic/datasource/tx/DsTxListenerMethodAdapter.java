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
package com.baomidou.dynamic.datasource.tx;

import com.baomidou.dynamic.datasource.annotation.DsTxEventListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.support.TransactionSynchronization;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * DsTxListenerMethodAdapter Referenced from org.springframework.transaction.event.TransactionalApplicationListenerMethodAdapter
 */
public class DsTxListenerMethodAdapter extends ApplicationListenerMethodAdapter {
    private final DsTxEventListener dsTxEventListener;

    public DsTxListenerMethodAdapter(String beanName, Class<?> targetClass, Method method) {
        super(beanName, targetClass, method);
        DsTxEventListener annotation = AnnotatedElementUtils.findMergedAnnotation(method, DsTxEventListener.class);
        if (annotation == null) {
            throw new IllegalStateException("No DsTxEventListener annotation found on method: " + method);
        }
        this.dsTxEventListener = annotation;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (Objects.nonNull(TransactionContext.getXID())) {
            DsTxEventSynchronization dsTxSynchronization = createTransactionSynchronization(event);
            TransactionContext.registerSynchronization(dsTxSynchronization);
        }

    }

    private DsTxEventSynchronization createTransactionSynchronization(ApplicationEvent event) {
        return new DsTxEventSynchronization(this, event, this.dsTxEventListener.phase());
    }

    private static class DsTxEventSynchronization implements TransactionSynchronization {
        private final ApplicationListenerMethodAdapter listener;

        private final ApplicationEvent event;

        private final TransactionPhase phase;

        public DsTxEventSynchronization(ApplicationListenerMethodAdapter listener,
                                        ApplicationEvent event, TransactionPhase phase) {

            this.listener = listener;
            this.event = event;
            this.phase = phase;
        }

        @Override
        public int getOrder() {
            return this.listener.getOrder();
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            if (this.phase == TransactionPhase.BEFORE_COMMIT) {
                processEvent();
            }
        }

        @Override
        public void afterCommit() {
            if (this.phase == TransactionPhase.AFTER_COMMIT) {
                processEvent();
            }
        }

        @Override
        public void afterCompletion(int status) {
            if (this.phase == TransactionPhase.AFTER_ROLLBACK && status == STATUS_ROLLED_BACK) {
                processEvent();
            } else if (this.phase == TransactionPhase.AFTER_COMPLETION) {
                processEvent();
            }
        }

        protected void processEvent() {
            this.listener.processEvent(this.event);
        }
    }
}
