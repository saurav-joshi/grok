package com.iaasimov.recommender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.collect.Sets;
import com.iaasimov.dao.ResultSetDao;
import com.iaasimov.entity.ResultSet;
import com.iaasimov.entity.SemanticStoreQuery;
import com.iaasimov.entity.UserProfile;
import com.iaasimov.entityextraction.EntityExtractionUtil;
import com.iaasimov.entityextraction.LocationFromAddress;
import com.iaasimov.workflow.FlowManagement;
import com.iaasimov.workflow.GlobalConstantsNew;

import scala.Tuple2;

public class Recommender<T> {
    private static Recommender recommender;

    String semanticStoreULR;
    ResultSetDao resultSetDao;
    String tagFilePath = Constants.TAG_EXTEND_FILE_PATH;
    ResultWrapper resultWrapper = new ResultWrapper();

    Map<String,String> attribute2SolrField = new HashMap<>();
    int preferenceFrequencyLimit = 5;
    String rankByDistancePoint;



    public static Recommender getInstance() {
        return recommender == null ? new Recommender() : recommender;
    }

    public Recommender(){
      semanticStoreULR = GlobalConstantsNew.getInstance().solrUrl;
        resultSetDao = new ResultSetDao(semanticStoreULR, tagFilePath);
        attribute2SolrField.put("$country",Constants.SERVICE_COUNTRY_FIELD);
        attribute2SolrField.put("$industry",Constants.SERVICE_INDUSTRY_FIELD);
        attribute2SolrField.put("$product", Constants.SERVICE_PRODUCT_FIELD);
        attribute2SolrField.put("$region", Constants.SERVICE_REGION_FIELD);
        attribute2SolrField.put("$use_case", Constants.SERVICE_USECASE_FIELD);
        //attribute2SolrField.put("$businessUsecase", Constants.SERVICE_USECASE_FIELD);
        attribute2SolrField.put("$use_case_details", Constants.SERVICE_USECASE_FIELD);
        attribute2SolrField.put("$customerOutcome", Constants.SERVICE_CUSTOMER_OUTCOMR_FIELD);
        attribute2SolrField.put("$csm", Constants.SERVICE_CSM_FIELD);
        attribute2SolrField.put("$customername", Constants.SERVICE_CUSTOMER_FIELD);
        //attribute2SolrField.put("$detailedcustomername", Constants.SERVICE_DETAILED_CUSTOMER_FIELD);
        attribute2SolrField.put("$lastModified", Constants.SERVICE_LAST_MODIFIED_FIELD);
        attribute2SolrField.put("$url", Constants.SERVICE_URL_FIELD);
        attribute2SolrField.put("$urlList", Constants.SERVICE_URL_LIST_FIELD);
        //attribute2SolrField.put("$customer", Constants.SERVICE_CUSTOMER_FIELD);
        attribute2SolrField.put("$docHandle", Constants.SERVICE_GENEREAL_QUERY);
        attribute2SolrField.put("docBody", Constants.SERVICE_GENEREAL_RESPONSE);
        attribute2SolrField.put("$domain", Constants.SERVICE_SIMILAR_QUERY);

    }

    public static SemanticStoreQuery createRecommenderQuery(Set<String> questionType, List<EntityExtractionUtil.EntityExtractionResult> entities, Map<String,String> otherParas, int page, boolean ucbyol , String qVal) {
        System.out.println("Entities to be converted to query : " + entities);
        System.out.println("Query type to use:" + questionType);

        if(questionType == null){
            System.out.println("Empty question types !!!!!!!!!!!! : " + questionType);
        }

        SemanticStoreQuery rq = new SemanticStoreQuery();
        Map<String, List<String>> properties = mapEntityList2SolrProperty(entities, false);

        // process other parameters here
        if(otherParas != null && otherParas.size() != 0){
            otherParas.forEach((key,val)->{
                switch (key){
                    case Constants.GEO_DISTANCE_TO_FILTER: {
                        properties.put(Constants.GEO_DISTANCE_TO_FILTER, Arrays.asList(val));
                        rq.setDistanceRefine(true);
                        break;
                    }
//                    case Constants.API_PARA_CITY: {
//                        // TODO: update the rules if we give derived city higher priority.
//                        if(!properties.containsKey(Constants.DATACENTER_ADDRESS_FIELD)){
//                            properties.put(Constants.DATACENTER_ADDRESS_FIELD, Arrays.asList(val));
//                        }
//                        break;
//                    }
//                    case Constants.API_PARA_GEO: {
//                        // get the city of that geo
//                        String cityOfGeo = null;
//                        if(val != null && val.split(",").length == 2){
//                            try {
//                                cityOfGeo = GeoCalculator.getCityFromLatLongOpenMap(val.split(",")[0], val.split(",")[1]);
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//                        }
//
//                        // use the geo only when either no city is given or the geo's city is the same with the given city.
//                        if(!properties.containsKey(Constants.DATACENTER_ADDRESSCITY_FIELD)||(
//                                properties.containsKey(Constants.DATACENTER_ADDRESSCITY_FIELD) && cityOfGeo != null &&
//                                properties.get(Constants.DATACENTER_ADDRESSCITY_FIELD).contains(cityOfGeo.toLowerCase())
//                                )){
//                            properties.put(Constants.SERVICE_GEO_FIELD, Arrays.asList(val));
//                        }
//                        break;
//                    }
                    // TODO: 9/22/17: handle more other parameters here
                    default:{
                        System.out.println("These parameters cannot be handled now");
                    }
                }
            });
        }

        if (questionType.contains("ConsumerQuery.SimilarServices")) {
            System.out.println(">>question types - looking for similar to: " + questionType);
            Map<Constants.RecommenderTarget, Map<String, List<String>>> similarTo = new HashMap<>();
            similarTo.put(Constants.RecommenderTarget.CLOUD_SERVICES, properties);
            rq.setTarget(Constants.RecommenderTarget.CLOUD_SERVICES);
            rq.setSimilarTo(similarTo);
            rq.setQueryType(SemanticStoreQuery.QueryType.SimilarTo);
        }else if (questionType.contains("CustomerQuery.ClassMember")
            || questionType.contains("CustomerQuery.ClassMember.Count")
            || questionType.contains("ConsumerQuery.Refine")
            || questionType.contains("UserRefine.Attribute")) {
            System.out.println(">>question types - looking for a class member: " + questionType);
            rq.setTarget(Constants.RecommenderTarget.CLOUD_SERVICES);

            if (ucbyol) {
                java.util.List<String> ll= new ArrayList<>();
                ll.add(qVal);
                properties.replace("docHandle", ll);

            }
            rq.setProperties(properties);
            rq.setQueryType(SemanticStoreQuery.QueryType.LookingForClassMember);
        }else if (questionType.contains("ConsumerQuery.SurpriseMe")) {
            System.out.println(">>question type - supprise me: " + questionType);
            rq.setTarget(Constants.RecommenderTarget.CLOUD_SERVICES);
            rq.setProperties(properties);
            rq.setQueryType(SemanticStoreQuery.QueryType.GeneralQuery);
        }else {
            System.out.println(">>Other question types !!!!!!!!!!!! : " + questionType);
            rq.setQueryType(SemanticStoreQuery.QueryType.Others);
        }
        rq.setPages(page);
        return rq;
    }

