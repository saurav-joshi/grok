package com.iaasimov.state;

import com.iaasimov.entity.Conversation;
import com.iaasimov.workflow.FlowManagement;
import com.iaasimov.workflow.LibraryUtil;

import java.util.List;

public class ExpectationManagementState extends State {

    @Override
    public void setStateType() {
        this.stateType = "ExpectationManagementState";
    }

    @Override
    public String process(Conversation conversation){
        if(conversation.getLatestQA().getQuestion().length()>1
                && conversation.getLatestEntities().isEmpty() && !conversation.isPreProcess()){
            conversation = FlowManagement.analyzeWithPartialMatchStep(conversation);
            return "EndState";
        }
        List<String> paths = conversation.getLatestStatePaths();
        int numOfEM = 0;
        for(int i = conversation.getQaList().size()-2; i> -1; i--){
            if(conversation.getQaList().get(i).getStatePaths().contains("ExpectationManagementState")){
                numOfEM++;
            }
            else break;
        }
        if(numOfEM > 2){
            conversation.getLatestAnswer().setMessage(LibraryUtil.getRandomPatternByQuestionClass("ExpectationManagement.UserGuide").getSystemMessage());
        }
        else if(paths.stream().anyMatch(x->x.equals("ConsumerQueryState") || x.equals("UserRefineState") || x.equals("ClarificationState")))
            conversation.getLatestAnswer().setMessage(LibraryUtil.getRandomPatternByQuestionClass("ExpectationManagement.NoResult").getSystemMessage());
        else if (paths.stream().anyMatch(x->x.equals("ShowMoreState")))
            conversation.getLatestAnswer().setMessage(LibraryUtil.getRandomPatternByQuestionClass("ExpectationManagement.NoMoreResult").getSystemMessage());
        else
            conversation.getLatestAnswer().setMessage(LibraryUtil.getRandomPatternByQuestionClass("ExpectationManagement.Unknown").getSystemMessage());
        return "EndState";
    }

    @Override
    public void allowedInputStateTypes(){
        allowedInputStateTypes.add("StartState");
        allowedInputStateTypes.add("SystemGreetingState");
        allowedInputStateTypes.add("ConsumerQueryState");
        allowedInputStateTypes.add("UserRefineState");
    }

    public boolean guardCheck(Conversation con){
        return true;
    }
}
