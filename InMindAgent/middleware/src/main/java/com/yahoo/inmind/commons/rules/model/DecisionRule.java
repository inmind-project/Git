package com.yahoo.inmind.commons.rules.model;

import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.commons.rules.control.DecisionRuleValidator;
import com.yahoo.inmind.commons.rules.control.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by oscarr on 9/28/15.
 */
public class DecisionRule {
    private ArrayList<ConditionElement> conditions;
    private String regularExpression;
    private ArrayList<ActionElement> actions;
    private String ruleID;

    @Exclude
    private HashMap<String, HashMap<String, Object>> cacheMemory;

    public DecisionRule() {
        conditions = new ArrayList<>();
        actions = new ArrayList<>();
        cacheMemory = new HashMap<>();
    }

    public String getRuleID() {
        if( ruleID == null ){
            Util.getUUID();
        }
        return ruleID;
    }

    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public ArrayList<ConditionElement> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<ConditionElement> conditions) {
        this.conditions = conditions;
    }

    public ArrayList<ActionElement> getActions() {
        return actions;
    }

    public void setActions(ArrayList<ActionElement> actions) {
        this.actions = actions;
    }

    public String getRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
    }

    public void addCondition( String nameOfTerm, PropositionalStatement propositionalStatement ){
        conditions.add( new ConditionElement( nameOfTerm, propositionalStatement ) );
    }

    public void addAction( String nameOfComponent, HashMap<String, Object> attributes ){
        actions.add( new ActionElement( nameOfComponent, attributes ) );
    }

    public List<ActionElement> extractActions( String componentName){
        List<ActionElement> actions = new ArrayList<>();
        for( ActionElement actionElement : this.actions ){
            if( actionElement.componentName.equals( componentName )){
                actions.add( actionElement );
            }
        }
        return actions;
    }

    public List<ConditionElement> extractConditions( String componentName ){
        List<ConditionElement> conditions = new ArrayList<>();
        for( ConditionElement conditionElement : this.conditions ){
            if( conditionElement.getProposition().getComponentName().equals(componentName)){
                conditions.add( conditionElement );
            }
        }
        return conditions;
    }


    public HashMap<String, HashMap<String, Object>> getCacheMemory() {
        return cacheMemory;
    }

    public void setPropositionFlag(PropositionalStatement propositionalStatement, boolean flag,
                                   ArrayList triggeredConditions) {
        //check whether all flags are true
        boolean checked = true;

        for( ConditionElement conditionElement : conditions ){
            if( conditionElement.proposition == propositionalStatement ){
                conditionElement.setFlag( flag );
            }
            if( !conditionElement.isFlag() ){
                checked = false;
            }
        }

        // if all conditions are true then trigger the actions
        if( checked ){
            DecisionRuleValidator.getInstance().triggerActions( this, triggeredConditions );
        }
    }

    public void destroy() {
        for( ConditionElement conditionElement : conditions ){
            conditionElement.destroy();
        }
        for( ActionElement actionElement : actions ){
            actionElement.destroy();
        }
        conditions = null;
        actions = null;
    }

    /***************************************** HELPER CLASSES *************************************/

    public class ConditionElement {
        private String term;
        private PropositionalStatement proposition;
        @Exclude private boolean flag;

        public ConditionElement(String term, PropositionalStatement proposition) {
            this.term = term;
            this.proposition = proposition;
            this.proposition.addRule( DecisionRule.this );
        }

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public PropositionalStatement getProposition() {
            return proposition;
        }

        public void setProposition(PropositionalStatement proposition) {
            this.proposition = proposition;
        }

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }


        public void destroy() {
            proposition.destroy();
            proposition = null;
        }
    }

    public class ActionElement{
        private String componentName;
        private HashMap<String, Object> attributes;

        public ActionElement(String component, HashMap attributes) {
            this.componentName = component;
            this.attributes = attributes;
        }

        public String getComponentName() {
            return componentName;
        }

        public void setComponentName(String componentName) {
            this.componentName = componentName;
        }

        public HashMap<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(HashMap<String, Object> attributes) {
            this.attributes = attributes;
        }

        public void destroy() {
            attributes.clear();
            attributes = null;
        }
    }
}

