package com.yahoo.inmind.commons.rules.model;

import com.yahoo.inmind.commons.rules.control.Exclude;
import com.yahoo.inmind.commons.control.Constants;

import java.util.ArrayList;

/**
 * Created by oscarr on 9/15/15.
 */
public abstract class PropositionalStatement {
    protected String componentName;
    protected String attribute;
    protected String operator;
    protected String value;
    protected String referenceAttribute;
    /** a propositional statement can be related to 0 or many rules **/
    @Exclude //we need to exclude this for json parsing
    protected ArrayList<DecisionRule> rules;

    public PropositionalStatement() {
        rules = new ArrayList<>();
    }

    public PropositionalStatement(String attribute, String operator, String value) {
        this();
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public PropositionalStatement(String attribute, String operator, String value, String referenceAttribute) {
        this(attribute, operator, value.toString());
        this.referenceAttribute = referenceAttribute;
    }

    /**
     * In those cases where the preposition is listening to events in order to validate some data
     * you should implement this method and subscribe to the MessageBroker, otherwise just leave it empty
     */
    public void subscribe(){}

    public boolean validateNumbers(Number attribute, Number value){
        if ( this.operator.equals(Constants.OPERATOR_EQUALS_TO)
                && attribute.doubleValue() == value.doubleValue()) {
            return true;
        } else if ( this.operator.equals(Constants.OPERATOR_HIGHER_THAN)
                && attribute.doubleValue() > value.doubleValue()) {
            return true;
        } else if ( this.operator.equals(Constants.OPERATOR_LOWER_THAN)
                && attribute.doubleValue() < value.doubleValue()) {
            return true;
        }
        return false;
    }

    public boolean validateStrings(String attribute){
        if (this.operator.equals(Constants.OPERATOR_EQUALS_TO)
                && attribute.equalsIgnoreCase( this.value ) ) {
            return true;
        } else if (this.operator.equals(Constants.OPERATOR_CONTAINS_STRING)
                && attribute.contains( this.value ) ) {
            return true;
        }
        return false;
    }

    public void addRule(DecisionRule rule){
        rules.add( rule );
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    abstract public Object validate( Object objValue );

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}
