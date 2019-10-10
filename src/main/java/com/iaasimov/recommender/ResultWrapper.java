package com.iaasimov.recommender;

import com.iaasimov.entity.RecommenderResultsReview;
import com.iaasimov.entity.SemanticStoreQuery;
import com.iaasimov.entity.ResultSet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.ArrayList;
import java.util.List;

public class ResultWrapper <T> {

    public List<String> getSimpleResults(SemanticStoreQuery semanticStoreQuery, SolrDocumentList results){
        List<String> resultsToReturn = new ArrayList<>();
        if(semanticStoreQuery.getTarget() == Constants.RecommenderTarget.CLOUD_SERVICES){
            for(SolrDocument aResult : results){
                resultsToReturn.add((String)aResult.getFieldValue(Constants.SERVICE_PRODUCT_FIELD));
            }
        }
        return resultsToReturn;
    }

    public List<T> getResults(SemanticStoreQuery semanticStoreQuery, SolrDocumentList results, DiscountHandler discountHandler){

        List<ResultSet> resultsToReturn = new ArrayList<>();
        if(semanticStoreQuery.getTarget() == Constants.RecommenderTarget.CLOUD_SERVICES){

            if(results == null || results.size() == 0){
                return null;
            }
            for(SolrDocument aResult : results){
                String sId = String.valueOf(aResult.getFieldValue(Constants.SERVICE_ID_FIELD));
                ResultSet rrest = new ResultSet(
                        sId,
                        (String)aResult.getFieldValue(Constants.SERVICE_PRODUCT_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_COUNTRY_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_REGION_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_INDUSTRY_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_EMOTIONAL_CONNECT_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_CSM_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_CUSTOMER_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_URL_FIELD),
                        (List<String>)aResult.getFieldValue(Constants.SERVICE_URL_LIST_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_USECASE_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_USECASE_DETAILS_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_GENEREAL_QUERY),
                        (String)aResult.getFieldValue(Constants.SERVICE_SIMILAR_QUERY),
                        (String)aResult.getFieldValue(Constants.SERVICE_GENEREAL_RESPONSE),
                        (String)aResult.getFieldValue(Constants.SERVICE_DEAL_SIZE_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_ADVANTAGE_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_KEY_OBJECTIONS_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_CUSTOMER_BACKGROUND_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_BUSINESS_PAIN_POINTS_FIELD),
                        (String)aResult.getFieldValue(Constants.SERVICE_USECASE_TYPE_FIELD)


                );
                resultsToReturn.add(rrest);
            }
            return (List<T>) resultsToReturn;
        }

            return (List<T>) resultsToReturn;
    }


    public ResultSet getServiceInfo(SolrDocument aResult){
        String sId = String.valueOf(aResult.getFieldValue(Constants.SERVICE_ID_FIELD));
        ResultSet rrest = new ResultSet(
                sId,
                (String)aResult.getFieldValue(Constants.SERVICE_PRODUCT_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_COUNTRY_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_REGION_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_INDUSTRY_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_EMOTIONAL_CONNECT_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_CSM_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_CUSTOMER_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_URL_FIELD),
                (List<String>)aResult.getFieldValue(Constants.SERVICE_URL_LIST_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_USECASE_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_USECASE_DETAILS_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_GENEREAL_QUERY),
                (String)aResult.getFieldValue(Constants.SERVICE_SIMILAR_QUERY),
                (String)aResult.getFieldValue(Constants.SERVICE_GENEREAL_RESPONSE),
                (String)aResult.getFieldValue(Constants.SERVICE_DEAL_SIZE_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_ADVANTAGE_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_KEY_OBJECTIONS_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_CUSTOMER_BACKGROUND_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_BUSINESS_PAIN_POINTS_FIELD),
                (String)aResult.getFieldValue(Constants.SERVICE_USECASE_TYPE_FIELD)

        );
        return rrest;
    }
}
