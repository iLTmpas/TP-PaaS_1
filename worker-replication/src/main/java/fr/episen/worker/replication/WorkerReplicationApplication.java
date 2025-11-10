package fr.episen.worker.replication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorkerReplicationApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerReplicationApplication.class, args);
    }

}
