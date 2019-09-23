package com.iaasimov.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import com.iaasimov.data.repo.UserProfileRepo;
import com.iaasimov.entity.*;
import com.iaasimov.entityextraction.NegationDetector;
import com.iaasimov.workflow.FlowManagement;
import com.iaasimov.workflow.GeoCalculator;
import com.iaasimov.data.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iaasimov.dao.WorkflowDao;
import com.iaasimov.data.repo.ConversationRepo;
import com.iaasimov.data.repo.ConversationRepoImpl;
import com.iaasimov.data.repo.UserProfileRepoImpl;
import com.iaasimov.entityextraction.LocationFromAddress;
import com.iaasimov.workflow.LibraryUtil;

import static com.iaasimov.entity.MultiLingual.translateText;

@RestController
public class ConversationController extends BaseController {

    //static FlowManagement ma = FlowManagement.init();

    private enum TYPE {QUERY, NEW, BEGIN}
    @Autowired
    UserProfileRepo userProfileRepo;
    @Autowired
    ConversationRepo conversationRepo;
    @Autowired
    ConversationRepoImpl conversationCustomRepo;
    @Autowired
    NegationDetector negationDetector;

    @Autowired
    UserProfileRepoImpl userProfileRepoCustom;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        //com.oracle.iaasimov.data.model.Conversation conversation=conversationRepo.findByUserProfileUserId("14f329be-bcb4-4f61-868a-bb53474c6795");
        com.iaasimov.data.model.Conversation conversation_nw = new com.iaasimov.data.model.Conversation();
        conversation_nw.setUserProfile(userProfileRepo.findOne("14f329be-bcb4-4f61-868a-bb53474c6795"));
        conversationRepo.save(conversation_nw);
        String test = "tell me about Oracle cloud";
        String keyword = "Oracle cloud";
        System.out.println(negationDetector.isNegation(test.split("\\s+"),negationDetector.negationScope(test.split("\\s+")),keyword.split("\\s+")));
    	return conversation_nw.getConversationId()+"";
    }
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public Object receive(@RequestBody UserMessage um, HttpServletResponse res) {
        System.out.println("---------------->>Collecting user profile<<----------------");
        com.iaasimov.entity.UserProfile up = userProfileRepoCustom.findByUserIdCustom(um.getToken());
    	if (up == null) {
    		res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    		//return getRespErr("Invalid token key.");
    	}
    	if (!contains(um.getType())) {
    		res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		return getRespErr("Invalid type key.");
    	}
    	if (TYPE.QUERY.toString().equalsIgnoreCase(um.getType()) && (um.getQuestion() == null || um.getQuestion().trim().equals(""))
                && (um.getLatitude() == null || um.getLatitude().trim().equals("") || um.getLongitude() == null || um.getLongitude().trim().equals(""))) {
    		res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		return getRespErr("Please ask any questions.");
    	}

    	// check number of request limited
    	if (isRequestLimited(um.getToken())) {
    		res.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
    		return getRespErr("Your application is expired. Please contact your admin.");
    	}

        System.out.println("---------------->>     Question: "+ um.getQuestion()+ "     <<----------------");
        com.iaasimov.entity.Conversation con = conversationCustomRepo.findCustomConByUserId(um.getToken());

        Random rand = new Random();
        if (con == null) {
            com.iaasimov.data.model.Conversation conversationModel = new com.iaasimov.data.model.Conversation();
            UserProfile userProfileModel = new UserProfile();
            userProfileModel.setName(up.getName());
            userProfileModel.setUserId(um.getToken());

            conversationModel.setUserProfile(userProfileModel);
            conversationRepo.save(conversationModel);

            con = new com.iaasimov.entity.Conversation();
            con.setId((int)conversationRepo.findByUserProfileUserId(um.getToken()).getConversationId());
            con.setUserId(um.getToken());
            con.setUserName(up.getName());
            con.setEMailName(um.geteMail());
            con.setQaList(new ArrayList<>());
        }
        con.setEMailName(um.geteMail());
        con.setSimilar(um.isSimilar());
        con.setDomain(um.getDomain());
        boolean languageFlag = false;
        um.setOriginalQuestion(um.getQuestion());
        String question = languageFlag ? nativeLanguagueSupport(um.getQuestion()): um.getQuestion();
        um.setQuestion(question);
        QA qa = new QA();
            //String question = marshallMessage(um.getQuestion());

            qa.setQuestion(um.getQuestion() == null ? "" : um.getQuestion().replaceAll("\\?|,|“|”|-|–", " ").toLowerCase().trim());
            qa.setOriginalQuestion(um.getQuestion() == null ? "" : um.getQuestion().replaceAll("\\?|,|“|”|-", " ").toLowerCase().trim());
            qa.setTime(new Date());
            qa.setConversation_id(con.getId());
            qa.setId(con.getQaList().size()+1);
        if(um.getLatitude() != null && org.apache.commons.lang3.math.NumberUtils.isParsable(um.getLatitude())
                && um.getLongitude() != null && org.apache.commons.lang3.math.NumberUtils.isParsable(um.getLongitude())){
            qa.setGeo(String.join(",", Arrays.asList(um.getLatitude(), um.getLongitude())));
            //TODO: move city and location from start state to here
        }
        con.getQaList().add(qa);

        Answer answer = new Answer() ;
        if(um.getType().equalsIgnoreCase(TYPE.QUERY.toString())){
            //if geo share and question is empty, set question is the city
            if(qa.getQuestion().isEmpty() && !qa.getGeo().isEmpty()){
                String city = GeoCalculator.getCityFromLatLongOpenMap(qa.getGeo().split(",")[0], qa.getGeo().split(",")[1]);
                if(con.getQaList().size()<3){
                    if(!LocationFromAddress.getCitiesCovered().contains(city)){
                        con.getLatestQA().getAnswer().setMessage(LibraryUtil.getRandomPatternByQuestionClass("Warning.CoveredCity").getSystemMessage());
                    }else{
                        //new user share location => show acknowledgement and tip
                        String greeting = getDialogByCity(city, dialogTypeByCity.SystemAcknowledgement.name()) + "\n"
                                + LibraryUtil.getRandomPatternByQuestionClass("SystemTip").getSystemMessage() + "\n"
                                + getDialogByCity(city, dialogTypeByCity.SystemStarted.name());
                        con.getLatestQA().getAnswer().setMessage(greeting);
                    }
                    answer = con.getLatestQA().getAnswer();
                }
            }else{
                answer = query(con);
            }
        } else if(um.getType().equalsIgnoreCase(TYPE.NEW.toString()))
            answer = newUserGreeting(con);
        else if(um.getType().equalsIgnoreCase(TYPE.BEGIN.toString()))
            answer = userBeginGreeting(con);

        WorkflowDao.getInstance().insertListQA(con);
        //userProfileRepoCustom.saveUserProfile(UserProfiling.profiling(Collections.singletonList(con)));
        //tracking query from user success or fail
        if (answer.getMessage() == null) {
        	trackingRequest(um.getToken(), false);
        } else {
        	trackingRequest(um.getToken(), true);
        }

        res.setStatus(HttpServletResponse.SC_OK);
        return answer;
    }

