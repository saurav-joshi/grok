package com.iaasimov.state;

import com.iaasimov.entity.Conversation;
import com.iaasimov.entity.QA;
import com.iaasimov.workflow.LibraryUtil;

public class HelpState extends State{
    @Override
    public void setStateType() {
        this.stateType = "HelpState";
    }

    @Override
    public void allowedInputStateTypes() {
        allowedInputStateTypes.add("ConsumerQueryState");
    }

    @Override
    public String process(Conversation conversation) {
        LibraryUtil.Pattern currentPattern = conversation.getLatestQA().getMatchedPattern();
        if(currentPattern != null){
            String answerMessage = currentPattern.getSystemMessage();
            QA secondLastQA = conversation.getQaList().size() < 2 ? null : conversation.getQaList().get(conversation.getQaList().size() - 2);
            if (secondLastQA != null) {
                if(secondLastQA.getStatePaths().contains("BookingState")){
                    if(currentPattern.getQuestionType().contains("Help.Help"))
                        answerMessage = LibraryUtil.getRandomPatternByQuestionClass("ExpectationManagement.IntentChange.Help.Help").getSystemMessage() + "<br>" + answerMessage;
                    else
                        answerMessage = LibraryUtil.getRandomPatternByQuestionClass("ExpectationManagement.IntentChange.Help.NegativeExpression").getSystemMessage();
    }
}
            conversation.getLatestQA().getAnswer().setMessage(answerMessage);
                    return "EndState";
                    }else{//do noting, route to expectation management
                    return "ExpectationManagementState";
                    }
    }

    public boolean guardCheck(Conversation con){
        return true;
    }
}
