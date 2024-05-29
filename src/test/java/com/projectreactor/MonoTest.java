package com.projectreactor;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

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
}
