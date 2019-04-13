/*
 * Copyright 2015-2019 Futeh Kao
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

package net.e6tech.elements.network.cluster.catalyst;

import net.e6tech.elements.network.cluster.catalyst.dataset.DataSet;

import java.io.Serializable;
import java.util.Collection;

public class Reduce<T, R> extends Series<T, R> {

    public Reduce() {
    }

    public Reduce(Series<T, R> other) {
        super(other);
    }

    public R reduce(Catalyst<? extends Reactor> catalyst, DataSet<T> dataSet, ReduceOp<R> reduce) {
        Collection<R> result = catalyst.collect(dataSet, this, (reactor, collection) -> reduce.reduce(collection));
        return reduce.reduce(result);
    }

    @FunctionalInterface
    public interface ReduceOp<T> extends Serializable {

        default T reduce(Collection<T> collection) {
            T accumulator = null;
            boolean first = true;
            for (T r : collection) {
                if (r == null)
                    continue;
                if (first) {
                    first = false;
                    accumulator = r;
                } else {
                    accumulator = reduce(accumulator, r);
                }
            }
            return accumulator;
        }

        T reduce(T t1, T t2);
    }

}