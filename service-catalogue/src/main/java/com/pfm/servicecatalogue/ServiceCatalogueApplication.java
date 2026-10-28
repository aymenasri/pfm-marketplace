package com.pfm.servicecatalogue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceCatalogueApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCatalogueApplication.class, args);
    }

}