//    private String marshallMessage(String m){
//
//    }

    private String nativeLanguagueSupport(String question)
    {
        //String nativeLang = Multilingual.getInstance().detectLanguage(question);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);

        String nativeLang = MultiLingual.detectLanguage(question, out);

        if( nativeLang != "en") {
            question = MultiLingual.translateText(question, out);
        }
        return question;
    }

    private Answer query(com.iaasimov.entity.Conversation con){
        try{
            FlowManagement.init().process(con);
        }catch (Exception e){
            e.printStackTrace();
            con.getLatestQA().getAnswer().setMessage(LibraryUtil.getRandomPatternByQuestionClass("ExpectationManagement.UserGuide").getSystemMessage());
        }
        return con.getLatestAnswer();
    }

    private Answer newUserGreeting(com.iaasimov.entity.Conversation con){
    	String greeting = LibraryUtil.getRandomPatternByQuestionClass("SystemGreeting.FirstTimeUser").getSystemMessage()
                .replace("#UserName",con.getEMailName() );
                //.replace("#UserName",con.getUserName());
        //greeting = greeting + " " + LibraryUtil.getRandomPatternByQuestionClass("SystemRequestLocation").getSystemMessage();
        //LibraryUtil.Pattern disPattern = LibraryUtil.getRandomPatternByQuestionClass("Clarify.ShareLocation");
        //con.getLatestQA().getAnswer().setSuggestion(new Suggestion(disPattern.getSystemMessage(), Arrays.asList(disPattern.getPatternAtt().trim().split("\\|\\|")), "Clarify.ShareLocation"));
        con.getLatestQA().getAnswer().setMessage(greeting);
       return con.getLatestAnswer();
    }

    private Answer userBeginGreeting(com.iaasimov.entity.Conversation con){
        String greeting =LibraryUtil.getRandomPatternByQuestionClass("SystemGreeting.General").getSystemMessage()
                .replace("#UserName",con.getUserName()) + "\n"
                + LibraryUtil.getRandomPatternByQuestionClass("TipsOfTheDay").getSystemMessage(); //tips

        //TODO: use last known geo (return user)
        con.getLatestQA().getAnswer().setMessage(greeting);
        return con.getLatestAnswer();
    }

    private boolean contains(String type) {
        for (TYPE t : TYPE.values()) {
            if (t.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    private enum dialogTypeByCity {SystemStarted, SystemAcknowledgement}
    private String getDialogByCity(String city, String dialog){
        try{
            return LibraryUtil.getRandomPatternByQuestionClass(dialog + "." + city).getSystemMessage();
        }catch (Exception e){
            return "";
        }
    }

}

