package com.projectreactor;

import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Slf4j
public class FluxTest {
    @Test
    public void fluxSubscriber() {
        Flux<String> fluxString = Flux.just("William", "Suane", "DevDojo", "Academy")
                .log();
        StepVerifier.create(fluxString)
                .expectNext("William", "Suane", "DevDojo", "Academy")
                .verifyComplete();
    }

    @Test
    public void fluxSubsciberNumbers() {
        Flux<Integer> flux = Flux.range(1, 5)
                .log();
        flux.subscribe(i -> log.info("Number {}", i));

        log.info("=========================");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();

    }

    @Test
    public void fluxSubscriberFromList() {
        Flux<Integer> flux = Flux.fromIterable(List.of(1, 2, 3, 4, 5, 6))
                .log();

        flux.subscribe(i -> log.info("Number: {}", i));
        log.info("=========================");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6)
                .verifyComplete();

    }

}
