package com.yahoo.inmind.orchestration.control;

import android.util.Log;

import com.yahoo.inmind.orchestration.model.Behavior;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author oromero
 */
public class BehaviorNetwork {
	private Vector<Behavior> modules = new Vector<>();
	private Vector <String> states = new Vector<>();
	private Vector <String> goals = new Vector<>();
	private Vector <String> goalsResolved = new Vector <>();
    private Vector <String> oneTimeGoals = new Vector <>();
	
	private double pi = 20; // the mean level of activation,
	private double theta = 45; //15 the threshold of activation, where is lowered 10% every time no module could be selected, and is reset to its initial value whenever a module becomes active.
	private double initialTheta = 45;//15
	private double phi = 20;  // 90 the amount of activation energy injected by the state per true proposition,
	private double gamma = 70;  // 20 the amount of activation energy injected by the goals per goal,
	private double delta = 50; // 90 not defined the amount of activation energy taken away by the protected goals per protected goal.
	private double[][] activationSuccesors;
	private double[][] activationPredeccesors;
	private double[][] activationConflicters;	
	private double[] activationInputs;
	private double[] activationLinks;
	private boolean execution = false;
	private int indexBehActivated = -1;
    Double[] activations;

	public Vector<Behavior> getModules() {
		return modules;
	}

    public Double[] getActivations() {
        return activations;
    }

    public void setModules(Vector<Behavior> modules, int size) {

	}	

	public double getPi() {
		return pi;
	}

	public void setPi(double pi) {
		this.pi = pi;
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}
	
	public double getInitialTheta() {
		return initialTheta;
	}

	public void setInitialTheta(double itheta) {
		this.initialTheta = itheta;
	}

	public double getPhi() {
		return phi;
	}

	public void setPhi(double phi) {
		this.phi = phi;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}
	
	/**
	 * a function S (t) returning the propositions that are observed to be true at time t
	 * (the state of the environment as perceived by the agent) S being implemented
	 * by an independent process (or the real world),
	 */
	public Vector<String> getState (){

		return new Vector();
	}
		
	
	public void setState(Vector<String> states){

	}
	
	/**
	 * a function G(t) returning the propositions that are a goal of the agent at time
	 * t G being implemented by an independent process,
	 */
	public Vector<String> getGoals (){

		return new Vector<>();
	}
	
	public void setGoals(Vector<String> goals){

	}
	
	/**
	 * a function R(t) returning the propositions that are a goal of the agent that
	 * has already been achieved at time t R being implemented by an independent
	 * process (e.g. some internal or external goal creator),
	 */
	public Collection<String> getGoalsR (){

		return new Vector<>();
	}

	public void setGoalsR(Vector<String> goalsR){

	}

    /**
     * Set of goals that should be achieved only one time (protected goals)
     * @param goalsR
     */
    public void setOneTimeGoals(List<String> goalsR){

    }


	/**
	 * A function executable(i t), which returns 1 if competence module i is executable
	 * at time t (i.e., if all of the preconditions of competence module i are members
	 * of S (t)), and 0 otherwise.
	 */
	private boolean executable (int i){			
		return modules.get(i).isExecutable(states);
	}
	
	/**
	 * a function M (j), which returns the set of modules that match proposition j ,
	 * i.e., the modules x for which j E cx,
	 */
	public Vector<Behavior> matchProposition (String proposition){
		return null;
	}
	
	/**
	 * a function A(j ), which returns the set of modules that achieve proposition j ,
	 * i.e., the modules x for which j E ax,
	 */
	public Vector<Behavior> achieveProposition (String proposition){
		Vector<Behavior> behaviors = new Vector<Behavior>();

		return behaviors;
	}
	
	/**
	 * a function U (j ), which returns the set of modules that undo proposition j , i.e.,
	 * the modules x for which j E dx,
	 */
	public Vector<Behavior> undoProposition (String proposition){
		Vector<Behavior> behaviors = new Vector<Behavior>();

		return behaviors;
	}
	
	/**
	 * a function U (j ), which returns the set of modules that undo proposition j , i.e.,
	 * the modules x for which j E dx, and j E S(t)
	 */
	public Vector<Behavior> undoPropositionState (String proposition, int indexBehx){
		Vector<Behavior> behaviors = new Vector<Behavior>();				

		return behaviors;
	}
	
