package com.iaasimov.state;

import com.iaasimov.entity.Conversation;
import com.iaasimov.entity.QA;
import com.iaasimov.entity.Suggestion;
import com.iaasimov.workflow.GlobalConstantsNew;
import com.iaasimov.workflow.LibraryUtil;
import com.iaasimov.workflow.Parser;
import scala.Tuple2;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StartState extends State {

    public void setStateType() {
        this.stateType = "StartState";
    }

    public StartState() {

    }

    public String process(Conversation conversation) {
        try{

            String question = String.join(" ", conversation.getLatestQA().getCleanedQuestionPatternWords());

            // check whether it is a clarification
            if(conversation.getQaList().size() > 1){
                Suggestion secondLastQASuggestion = conversation.getQaList().get(conversation.getQaList().size() - 2).getAnswer().getSuggestion();
                if(secondLastQASuggestion != null){
                    List<String> options = secondLastQASuggestion.getOptions();
                    // TODO: 09/28/2017: may need to change the criteria for clarification and others
                    if(secondLastQASuggestion.getType() != null
                            && options !=null && options.contains(conversation.getLatestQueston())){
                        LibraryUtil.Pattern clarifyPattern = LibraryUtil.getRandomPatternByQuestionClass(secondLastQASuggestion.getType());
                        conversation.getLatestQA().setMatchedPatternIdInLibrary(clarifyPattern.getId());
                        return "ClarificationState";
                    }
                }
            }
//            if(conversation.isSimilar())
//                return "DefaultState";


            List<String> sentences = Parser.getSentencesfromText(question);
            System.out.println("question before classification : "+sentences);

            Tuple2<LibraryUtil.Pattern, Double> pattern_score = getCorrectPattern(question,conversation);
            if(pattern_score == null){
                return "ExpectationManagementState";
            }
            System.out.println("------ matched pattern:" + pattern_score._1.getLibraryName() + "--- score:" + pattern_score._2);
            conversation.getLatestQA().setMatchedPatternIdInLibrary(pattern_score._1.getId());
            System.out.println(pattern_score._1.getQuestionPatternWords()+" Score: "+pattern_score._2);
            System.out.println("matched pattern library:" + conversation.getLatestQA().getMatchedPattern().getLibraryName());

            String city = conversation.getCurrentCityOfUser();
            if(!city.isEmpty()){
                if(city.contains("||")){
                    //make suggestion and clarify
                    conversation.getLatestQA().getAnswer().setMessage("What you are looking for is available in several cities.");
                    conversation.getLatestQA().getAnswer().setSuggestion(new Suggestion(LibraryUtil.getRandomPatternByQuestionClass("Clarify.MultiCity").getSystemMessage(),
                            Arrays.asList(city.trim().split("\\|\\|")), "Clarify.MultiCity"));
                    return "EndState";
                }

                conversation.getLatestQA().setCity(city);
            }

            if (pattern_score._2 >= 0.55){

//                    return Arrays.stream(StateType.values())
//                            .filter(x -> x.toString().equals(pattern_score._1.getLibraryName()))
//                            .findFirst()
//                            .get();

              return GlobalConstantsNew.getInstance().iaaSimovStates.stream()
                .filter(x -> x.equals(pattern_score._1.getLibraryName()))
                .findFirst()
                .get();

            }

            else {
                if(conversation.getLatestEntities().stream().filter(
                        x -> !(x.getEntityName().contains("phone") ||  x.getEntityName().contains("pax")
                                ||  x.getEntityName().contains("distance") || x.getEntityName().contains("changeintentwords")
                                ||x.getEntityName().contains("accompany") || x.getEntityName().contains("occasion") || x.getEntityName().contains("regular")
                                ||  x.getEntityName().contains("email") ||  x.getEntityName().contains("date")))
                        .count() > 0){
                    System.out.println("Can not classify the question, but have extracted entity ! ");
                    System.out.println("Execute default Search query, matching pattern library:DefaultState");
                    // spin a generic Google search type of query...
                    conversation.getLatestQA().setMatchedPatternIdInLibrary(30);
                    return "DefaultState";
                    //return "ConsumerQueryState";
                }
                // spin a generic Google search type of query...

                System.out.println("Can not classify the question and also no entity extracted !");
                conversation.getLatestQA().setMatchedPatternIdInLibrary(30);
                return "DefaultState";
                //return "ExpectationManagementState";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "ExpectationManagementState";
        }
    }

    public void allowedInputStateTypes() {

    }

    public boolean guardCheck(Conversation con){
        return true;
    }

    public Tuple2<LibraryUtil.Pattern, Double> getCorrectPattern(String question, Conversation conversation){
        // Case -1: deal with empty question with geo only.
        if(question.trim().equals("")
                && conversation.getLatestQA().getOriginalQuestion().trim().equals("")
                && conversation.getLatestQA().getGeo() != null){
            // return as a consumer query where system refine answers are also checked
            return new Tuple2<>(LibraryUtil.getRandomPatternByQuestionClass("ConsumerQuery.ClassMember"), 1.0);
        }

        // Case 0: solve any entity confusion: 1). distance vs pax vs phone for number only case
        boolean flagOfNum = false;
        Matcher matcher = Pattern.compile("([0-9]*[.])?[0-9]+").matcher(conversation.getLatestQA().getOriginalQuestion());
        if(matcher.find()){
            String theNum =  matcher.group(0);
            String restOfString = conversation.getLatestQA().getOriginalQuestion().replaceAll(theNum,"");
            if(restOfString.trim().length() == 0){
                flagOfNum = true;
            }
        }

        // Case 1: use the original question to match the patterns, to handle some fixed answer problems
        if(conversation.getLatestQA().getOriginalQuestion() != null && conversation.getLatestQA().getOriginalQuestion().trim().length() != 0){
            Tuple2<LibraryUtil.Pattern, Double> matchOriginal = matchSentencesWithPattern(Arrays.asList(String.join(" ", Parser.lemmatizeAndLowercaseText(conversation.getLatestQA().getOriginalQuestion()))), conversation);
            System.out.println("Case 1: try with original question:" + conversation.getLatestQA().getOriginalQuestion() + " --matching score:" + matchOriginal._2);
            if(matchOriginal._2 > 0.8){
                return matchOriginal;
            }
        }

        // Case 2: In case of multiple sentences in the question, match as one sentences, to handle the long patterns (e.g, multiple sentence as well)
        List<String> sentences = Parser.getSentencesfromText(question);
        if(sentences.size() > 1){
            Tuple2<LibraryUtil.Pattern, Double> matchOneSentence = matchSentencesWithPattern(Arrays.asList(question), conversation);
            System.out.println("Case 2: try with question pattern (one sentence):" + question + " --matching score:" + matchOneSentence._2);
            if(matchOneSentence._2 >= 0.8){
                return matchOneSentence;
            }
        }

        // Case 3: match each sentence one by one, and find the optimal matched one.
        Tuple2<LibraryUtil.Pattern, Double> matchOptimal = matchSentencesWithPattern(sentences, conversation);
        System.out.println("Case 3: try with question pattern (optimal sentence):" + question + " --matching score:" + matchOptimal._2);
        if(matchOptimal._2 >= 0.7) {
            return matchOptimal;
        }

        String  entityNames = String.join(" ", conversation.getLatestQA().getEntityKeyList());
        List<String> entityList = Parser.getSentencesfromText(entityNames);

        Tuple2<LibraryUtil.Pattern, Double> matchEntityScore = matchSentencesWithPattern(entityList, conversation);
        System.out.println("Case 4: try with Entity Set:" + question + " --matching score:" + matchEntityScore._2);
        return matchEntityScore;

    }

    // given the current conversation and the sentences in the query, find the correct pattern
    public Tuple2<LibraryUtil.Pattern, Double> matchSentencesWithPattern(List<String> sentences, Conversation conversation){
        // calculate the match score for each sentence, and track the maxscore and the corresponding sentence
        Map<String,List<Tuple2<LibraryUtil.Pattern,Double>>> sentenceScoreMap = new HashMap<>();
        String maxSentence = null;
        double maxScore = -1;
        for(String sentence : sentences){
            List<Tuple2<LibraryUtil.Pattern,Double>> pattern_scores = LibraryUtil.patternClassificationMultiple(new ArrayList<>(Arrays.asList(sentence.split("\\s+"))),LibraryUtil.flatContextsMap);
            sentenceScoreMap.put(sentence, pattern_scores);
            if(pattern_scores.get(0)._2 > maxScore){
                maxScore = pattern_scores.get(0)._2;
                maxSentence = sentence;
            }
        }

        // get the sentence with the highest score
        if(sentenceScoreMap.get(maxSentence).stream().map(x->x._1.getLibraryName()).distinct().count() == 1){
            System.out.println("******* Single pattern state is matched *******" );
            return sentenceScoreMap.get(maxSentence).get(new Random().nextInt(sentenceScoreMap.get(maxSentence).size()));
        }else{
            System.out.println("******* Multiple patterns states are matched *******");
            Set<String> potentialStates = sentenceScoreMap.get(maxSentence).stream().map(x->x._1.getLibraryName()).collect(Collectors.toSet());
            System.out.println("******* " + potentialStates);

            QA secondLastQA = conversation.getQaList().size() < 2 ? null : conversation.getQaList().get(conversation.getQaList().size() - 2);
            if(potentialStates.contains("UserRefineState") && potentialStates.contains("ConsumerQueryState")){
                if(secondLastQA == null ||
                        secondLastQA.getStatePaths().size() == 0 ||
                        secondLastQA.getStatePaths().contains("UserGreetingState") ||
                        secondLastQA.getStatePaths().contains("MotivationState") ||
                        secondLastQA.getStatePaths().contains("HelpState") ||
                        //secondLastQA.getStatePaths().contains(StateType.ExpectationManagementState) ||
                        secondLastQA.getStatePaths().contains("BookingState")){
                    return sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("ConsumerQueryState")).findFirst().get();
                }

                if(secondLastQA.getStatePaths().contains("FixedAnswerState")){
                    return sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("ConsumerQueryState")).findFirst().get();
                }

                // for all the other cases such as: ShowMoreState, ShowMoreState+ConsumerQueryState, ConsumerQueryState, UserRefineState
                if(conversation.isLastQAWithinTolerantTime(conversation.getLatestQA(), secondLastQA)){
                    return sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("UserRefineState")).findFirst().get();
                }else{
                    return sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("ConsumerQueryState")).findFirst().get();
                }
            }else if(potentialStates.contains("UserRefineState") && potentialStates.contains("SystemRefineState")){
                if(secondLastQA == null ||
                        secondLastQA.getStatePaths().size() == 0 ||
                        secondLastQA.getStatePaths().contains("UserGreetingState") ||
                        secondLastQA.getStatePaths().contains("MotivationState") ||
                        secondLastQA.getStatePaths().contains("HelpState") ||
                        secondLastQA.getStatePaths().contains("ExpectationManagementState") ||
                        secondLastQA.getStatePaths().contains("BookingState")){
                    return sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("SystemRefineState")).findFirst().get();
                }
                if(secondLastQA.getStatePaths().contains("FixedAnswerState")){
                    return sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("SystemRefineState")).findFirst().get();
                }
                if(secondLastQA.getStatePaths().contains("ConsumerQueryState") &&
                        secondLastQA.getStatePaths().contains("SystemRefineState")){
                    return sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("SystemRefineState")).findFirst().get();
                }
                // for the other cases:
                if(secondLastQA.getEntities().stream().filter(x->x.getEntityName().contains("#location")).count() > 0 || secondLastQA.getEntities().stream().filter(x->x.getEntityName().contains("$location")).count()>0){
                    return sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("UserRefineState")).findFirst().get();
                }

                return sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("UserRefineState")).findFirst().get();
            }if(potentialStates.contains("ConsumerQueryState")
                    &&( potentialStates.contains("FixedAnswerState")
                        || potentialStates.contains("HelpState")
                        || potentialStates.contains("UserGreetingState"))
                    ){
                return  sentenceScoreMap.get(maxSentence).stream().filter(x->x._1.getLibraryName().equalsIgnoreCase("ConsumerQueryState")).findFirst().get();
            }else{
                // other multiple cases/
                //TODO: should not using random to make test's results consistent
                System.out.println("******* Multiple other states found");
                return sentenceScoreMap.get(maxSentence).get(0);///.get(new Random().nextInt(sentenceScoreMap.get(maxSentence).size()));
            }
        }
    }
}
