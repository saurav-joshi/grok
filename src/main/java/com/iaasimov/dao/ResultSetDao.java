package com.iaasimov.dao;

import com.iaasimov.recommender.Constants;
import com.google.common.collect.Sets;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResultSetDao extends SolrDao {
    boolean isEnableDemoMode = false; // demo mode
    boolean isBoostBySentiment = false;// enable boosting by sentiment
    boolean enableFuzzySearch = true;
    int fuzzyDegree = 3;
    int defaultNoOfRowsToReturn = 10;
    String relevanceSentimentCombineMethod = Constants.RELEVANCE_SENTIMENT_METHOD_BOOST;
    Map<String, String> tagWeights = null;

    private List<String> metaFieldsList = Arrays.asList(Constants.SERVICE_PRODUCT_FIELD,
            Constants.SERVICE_DIVISION_FIELD, Constants.SERVICE_REGION_FIELD,
            Constants.SERVICE_COUNTRY_FIELD, Constants.SERVICE_INDUSTRY_FIELD,
            Constants.SERVICE_USECASE_FIELD, Constants.SERVICE_CUSTOMER_FIELD,
            Constants.SERVICE_DISPLAY_CUSTOMER_FIELD, Constants.SERVICE_DISPLAY_COUNTRY_FIELD
            ,Constants.SERVICE_EMOTIONAL_CONNECT_FIELD,
            Constants.SERVICE_CSM_FIELD,
            Constants.SERVICE_GENEREAL_QUERY,
            Constants.SERVICE_ENTITIES_QUERY,
            Constants.SERVICE_FLYING_CITY_FIELD,
            Constants.SERVICE_SIMILAR_QUERY,
            Constants.SERVICE_GENEREAL_RESPONSE);

    Set<String> serviceMetaFields = Sets.newHashSet(metaFieldsList);
    private Set<String> restFields = new HashSet<String>(ListUtils.union(metaFieldsList, Arrays.asList(Constants.SERVICE_URL_FIELD, Constants.SERVICE_USECASE_DETAILS_FIELD)));

    public Set<String> getRestFields() {
        return restFields;
    }

    public ResultSetDao(String solrURL, String tagFilePath) {
        super(solrURL);
        // load the tag weights from the resource folder
        this.tagWeights = loadTagWeightPool(tagFilePath);  //use defulte tag pool file
    }


    public Set<String> getServiceMetaFields() {
        return serviceMetaFields.stream().filter(x->x!=null).collect(Collectors.toSet());
    }

    public void setServiceMetaFields(Set<String> restMetaFields) {
        this.serviceMetaFields = restMetaFields;
    }


    public SolrQuery getServiceById(String id){
        if(null == id){
            return null;
        }
        SolrQuery solrQuery = new SolrQuery(Constants.SERVICE_ID_FIELD + ":" + id);
        return solrQuery;
    }

    public SolrQuery getServicesByIdList(List<String> ids) {
        if(ids == null || ids.size() == 0){
            return null;
        }

        StringJoiner sj = new StringJoiner(" " + Constants.DELIMITER_OR + " ", Constants.SERVICE_ID_FIELD+":(", ")");
        for(String id: ids){
            if(id == null || id.trim().length() == 0){
                continue;
            }
            sj.add(id);
        }
        if(sj.toString().length() != 0){
            SolrQuery solrQuery = new SolrQuery(sj.toString());
            solrQuery.addFilterQuery(Constants.NAMESPACE_FIELD + ":" + Constants.NAMESPACE_VALUE_ORACLE_CLOUD);
            solrQuery.setRows(ids.size());
            return solrQuery;
        }
        return null;
    }

    public SolrQuery getServiceByName(String name, boolean exactMatch, int noOfResults, String fieldType){
        if(name == null || name.trim().length() == 0){
            return null;
        }
        String field = fieldType == Constants.SERVICE_PRODUCT_FIELD ? Constants.SERVICE_PRODUCT_FIELD: Constants.SERVICE_SIMILAR_QUERY;
        SolrQuery solrQuery = null;
        if(exactMatch){
            solrQuery = new SolrQuery(field + ":\"" + name + "\"");
        }else{
            solrQuery = new SolrQuery(field + ":(" + name + ")");
        }
        // control the number of results to return
        if(noOfResults <= 0){
            solrQuery.setRows(defaultNoOfRowsToReturn);
        }else{


            solrQuery.setRows(noOfResults);
        }
        return solrQuery;
    }


    public SolrQuery getSimilarServicesByAspect(String restId, String aspect, int noOfResults){
        if(null == restId || 0 == restId.trim().length()){
            return null;
        }
        SolrQuery squery = getServiceById(restId);
        if(squery == null){
            return null;
        }
        SolrDocumentList services = searchDoc(squery);
        if(services == null){
            return null;
        }
        SolrDocument service = services.get(0);

        Object aspectDesc = null;

        aspectDesc = service.getFieldValue(aspect);
        if(aspectDesc == null){
            return null;
        }
        List<String> entities=null;
        if(serviceMetaFields.contains(aspect)){
            // replace , with space and then split by space
            entities = Arrays.asList(((String)aspectDesc).replace(","," ").split(" +"));

        }else{
            System.out.println("Invalid aspect is given.");
            return null;
        }

        StringJoiner sj = new StringJoiner(" " + Constants.DELIMITER_OR + " ", aspect + ":(", ")");
        for(String entity : entities){
            sj.add("\"" + entity + "\"");
        }

        // filter out the service itself
        String qstring = sj.toString() + " " + Constants.DELIMITER_AND + " -" + Constants.SERVICE_ID_FIELD + ":\"" + restId + "\"";

        SolrQuery solrQuery = new SolrQuery(qstring);
        solrQuery.addFilterQuery(Constants.NAMESPACE_FIELD + ":" + Constants.NAMESPACE_VALUE_ORACLE_CLOUD);

        // set other common query fields if any
        squery.setFields(Constants.SOLR_ALL_FIELDS, Constants.SOLR_SCORE);

        // control number of results to return
        if(noOfResults <= 0){
            solrQuery.setRows(defaultNoOfRowsToReturn);
        }else{
            solrQuery.setRows(noOfResults);
        }
        return solrQuery;

    }

    public SolrQuery searchServicesByAspectSingle(String aspect, String keyword, int noOfResults, boolean enableHighlight, boolean enableExpansion){
        if(null == keyword || keyword.length() == 0){
            return null;
        }
        if(enableExpansion){
            String keywordsQueryString = constructQueryStringFromTag(keyword, enableExpansion);
            return searchServicesByAspectSingle(aspect, keywordsQueryString,noOfResults, enableHighlight);
        }else{
            return searchServicesByAspectSingle(aspect, keyword,noOfResults, enableHighlight);
        }
    }

    public SolrQuery searchServicesByAspectList(String aspect, List<String> keywords, String delimiter, int noOfResults, boolean enableHighlight, boolean enableExpansion){
        if(null == keywords || keywords.size() == 0){
            return null;
        }
        if(enableExpansion){
            String keywordsQueryString = constructQueryStringFromTags(keywords, delimiter, true);
            return searchServicesByAspectSingle(aspect, keywordsQueryString, noOfResults,enableHighlight);
        }else{
            StringJoiner sj = new StringJoiner(" " + delimiter + " ", "(", ")");
            for(String kw : keywords){
                sj.add("\"" + kw + "\"");
            }
            return searchServicesByAspectSingle(aspect, sj.toString(), noOfResults, enableHighlight);
        }
    }
    public SolrQuery searchServicesByAspectSingle(String aspect, String keywordsQueryString, int noOfResults, boolean enableHighlight) {
        if(null == keywordsQueryString || keywordsQueryString.length() == 0){
            return null;
        }
        List<String> columnList = new ArrayList<>();

        if(serviceMetaFields.contains(aspect)){
            columnList.add(aspect);
        }else if(aspect.equalsIgnoreCase(Constants.SERVICE_GENERAL_LOCATION_FIELD)){
            columnList.add(Constants.DATACENTER_ADDRESS_FIELD);
        }else{
            System.out.println("Invalid aspect is given.");
            return null;
        }

        StringJoiner sj = new StringJoiner(" OR ", "", "");
        for(String clm : columnList){
            sj.add(keywordsQueryString);
        }
        SolrQuery solrQuery = new SolrQuery(sj.toString());

        // set the name space filter
        solrQuery.addFilterQuery(Constants.NAMESPACE_FIELD + ":" + Constants.NAMESPACE_VALUE_ORACLE_CLOUD);

        // sort by relevance
        solrQuery.addSort(Constants.SOLR_SCORE, SolrQuery.ORDER.desc);

        // set other common query fields if any
        solrQuery.setFields(Constants.SOLR_ALL_FIELDS, Constants.SOLR_SCORE);

        // control number of results to return
        if(noOfResults <= 0){
            solrQuery.setRows(defaultNoOfRowsToReturn);
        }else{
            solrQuery.setRows(noOfResults);
        }

        // configure highlighting if any
        if(enableHighlight == true){
            if(aspect.equalsIgnoreCase(Constants.SERVICE_GENERAL_LOCATION_FIELD)){
                setHighlightQuery(solrQuery, Constants.NO_OF_SNIPPETS, Constants.DATACENTER_ADDRESS_FIELD);
            }else{
                setHighlightQuery(solrQuery, Constants.NO_OF_SNIPPETS, aspect);
            }
        }
        return solrQuery;
    }

    public SolrQuery searchservicesByAspect(String aspect, Map<String, String> keywords, String delimiter, int noOfResults, boolean enableHighlight){
        if(null == keywords || keywords.size() == 0){
            return null;
        }
        // set up default values here:
        if(delimiter == null){
            delimiter = Constants.DELIMITER_OR;
        }
        // convert the keyword map into a query string
        StringJoiner sj = new StringJoiner(" " + delimiter + " ", "(", ")");
        for(Map.Entry<String,String> entry : keywords.entrySet()){
            sj.add("(" + entry.getKey() + ")^" + entry.getValue());
        }
        return searchServicesByAspectSingle(aspect, sj.toString(),noOfResults,enableHighlight);
    }

    public SolrQuery getPopularBOM(String location){
        SolrQuery solrQuery;
        if(location != null){
            solrQuery = searchServicesByAspectSingle(Constants.DATACENTER_ADDRESS_FIELD, location, 10, false, false);
        }else{
            solrQuery = new SolrQuery("*:*");
        }
        return solrQuery;
    }

    public Map<String, Integer> getPolurlarserviceTagsByAspect(String aspect, String aspectKeyword, int noOfResults, String typeOftags, int topk){
        SolrQuery squery = searchServicesByAspectSingle(aspect, aspectKeyword, noOfResults, false, false);
        SolrDocumentList services = searchDoc(squery);
        if(services == null || services.size() == 0){
            return null;
        }
        Map<String, Integer> tagsCountMap = new HashMap<String, Integer>();
        for(SolrDocument rest : services){
            if(null != rest.getFieldValue(typeOftags)){
                String entityStr = ((List<String>)rest.getFieldValue(typeOftags)).get(0);
                List<String> entities = Arrays.asList(entityStr.split(","));
                for(String entity : entities){
                    if(tagsCountMap.containsKey(entity)){
                        tagsCountMap.put(entity, tagsCountMap.get(entity)+1);
                    }else{
                        tagsCountMap.put(entity, 1);
                    }
                }
            }
        }

        Map<String, Integer> sortedTagsCountMap = tagsCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        if(topk == -1){
            return sortedTagsCountMap;
        }
        Map<String, Integer> restaultTagCountMap = new LinkedHashMap<String, Integer>();
        // for the tags in each dimension based on the popularity
        int count = 0;
        for(Map.Entry<String, Integer> entry : tagsCountMap.entrySet()){
            restaultTagCountMap.put(entry.getKey(), entry.getValue());
            count++;
            if(count == topk){
                break;
            }
        }
        return restaultTagCountMap;
    }


    public SolrQuery getQueryFromTag(String tag, String field, String delimiter, int noOfResults, boolean enableHighlight, boolean enableExpansion){
        List<String> tags = new ArrayList<String>();
        tags.add(tag);
        return getQueryFromTags(tags, field, delimiter, noOfResults, enableHighlight, enableExpansion);
    }

    public SolrQuery getQueryFromTags(List<String> tags, String field, String delimiter, int noOfResults, boolean enableHighlight, boolean enableExpansion){
        if(field == null || field.length() == 0){
            return null;
        }
        String expendedTagStr = constructQueryStringFromTags(tags, delimiter, enableExpansion);
        if(expendedTagStr == null || expendedTagStr.length() == 0){
            return null;
        }
        String queryString = field + ":(" + expendedTagStr + ")";
        SolrQuery squery = wrapRelevanceAndSentiment(queryString);

        if(squery != null){
            if(noOfResults <= 0){
                noOfResults = defaultNoOfRowsToReturn;
            }
            squery.setRows(noOfResults);
        }
        // configure highlighting if any
        if(enableHighlight == true){
            setHighlightQuery(squery, Constants.NO_OF_SNIPPETS, field);
        }
        return squery;
    }

    public String constructQueryStringFromServiceGroup(List<String>l, String fieldType){
        return constructQueryStringFromServices(l, fieldType, null);
    }

    public String constructQueryStringFromServices(List<String>list, String fieldType, List<String> aspects){
        Map<String, StringJoiner>aspectValueMap = new HashMap<>();

        for(String sid : list) {
            SolrQuery squery;

            // get the description of each given service
            if(fieldType == Constants.SERVICE_PRODUCT_FIELD ||fieldType == Constants.SERVICE_SIMILAR_QUERY ){
                squery = getServiceByName(sid,true,10, fieldType);
            }else{
                squery = getServiceById(sid);
            }
            SolrDocumentList services = searchDoc(squery);

            // skip empty services
            if (services == null || 0 == services.size()) {
                continue;
            }

            String restDesc = null;
            if(aspects == null || aspects.size() == 0){
                aspects = new ArrayList<>();
                aspects.add(Constants.SERVICE_GENEREAL_QUERY);

            }


            // skip Entities which has no aspect info
            SolrDocumentList refinedRests = new SolrDocumentList();
            for(SolrDocument rr : services){
                boolean hasContent = false;
                for(String a : aspects){
                    if(rr.getFieldValue(a) != null){
                        hasContent = true;
                    }
                }
                if(hasContent){
                   refinedRests.add(rr);
                }
            }
            if(refinedRests.size() == 0){
                continue;
            }
            services = refinedRests;

            Float restSenti = 0.0f;

            for(String ap:aspects){
                StringJoiner apsj = null;
                if(aspectValueMap.get(ap) != null){
                    apsj = aspectValueMap.get(ap);
                }else{
                    apsj = new StringJoiner(" " + Constants.DELIMITER_OR + " ", "", "");
                }

                List<String>entites = new ArrayList<>();
                try{//TODO: why only get the first result
                    String obj = (String)services.get(0).getFieldValue(ap);
                    if(obj != null){
                        String [] tmp = obj.trim().split(",");
                        for(String t : tmp){
                            entites.add(t);
                        }
                    }
                }
                catch (Exception e){
                    List<String> obj = (List<String>)services.get(0).getFieldValue(ap);
                    if(obj != null){
                        obj.forEach(x->{
                            String [] tmp = x.trim().split(",");
                            for(String t : tmp){
                                entites.add(t);
                            }
                        });
                    }
                }

                if(entites.size() == 0){
                    continue;
                }

                Map<String, Long> entityMap = entites.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                entityMap = entityMap.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));

                if(entityMap != null && entityMap.size() != 0){
                    StringJoiner sj = new StringJoiner(" " + Constants.DELIMITER_OR + " ", "", "");
                    int entityCount = 0;
                    for (Map.Entry<String, Long> entry : entityMap.entrySet()){
                        entityCount++;
                        if(entityCount>50){
                            break;
                        }
                        String oneSample = null;
                        if(enableFuzzySearch){
                            oneSample = "\"".concat(entry.getKey()).concat("\"~").concat(String.valueOf(fuzzyDegree)).concat("^").concat(String.valueOf(entry.getValue()));
                        }else{
                            oneSample = "\"".concat(entry.getKey()).concat("\"^").concat(String.valueOf(entry.getValue()));
                        }
                        sj.add(oneSample);
                    }

                    String oneAspectString = sj.toString();
                    // append the query string to the result query string
                    apsj.add(oneAspectString);
                    aspectValueMap.put(ap, apsj);
                }
            }
        }

        StringJoiner fnsj = new StringJoiner(" " + Constants.DELIMITER_OR + " ", "", "");
        for(Map.Entry<String, StringJoiner>entry : aspectValueMap.entrySet()){
            String apkey = entry.getKey();
            fnsj.add(apkey+":("+entry.getValue().toString()+")");
        }
        return fnsj.toString();
    }


    public String constructQueryStringFromTag(String tag, boolean enableExpansion){
        if(tag == null || tag.trim().length() == 0){
            return null;
        }
        List<String> tags = new ArrayList<String>();
        tags.add(tag);
        return constructQueryStringFromTags(tags, Constants.DELIMITER_AND, enableExpansion);
    }

    public String constructQueryStringFromTags(List<String> tags, String delimiter, boolean enableExpansion){
        if(tags == null || tags.size() == 0){
            return null;
        }
        if(delimiter == null){
            delimiter = Constants.DELIMITER_OR;
        }
        StringJoiner queryString = new StringJoiner(" " + delimiter + " ", "", "");
        for(String tag : tags ){
            // check wheter the tag contains tag keys or not, if yes, ignore it.
            if( -1 != tag.lastIndexOf(":")){
                System.out.println("Original pair:" + tag);
                tag = tag.substring(tag.lastIndexOf(":") + 1, tag.length());
                System.out.println("refined tag:" + tag);
            }

            // try to get the expanded tags words
            if(enableFuzzySearch){
                if(enableExpansion && tagWeights.containsKey(tag)){
                    String expendedTags = tagWeights.get(tag);
                    String fuzzyExpendedTags = expendedTags.replaceAll("\"\\^", "\"~" + fuzzyDegree + "\\^");
                    queryString.add(fuzzyExpendedTags);
                }else{
                    String fuzzyTag = "\"" + tag + "\"~" + fuzzyDegree;
                    queryString.add(fuzzyTag);
                }
            }
            else{
                // try to get the expanded tags words
                if(enableExpansion && tagWeights.containsKey(tag)){
                    queryString.add(this.tagWeights.get(tag));
                }else{ // try to get the expanded tags words
                    queryString.add(tag);
                }
            }

        }
        return queryString.toString();
    }

    public String[] preprocessNameValues(List<String> tags, String delimiter){
        // In case there are numbers in the service name, preprocess it for valid solr query
        if(tags == null || tags.size() == 0){
            return null;
        }

        String [] resultStrings = new String [2];
        if(delimiter == null){
            delimiter = Constants.DELIMITER_OR;
        }
        StringJoiner queryString = new StringJoiner(" " + delimiter + " ", "", "");
        StringJoiner exactMatchQueryString = new StringJoiner(" " + delimiter + " ", "", "");
        for(String tag : tags){
            if(tag == null || tag.trim().length() == 0){
                continue;
            }
            // old version
//            queryString.add("\"" + tag + "\"");
            // new version by enabling
            List<String> tmp = Arrays.asList(tag.split("\\s+"));
            List<String> newTag = tmp.stream().filter(x->x.length()!=0).map(x->{
                if(x.length() > 1)
                    return "+" + x;
                else
                    return x;
            }).collect(Collectors.toList());
            queryString.add("(" + String.join(" ", newTag) + ")");

            // construct the string for exact match using the service name field.
            exactMatchQueryString.add("\"" + capitalize(tag) + "\"");
        }
        resultStrings[0] = queryString.toString();
        resultStrings[1] = exactMatchQueryString.toString();
        return resultStrings;
    }

    private static String capitalize(String string) {
        if (string == null) return null;
        String[] wordArray = string.split(" "); // Split string to analyze word by word.
        int i = 0;
        lowercase:
        for (String word : wordArray) {
            if (word != wordArray[0]) { // First word always in capital
                String [] lowercaseWords = {"an", "as", "and", "although", "at", "because", "but", "by", "for", "in", "nor", "of", "on", "or", "so", "the", "to", "up", "yet", "am", "is", "are"};
                for (String word2 : lowercaseWords) {
                    if (word.equals(word2)) {
                        wordArray[i] = word;
                        i++;
                        continue lowercase;
                    }
                }
            }
            char[] characterArray = word.toCharArray();
            characterArray[0] = Character.toTitleCase(characterArray[0]);
            wordArray[i] = new String(characterArray);
            i++;
        }
        return StringUtils.join(wordArray, " "); // Re-join string
    }

    public SolrQuery wrapRelevanceAndSentiment(String qstr){
        if(qstr == "" || qstr == null){
            return null;
        }
        SolrQuery squery = new SolrQuery();
        if(relevanceSentimentCombineMethod == Constants.RELEVANCE_SENTIMENT_METHOD_SORT){
            // sort by mulitple values: score, sentiment.  the second comes to effect only when the first field results in tie
            squery.setQuery(qstr);
            squery.addSort(Constants.SOLR_SCORE, SolrQuery.ORDER.desc);

        }
        return squery;
    }

