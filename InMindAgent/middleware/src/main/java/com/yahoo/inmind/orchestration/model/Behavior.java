package com.yahoo.inmind.orchestration.model;

import java.util.Collection;
import java.util.Vector;

/**
 * A competence module i can be described by a tuple (ci ai di zi). Where: 
 * ci is a list of preconditions which have to be full-filled before the 
 * competence module can become active. ai and di represent the expected 
 * effects of the competence module's action in terms of an add-list and a 
 * delete-list. In addition, each competence module has a level of activation zi
 * 
 * @author oromero
 *
 */
public class Behavior {
	private String name;
	private Vector <String> preconditions = new Vector <String>();
	private Vector <String> addList = new Vector <String>();
	private Vector <String> deleteList = new Vector <String>();
	private double activation = 0;
	private int id;
	private boolean executable = false, activated = false;
	
	public Behavior(String name, String[] preconds, String[] addlist, String[] deletelist){

	}

	public Vector <String> getAddList() {
		return addList;
	}
	public void setAddList(Vector <String> addList) {
		this.addList = addList;
	}
	public Vector <String> getDeleteList() {
		return deleteList;
	}
	public void setDeleteList(Vector <String> deleteList) {
		this.deleteList = deleteList;
	}
	public double getActivation() {
		return activation;
	}
	public void setActivation(double activation) {
		this.activation = activation;
	}
	public Collection<String> getPreconditions() {
		if(preconditions != null && preconditions.size() > 0)
			return preconditions;
		return new Vector<String>();
	}
	public void setPreconditions(Vector <String> preconditions) {
		this.preconditions = preconditions;
	}
	public int getId(){
		return this.id;
	}
	public boolean getExecutable(){
		return executable;
	}
	public String getName(){
		return name;
	}
	public boolean getActivated(){
		return activated;
	}
	public void setActivated(boolean a){
		activated = a;
	}
	
	public void setPrecondition(String proposition){
		preconditions.add(proposition);
	}
	
	/**
	 * Determines if is into the add-list
	 * @param proposition
	 * @return
	 */
	public boolean isSuccesor(String proposition){
		return addList.contains(proposition) == true;
	}
	
	/**
	 * Determines if is into the delete-list
	 * @param proposition
	 * @return
	 */
	public boolean isInhibition(String proposition){
		return deleteList.contains(proposition) == true;
	}
	public void setAddList(String proposition){
		addList.add(proposition);
	}
	
	/**
	 * Determines if is into the preconditions set
	 * @param proposition
	 * @return
	 */
	public boolean isPrecondition(String proposition){
		return preconditions.contains(proposition) == true;
	}
	
	/**
	 * the input of activation to module x from the state at time t is
	 * @param states
	 * @param matchedStates
	 * @param phi
	 * @return
	 */
	public double calculateInputFromState(Vector<String> states, int[] matchedStates, double phi){
		return 0;
	}
	
	/**
	 * The input of activation to competence module x from the goals at time t is
	 * @param goals
	 * @param achievedPropositions
	 * @param gamma
	 * @return
	 */
	public double calculateInputFromGoals(Vector<String> goals, int[] achievedPropositions, double gamma){
		return 0;
	}
	
	/**
	 * The removal of activation from competence module x by the goals that are protected
	 * at time t is.
	 * @param goalsR
	 * @param undoPropositions
	 * @param delta
	 * @return
	 */
	public double calculateTakeAwayByProtectedGoals(Vector<String> goalsR, int[] undoPropositions, double delta){
		return 0;
	}
	
	/**
	 * A function executable(i t), which returns 1 (true) if competence module i is executable
	 * at time t (i.e., if all of the preconditions of competence module i are members
	 * of S (t)), and 0 (false) otherwise.
	 */
	public boolean isExecutable (Vector <String> states){	
		return false;
	}
		
	public void resetActivation(boolean reset){

	}
	
	public void updateActivation(double act){

	}
	
	public void decay(double factor){
		activation *= factor;
	}

    public void setId(int id) {
        this.id = id;
    }
}
