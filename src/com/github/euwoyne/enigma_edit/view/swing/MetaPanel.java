package com.github.euwoyne.enigma_edit.view.swing;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.github.euwoyne.enigma_edit.Resources;
import com.github.euwoyne.enigma_edit.model.LevelInfo;

public class MetaPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private final GroupLayout layout;
	
	private final LabeledText title;
	private final LabeledText subtitle;
	private final LabeledText author;
	private final LabeledText email;
	private final LabeledText homepage;
	
	private class LabeledText
	{
		public final JLabel     label;
		public final JTextField text;
		
		public LabeledText(String label)
		{
			this.label = new JLabel(label);
			this.label.setHorizontalAlignment(JLabel.RIGHT);
			this.text  = new JTextField();
		}
		
		public String getText()         {return text.getText();}
		public void   setText(String s) {text.setText(s);}
	}
	
	public MetaPanel()
	{
		// setup components
		title    = new LabeledText(Resources.uiText.getString("LevelInfo.title")    + ":");
		subtitle = new LabeledText(Resources.uiText.getString("LevelInfo.subtitle") + ":");
		author   = new LabeledText(Resources.uiText.getString("LevelInfo.author")   + ":");
		email    = new LabeledText(Resources.uiText.getString("LevelInfo.mail")     + ":");
		homepage = new LabeledText(Resources.uiText.getString("LevelInfo.homepage") + ":");
		
		// setup layout
		this.setLayout(new GroupLayout(this));
		layout = (GroupLayout)this.getLayout();
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(
					layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(title.label)
					.addComponent(title.text))
				.addGroup(
					layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(subtitle.label)
					.addComponent(subtitle.text))
				.addGroup(
					layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(author.label)
					.addComponent(author.text))
				.addGroup(
					layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(email.label)
					.addComponent(email.text))
				.addGroup(
					layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(homepage.label)
					.addComponent(homepage.text))
			);
		
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
					.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(title.label)
						.addComponent(subtitle.label)
						.addComponent(author.label)
						.addComponent(email.label)
						.addComponent(homepage.label))
					.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(title.text)
						.addComponent(subtitle.text)
						.addComponent(author.text)
						.addComponent(email.text)
						.addComponent(homepage.text))
				);
			
		this.setLayout(layout);
		this.setBorder(new CompoundBorder(
				new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(
						Resources.uiText.getString("LevelInfo.caption"))));
	}
	
	public void fromLevelInfo(LevelInfo info)
	{
		title   .setText(info.identity.title);
		subtitle.setText(info.identity.subtitle);
		author  .setText(info.author.name);
		email   .setText(info.author.email);
		homepage.setText(info.author.homepage);
	}
	
	public void toLevelInfo(LevelInfo info)
	{
		info.identity.title    = title.getText();
		info.identity.subtitle = subtitle.getText();
		info.author.name       = author.getText();
		info.author.email      = email.getText();
		info.author.homepage   = homepage.getText();
	}
}

