package enigma_edit.control;

import java.awt.event.ActionEvent;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Icon;

public abstract class Action extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private final int               id;
	private HashSet<AbstractButton> sources;
	
	public Action(int id, String name)
	{
		super(name);
		this.id = id;
		this.sources = new HashSet<AbstractButton>();
	}
	
	public Action(int id, String name, Icon icon)
	{
		super(name, icon);
		this.id = id;
		this.sources = new HashSet<AbstractButton>();
	}
	
	public int  getId()                          {return id;}
	public void addSource(AbstractButton button) {sources.add(button);}
	public void setSelected(boolean state)       {for (AbstractButton but : sources) but.setSelected(state);}
	
	abstract public void actionPerformed(ActionEvent e);
}