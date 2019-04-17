package com.iaasimov.state;

import com.iaasimov.entity.Conversation;

public class EndState extends State {


    public void setStateType() {
        this.stateType = "EndState";
    }

    public String process(Conversation conversation){
        return "EndState" ;
    }

    public void allowedInputStateTypes(){
        allowedInputStateTypes.add("ExpectationManagementState");
        allowedInputStateTypes.add("ConsumerQueryState");
        allowedInputStateTypes.add("SystemGreetingState");
        allowedInputStateTypes.add("UserRefineState");
        allowedInputStateTypes.add("MotivationState");
        allowedInputStateTypes.add("UserGreetingState");
    }

    public boolean guardCheck(Conversation con){
        return true;
    }
}
