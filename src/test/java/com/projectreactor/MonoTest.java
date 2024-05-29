package com.projectreactor;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 * Publisher <- (subcribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * until:
 * 1. Publisher sends all the objects requested.
 * 2. Publisher sends all the objects it has. (onComplete) subscriber and
 * subscription will be canceled
 * 3. There is an error. (onError) -> subsciber and subscription will be
 * canceled
 */
public class MonoTest {
    @Test
    public void test() {
        log.info("Everthing working as intended!!!");
    }

    @Test
    public void monoSubscriber() {
        String name = "William Suane";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("==========================");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "William Suane";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Value: {}", s));
        log.info("==========================");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubcriberConsumerError() {
        String name = "William Suane";
        Mono<String> mono = Mono.just(name)
                .map(s -> {
                    throw new RuntimeException("Testing mono with error");
                });

        mono.subscribe(s -> log.info("Value: {}", s), e -> log.info("Something bad happened !!!"));
        mono.subscribe(s -> log.info("Value: {}", s), Throwable::printStackTrace);
        log.info("==========================");
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }
}
