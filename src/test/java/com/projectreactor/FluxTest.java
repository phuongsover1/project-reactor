package com.projectreactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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

    @Test
    public void fluxSubscriberIntervalOne() throws InterruptedException {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .log();

        interval.subscribe(i -> log.info("Number {}", i));

        Thread.sleep(3000);
    }

    @Test
    public void fluxSubscriberIntervalTwo() {
        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofDays(1))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1)).log();
    }

    @Test
    public void fluxSubscriberPrettyBackpressure() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log()
                .limitRate(3);

        flux.subscribe(i -> log.info("Number: {}", i));
    }

    @Test
    public void connectableFlux() throws InterruptedException {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish();

        connectableFlux.connect();

        log.info("Thread sleeping for 300ms");
        Thread.sleep(300);

        connectableFlux.subscribe(i -> log.info("Sub1 - Number: {}", i));

        log.info("Thread sleeping for 200ms");
        Thread.sleep(200);

        connectableFlux.subscribe(i -> log.info("Sub2 - Number: {}", i));
    }

    @Test
    public void connectableFluxTest() throws InterruptedException {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish();

        log.info("================ TEST 1 ================");
        StepVerifier.create(connectableFlux)
                .then(connectableFlux::connect)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
        log.info("================ TEST 2 ================");
        StepVerifier.create(connectableFlux)
                .then(connectableFlux::connect)
                .thenConsumeWhile(i -> i <= 5)
                .expectNext(6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void connectableFluxAutoConnect() {
        Flux<Integer> fluxAutoConnect = Flux.range(1, 5)
                .log()
//                /.delayElements(Duration.ofMillis(100))
                .publish()
                .autoConnect(3);
        fluxAutoConnect.subscribe(i -> log.info("Sub1 - Number: {}", i));
        fluxAutoConnect.subscribe(i -> log.info("Sub2 - Number: {}", i));
        fluxAutoConnect.subscribe(i -> log.info("Sub3 - Number: {}", i));

        /*log.info("=========================================");
        StepVerifier.create(fluxAutoConnect)
                .then(fluxAutoConnect::subscribe)
                .expectNext(1, 2, 3, 4, 5)
                .expectComplete()
                .verify();*/

    }

    @Test
    public void subscribeOnIO() {
        Mono<List<String>> list = Mono.fromCallable(() -> Files.readAllLines(Path.of("text-file")))
                .log()
                .subscribeOn(Schedulers.boundedElastic());

        StepVerifier.create(list)
                .expectSubscription()
                .thenConsumeWhile(l -> {
                    Assertions.assertFalse(l.isEmpty());
                    log.info("Size {}", l.size());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void switchIfEmpty() {
        Flux<Object> flux = Flux.empty()
                .switchIfEmpty(Flux.just("Not empty anymore!!!"))
                .log();

        StepVerifier.create(flux)
                .expectNext("Not empty anymore!!!")
                .verifyComplete();
    }

    @Test
    public void deferOperator() throws InterruptedException {
        // Mono<Long> just = Mono.just(System.currentTimeMillis());
        Mono<Long> defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));
        defer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);
        defer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);
        defer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(100);
        defer.subscribe(l -> log.info("time {}", l));

    }
}
