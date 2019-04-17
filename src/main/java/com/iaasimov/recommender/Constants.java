package com.iaasimov.recommender;

public class Constants {
    public static String TAG_EXTEND_FILE_PATH = "recommender-config/tagsWithWeights.txt";

    // Cloud services metadata fields
    public static final String SERVICE_ID_FIELD = "id";
    public static final String SERVICE_PRODUCT_FIELD = "product";
    public static final String SERVICE_REGION_FIELD = "region";
    public static final String SERVICE_COUNTRY_FIELD = "country";
    public static final String SERVICE_INDUSTRY_FIELD = "industry";
    public static final String SERVICE_EMOTIONAL_CONNECT_FIELD = "emotional_connect";
    public static final String SERVICE_USECASE_FIELD = "use_case";
    public static final String SERVICE_USECASE_DETAILS_FIELD = "use_case_details";
    public static final String SERVICE_CUSTOMER_FIELD = "customer_name";
    public static final String SERVICE_CUSTOMER_OUTCOMR_FIELD = "customer_outcome";
    public static final String SERVICE_CSM_FIELD = "csm";
    public static final String SERVICE_LAST_MODIFIED_FIELD = "last_modified";
    public static final String SERVICE_URL_FIELD = "url";

    public static final String SERVICE_GENERAL_LOCATION_FIELD = "general_location";
    public static final String SERVICE_DISCOUNT_FIELD = "service_discount";
    public static final String SERVICE_OVERALL_RATING_FIELD = "service_rating";
    public static final String SERVICE_GEO_FIELD = "service_geo";
    public static final String DATACENTER_ADDRESS_FIELD = "data_center_address";
    public static final String SERVICE_GENEREAL_QUERY = "docHandle";
    public static final String SERVICE_SIMILAR_QUERY = "domain";
    public static final String SERVICE_GENEREAL_RESPONSE = "docBody";

    // name space for different tables
    public static String NAMESPACE_FIELD = "category";
    public static String NAMESPACE_VALUE_ORACLE_CLOUD = "OracleCloud";
    public static String NAMESPACE_VALUE_ORACLE_REST = "OracleOther";

    // default solr fields
    public static final String SOLR_ALL_FIELDS = "*";
    public static final String SOLR_SCORE = "score";
    public static final String SOLR_DISTANCE = "_dist_";
    public static final String SOLR_ORDER_ASC = "asc";
    public static final String SOLR_ORDER_DESC = "desc";

    // delimiter constants
    public static final String DELIMITER_OR = "OR";
    public static final String DELIMITER_AND = "AND";

    // sentiment sort orders
    public static final String SENTIMENT_NEUTRAL = "neutral";

    // constants about the method name of combining relevance and sentiment
    public static final String RELEVANCE_SENTIMENT_METHOD_SORT = "sort";
    public static final String RELEVANCE_SENTIMENT_METHOD_BOOST = "boost";
    public static final int NO_OF_SNIPPETS = 20;

    // distance to the geo
    public static final String GEO_DISTANCE_TO_FILTER = "distance";

    // other parameters from apis
    public static final String API_PARA_GEO = "geo";
    public static final String API_PARA_CITY = "city";

    // target enum
    public enum RecommenderTarget {
        CLOUD_SERVICES, RELATIONSHIP_EXTRACTION
    }

    public enum RankCriteria {
        RELEVANCE, SENTIMENT, POPULARITY, DISTANCE
    }

    public static final String CONTEXT_GENERAL = "General";
    public static final String CONTEXT_REGULAR = "$regular";

    public static int DEFAULT_PAGE_NO = 1;
    public static int DEFAULT_PAGE_SIZE = 100;
    public static int DEFAULT_PAGE_NO_TO_FETCH = 5;
    public static float DEFAULT_DISTANCE_TO_FILTER = 1;
    public static float DEFAULT_DISTANCE_LIMIT_TO_EXTEND = 8;
}
