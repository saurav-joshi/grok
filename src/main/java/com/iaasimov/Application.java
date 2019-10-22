package com.iaasimov;

import com.iaasimov.workflow.FlowManagement;
import com.iaasimov.workflow.GlobalConstantsNew;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(value = {"com.iaasimov.data.repo", "com.iaasimov.repository"})
@EntityScan(value= {"com.iaasimov.data.model", "com.iaasimov.tables"})
@SpringBootApplication
public class Application {

    public static void main(String[] args) {


      ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        GlobalConstantsNew.getInstance().initGlobalConstants(ctx);
        FlowManagement.init();
    }

}
