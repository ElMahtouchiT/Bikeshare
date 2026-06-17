package be.iccbxl.tfe.Bikeshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BikeShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(BikeShareApplication.class, args);
    }
}
