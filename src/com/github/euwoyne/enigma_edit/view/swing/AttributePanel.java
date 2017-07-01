package com.github.euwoyne.enigma_edit.view.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

import com.github.euwoyne.enigma_edit.Resources;
import com.github.euwoyne.enigma_edit.model.Tileset;

class AttributePanel extends JPanel implements Scrollable
{
	private static final long serialVersionUID = 1L;
	
	private static final Color cOn  = new Color( 0x0, 0xA0, 0x0);
	private static final Color cOff = new Color(0xA0,  0x0, 0x0);
	
	private static final String strObject   = Resources.uiText.getString("ObjectPanel.object");
	private static final String strTrue     = Resources.uiText.getString("ObjectPanel.true");
	private static final String strFalse    = Resources.uiText.getString("ObjectPanel.false");
	private static final String strYes      = Resources.uiText.getString("ObjectPanel.yes");
	private static final String strNo       = Resources.uiText.getString("ObjectPanel.no");
	private static final String strOn       = Resources.uiText.getString("ObjectPanel.on");
	private static final String strOff      = Resources.uiText.getString("ObjectPanel.off");
	private static final String strEnabled  = Resources.uiText.getString("ObjectPanel.enabled");
	private static final String strDisabled = Resources.uiText.getString("ObjectPanel.disabled");
	
	private static class SwitchButton extends JToggleButton implements ItemListener
	{
		private static final long serialVersionUID = 1L;
		private final String[] states;
		private final Color[]  bgColors;
		
		SwitchButton(String[] states, boolean initialState, Color[] backgroundColors)
		{
			this.states   = states.clone();
			this.bgColors = new Color[states.length];
			
			if (backgroundColors == null)
				Arrays.fill(bgColors, UIManager.getColor("Button.background"));
			else
				Arrays.setAll(bgColors, i -> i < backgroundColors.length ? backgroundColors[i]
				                                                         : UIManager.getColor("Button.background"));
			this.setBackground(bgColors[initialState ? 1 : 0]);
			this.setText(states[initialState ? 1 : 0]);
			this.addItemListener(this);
			this.setSelected(initialState);
		}
		
