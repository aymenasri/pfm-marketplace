package com.pfm.servicecommande.feignClient;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

public class FeignRetryConfiguration {
    @Bean
    Retryer retryer() {
        return new Retryer.Default(100, 500, 3);
    }
}
