/*
 * Copyright 2017 Futeh Kao
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

package net.e6tech.elements.network.cluster;

import net.e6tech.elements.common.util.concurrent.Async;
import net.e6tech.elements.network.cluster.invocation.Registry;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Created by futeh.
 */
@SuppressWarnings("all")
public class RegistryTest {

    public static ClusterNode create(int port) {
        return ClusterNodeTest.create(port);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simple1() throws Exception {
        ClusterNode clusterNode = create(2552);
        Registry registry = clusterNode.getRegistry();

        registry.register("blah", (actor, sv) -> {
            return ((String)sv[0]).toUpperCase();
        })
        .whenComplete((ret, exception) -> {
            if (exception != null)
                System.out.println(exception);
            else
                System.out.println(ret);} );

        while (registry.routes("blah").size() < 1)
            Thread.sleep(100);

        // routing ServiceMessage
        long start = System.currentTimeMillis();
        registry.route("blah", 5000L)
                .apply(new Object[] { "hello world!"})
                .thenAccept(result -> {
                    System.out.println(result);
                    System.out.println("duration: " + (System.currentTimeMillis() - start));
                })
                .exceptionally(error -> {
                    System.out.println(error);
                    return null;
                });

        long start2 = System.currentTimeMillis();
        registry.route("blah", 5000L)
                .apply(new Object[] { "goodbye!" })
                .thenAccept(result -> {
                    System.out.println(result);
                    System.out.println("duration: " + (System.currentTimeMillis() - start2));
                });

        Thread.sleep(2000L);

        Async<Function> async = registry.async("blah",  Function.class, 100l);
        async.apply(svc -> svc.apply("One more time"))
                .thenAccept(result -> System.out.println(result));
        Thread.sleep(2000L);
    }


    @Test
    public void async() throws Exception {
        ClusterNode clusterNode = create(2552);
        Registry registry = clusterNode.getRegistry();

        registry.register("blah", X.class, new X() {
            @Override
            public int doSomething(int x) {
                return 0;
            }

            @Override
            public void returnsVoid(int x) {
            }

            @Override
            public Response request(Request request) {
                Response response = new Response();
                response.map = request.map;
                return response;
            }
        });
        Thread.sleep(100L);

        Async<X> async = registry.async("blah", X.class, 5000L);
        async.apply(p -> p.doSomething(5))
                .thenAccept(result -> {
                    System.out.println("This should be 0 -> " + result);
                });

        async.accept(p -> p.returnsVoid(5))
                .thenAccept(result -> {
                    System.out.println("This should be null -> " + result);
                });

        Thread.sleep(2000L);
    }

    // For the following test, start asyncVM1, asyncVM1_1 and then asyncVM2.
    // asyncVM2 will then submit jobs to the cluster.
    @Test
    public void asyncVM1() throws Exception {
        ClusterNode clusterNode = create(2551);
        Registry registry = clusterNode.getRegistry();

        registry.register("blah", X.class, new X() {
            @Override
            public int doSomething(int x) {
                return x * x;
            }

            @Override
            public void returnsVoid(int x) {
            }

            @Override
            public Response request(Request request) {
                Response response = new Response();
                response.map = request.map;
                return response;
            }
        });
        synchronized (this) {
            wait();
        }
    }

    @Test
    public void asyncVM1_1() throws Exception {
        ClusterNode clusterNode = create(2553);
        Registry registry = clusterNode.getRegistry();

        registry.register("blah", X.class, new X() {
            @Override
            public int doSomething(int x) {
                return x * x;
            }

            @Override
            public void returnsVoid(int x) {
            }

            @Override
            public Response request(Request request) {
                Response response = new Response();
                response.map = request.map;
                return response;
            }
        });
        synchronized (this) {
            wait();
        }
    }

    @Test
    public void asyncVM2() throws Exception {
        ClusterNode clusterNode = create(2552);
        Registry registry = clusterNode.getRegistry();
        long start = System.currentTimeMillis();
        AtomicInteger member = new AtomicInteger(0);

        synchronized (member) {
            while (member.get() == 0) {
                member.wait();
            }
        }
        System.out.println("Detected announcement in " + (System.currentTimeMillis() - start) + "ms");

        Collection routes = registry.routes("blah", X.class);

        for (int i = 0; i< 10; i++) {
            ClusterAsync<X> async = registry.async("blah", X.class, 5000L);
            int arg = i;
            async.ask(p -> p.doSomething(arg))
                    .thenAccept(result -> {
                        System.out.println("value=" + result.getValue() + " sender=" + result.getResponder());
                    });
        }

        Async<X> async = registry.async("blah", X.class, 5000L);
        Request request = new Request();
        request.map.put("key", "value");
        async.apply(p -> p.request(request))
                .thenAccept(result -> {
                    System.out.println(((Response) result).map);
                });

        long callVoidStart = System.currentTimeMillis();
        async.accept(p -> p.returnsVoid(7) )
                .thenRun(() -> {
                    System.out.println("Got response: " + (System.currentTimeMillis() - callVoidStart) + "ms");
                });

        Thread.sleep(2000L);
    }

    interface X {
        int doSomething(int x);
        void returnsVoid(int x);
        Response request(Request request);
    }

    public static class Request {
        Map map = new HashMap();
    }

    public static class Response  {
        Map map = new HashMap();
    }

}
