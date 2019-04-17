package com.iaasimov;

import com.iaasimov.workflow.FlowManagement;
import com.iaasimov.workflow.GlobalConstantsNew;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(value = "com.iaasimov.data.repo")
@EntityScan(value="com.iaasimov.data.model")
@SpringBootApplication
public class Application {

    public static void main(String[] args) {


      ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        GlobalConstantsNew.getInstance().initGlobalConstants(ctx);
        FlowManagement.init();
    }

}