	/**
	 * The impact of the state, goals and protected goals on the activation level of a
	 / module is computed.
	 */
	public void computeActivation (){

	}
	
	/**
	 * The way the competence module activates and inhibits related modules through
 	 * its successor links, predecessor links and conflicter links is computed.
	 */
	public void computeLinks (){	

	}
	
	/**
	 * An executable competence module x spreads activation forward. It increases
	 * (by a fraction of its own activation level) the activation level of those 
	 * successors y for which the shared proposition p E ax n cy is not true. 
	 * Intuitively, we want these successor modules to become more activated because 
	 * they are `almost executable', since more of their preconditions will be fulfilled 
	 * after the competence module has become active. 
	 * Formally, given that competence module x = (cx ax dx zx) is executable, 
	 * it spreads forward through those successor links for which the proposition 
	 * that defines them p E ax is false.
	 */
	private double[] spreadsForward(int indexBehavior, boolean executable){
		double[] activation = new double[modules.size()];

		return activation;
	}
	
	/**
	 * A competence module x that is NOT executable spreads activation backward.
	 * It increases (by a fraction of its own activation level) the activation level of
	 * those predecessors y for which the shared proposition p E cx n ay is not true.
	 * Intuitively, a non-executable competence module spreads to the modules that
 	 * `promise' to fulfill its preconditions that are not yet true, so that the competence
	 * module may become executable afterwards. Formally, given that competence
	 * module x = (cx ax dx zx) is not executable, it spreads backward through those
	 * predecessor links for which the proposition that defined them p E cx is false.
	 * @param indexBehavior
	 * @param executable
	 * @return
	 */
	private double[] spreadsBackward(int indexBehavior, boolean executable){
		double[] activation = new double[modules.size()];

		return activation;
	}
	
	/**
	 * Inhibition of Conflicters
	 * Every competence module x (executable or not) decreases (by a fraction of its
	 * own activation level) the activation level of those conflicters y for which the
	 * shared proposition p E cx n dy is true. Intuitively, a module tries to prevent a
	 * module that undoes its true preconditions from becoming active. Notice that
	 * we do not allow a module to inhibit itself (while it may activate itself). In
	 * case of mutual conflict of modules, only the one with the highest activation
	 * level inhibits the other. This prevents the phenomenon that the most relevant
	 * modules eliminate each other. Formally, competence module x = (cx ax dx zx)
	 * takes away activation energy through all of its conflicter links for which the
	 * proposition that defines them p E cx is true, except those links for which there
	 * exists an inverse conflicter link that is stronger.
	 */
	private double[] takesAway(int indexBehavior){
		double[] activation = new double[modules.size()];				

		return activation;
	}
	
	/**
	 * Finds the intersection set S(t) n cy n dx
	 * @param indexBehx
	 * @param indexBehy
	 * @return
	 */
	private boolean inverseTakesAway(int indexBehx, int indexBehy){
		return false;
	}
	
	/**
	 * A decay function ensures that the overall activation level remains constant.
	 */
	public void applyDecayFx (){

	}
	
	/**
	 * The competence module that fulfills the following three conditions becomes
 	 * active: (i) It has to be executable, (ii) Its level of activation has to surpass a
 	 * certain threshold and (iii) It must have a higher activation level than all other
 	 * competence modules that fulfill conditions (i) and (ii). When two competence
 	 * modules fulfilll these conditions (i.e., they are equally strong), one of them is
 	 * chosen randomly. The activation level of the module that has become active is
 	 * reinitialized to 0 2. If none of the modules fulfills conditions (i) and (ii), the
 	 * threshold is lowered by 10%.
	 */
	public void activateBehavior (){

	}
	
	/**
	 * Execute the behavior
	 * @param behIndex
	 */
	private void execute(int behIndex){

	}
	
	/**
	 * Protect the goals achieved
	 * @param beh
	 */
	private void protectGoals(Behavior beh){

	}
	
	/**
	 * updates the activation of each behavior
	 */
	public void updateActivation(){

	}
	
	/**
	 * Reset the activation of the behaviors
	 */
	public void reset(){

	}


    /**
     * this method execute the spreading activation dynamics and select a behavior (if applicable)
     * @return
     */
    public int selectBehavior( ){

        return 0;
    }
}
