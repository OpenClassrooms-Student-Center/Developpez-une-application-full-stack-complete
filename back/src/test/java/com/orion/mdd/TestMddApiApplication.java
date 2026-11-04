package com.orion.mdd;

import org.springframework.boot.SpringApplication;

/**
 * Starts the application on a throwaway MySQL container, created at startup and removed when the application stops.
 * The end-to-end tests run against it, so they never touch the development database.
 */
public class TestMddApiApplication {

    static void main(String[] args) {
        SpringApplication.from(MddApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
