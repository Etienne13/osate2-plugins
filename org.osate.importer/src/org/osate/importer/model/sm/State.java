package org.osate.importer.model.sm;

import java.util.ArrayList;
import java.util.List;

import org.osate.importer.Utils;

public class State {
	private String 				name;
	private int					identifier;
	private List<Transition>	incomingTransitions;
	private List<Transition>	outgoingTransitions;
	private StateMachine		internalStateMachine;
	private String 				entrypoint;
	
	public State ()
	{
		this.identifier				= Utils.INVALID_ID;
		this.name 					= null;
		this.incomingTransitions 	= new ArrayList<Transition> ();
		this.outgoingTransitions 	= new ArrayList<Transition> ();
		this.internalStateMachine	= new StateMachine ();
		this.entrypoint				= null;
	}
	
	public String getEntrypoint ()
	{
		return this.entrypoint;
	}
	
	public boolean hasEntrypoint ()
	{
		return (entrypoint != null);
	}
	
	public void setEntrypoint (String ep)
	{
		String t = null;
		if (ep != null)
		{
			t = ep.replace(';', ' ').replaceAll("=", ":=");
		}
		this.entrypoint = t;
	}
	
	public StateMachine getStateMachine ()
	{
		return this.internalStateMachine;
	}
	
	public void setStateMachine (StateMachine sm)
	{
		this.internalStateMachine = sm;
	}
	
	public State (String n)
	{
		this ();
		this.name = n;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName (String n)
	{
		String s = n.replace('\n', '_');
		this.name = s;
	}
	
	public boolean isValid ()
	{
		if (name.contains("="))
		{
			return false;
		}
		return true;
	}
	
	public void setIdentifier (int i)
	{
		this.identifier = i;
	}
	
	public int getIdentifier ()
	{
		return this.identifier;
	}
	
	public void addIncomingTransition (Transition t)
	{
		this.incomingTransitions.add(t);
	}
	
	public void addOutgoingTransition (Transition t)
	{
		this.outgoingTransitions.add(t);
	}
	
	public List<Transition> getIncomingTransitions ()
	{
		return this.incomingTransitions;
	}
	
	public List<Transition> getOutgoingTransitions ()
	{
		return this.outgoingTransitions;
	}
	
	public String toString ()
	{
		String res;
		res = "State ";
		
		if (this.name != null)
		{
			res += "name=" + name;
		}
		
		if (this.identifier != Utils.INVALID_ID)
		{
			res += "|id=" + this.identifier;
		}
		
		return res;
	}
	
}