//    public void setDemoFilterQuery(SolrQuery squery){
//        if(squery != null){
//            squery.addFilterQuery(Constants.SERVICE_IMAGE_FIELD + ":[* TO *]");
//        }
//    }
    public void setSortingbyDistanceToLocation(SolrQuery solrQuery, double currentLocationLat, double currentLocationLon){
        if(solrQuery != null){
            // &sfield=store&pt=45.15,-93.85&sort=geodist() asc&fl=_dist_:geodist()
            solrQuery.set("sfield", Constants.SERVICE_GEO_FIELD);
            solrQuery.set("pt", String.valueOf(currentLocationLat + "," + String.valueOf(currentLocationLon)));
            solrQuery.addSort("geodist()", SolrQuery.ORDER.asc);
            solrQuery.addSort(Constants.SOLR_SCORE, SolrQuery.ORDER.desc);

//            solrQuery.set("sort", "geodist() asc");
            solrQuery.setFields(Constants.SOLR_ALL_FIELDS, Constants.SOLR_SCORE, Constants.SOLR_DISTANCE+":geodist()");
        }
    }

    public void addFilterQuery(SolrQuery solrQuery, String field, String value){
        if(solrQuery != null)
            solrQuery.addFilterQuery(field + ":" + value);
    }

    public void addFilterQuery(SolrQuery solrQuery, String field, List<String> values, boolean keepResults){
        if(solrQuery != null){
            StringJoiner sj = new StringJoiner(" " + Constants.DELIMITER_OR + " ", "(", ")");
            values.forEach(x->{
                if(x != null && x.trim().length() != 0){
                    sj.add("\"" + x + "\"");
                }
            });
            if(sj.toString().length() != 0){
                if(keepResults){
                    solrQuery.addFilterQuery(field + ":" + sj.toString());
                }else{
                    solrQuery.addFilterQuery("-" + field + ":" + sj.toString());
                }
            }
        }
    }

    public SolrQuery getDiscoveredItems(List<String> likedRest){
        if(likedRest == null || likedRest.size() == 0){
            return null;
        }
        String idString = String.join(" ", likedRest);
        if(idString.trim().length() == 0){
            return null;
        }
        StringJoiner sj = new StringJoiner(" " + Constants.DELIMITER_OR + " ", "", "");
        System.out.println("****************************");
        System.out.println("discovery query:" + sj.toString());
        SolrQuery solrQuery = new SolrQuery(sj.toString());
        return solrQuery;
    }


    // load the tag weights.
    public static Map<String, String> loadTagWeightPool(String fileName){
        // set the default file in case null is given
        if(null == fileName){
            fileName = Constants.TAG_EXTEND_FILE_PATH;
        }

        // result variable
        Map<String, String> tagWeigths = new HashMap<String, String>();

        // load tags and weights and save into the tagweights map
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                String [] tmp = line.split("\t");
                tagWeigths.put(tmp[0], tmp[1]);
            }
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return tagWeigths;
    }

    public List<String> processNumberInString(String line){
        line = line.replaceAll("[0-9]+", "#@#");
        String [] temp = line.trim().split("#@#");
        List<String> rst = null;
        for(int i = 0; i<temp.length; i++){
            String s = temp[i].trim();
            if(s.length() != 0){
                if(rst == null){
                    rst = new ArrayList<>();
                }
                rst.add(s);
            }
        }
        return rst;
    }
} // class end
