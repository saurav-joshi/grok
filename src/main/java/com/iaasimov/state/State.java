package com.iaasimov.state;

import com.iaasimov.entity.Conversation;

import java.util.HashSet;
import java.util.Set;

public abstract class State {

     //public StateType stateType;
     public String stateType;

     public State(){

     }

     public Set<String> allowedInputStateTypes = new HashSet<>();

     public abstract void setStateType();

     public abstract void allowedInputStateTypes();

     public abstract boolean guardCheck(Conversation con);

     /**
      * @param conversation
      * @return next state to jump and accumulated Conversation with inferred/extracted information
      */
     //public abstract StateType process(Conversation conversation);
     public abstract String process(Conversation conversation);

     public String getStateType(){
          return this.stateType;
     }

     public Set<String> getAllowedInputStateTypes(){
          return allowedInputStateTypes;
     }

     public boolean checkAllowedInputStateType(String stateType){
          return allowedInputStateTypes.contains(stateType);
     }
}
