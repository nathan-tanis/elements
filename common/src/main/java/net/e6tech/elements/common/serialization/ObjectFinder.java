/*
Copyright 2015 Futeh Kao

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.e6tech.elements.common.serialization;

import net.e6tech.elements.common.resources.Resources;

/**
 * Created by futeh.
 */
public interface ObjectFinder {

    // used for replacing objects that should be lazy initialized
    Object replaceObject(Object obj);

    // check if ObjectReference can be created for the type
    boolean hasObjectReference(Resources resources, Class cls);

    ObjectReference toReference(Resources resources, Object object);

    Object toObject(Resources resources, ObjectReference ref);
}