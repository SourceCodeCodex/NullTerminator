package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class GenericWindow extends JFrame
	{
	private static final long serialVersionUID = -7687385632103743903L;
	private JTextArea textArea = new JTextArea();
	
	public GenericWindow(String title, String content)
		{
		genericConstruction(title);
		setContent(content);
		}
	
	public GenericWindow(String title)
		{
		genericConstruction(title);
		}
	
	private void genericConstruction(String title)
		{
		int length=500;
		int height=500;
		setSize(new Dimension(length,height));
		setLocationRelativeTo(null);
		setTitle(title);
		setLayout(new BorderLayout());
		setBackground(Color.LIGHT_GRAY);
		textArea.setEditable(false);
		textArea.setBackground(Color.LIGHT_GRAY);
		//textArea.setSize(new Dimension(500,200));
		textArea.setLineWrap(true);
		}
	
	public void setContent(String content)
		{
		textArea.setText(content);
		add(textArea,BorderLayout.NORTH);
		}
	
	public void showWindow()
		{
		repaint();
		setVisible(true);
		}
	}