		SwitchButton(String[] states, boolean initialState, Color[] backgroundColors, boolean enabled)
		{
			this(states, initialState, backgroundColors);
			this.setEnabled(enabled);
		}
		
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			final boolean state = (e.getStateChange() == ItemEvent.SELECTED);
			this.setBackground(bgColors[state ? 1 : 0]);
			this.setText(states[state ? 1 : 0]);
		}
	}
	
	private static class IntegerSpinner extends JSpinner
	{
		private static final long serialVersionUID = 1L;
		
		IntegerSpinner(Tileset.Attribute attr, String value)
		{
			final Dimension size = getPreferredSize();
			setModel(new SpinnerNumberModel(Integer.parseInt(value), attr.getMin().intValue(), attr.getMax().intValue(), 1));
			this.getEditor().setPreferredSize(size);
			setPreferredSize(size);
		}
	}
	
	private static class FloatSpinner extends JSpinner
	{
		private static final long serialVersionUID = 1L;
		
		FloatSpinner(Tileset.Attribute attr, String value)
		{
			final Dimension size = getPreferredSize();
			setModel(new SpinnerNumberModel(Double.parseDouble(value), attr.getMin().doubleValue(), attr.getMax().doubleValue(), 0.1));
			this.getEditor().setPreferredSize(size);
			setPreferredSize(size);
		}
	}
	
	private String[] getEnumData(Tileset.Attribute attr)
	{
		String[] stringArr = new String[0];
		String   i18nId;
		stringArr = attr.getEnums().toArray(stringArr);
		for (int idx = 0; idx < stringArr.length; ++idx)
		{
			i18nId = attr.getI18n() + "_" + stringArr[idx];
			if (tileset.hasString(i18nId))
				stringArr[idx] = tileset.getString(i18nId).get(lang);
		}
		return stringArr;
	}
	
	private class EnumBox extends JComboBox<String>
	{
		private static final long serialVersionUID = 1L;
		
		EnumBox(Tileset.Attribute attr, String value)
		{
			super(getEnumData(attr));
			int selected = attr.getEnums().indexOf(value);
			this.setSelectedIndex(selected);
			if (selected == -1)
			{
				this.setEditable(true);
				this.getEditor().setItem(value);
			}
		}
	}
	
	private static class TextField extends JTextField
	{
		private static final long serialVersionUID = 1L;
		
		TextField(String value)
		{
			super(value, 16);
		}
		
		TextField(String value, boolean enabled)
		{
			this(value);
			this.setEnabled(enabled);
		}
	}
	
	private class GroupPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		ArrayList<JLabel>     labels;
		ArrayList<JComponent> values;
		
		GroupPanel(String title)
		{
			this.setBorder(BorderFactory.createTitledBorder(title));
			this.labels = new ArrayList<JLabel>();
			this.values = new ArrayList<JComponent>();
			this.setLayout(new GroupLayout(this));
			this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		}
		
		private JComponent createAttributeControl(Tileset.Attribute attr, Tileset.AttributeProvider values)
		{
			final String value = values.hasAttribute(attr.getName()) ? values.getAttribute(attr.getName()) : attr.getDefault();
			
			if (attr.enumSize() > 0)
				return new EnumBox(attr, value);
			
			switch (attr.getType())
			{
			case BOOLEAN:
				{final boolean v = value.equalsIgnoreCase("true");
				switch (attr.getUi())
				{
				case ATTR:  return new SwitchButton(new String[]{strFalse, strTrue}, v, new Color[]{cOff, cOn});
				case YESNO: return new SwitchButton(new String[]{strNo,    strYes},  v, new Color[]{cOff, cOn});
				case ONOFF: return new SwitchButton(new String[]{strOff,   strOn},   v, new Color[]{cOff, cOn});
				case ENABLEDISABLE:
					return new SwitchButton(new String[]{strDisabled, strEnabled}, v, new Color[]{cOff, cOn});
				case SWITCH:
				case VISUAL:
				case CONNECTIONS:
				case CLUSTER:
				case READONLY:
					return new SwitchButton(new String[]{strFalse, strTrue}, v, new Color[]{cOff, cOn}, false);
				}}
			case INTEGER:   return new IntegerSpinner(attr, value);
			case FLOAT:     return new FloatSpinner(attr, value);
			case STRING:    return new TextField(value);
			case AUTOFLOAT: return new FloatSpinner(attr, value);
				
			case TOKEN:     return new TextField(value);
			case POSITION:  return new TextField(value);
			
			case DIRECTION:
				return new JComboBox<String>(new String[]{"North", "East", "South", "West"});
				
			case DIRECTION_NODIR:
				return new JComboBox<String>(new String[]{"[none]", "North", "East", "South", "West"});
				
			case DIRECTION_RND:
				return new JComboBox<String>(new String[]{"[random]", "North", "East", "South", "West"});
				
			case DIRECTION8:
				return new JComboBox<String>(new String[]{"North", "North-East", "East", "South-East",
				                                          "South", "South-West", "West", "North-West"});
				
			case DIRECTION8_NODIR:
				return new JComboBox<String>(new String[]{"[none]", "North", "North-East", "East", "South-East",
				                                                    "South", "South-West", "West", "North-West"});
				
			case DIRECTION8_RND:
				return new JComboBox<String>(new String[]{"North", "North-East", "East", "South-East",
				                                          "South", "South-West", "West", "North-West", "[random]"});
				
			case CONNECTIONS: return new TextField(value);
			case FLAGS:return new TextField(value);
			case STONE:return new TextField(value);
			case ITEM:return new TextField(value);
			case FLOOR:return new TextField(value);
			case KIND:return new TextField(value);
			case SELECTION:return new TextField(value);
			default: return new TextField(value, false);
			}
		}
		
		private void updateLayout()
		{
			this.removeAll();
			GroupLayout layout = (GroupLayout)this.getLayout();
			
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			GroupLayout.SequentialGroup    group;
			java.util.Iterator<JLabel>     itLabels;
			java.util.Iterator<JComponent> itValues;
			
			// setup vertical group
			group = layout.createSequentialGroup();
			itLabels = labels.iterator();
			itValues = values.iterator();
			while (itLabels.hasNext() && itValues.hasNext())
			{
				group.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(itLabels.next())
					.addComponent(itValues.next()));
			}
			layout.setVerticalGroup(group);
			
			// setup horizontal group
			group = layout.createSequentialGroup();
			GroupLayout.ParallelGroup pgroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
			for (itLabels = labels.iterator(); itLabels.hasNext();)
				pgroup.addComponent(itLabels.next());
			group.addGroup(pgroup);
			pgroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			for (itValues = values.iterator(); itValues.hasNext();)
				pgroup.addComponent(itValues.next());
			group.addGroup(pgroup);
			layout.setHorizontalGroup(group);
			
			// request redraw
			this.invalidate();
		}
		
		void add(Tileset.Attribute attr, Tileset.AttributeProvider valueSource)
		{
			if (attr.getI18n() != null && tileset.hasString(attr.getI18n()))
				labels.add(new JLabel(tileset.getString(attr.getI18n()).get(lang) + ":"));
			else
				labels.add(new JLabel(attr.getName()));
			values.add(createAttributeControl(attr, valueSource));
			updateLayout();
		}
		
		@Override
		public Dimension getMinimumSize()
		{
			return super.getPreferredSize();
		}
	}
	
	private final Tileset tileset;
	private final String  lang;
	
	public AttributePanel(Tileset tileset, Locale locale)
	{
		this.tileset = tileset;
		this.lang    = locale.getLanguage();
	}
	
	public void show(Tileset.Kind kind)
	{
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = c.gridy = 0;
		
		this.removeAll();
		
		final GroupPanel pnlKindAttr = new GroupPanel(strObject);
		for (Tileset.Attribute attr : kind.getAttributes().values())
		{
			if (!kind.getGroup().hasAttribute(attr.getName()))
				pnlKindAttr.add(attr, kind);
		}
		if (pnlKindAttr.getComponentCount() > 0)
			this.add(pnlKindAttr, c);
		
		for (Tileset.AttrGroup attrGroup : kind.getGroup().getAttrGroups())
		{
			final GroupPanel pnlAttributes = new GroupPanel(tileset.getString(attrGroup.getI18n()).get(lang));
			for (Tileset.Attribute attr : attrGroup)
			{
				pnlAttributes.add(attr, kind);
			}
			if (pnlAttributes.getComponentCount() > 0)
			{
				++c.gridy;
				this.add(pnlAttributes, c);
			}
		}
		
		this.revalidate();
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return super.getPreferredSize();
	}
	
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 16;
	}
	
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 16;
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}

