/**
 * Created by SAURAV on 10-10-2017.
 */
package com.iaasimov.workflow;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import java.util.Arrays;
import java.util.List;


public class GlobalConstantsNew {
    private static GlobalConstantsNew ourInstance = new GlobalConstantsNew();

    public static GlobalConstantsNew getInstance() {
        return ourInstance;
    }

    public GlobalConstantsNew() {
    }

    public void initGlobalConstants(ConfigurableApplicationContext ctx)
    {

      ConfigurableEnvironment env = ctx.getEnvironment();

      this.stemExSuffix = Arrays.asList(env.getProperty("app.stem.exclude.wordSuffix").split("\\s*,\\s*"));
      this.stemExWords = Arrays.asList(env.getProperty("app.stem.exclude.wordNames").split("\\s*,\\s*"));
      this.stemExWordL = env.getProperty("app.stem.exclude.wordLength");
      this.taxonomyBucket = env.getProperty("infra.objectStore.taxonomyBucket");
      this.coreVocab = env.getProperty("infra.objectStore.coreVocab");
      this.auxiliaryVocab = env.getProperty("infra.objectStore.auxiliaryVocab");
      this.avDistance = auxiliaryVocab + "/" + env.getProperty("infra.objectStore.auxiliaryVocab.distance");
      this.avLocation = auxiliaryVocab + "/" + env.getProperty("infra.objectStore.auxiliaryVocab.location");
      this.avCoverage = auxiliaryVocab + "/" + env.getProperty("infra.objectStore.auxiliaryVocab.coverage");
      this.iaaSimovStates= Arrays.asList(env.getProperty("app.iaasimov.states").split("\\s*,\\s*"));
      this.accessKey= env.getProperty("infra.objectStore.accessKey");
      this.secretKey= env.getProperty("infra.objectStore.secretKey");
      this.namespace= env.getProperty("infra.objectStore.namespace");
      this.region= env.getProperty("infra.objectStore.region");
      this.objectStoreEndpoint= env.getProperty("infra.objectStore.endpoint");
      this.iaaSimovPatterns= env.getProperty("app.iaasimov.patternFilePath");
      this.iaaSimovMultilingual= env.getProperty("app.iaasimov.multilingualSupport");
      this.dbUrl = env.getProperty("spring.datasource.url");;
      this.dbUsername= env.getProperty("spring.datasource.username");
      this.dbPassword= env.getProperty("spring.datasource.password");
      this.solrUrl = env.getProperty("infra.solr.url");
    }

    public  String taxonomyBucket;
    public  String iaaSimovMultilingual;
    public  String region;
    public  String namespace;
    public  String coreVocab;
    public  String auxiliaryVocab;
    public  String avDistance;
    public  String avLocation;
    public  String avCoverage;
    public List<String>stemExSuffix;
    public List<String>stemExWords;
    public List<String>iaaSimovStates;
    public String accessKey;
    public String secretKey;
    public String stemExWordL;
    public String objectStoreEndpoint;
    public String iaaSimovPatterns;
    public String dbUsername;
    public String dbPassword;
    public String dbUrl;
    public String solrUrl;


}
