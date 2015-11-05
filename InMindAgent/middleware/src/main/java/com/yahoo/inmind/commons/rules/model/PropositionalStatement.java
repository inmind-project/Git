package com.yahoo.inmind.commons.rules.model;

import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.rules.control.Exclude;

import java.util.ArrayList;
import java.util.List;

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
    protected MessageBroker mb;

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
    public void subscribe(){
        mb = MessageBroker.getExistingInstance( this );
        mb.subscribe(this);
    }

    public void unsubscribe(){
        if( mb != null ) {
            mb.unsubscribe(this);
        }
    }


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

    /**
     * It validates all the objects associated to this proposition, for instance, CalendarProposition
     * will validate all user's calendar events.
     * @return
     */
    public abstract ArrayList validate();

    public ArrayList getList( Object validatedObject ){
        ArrayList list = new ArrayList();
        if( validatedObject != null ) {
            if( validatedObject instanceof List){
                for( Object element : (List) validatedObject ){
                    Object result = validate(element);
                    if (result != null) {
                        list.add(result);
                    }
                }
            }else {
                Object result = validate(validatedObject);
                if (result != null) {
                    list.add(result);
                }
            }
        }
        return list;
    }

    abstract public Object validate( Object objValue );

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

    public String getReferenceAttribute() {
        return referenceAttribute;
    }

    public void setReferenceAttribute(String referenceAttribute) {
        this.referenceAttribute = referenceAttribute;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public void destroy(){
        unsubscribe();
        rules.clear();
        rules = null;
    }
}
