/*
 * Copyright 2015 Futeh Kao
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

package net.e6tech.elements.common.resources;

import net.e6tech.elements.common.notification.NotificationCenter;

/**
 * Created by futeh.
 */
public interface ResourcePool {

    ResourceManager getResourceManager();

    default <T> T getBean(String name) {
        return getResourceManager().getBean(name);
    }

    default <T> T getBean(Class<T> cls) {
        return getResourceManager().getBean(cls);
    }

    default NotificationCenter getNotificationCenter() {
        return getResourceManager().getNotificationCenter();
    }

    <T> T bind(Class<T> cls, T resource) ;  // 1

    <T> T rebind(Class<T> cls, T resource); // 1

    <T> T unbind(Class<T> cls); //1

    void bindClass(Class cls, Class service);  // 1

    <T> T bindNamedInstance(String name, Class<T> cls, T resources); // 1

    <T> T rebindNamedInstance(String name, Class<T> cls, T resource);

    <T> T inject(T obj) ;

    default <T> T newInstance(Class<T> cls) {
        try {
            T instance = cls.newInstance();
            inject(instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}