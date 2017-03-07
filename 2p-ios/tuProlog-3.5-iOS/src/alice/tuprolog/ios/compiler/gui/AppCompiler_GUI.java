package alice.tuprolog.ios.compiler.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import alice.tuprolog.ios.compiler.AppCompiler;

public class AppCompiler_GUI extends JFrame implements ActionListener
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	private static final long serialVersionUID = 1L;
	
	private static final AppCompiler_GUI gui = new AppCompiler_GUI();

	private JLabel labelCompilationTools = new JLabel("Choose tool: ");
	private JLabel labelMode = new JLabel("Choose mode: ");
    private JLabel labelDevices = new JLabel("Choose device: ");
    private JLabel labelID = new JLabel("Bundle ID: it.unibo.tuPrologMobile");
    private String[] compilationTools = {"RoboVM", "BugVM"};
    private JComboBox<String> tools = new JComboBox<String>(compilationTools);
    private String[] mode = {"iOS simulator", "Physical device"};
    private JComboBox<String> modeOfDeploy = new JComboBox<String>(mode);
    private String[] models = {"iPhone 5/5C"};
    private JComboBox<String> availableDevices = new JComboBox<String>(models);
    private JButton buttonCompile = new JButton("Compile");
    private String[] devices = {"iPhone 5/5C", "iPhone 5S", "iPhone SE", "iPhone 6", "iPhone 6 Plus", 
    		"iPhone 6S", "iPhone 6S Plus", "iPhone 7", "iPhone 7 Plus"};
     
    public AppCompiler_GUI() 
    {
        super("tuProlog iOS Installer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel newPanel = new JPanel(new GridBagLayout());
         
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(12, 12, 12, 12);
        
        constraints.gridx = 0;
        constraints.gridy = 0;     
        newPanel.add(labelCompilationTools, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 0;    
        tools.addActionListener(this);
        modeOfDeploy.addActionListener(this);
        newPanel.add(tools, constraints);
 
        constraints.gridx = 0;
        constraints.gridy = 1;     
        newPanel.add(labelMode, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 1;     
        newPanel.add(modeOfDeploy, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 2;     
        newPanel.add(labelDevices, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 2;     
        newPanel.add(availableDevices, constraints);
         
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        buttonCompile.setEnabled(true);
        buttonCompile.addActionListener(this);
        newPanel.add(buttonCompile, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 4;     
        labelID.setVisible(false);
        newPanel.add(labelID, constraints);
         
        newPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Compiler settings:"));
        
        add(newPanel);
        pack();
        setLocationRelativeTo(null);
    }
     
    public static void main(String[] args) 
    {
        try 
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
         
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                gui.setVisible(true);
            }
        });
    }
    
    @Override
	public void actionPerformed(ActionEvent e) 
	{
    	if(e.getSource() instanceof JComboBox)
    		actionPerformedJComboBox();
    	else
    		actionPerformedButton();
	}

	private void actionPerformedJComboBox() 
	{
		availableDevices.removeAllItems();
		String mode = (String) modeOfDeploy.getSelectedItem();
		if(mode.equalsIgnoreCase("iOS simulator"))
		{
			labelID.setVisible(false);
			pack();
			String tool = (String) tools.getSelectedItem();
			if(tool.equalsIgnoreCase("bugvm"))
				availableDevices.addItem("iPhone 6");
			else
				availableDevices.addItem("iPhone 5/5C");
		}
		else
		{
			labelID.setVisible(true);
			pack();
			for(String s : devices)
				availableDevices.addItem(s);
		}
	}

	private void actionPerformedButton() 
	{
		String tool = (String) tools.getSelectedItem();
		String mode = (String) modeOfDeploy.getSelectedItem();
		String modelOfDevice = (String) availableDevices.getSelectedItem();
		new Runnable()
		{
			@Override
	        public void run()
	        {
				String arch = "";
				
				if(mode.equalsIgnoreCase("iOS simulator"))
				{
					if(tool.equalsIgnoreCase("bugvm"))
						arch = "x86_64";
					else
						arch = "x86";
				}
				else
				{
					if(modelOfDevice.equalsIgnoreCase("iPhone 5/5C"))
						arch = "thumbv7";
					else
						arch = "arm64";
				}
				
				AppCompiler.compile(tool.toLowerCase(), arch);
	        }
	    }.run();
	}
}
