package com.example.homework45.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.stream.Stream;

@Service
public class InfoService {

    private static final Logger LOG = LoggerFactory.getLogger(InfoService.class);

    public void testParallelStream(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("plain stream");
        Stream.iterate(1L, a -> a + 1)
                .limit(10_000L)
                .reduce(0L, (a, b) ->{
                    long s = 0;
                    for (int i = 0; i < a + b; i++){
                        s += 1;
                    }
                    return s;
                });
        stopWatch.stop();
        stopWatch.start("parallel stream");
        long sum = Stream.iterate(1L, a -> a + 1)
                .limit(10_000L)
                .parallel()
                .reduce(0L, (a, b) -> {
                    long s = 0;
                    for (int i = 0; i < a + b; i++){
                        s += 1;}
                    return s;
                });
        stopWatch.stop();
        LOG.info("Calculated value is {}; {}", sum, stopWatch.prettyPrint());
    }
}