    public static Map<String, List<String>> mapEntityList2SolrProperty(List<EntityExtractionUtil.EntityExtractionResult> entities, boolean isProfileing){
        Map<String, List<String>> properties = new HashMap<>();
        if (entities != null) {
        	for (EntityExtractionUtil.EntityExtractionResult en : entities) {
                String value = String.join(" ", en.getEntityValue());
                if(value.trim().length() == 0){
                    continue;
                }
                switch (en.getEntityName()) {
                    case "$product": {
                        properties.computeIfAbsent(Constants.SERVICE_PRODUCT_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }
                    case "$region": {
//                        value = FlowManagement.contextToEntityMapping.getOrDefault(value,value);
                        //if(isProfileing) value = value.replaceAll("\\b(in|on|near|within|along|close to|at|around|next to|across|to)\\b", "").trim();
                        properties.computeIfAbsent(Constants.SERVICE_REGION_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }

                    case "$division": {
//                        value = FlowManagement.contextToEntityMapping.getOrDefault(value,value);
                        //if(isProfileing) value = value.replaceAll("\\b(in|on|near|within|along|close to|at|around|next to|across|to)\\b", "").trim();
                        properties.computeIfAbsent(Constants.SERVICE_DIVISION_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }

                    case "$country": {
                        value = value.replace(" price","");
                        properties.computeIfAbsent(Constants.SERVICE_COUNTRY_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }

                    case "$industry": {
                        properties.computeIfAbsent(Constants.SERVICE_INDUSTRY_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }
                    case "$emotionalConnect": {
                        properties.computeIfAbsent(Constants.SERVICE_EMOTIONAL_CONNECT_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }

                    case "$usecase": {
                            properties.computeIfAbsent(Constants.SERVICE_USECASE_FIELD, v -> new ArrayList<>()).add(value);

                        break;
                    }

                    case "$businessusecase": {
                        properties.computeIfAbsent(Constants.SERVICE_USECASE_DETAILS_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }
//                    case "$customer": {
//                            properties.computeIfAbsent(Constants.SERVICE_CUSTOMER_FIELD, v -> new ArrayList<>()).add("0.5");
//                        break;
//                    }

                    case "$csm": {
                        properties.computeIfAbsent(Constants.SERVICE_CSM_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }
                    case "$ucq": {
                        properties.computeIfAbsent(Constants.SERVICE_GENEREAL_QUERY, v -> new ArrayList<>()).add(value);
                        break;
                    }
                    case "$entities":
                    case "$requesttype": {
                        properties.computeIfAbsent(Constants.SERVICE_ENTITIES_QUERY, v -> new ArrayList<>()).add(value);
                        break;
                    }
                    case "$domain": {
                        properties.computeIfAbsent(Constants.SERVICE_SIMILAR_QUERY, v -> new ArrayList<>()).add(value);
                        break;
                    }
                    case "$customername": {
                        properties.computeIfAbsent(Constants.SERVICE_CUSTOMER_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }
                    case "$flyingcity": {
                        properties.computeIfAbsent(Constants.SERVICE_FLYING_CITY_FIELD, v -> new ArrayList<>()).add(value);
                        break;
                    }

                    // todo: add more dimensions here if needed
                }
            }
        }
        return properties;
    }

    public List<T> getRecommendationResults(SemanticStoreQuery recommenderQuery, UserProfile userProfile){
        SolrDocumentList results = calculateRecommendations(recommenderQuery, userProfile);
        // only display Services names rest of the information can be too much of details
        return resultWrapper.getResults(recommenderQuery, results, null);
    }


    public SolrDocumentList calculateRecommendations(SemanticStoreQuery query, UserProfile userProfile){
        // Clear off the geo parameters
        rankByDistancePoint = null;

        // return directly if no query or user profile is given
        if((query == null|| query.isEmpty()) && (userProfile == null||!userProfile.hasContextPreference())){
            System.out.println("Error: sorry, no recommender query or user profile is given.");
            return null;
        }

        // as offer info exist in a separate file, take the offer field out of the property or similar to if available.
        boolean isOfferAsked = false;
        if(query != null){
            if(query.getProperties() != null && query.getProperties().size() != 0){
                if(query.getProperties().get(Constants.SERVICE_DISCOUNT_FIELD) != null){
                    query.getProperties().remove(Constants.SERVICE_DISCOUNT_FIELD);
                    isOfferAsked = true;
                }
            }
            Map<Constants.RecommenderTarget, Map<String, List<String>>> similarTos = query.getSimilarTo();
            if(similarTos != null && similarTos.size()!=0 && similarTos.get(Constants.RecommenderTarget.CLOUD_SERVICES) != null ){
                similarTos.get(Constants.RecommenderTarget.CLOUD_SERVICES).remove(Constants.SERVICE_DISCOUNT_FIELD);
                isOfferAsked = true;
            }
        }

        // construct solrQuery for different cases
        SolrQuery solrQuery = null;
        if (userProfile == null || !userProfile.hasContextPreference()){
            System.out.println("================\nRecommend by query only");
            solrQuery = parseQuery(query, null);
        }else{
            System.out.println("================\nRecommend by both query and profile");
            solrQuery = parseQuery(query, userProfile);
        }

        // get the recommendation results based on the constructed solrQuery
        SolrDocumentList results = null;

        try {
            System.out.println("Executing Solr Query for User query");
            System.out.println(solrQuery);
            results = resultSetDao.searchDoc(solrQuery);
        }catch (Exception e){
            System.out.println("Exception while searching for solr.");
            e.printStackTrace();
        }

        // return directly if no results are found
        if(results == null || results.size() == 0){
            return null;
        }

        // rerank with user liked rest
        results = rerankResultsWithUserprofile(results, userProfile);

        // control the page of results to be returned if available
//        if(query.getPages() <= 1)
//            return results;
        SolrDocumentList pageResults = new SolrDocumentList();
        int cnt = (query.getPages() - 1) * query.getNumOfResult();
        while(cnt < Math.min(results.size(), query.getNumOfResult()* query.getPages())){
            pageResults.add(results.get(cnt));
            cnt ++;
        }
        return pageResults;
    }

    public List<ResultSet> getServicesInfoByIds(List<String> restIds){
        if(restIds == null || restIds.size() == 0)
            return null;
        List<ResultSet> results = new ArrayList<>();
        restIds.forEach(x->{
            ResultSet arest = getRestaurantInfoById(x);
            if(arest != null){
                results.add(arest);
            }
        });
        return results.size() == 0 ? null : results;
    }

    public ResultSet getRestaurantInfoById(String restId){
        if(restId == null || restId.trim().length() == 0)
            return null;
        SolrQuery solrQuery = new SolrQuery(Constants.SERVICE_ID_FIELD + ":" + restId);
        SolrDocumentList results = null;
        try{
            results = resultSetDao.searchDoc(solrQuery);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(results == null || results.size() < 1){
            System.out.println("Invalid restaurant id - " + restId + ". No restaurant found.");
            return null;
        }
        return resultWrapper.getServiceInfo(results.get(0));
    }


    public String convertPreference2SolrString(Map<String, Map<String, Long>> attributePreference){
        StringJoiner outsj = new StringJoiner(" " + Constants.DELIMITER_OR + " ","","");

        attributePreference.forEach((attr, val)->{
            String solrField = null;
            if(attribute2SolrField.get(attr) != null){
                solrField = attribute2SolrField.get(attr);

                StringJoiner solrValSj = new StringJoiner(" OR ", "", "");
                for(Map.Entry<String,Long> entry:val.entrySet()){
                    solrValSj.add("\""+entry.getKey()+"\"^"+entry.getValue());
                }
                if(solrValSj.toString().length() != 0){
                    String solrString = solrField+":("+solrValSj.toString()+")";
                    outsj.add(solrString);
                }
            }
        });
        return outsj.toString();
    }

    public SolrQuery parseQuery(SemanticStoreQuery query, UserProfile userProfile){
        Map<String, List<String>> properties = query.getProperties();
        Map<Constants.RecommenderTarget, Map<String, List<String>>> similarTos = query.getSimilarTo();
        List<String> fields = query.getFields();
        List<Constants.RankCriteria> rankCriteria = query.getRankCriteria();

        //Set<String> reviewFields = resultSetDao.getReviewMetaFields();
        Set<String> restFields = resultSetDao.getRestFields();
        SolrQuery solrQuery = null;

        // parse the properties
        Map<String, List<String>> restProperties = new HashMap<>();
        Map<String, List<String>> reviewProperties = new HashMap<>();
        List<String> contextProperties = new ArrayList<>();
        // dispatch properties
        if(properties != null && properties.size() != 0) {
            for(Map.Entry<String,List<String>> entry : properties.entrySet()){
                String propertyKey = entry.getKey();
                List<String> propertyValues = entry.getValue();
                if(propertyKey == Constants.SERVICE_ID_FIELD){
                    if(query.getTarget() == Constants.RecommenderTarget.CLOUD_SERVICES){
                        restProperties.put(propertyKey, propertyValues);
                    }else if(query.getTarget() == Constants.RecommenderTarget.RELATIONSHIP_EXTRACTION){
                        reviewProperties.put(propertyKey, propertyValues);
                    }
                }
                if(restFields.contains(propertyKey)){
                    restProperties.put(propertyKey, propertyValues);
                }
                if(propertyKey.equalsIgnoreCase(Constants.GEO_DISTANCE_TO_FILTER)){
                    restProperties.put(propertyKey, propertyValues);
                }
                if( propertyKey.equalsIgnoreCase(Constants.CONTEXT_REGULAR)){
                    contextProperties.addAll(propertyValues);
                }
            }
        }


        // refine properities using userprofile under the particular context
        //refineRestPropertyWithUserProfile(restProperties, userProfile, contextProperties);

        // process the properties for different recommender targets
        if(query.getTarget() == Constants.RecommenderTarget.CLOUD_SERVICES){
            if(query.getQueryType().equals(SemanticStoreQuery.QueryType.GeneralQuery)){
                solrQuery = processGeneralQueries(restProperties, userProfile);
            }else if(query.getQueryType().equals(SemanticStoreQuery.QueryType.LookingForClassMember)){
                solrQuery = parseRestProperty(restProperties);
            }
        }else if(query.getTarget() == Constants.RecommenderTarget.RELATIONSHIP_EXTRACTION){
            if(restProperties.size() == 0 && reviewProperties.size() == 0){
                System.out.println("Case 2: no review properties are given");
            }else if(restProperties.size() != 0){
                System.out.println("==process getting restaurants for filtering review");
                // process restaurant properties to get restaurant info.
                SolrQuery restSolrQuery = parseRestProperty(restProperties);
                if(restSolrQuery != null){
                    SolrDocumentList rests = resultSetDao.searchDoc(restSolrQuery);

                    List<String> restIds = new ArrayList<>();;

                    for(SolrDocument arest : rests){
                        restIds.add((String)arest.getFieldValue(Constants.SERVICE_ID_FIELD));
                    }

                }

                // process review normally
                solrQuery = parseDocumentProperty(reviewProperties);
            }else{
                System.out.println("== process getting review by review properties");
                // process review properties normally
                solrQuery = parseDocumentProperty(reviewProperties);
            }
        }else{
            System.out.println("Sorry, this recommender target is not supported by now.");
        }

        // parse similarito
        if(similarTos != null && similarTos.size() != 0) {
            if(query.getTarget() == Constants.RecommenderTarget.CLOUD_SERVICES){
                Map<String, List<String>> restSimilarTo = similarTos.get(Constants.RecommenderTarget.CLOUD_SERVICES);
                solrQuery = parseServicesSimilarTo(restSimilarTo);
            }
        }

        // parse rank criteria, fields, and no of results
        if(solrQuery != null) {
            // parse and configure the rank criteria
            if(rankCriteria != null  && rankCriteria.size() != 0){
                for(int i = 0; i< rankCriteria.size(); i++){
                    Constants.RankCriteria field = rankCriteria.get(i);
                    String rankField = Constants.SOLR_SCORE;
                    SolrQuery.ORDER order = SolrQuery.ORDER.desc;

                    if (field == Constants.RankCriteria.POPULARITY) {
                        rankField = Constants.SERVICE_OVERALL_RATING_FIELD;
                        order = SolrQuery.ORDER.desc;
                    }
                    solrQuery.addOrUpdateSort(rankField,order);
                }
            }else{
                solrQuery.addOrUpdateSort(Constants.SOLR_SCORE,SolrQuery.ORDER.desc);
            }

            // configure to rank by distance as a second rank criteria
            if(rankByDistancePoint != null && rankByDistancePoint.length() != 0){
                System.out.println("Rank by distance to point:" + rankByDistancePoint);
                //solrQuery.set("sfield", Constants.SERVICE_GEO_FIELD);
                solrQuery.set("pt", rankByDistancePoint);
                if(!query.isDistanceRefine()) solrQuery.addOrUpdateSort("geodist()", SolrQuery.ORDER.asc);
            }

            // parse and configure the fields
            if(fields != null && fields.size() != 0){
                fields.forEach(solrQuery::addField);
            } else {
                solrQuery.addField(Constants.SOLR_ALL_FIELDS);// by default include all the fields
            }
            solrQuery.addField(Constants.SOLR_SCORE);
            if(rankByDistancePoint != null && rankByDistancePoint.length() != 0){
                solrQuery.addField(Constants.SOLR_DISTANCE + ":geodist()");
            }

            // parse and configure the idsToFilterOut fields if necessary:
            Set<String> ids2filterOut = getRestIdsToFilterOut(query,userProfile);
            solrQuery = parseIdsToFilterOut(solrQuery, query.getTarget(), ids2filterOut);

            // parse and set number of results to return
            solrQuery = parseNumberOfResults(solrQuery,Constants.DEFAULT_PAGE_NO_TO_FETCH, query.getNumOfResult());
        }
        return solrQuery;
    }

    public Set<String> getRestIdsToFilterOut(SemanticStoreQuery query, UserProfile userProfile){
        if(query == null)
            return null;
        Map<String, List<String>> properties = query.getProperties();
        Map<Constants.RecommenderTarget, Map<String, List<String>>> similarTos = query.getSimilarTo();
        // get the ids that indicated in the query
        Set<String> ids2filterOut = query.getIdsToFilterOut();

        if(query.getTarget() == Constants.RecommenderTarget.CLOUD_SERVICES){
            // filter out user's disliked restaurants if available in their user profile
            if(userProfile != null && userProfile.getDislikedRests() != null && userProfile.getDislikedRests().size() != 0){
                Set<String> dislikedRestIds = new HashSet<>(userProfile.getDislikedRests().stream().filter(x->x.trim().length()!=0).collect(Collectors.toList()));
                if(dislikedRestIds.size() != 0){
                    if(ids2filterOut == null){
                        ids2filterOut = new HashSet<>();
                    }
                    ids2filterOut.addAll(dislikedRestIds);
                }
            }
            // double check what users are searching for
            if(ids2filterOut == null || ids2filterOut.size() == 0)
                return ids2filterOut;
            Set<String> restNamesInQuery = new HashSet<>();
            if(properties != null && properties.size() != 0){
                List<String> tmp = properties.get(Constants.SERVICE_PRODUCT_FIELD);
                if(tmp != null && tmp.size() != 0){
                    restNamesInQuery.addAll(tmp.stream().filter(x->x.trim().length()!=0).collect(Collectors.toSet()));
                }
            }
            if(similarTos != null && similarTos.size() != 0){
                Map<String, List<String>> restSimilarTo = similarTos.get(Constants.RecommenderTarget.CLOUD_SERVICES);
                List<String> tmp = restSimilarTo.get(Constants.SERVICE_PRODUCT_FIELD);
                if(tmp != null && tmp.size() != 0){
                    restNamesInQuery.addAll(tmp.stream().filter(x->x.trim().length()!=0).collect(Collectors.toSet()));
                }
            }
            if(restNamesInQuery.size() != 0){
                // remove it from rests2FilterOut
                Set<String> idsToKeep = new HashSet<>();
                restNamesInQuery.stream().forEach(x->{
                    SolrQuery sq = resultSetDao.getServiceByName(x, true, 10, Constants.SERVICE_PRODUCT_FIELD);
                    if(sq != null){
                        try{
                            SolrDocumentList ids = resultSetDao.searchDoc(sq);
                            if(ids != null){
                                idsToKeep.addAll(ids.stream().map(e->String.valueOf(e.getFieldValue(Constants.SERVICE_ID_FIELD))).collect(Collectors.toSet()));
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                if(idsToKeep.size() != 0){
                    ids2filterOut.removeAll(idsToKeep);
                }
            }
        }
        return ids2filterOut;
    }

    public SolrQuery parseIdsToFilterOut(SolrQuery solrQuery, Constants.RecommenderTarget target, Set<String> idsToFilterOut){
        if(solrQuery != null){
            if(idsToFilterOut != null && idsToFilterOut.size() != 0){
                if(target == Constants.RecommenderTarget.RELATIONSHIP_EXTRACTION){
                    resultSetDao.addFilterQuery(solrQuery, Constants.SERVICE_ID_FIELD, new ArrayList<>(idsToFilterOut),false);
                }else{
                    resultSetDao.addFilterQuery(solrQuery, Constants.SERVICE_ID_FIELD, new ArrayList<>(idsToFilterOut),false);
                }
            }
        }
        return solrQuery;
    }

    public SolrQuery parseNumberOfResults(SolrQuery solrQuery, int pages, int numOfResults){
        if(solrQuery != null){
            if(pages <= 0){
                pages = 1;
            }
            if(numOfResults <= 0){
                numOfResults = Constants.DEFAULT_PAGE_SIZE;
            }
            solrQuery.setRows(numOfResults*pages);
        }
        return solrQuery;
    }

    public SolrQuery parseRestProperty(Map<String, List<String>> properties){
        if(properties == null || properties.size() == 0)
            return null;

        SolrQuery solrQuery = null;

        // get the geo/location info out from the property, and convert it to a solr query string
        String fqString = convertGeoLocation2SolrString(properties);

        // process the other entities and convert it to a solr query string
        String propertyString = convertRestProperty2SolrString(properties);

        // use the property string as the main query
        if(propertyString.toString().length() != 0){
            System.out.println("The query string is:" + propertyString.toString());
            solrQuery = new SolrQuery(propertyString.toString());
        }

        // add geolocation info as filter if available
        if(fqString != null && fqString.length() != 0){
            if(solrQuery == null){
                solrQuery = new SolrQuery("*:*");
            }
            solrQuery.addFilterQuery(fqString);
        }
        return solrQuery;
    }

    public SolrQuery parseDocumentProperty(Map<String, List<String>> properties){
        SolrQuery solrQuery = null;
        StringJoiner qsj = new StringJoiner(" " + Constants.DELIMITER_AND + " ", "", "");

        for(Map.Entry<String, List<String>> entry : properties.entrySet()) {
            String property = entry.getKey();
            List<String> values = entry.getValue();

            if (property.equalsIgnoreCase(Constants.SERVICE_ID_FIELD)) { // metadata fields
                if(values.size() != 0){
                    if(solrQuery == null){
                        solrQuery = new SolrQuery();
                    }
                    resultSetDao.addFilterQuery(solrQuery, property, values, true);
                }
            } else {// other fields
                String tagString = resultSetDao.constructQueryStringFromTags(values, Constants.DELIMITER_OR, true);
                if(tagString != null && tagString.length() != 0) {
                    qsj.add(property + ":(" + tagString + ")");
                }
            }
        }

        if(qsj.toString().length() != 0){
            if(solrQuery != null){
                solrQuery.setQuery(qsj.toString());
            }else{
                solrQuery = new SolrQuery(qsj.toString());
            }
        }else{
            if(solrQuery != null){
                solrQuery.setQuery(Constants.SOLR_ALL_FIELDS);
            }
        }

        return solrQuery;
    }

    public SolrQuery parseServicesSimilarTo(Map<String, List<String>> simRest){
        SolrQuery solrQuery = null;
        String queryString = null;
        System.out.println("====similar to properties");
        printProperties(simRest);
        // Get the restid or name field out as the similar target
        String fieldName = null;
        List<String> fieldValues = null;
        SolrQuery restQuery = null;
        if(simRest.get(Constants.SERVICE_ID_FIELD) != null){
            fieldValues = simRest.remove(Constants.SERVICE_ID_FIELD);
            fieldName = Constants.SERVICE_ID_FIELD;
        }else if(simRest.get(Constants.SERVICE_SIMILAR_QUERY) != null){
            fieldValues = simRest.remove(Constants.SERVICE_SIMILAR_QUERY);
            fieldName = Constants.SERVICE_SIMILAR_QUERY;
        }

        // get the restaurant names to filter out later
        Set<String> restNamesToFilter = new HashSet<>();
        if(fieldValues != null){
            if(fieldName == Constants.SERVICE_PRODUCT_FIELD){
                restNamesToFilter.addAll(fieldValues);
            }else{
                restQuery = resultSetDao.getServicesByIdList(fieldValues);
                SolrDocumentList rests = resultSetDao.searchDoc(restQuery);
                for(SolrDocument arest : rests){
                    restNamesToFilter.add(String.valueOf(arest.getFieldValue(Constants.SERVICE_PRODUCT_FIELD)));
                }
            }
        }

        // get the other fields as similar aspect to compare
        // get the location out first, geo and location field got removed as well in this function
        String fqString = convertGeoLocation2SolrString(simRest);

        if(fieldValues != null){
            if(simRest.keySet().size() == 0){
                queryString = resultSetDao.constructQueryStringFromServiceGroup(fieldValues,fieldName);
            }else{
                List<String> aspectList = new ArrayList<>();
                for(String field : simRest.keySet()){
                    if(field.equalsIgnoreCase("addresscity")){//only consider aspect of the restaurant, not city
                        continue;
                    }
                    if(field.equalsIgnoreCase(Constants.SERVICE_USECASE_DETAILS_FIELD)){
                        aspectList.add(Constants.SERVICE_USECASE_DETAILS_FIELD);
                    }else{
                        aspectList.add(field);
                    }
                }
                queryString = resultSetDao.constructQueryStringFromServices(fieldValues, fieldName, aspectList);
            }
            System.out.println("query string by similar to:" + queryString);
        }else{
            System.out.println("Sorry, no valid Twin with given domain yet supported");
        }

        if(queryString != null){
            solrQuery = new SolrQuery(queryString);
        }
        if(restNamesToFilter != null && restNamesToFilter.size() != 0){
            if(solrQuery == null){
                solrQuery = new SolrQuery("*:*");
            }
            resultSetDao.addFilterQuery(solrQuery, Constants.SERVICE_PRODUCT_FIELD, new ArrayList<String>(restNamesToFilter), false);
        }
        if(fqString != null && fqString.length() != 0){
            if(solrQuery == null){
                solrQuery = new SolrQuery("*:*");
            }
            solrQuery.addFilterQuery(fqString);
        }
        return solrQuery;
    }

    public String convertGeoLocation2SolrString(Map<String, List<String>> properties){
        String fqString = "";
        if(properties == null || properties.size() == 0){
            return fqString;
        }
        float distanceInKmToFilter = Constants.DEFAULT_DISTANCE_TO_FILTER;
        Set<String> refinedLocations = null;
        Set<Tuple2<String, String>> latLons = null;

        if(properties.get(Constants.GEO_DISTANCE_TO_FILTER) != null && properties.get(Constants.GEO_DISTANCE_TO_FILTER).size() != 0){
            String distance = properties.remove(Constants.GEO_DISTANCE_TO_FILTER).get(0);
            System.out.println("====distance is given by refine location");
            System.out.println(distance);
            if(distance != null && distance.trim().length() != 0 && NumberUtils.isNumber(distance)){
                distanceInKmToFilter = Float.valueOf(distance);
                System.out.println(distanceInKmToFilter);
            }else{
                System.out.println("WRONG BRANCH HERE" + distance);
            }
        }

        if(properties.get(Constants.SERVICE_GENERAL_LOCATION_FIELD) != null && properties.get(Constants.SERVICE_GENERAL_LOCATION_FIELD).size() !=0 ){
            List<String> locations = properties.remove(Constants.SERVICE_GENERAL_LOCATION_FIELD);
            refinedLocations = new HashSet<>();
            // preprocess the locations
            Set<String> extendables = Sets.newHashSet("near ", "next to ", "close to ", "around ");

            for (String location : locations) {
                String locationWithoutPres = FlowManagement.contextToEntityMapping.getOrDefault(location,location);
                refinedLocations.add(locationWithoutPres);

                // get geos from locations
                Boolean hasPre = false;
                for (String extendable : extendables) {
                    if(location.trim().startsWith(extendable)){
                        hasPre = true;
                    }
                }
                String city = cityFromProperties(properties);
                if(hasPre || !LocationFromAddress.isBuildingOrStreet(locationWithoutPres, city)){
                    Tuple2<Double,Double> latlontp =  LocationFromAddress.getLatLong(locationWithoutPres, city);
                    if(latlontp != null){
                        Tuple2<String, String> tuple2 = new Tuple2<>(String.valueOf(latlontp._1), String.valueOf(latlontp._2));
                        if(latLons == null){
                            latLons = new HashSet<>();
                        }
                        latLons.add(tuple2);
                    }
                }
            }
        }

        if(properties.get(Constants.SERVICE_GEO_FIELD) != null && properties.get(Constants.SERVICE_GEO_FIELD).size() != 0 ){
            String latlong = properties.remove(Constants.SERVICE_GEO_FIELD).get(0);
            if(latlong.trim().length() != 0 && latlong.split(",").length == 2){
                Tuple2<String,String> tuple2 = new Tuple2<>(latlong.split(",")[0],latlong.split(",")[1]);
                // add the api-geo to the latLons only when no location is explicitly given in the query
                if(refinedLocations == null || refinedLocations.size() == 0){
                    if(latLons == null){
                        latLons = new HashSet<>();
                        latLons.add(tuple2);
                    }
                }
            }
        }

        // loop the latLons to create multiple filter by geo distance
        if(latLons != null){
            StringJoiner geosj = new StringJoiner(" OR ", "", "");
            for(Tuple2<String, String> tp : latLons){
                StringJoiner sj = new StringJoiner(" ", "{!geofilt ", "}");
                sj.add("pt=" + tp._1 + "," + tp._2);
                sj.add("sfield=" + Constants.SERVICE_GEO_FIELD);
                sj.add("d=" + String.valueOf(distanceInKmToFilter));
                geosj.add(sj.toString());
                // use one of the location geo for distance ranking in case there is no api-geo is given
                if(rankByDistancePoint == null){
                    rankByDistancePoint = tp._1 + "," + tp._2;
                }
            }
            fqString = geosj.toString();
        }

        if(refinedLocations != null && refinedLocations.size() != 0){
            StringJoiner sj2 = new StringJoiner(" OR ", "", "");
            refinedLocations.forEach(loc->{
                sj2.add(Constants.DATACENTER_ADDRESS_FIELD + ":\"" + loc + "\"");
            });

            String fqLocString = sj2.toString();

            if(fqString.trim().length() == 0){
                fqString = fqLocString;
            }else{
                if(distanceInKmToFilter >= Constants.DEFAULT_DISTANCE_TO_FILTER){
                    fqString = fqLocString + " OR " + fqString;
                }
            }
        }

        System.out.println("*******************************");
        System.out.println("Filter query string is:" + fqString);
        return fqString;
    }

    private String convertRestProperty2SolrString(Map<String, List<String>> properties){
        Map<String,String> restMetaFields = resultSetDao.getServiceMetaFields().stream().collect(Collectors.toMap(i -> i, i -> i));
        StringJoiner qsj = new StringJoiner(" " + Constants.DELIMITER_AND + " ", "", "");
        for(Map.Entry<String, List<String>> entry : properties.entrySet()) {
            String property = entry.getKey();
            List<String> values = entry.getValue();

            String tagString = "";
            String exactNameMatchString = null;
            String exactCityMatchString = null;
            if (restMetaFields.get(property) != null) { // metadata fields
                if(property.equalsIgnoreCase(Constants.SERVICE_PRODUCT_FIELD)){
                    String[] tmp = resultSetDao.preprocessNameValues(values,Constants.DELIMITER_OR);
                    if(tmp != null && tmp.length == 2){
                        tagString = tmp[0];
                        exactNameMatchString = tmp[1];
                    }
                }else if(property.equalsIgnoreCase(Constants.DATACENTER_ADDRESS_FIELD)){
                    String [] tmp = resultSetDao.preprocessNameValues(values, Constants.DELIMITER_OR);
                    if(tmp != null && tmp.length == 2){
                        exactCityMatchString = tmp[1];
                    }
                }else{
                    tagString = resultSetDao.constructQueryStringFromTags(values, Constants.DELIMITER_OR, false);
                }
            } else {// other fields
                tagString = resultSetDao.constructQueryStringFromTags(values, Constants.DELIMITER_OR, true);
            }
            List<String> columnList = new ArrayList<>();
            if (property.equalsIgnoreCase(Constants.SERVICE_USECASE_DETAILS_FIELD)) {
                columnList.add(Constants.SERVICE_USECASE_DETAILS_FIELD);
            } else{
                columnList.add(property);
            }

            StringJoiner sj = new StringJoiner(" OR ", "", "");
            for (String clm : columnList) {
                if(clm.equalsIgnoreCase(Constants.SERVICE_PRODUCT_FIELD) && exactNameMatchString != null){
                    sj.add(Constants.SERVICE_PRODUCT_FIELD + ":(" + exactNameMatchString + ")");
                }
                if(clm.equalsIgnoreCase(Constants.DATACENTER_ADDRESS_FIELD) && exactCityMatchString != null){
                    tagString = exactCityMatchString;
                }
                sj.add(clm + ":(" + tagString + ")");
            }

            qsj.add("(" + sj.toString() + ")");
        }
        return qsj.toString();
    }


    public SolrQuery processGeneralQueries(Map<String, List<String>> restProperties, UserProfile userProfile){
        System.out.println("Enter processGeneralQueries function " + restProperties);
        if(restProperties != null){
            System.out.println(restProperties.size());
        }
        SolrQuery solrQuery = null;
        if(userProfile != null && userProfile.getLikedRests() != null && userProfile.getLikedRests().size() != 0){
            List<String> likedRest = userProfile.getLikedRests();

            // get from niche_items and discover items
            solrQuery = resultSetDao.getDiscoveredItems(likedRest);

            // filter using other aspects if available
            filterUsingRestProperties(solrQuery,restProperties);

            // filter using user liked restaurant ids.
            resultSetDao.addFilterQuery(solrQuery,Constants.SERVICE_ID_FIELD, likedRest, false);
        }

        // when no user profile or liked restaurants for constructing queries, using the properties
        if(solrQuery == null && restProperties != null && restProperties.size() != 0){
            solrQuery = parseRestProperty(restProperties);
        }

        // if there are still get some popular restaurants or random restaurants
        if(solrQuery == null){
            System.out.println("Not sure if I understood your requirements and hence suggesting the popular BOMs to look for... ");
            solrQuery = resultSetDao.getPopularBOM(null);
        }
        return solrQuery;
    }


    public void filterUsingRestProperties(SolrQuery solrQuery, Map<String, List<String>> restProperties){
        if(solrQuery == null || restProperties == null || restProperties.size() == 0)
            return;
        String fqString = convertGeoLocation2SolrString(restProperties);
        String propertyString = convertRestProperty2SolrString(restProperties);

        // combine both as a filter query
        if(propertyString.toString().length() != 0){
            if(fqString == null || fqString.length() == 0){
                fqString = propertyString.toString();
            }else{
                fqString = fqString + " AND " + propertyString.toString();
            }
        }

        // add the filter query
        if(fqString != null && fqString.length() != 0){
            solrQuery.addFilterQuery(fqString);
        }
    }

    public void printProperties(Map<String, List<String>> properties){
        if(properties == null){
            return;
        }
        properties.forEach((att, vallist)->{
            System.out.println("attribute:" + att);
            System.out.println(vallist);
        });
    }

    private boolean propertyOverlapped(Map<String, List<String>> properties, String attribute){
        if(properties.containsKey(attribute)){
            return true;
        }
//        Set<String> locationRelatedAttributes = Sets.newHashSet(Constants.DATACENTER_ADDRESS_FIELD, Constants.DATACENTER_ADDRESSCITY_FIELD, Constants.DATACENTER_ADDRESSCOUNTRY_FIELD,
//                Constants.DATACENTER_ADDRESS_FIELD, Constants.SERVICE_GENERAL_LOCATION_FIELD);
//        if(locationRelatedAttributes.contains(attribute) && CollectionUtils.intersection(locationRelatedAttributes, properties.keySet()).size() > 0){
//            return true;
//        }

        return false;
    }
    private void refineProperty(Map<String, List<String>> properties, Map<String, Map<String,Long>> preference,Set<String> attributesToConsider, int limit){
        if(properties == null || preference == null || preference.size() == 0 || attributesToConsider == null || attributesToConsider.size() == 0)
            return;

        // set default frequency limit if no valid value is given
        limit = limit <= 0 ? preferenceFrequencyLimit : limit;

        // select the top value for each
        for(Map.Entry<String, Map<String, Long>> outerEntry : preference.entrySet()){
            String attri = outerEntry.getKey();
            Map<String,Long> valCount = outerEntry.getValue();
            if(!propertyOverlapped(properties, attri) && attributesToConsider.contains(attri)){
                Map<String, Long> result = new LinkedHashMap<>();

                //sort map by count in descending order...
                valCount.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));

                // get the top 2 preferences whose frequency is larger than the limit, and add it to property
                int count = 0;
                List<String> valList = new ArrayList<String>();
                for(Map.Entry<String,Long> entry : result.entrySet()){
                    if(entry.getValue() > limit){
                        valList.add(entry.getKey());
                        count ++;
                        if(count == 2){
                            break;
                        }
                    }
                }
                if(valList.size() != 0){
                    properties.put(attri, valList);
                }
            }
        }
    }


    public SolrDocumentList rerankResultsWithUserprofile(SolrDocumentList existingResults, UserProfile userProfile){
        if(userProfile == null || userProfile.getLikedRests() == null || userProfile.getLikedRests().size() == 0)
            return existingResults;
        List<String> likedRests = userProfile.getLikedRests();
        LinkedHashMap<SolrDocument, Double> resultsScoreMap = new LinkedHashMap<>();

        // print the original results
//        System.out.println("Before reranking the results are: ");
//        existingResults.forEach(x->{
//            System.out.println(x.getFieldValue(Constants.RESTAURANT_ID_FIELD) + " --" + x.getFieldValue(Constants.RESTAURANT_NAME_FIELD)  + "--" + x.getFieldValue(Constants.SOLR_SCORE) + "--" + x.getFieldValue(Constants.SOLR_DISTANCE));
//        });

        // get weights and normalized scores
        float maxScore = Collections.max(existingResults.stream().map(x->(Float)x.getFieldValue(Constants.SOLR_SCORE)).collect(Collectors.toList()));
        float maxDistance = -1f;
        if(existingResults.stream().filter(x->x.getFieldValue(Constants.SOLR_DISTANCE) != null).count() > 0){
            maxDistance = Collections.max(existingResults.stream().filter(x->x.getFieldValue(Constants.SOLR_DISTANCE) != null).map(x->Float.valueOf(String.valueOf(x.getFieldValue(Constants.SOLR_DISTANCE)))).collect(Collectors.toList()));
        }

        float w_score = 0.333f;
        float w_dist = 0.333f;
        float w_likeness = 0.333f;
        if(maxDistance == -1f){
            w_score = 0.3f;
            w_dist = 0.0f;
            w_likeness = 0.7f;
        }else{
            w_score = 0.3f;
            w_dist = 0.3f;
            w_likeness = 0.4f;
        }

        // get the final score for each result
        for (SolrDocument x : existingResults) {
            double refinedScore = 0.0f;
            if(maxScore != 0.0f){
                refinedScore += w_score * (Float)x.getFieldValue(Constants.SOLR_SCORE)/maxScore;
            }
            if(maxDistance != -1f){
                if(maxDistance != 0f){
                    refinedScore += (1 - w_dist  * (Double)x.getFieldValue(Constants.SOLR_DISTANCE)/maxDistance);
                }
            }
            if(likedRests.contains(String.valueOf(x.getFieldValue(Constants.SERVICE_ID_FIELD)))){
                refinedScore += w_likeness;
            }
            resultsScoreMap.put(x,refinedScore);
        }
       List<SolrDocument> rst = resultsScoreMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).map(Map.Entry::getKey).collect(Collectors.toList());

        SolrDocumentList results2return = new SolrDocumentList();
        rst.forEach(x->{results2return.add(x);});
        return results2return;
    }

    private String cityFromProperties(Map<String, List<String>> properties){
        try{
            return properties.entrySet().stream().filter(x ->x.getKey().equalsIgnoreCase("addresscity"))
                    .collect(Collectors.toList()).stream().map(x -> x.getValue()).findFirst().get().get(0);
        }catch (Exception e){
            return "";
        }
    }

}
