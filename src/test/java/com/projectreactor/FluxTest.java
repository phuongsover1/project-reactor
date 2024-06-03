package com.projectreactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

@Slf4j
public class FluxTest {
    @Test
    public void fluxSubscriber() {
        Flux<String> fluxString = Flux.just("William", "Suane", "DevDojo", "Academy").log();
        StepVerifier.create(fluxString).expectNext("William", "Suane", "DevDojo", "Academy").verifyComplete();
    }

    @Test
    public void fluxSubsciberNumbers() {
        Flux<Integer> flux = Flux.range(1, 5).log();
        flux.subscribe(i -> log.info("Number {}", i));

        log.info("=========================");
        StepVerifier.create(flux).expectNext(1, 2, 3, 4, 5).verifyComplete();

    }

    @Test
    public void fluxSubscriberFromList() {
        Flux<Integer> flux = Flux.fromIterable(List.of(1, 2, 3, 4, 5, 6)).log();

        flux.subscribe(i -> log.info("Number: {}", i));
        log.info("=========================");
        StepVerifier.create(flux).expectNext(1, 2, 3, 4, 5, 6).verifyComplete();

    }

    @Test
    public void FluxSubscribeWithUglyBackPressureBehind() {
        Flux<Integer> flux = Flux.range(1, 10).log();

        flux.subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private Subscription subscription;
            private int requestCount = 2;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer t) {
                ++count;
                if (count >= requestCount) {
                    count = 0;
                    this.subscription.request(requestCount);
                }

            }

            @Override
            public void onError(Throwable t) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'onError'");
            }

            @Override
            public void onComplete() {
                log.info("COMPLETED !!!");
            }

        });

        log.info("=========================");
        StepVerifier.create(flux).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();
    }

    @Test
    public void FluxSubscribeWithNoUglyBackPressureBehind() {
        Flux<Integer> flux = Flux.range(1, 10).log();

        flux.subscribe(new BaseSubscriber<>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                // super.hookOnSubscribe(subscription);
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                ++count;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });

        log.info("=========================");
        StepVerifier.create(flux).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();
    }
}
