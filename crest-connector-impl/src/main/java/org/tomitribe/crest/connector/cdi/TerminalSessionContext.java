/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.connector.cdi;

import org.tomitribe.crest.connector.api.TerminalSessionScoped;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

public class TerminalSessionContext implements Context {

    private static final Logger logger = Logger.getLogger(TerminalSessionContext.class.getName());

    public static final ThreadLocal<TerminalState> state = new ThreadLocal<TerminalState>() {
        @Override
        protected TerminalState initialValue() {
            return new TerminalState();
        }
    };

    private final BeanManager beanManager;

    public TerminalSessionContext(final BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public Class<? extends Annotation> getScope() {
        return TerminalSessionScoped.class;
    }

    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        final Bean<T> bean = (Bean<T>) contextual;

        final TerminalState terminalState = state.get();

        if (terminalState == null) return null;

        return terminalState.getInstance(creationalContext, bean);
    }

    public <T> T get(Contextual<T> contextual) {
        final Bean<T> bean = (Bean<T>) contextual;

        final TerminalState terminalState = state.get();

        if (terminalState == null) return null;

        return terminalState.getInstance(null, bean);
    }

    public boolean isActive() {
        // This will never be null due to ThreadLocal.initialvalue() overriding
        return state.get() != null;
    }

    public static class TerminalState {

        private static int ids = 1000;
        private final int id = ids++;

        //These should be converted to thread safe collections
        private final Map<Class<?>, ScopedInstance<?>> map = new ConcurrentHashMap<Class<?>, ScopedInstance<?>>();

        public TerminalState() {
            logger.info(m("Constructed"));
        }

        private <T> T getInstance(final CreationalContext<T> creationalContext, final Bean<T> bean) {
            final Class<?> beanClass = bean.getBeanClass();

            logger.info(m("getInstance(%s)", beanClass.getName()));

            final ScopedInstance<?> scopedInstance = map.computeIfAbsent(beanClass, new Function<Class<?>, ScopedInstance<?>>() {
                @Override
                public ScopedInstance<?> apply(final Class<?> ignored) {
                    logger.info(m("create(%s)", beanClass.getName()));
                    return new ScopedInstance<T>(bean, creationalContext, bean.create(creationalContext));
                }
            });

            return (T) scopedInstance.getInstance();
        }

        public void destroy() {
            logger.info(m("destroying context"));
            //Since this is not a CDI NormalScope we are responsible for managing the entire lifecycle, including
            //destroying the beans
            for (ScopedInstance scopedInstance : map.values()) {
                logger.info(m("destroy(%s)", scopedInstance.getBean().getBeanClass().getName()));
                scopedInstance.getBean().destroy(scopedInstance.getInstance(), scopedInstance.getCreationalContext());
            }

            map.clear();
        }

        private String m(String format, Object... data) {
            final String message = String.format(format, data);
            return String.format("[%s] session(%s:%s) - %s", Thread.currentThread().getName(), id, ids, message);
        }
    }

    public static class ScopedInstance<T> {
        private final Bean<T> bean;
        private final CreationalContext<T> creationalContext;
        private final T instance;

        public ScopedInstance(final Bean<T> bean, final CreationalContext<T> creationalContext, final T instance) {
            this.bean = bean;
            this.creationalContext = creationalContext;
            this.instance = instance;
        }

        public Bean<T> getBean() {
            return bean;
        }

        public CreationalContext<T> getCreationalContext() {
            return creationalContext;
        }

        public T getInstance() {
            return instance;
        }
    }
}