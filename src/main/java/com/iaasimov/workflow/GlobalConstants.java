package com.iaasimov.workflow;

import java.io.Serializable;

//@Configuration
//@ConfigurationProperties(prefix = "app")
public class GlobalConstants implements Serializable {

  //public static final String taxonomiesBucketNameS3 = "tastebot-dictionary-latest";
  //public static final String taxonomiesFolderNameS3 = "QATaxonomy";

  //public static final String taxonomiesBucketNameS3 = "iaasimov-extended-taxonomy";
  //public static final String taxonomiesFolderNameS3 = "QATaxonomy";

  private String taxonomiesBucketNameS3;
  private String taxonomiesFolderNameS3;

  //public static final String taxonomiesBucketNameS3 = "iaasimov-taxonomy";
  //private static final String taxonomiesFolderNameS3 = "core_services";


  public static final String qaFolderNameS3 = "QAExtend";
  public static final String locationLatLongFile = "QAExtend/qa_Geolocation.tsv";
  public static final String citiesCoveredFile = "QAExtend/qa_CitiesCovered.tsv";
  public static final String paxDictionary = "QAExtend/qa_Pax.tsv";
  public static final String countryMapping = "QAExtend/qa_Country_Nationality_Mapping.tsv";
  public static final String distanceDictionary = "auxiliary/distance.tsv";


  public static final String patternFilePath = "Pattern_iaasimov.tsv";
  public static final String contextFilePath = "Context.tsv";
  public static final String synonymMappingFilePath = "SynonymMapping.tsv";
  public static final String solrUrl = "http://10.10.14.56:8983/solr/sg_rest_v2/";
  //public static final String solrUrl_V2 = "http://localhost:8983/solr/iaasimov";
//  public static final String solrUrl_V2 = "http://129.213.61.88:8983/solr/iaasimov";

    // Mysql Config
  //public static final String mysqlIP = "127.0.0.1";
  //public static final String mysqlPORT = "3306";
  //public static final String mysqlUser = "root";
  //public static final String mysqlPass = "Crayon123";
  //public static final String mysqlDB = "IaaSimov";

  public static final String mysqlIP = "129.213.81.119";
  public static final String mysqlPORT = "3306";
  public static final String mysqlUser = "iaasimov";
  public static final String mysqlPass = "iaasimov07";
  public static final String mysqlDB = "iaasimov";

  public static final int sessionInterval = 10;
  public static final int refineQuestionToleranceInterval = 5;

  //sentiment
  public static final String negativePhrasesPath = "sentiment/negative-phrases.txt";
  public static final String postNegPhrasesPath = "sentiment/post-negative-phrases.txt";
  public static final String conjunctionsFilePath = "sentiment/conjunctions.txt";
  public static final int preNegativePhraseWindow = 0;
  public static final int postNegativePhraseWindow = 2;


    public String getTaxonomiesBucketNameS3() {
        return taxonomiesBucketNameS3;
    }

    public void setTaxonomiesBucketNameS3(String taxonomiesBucketNameS3) {
        this.taxonomiesBucketNameS3 = taxonomiesBucketNameS3;
    }

    public String getTaxonomiesFolderNameS3() {
        return taxonomiesFolderNameS3;
    }

    public void setTaxonomiesFolderNameS3(String taxonomiesFolderNameS3) {
        this.taxonomiesFolderNameS3 = taxonomiesFolderNameS3;
    }

    public enum Entity {

    $serviceFeature,
    $servicename,
    $location
  }

}
