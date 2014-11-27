package GUI;

import jess.*;
import jessmw.*;

import java.lang.Thread;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public class funGUI extends JFrame implements Userfunction {
	private static final long serialVersionUID = 1L;
	private String functionName = "GUI";
	private JPanel contentPane;
	static Rete engine;
	static Odometry odo;
	JPanel panel, waypoints, goStop;
	JLabel lblETA;
	static JLabel txtETA;
	static double totDist;
	
	int min = 0, min1 = 0, min2 = 0;
	int sec = 0, sec1 = 0, sec2 = 0;
	
	JCheckBox chckbxWP1, chckbxWP2, chckbxWP3, chckbxWP4,
				chckbxWP5, chckbxWP6, chckbxWP7, chckbxWP8,
				chckbxWP9, chckbxWP10, chckbxWP11, chckbxWP12,
				chckbxWP13, chckbxWP14, chckbxWP15, chckbxWP16;
	int numChckbx = 0;
	
	JToggleButton btnStart;
	JToggleButton btnPause;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	
	JButton btnAccept, btnDecline;
	JLabel lblWP1, lblWP2, lblWP3, lblWP4,
			lblWP5, lblWP6, lblWP7, lblWP8,
			lblWP9, lblWP10, lblWP11, lblWP12,
			lblWP13, lblWP14, lblWP15, lblWP16;
	JLabel lblAD1, lblAD2, lblAD3, lblAD4,
			lblAD5, lblAD6, lblAD7, lblAD8,
			lblAD9, lblAD10, lblAD11, lblAD12,
			lblAD13, lblAD14, lblAD15, lblAD16;
	
	long start_time, end_time;
	double oldX, newX, oldY, newY, speed;
	private JPanel Delivery;
	
	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getNewX() {
		return newX;
	}

	public void setNewX(double newX) {
		this.newX = newX;
	}

	public double getNewY() {
		return newY;
	}

	public void setNewY(double newY) {
		this.newY = newY;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return functionName;
	}
	
	/**
	 * Create the frame.
	 */
	public funGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		waypoints = new JPanel();
		waypoints.setBounds(10, 10, 250, 115);
		panel.add(waypoints);
		
		chckbxWP1 = new JCheckBox("WP1");
		chckbxWP1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP1.isSelected()){
					System.out.println("Waypoint1 checked!");
					try {
						engine.assertString("(goal (waypoint 1))");
					} catch (JessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					lblWP1.setVisible(true);
					lblAD1.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint1 de-checked!");
					lblWP1.setVisible(false);
					lblAD1.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP1.setBounds(5, 5, 55, 25);
		
		chckbxWP2 = new JCheckBox("WP2");
		chckbxWP2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP2.isSelected()){
					System.out.println("Waypoint2 checked!");
					lblWP2.setVisible(true);
					lblAD2.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint2 de-checked!");
					lblWP2.setVisible(false);
					lblAD2.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP2.setBounds(5, 32, 55, 25);
		
		chckbxWP3 = new JCheckBox("WP3");
		chckbxWP3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP3.isSelected()){
					System.out.println("Waypoint3 checked!");
					lblWP3.setVisible(true);
					lblAD3.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint3 de-checked!");
					lblWP3.setVisible(false);
					lblAD3.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP3.setBounds(5, 59, 55, 25);
		
		chckbxWP4 = new JCheckBox("WP4");
		chckbxWP4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP4.isSelected()){
					System.out.println("Waypoint4 checked!");
					lblWP4.setVisible(true);
					lblAD4.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint4 de-checked!");
					lblWP4.setVisible(false);
					lblAD4.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP4.setBounds(5, 86, 55, 25);
		
		chckbxWP5 = new JCheckBox("WP5");
		chckbxWP5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP5.isSelected()){
					System.out.println("Waypoint5 checked!");
					lblWP5.setVisible(true);
					lblAD5.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint5 de-checked!");
					lblWP5.setVisible(false);
					lblAD5.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP5.setBounds(57, 5, 55, 25);
		
		chckbxWP6 = new JCheckBox("WP6");
		chckbxWP6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP6.isSelected()){
					System.out.println("Waypoint6 checked!");
					lblWP6.setVisible(true);
					lblAD6.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint6 de-checked!");
					lblWP6.setVisible(false);
					lblAD6.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP6.setBounds(57, 32, 55, 25);
		
		chckbxWP7 = new JCheckBox("WP7");
		chckbxWP7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP7.isSelected()){
					System.out.println("Waypoint7 checked!");
					lblWP7.setVisible(true);
					lblAD7.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint7 de-checked!");
					lblWP7.setVisible(false);
					lblAD7.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP7.setBounds(57, 59, 55, 25);
		
		chckbxWP8 = new JCheckBox("WP8");
		chckbxWP8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP8.isSelected()){
					System.out.println("Waypoint8 checked!");
					lblWP8.setVisible(true);
					lblAD8.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint8 de-checked!");
					lblWP8.setVisible(false);
					lblAD8.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP8.setBounds(57, 86, 55, 25);
		
		chckbxWP9 = new JCheckBox("WP9");
		chckbxWP9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP9.isSelected()){
					System.out.println("Waypoint9 checked!");
					lblWP9.setVisible(true);
					lblAD9.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint9 de-checked!");
					lblWP9.setVisible(false);
					lblAD9.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP9.setBounds(116, 5, 60, 25);
		
		chckbxWP10 = new JCheckBox("WP10");
		chckbxWP10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP10.isSelected()){
					System.out.println("Waypoint10 checked!");
					lblWP10.setVisible(true);
					lblAD10.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint10 de-checked!");
					lblWP10.setVisible(false);
					lblAD10.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP10.setBounds(116, 32, 60, 25);
		
		chckbxWP11 = new JCheckBox("WP11");
		chckbxWP11.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP11.isSelected()){
					System.out.println("Waypoint11 checked!");
					lblWP11.setVisible(true);
					lblAD11.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint11 de-checked!");
					lblWP11.setVisible(false);
					lblAD11.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP11.setBounds(116, 59, 60, 25);
		
		chckbxWP12 = new JCheckBox("WP12");
		chckbxWP12.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP12.isSelected()){
					System.out.println("Waypoint12 checked!");
					lblWP12.setVisible(true);
					lblAD12.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint12 de-checked!");
					lblWP12.setVisible(false);
					lblAD12.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP12.setBounds(116, 86, 60, 25);
		
		chckbxWP13 = new JCheckBox("WP13");
		chckbxWP13.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP13.isSelected()){
					System.out.println("Waypoint13 checked!");
					lblWP13.setVisible(true);
					lblAD13.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint13 de-checked!");
					lblWP13.setVisible(false);
					lblAD13.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP13.setBounds(182, 5, 60, 25);
		
		chckbxWP14 = new JCheckBox("WP14");
		chckbxWP14.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP14.isSelected()){
					System.out.println("Waypoint14 checked!");
					lblWP14.setVisible(true);
					lblAD14.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint14 de-checked!");
					lblWP14.setVisible(false);
					lblAD14.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP14.setBounds(182, 32, 60, 25);
		
		chckbxWP15 = new JCheckBox("WP15");
		chckbxWP15.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP15.isSelected()){
					System.out.println("Waypoint15 checked!");
					lblWP15.setVisible(true);
					lblAD15.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint15 de-checked!");
					lblWP15.setVisible(false);
					lblAD15.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP15.setBounds(182, 59, 60, 25);
		
		chckbxWP16 = new JCheckBox("WP16");
		chckbxWP16.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxWP16.isSelected()){
					System.out.println("Waypoint16 checked!");
					lblWP16.setVisible(true);
					lblAD16.setVisible(true);
					numChckbx++;
				} else {
					System.out.println("Waypoint16 de-checked!");
					lblWP16.setVisible(false);
					lblAD16.setVisible(false);
					numChckbx--;
				}
			}
		});
		chckbxWP16.setBounds(182, 86, 60, 25);
		
		waypoints.setLayout(null);
		waypoints.add(chckbxWP1);
		waypoints.add(chckbxWP2);
		waypoints.add(chckbxWP3);
		waypoints.add(chckbxWP4);
		waypoints.add(chckbxWP5);
		waypoints.add(chckbxWP6);
		waypoints.add(chckbxWP7);
		waypoints.add(chckbxWP8);
		waypoints.add(chckbxWP9);
		waypoints.add(chckbxWP10);
		waypoints.add(chckbxWP11);
		waypoints.add(chckbxWP12);
		waypoints.add(chckbxWP13);
		waypoints.add(chckbxWP14);
		waypoints.add(chckbxWP15);
		waypoints.add(chckbxWP16);
		
		goStop = new JPanel();
		goStop.setBounds(265, 10, 150, 115);
		panel.add(goStop);
		goStop.setLayout(null);
		
		// ToggleButton Start
		btnStart = new JToggleButton("Start");
		buttonGroup.add(btnStart);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(btnStart.isSelected()) {
					UIManager.put("ToggleButton.select", Color.GREEN);
					SwingUtilities.updateComponentTreeUI(btnStart);
					try {
						engine.assertString("(start)");
						start_time = System.nanoTime();
					} catch (JessException e1) {
						// TODO Auto-generated catch block
						// e1.printStackTrace();
					}
					catch(NullPointerException e1)
					{
						System.out.println(e1.getMessage());
					}
					chckbxWP1.setEnabled(false);
					chckbxWP2.setEnabled(false);
					chckbxWP3.setEnabled(false);
					chckbxWP4.setEnabled(false);
					chckbxWP5.setEnabled(false);
					chckbxWP6.setEnabled(false);
					chckbxWP7.setEnabled(false);
					chckbxWP8.setEnabled(false);
					chckbxWP9.setEnabled(false);
					chckbxWP10.setEnabled(false);
					chckbxWP11.setEnabled(false);
					chckbxWP12.setEnabled(false);
					chckbxWP13.setEnabled(false);
					chckbxWP14.setEnabled(false);
					chckbxWP15.setEnabled(false);
					chckbxWP16.setEnabled(false);
				}
			}
		});
		btnStart.setBounds(3, 10, 70, 25);
		goStop.add(btnStart);
		
		// ToggleButton Pause
		btnPause = new JToggleButton("Pause");
		buttonGroup.add(btnPause);
		btnPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(btnPause.isSelected()) {
					UIManager.put("ToggleButton.select", Color.RED);
					SwingUtilities.updateComponentTreeUI(btnPause);
					chckbxWP1.setEnabled(true);
					chckbxWP2.setEnabled(true);
					chckbxWP3.setEnabled(true);
					chckbxWP4.setEnabled(true);
					chckbxWP5.setEnabled(true);
					chckbxWP6.setEnabled(true);
					chckbxWP7.setEnabled(true);
					chckbxWP8.setEnabled(true);
					chckbxWP9.setEnabled(true);
					chckbxWP10.setEnabled(true);
					chckbxWP11.setEnabled(true);
					chckbxWP12.setEnabled(true);
					chckbxWP13.setEnabled(true);
					chckbxWP14.setEnabled(true);
					chckbxWP15.setEnabled(true);
					chckbxWP16.setEnabled(true);
					
					try {
						engine.assertString("(pause)");
					} catch (JessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		btnPause.setBounds(77, 10, 70, 25);
		goStop.add(btnPause);
		
		lblETA = new JLabel("Estimated Time Arrival");
		lblETA.setBounds(10, 50, 130, 15);
		goStop.add(lblETA);
		
		txtETA = new JLabel("00:00:00");
		txtETA.setHorizontalAlignment(SwingConstants.CENTER);
		txtETA.setBounds(20, 77, 114, 19);
		goStop.add(txtETA);
		//txtETA.setColumns(10);
		
		Delivery = new JPanel();
		Delivery.setBounds(10, 130, 405, 110);
		panel.add(Delivery);
		Delivery.setLayout(null);
		
		btnAccept = new JButton("Accept");
		btnAccept.setBounds(5, 20, 80, 25);
		Delivery.add(btnAccept);
		
		btnDecline = new JButton("Decline");
		btnDecline.setBounds(5, 60, 80, 25);
		Delivery.add(btnDecline);
		
		lblWP1 = new JLabel("1");
		lblWP1.setBounds(100, 10, 12, 15);
		lblWP1.setVisible(false);
		Delivery.add(lblWP1);
		
		lblWP2 = new JLabel("2");
		lblWP2.setBounds(100, 35, 12, 15);
		lblWP2.setVisible(false);
		Delivery.add(lblWP2);
		
		lblWP3 = new JLabel("3");
		lblWP3.setBounds(100, 60, 12, 15);
		lblWP3.setVisible(false);
		Delivery.add(lblWP3);
		
		lblWP4 = new JLabel("4");
		lblWP4.setBounds(100, 85, 12, 15);
		lblWP4.setVisible(false);
		Delivery.add(lblWP4);
		
		lblWP5 = new JLabel("5");
		lblWP5.setBounds(172, 10, 12, 15);
		lblWP5.setVisible(false);
		Delivery.add(lblWP5);
		
		lblWP6 = new JLabel("6");
		lblWP6.setBounds(172, 35, 12, 15);
		lblWP6.setVisible(false);
		Delivery.add(lblWP6);
		
		lblWP7 = new JLabel("7");
		lblWP7.setBounds(172, 60, 12, 15);
		lblWP7.setVisible(false);
		Delivery.add(lblWP7);
		
		lblWP8 = new JLabel("8");
		lblWP8.setBounds(172, 85, 12, 15);
		lblWP8.setVisible(false);
		Delivery.add(lblWP8);
		
		lblWP9 = new JLabel("9");
		lblWP9.setBounds(244, 10, 18, 15);
		lblWP9.setVisible(false);
		Delivery.add(lblWP9);
		
		lblWP10 = new JLabel("10");
		lblWP10.setBounds(244, 35, 18, 15);
		lblWP10.setVisible(false);
		Delivery.add(lblWP10);
		
		lblWP11 = new JLabel("11");
		lblWP11.setBounds(244, 60, 18, 15);
		lblWP11.setVisible(false);
		Delivery.add(lblWP11);
		
		lblWP12 = new JLabel("12");
		lblWP12.setBounds(244, 85, 18, 15);
		lblWP12.setVisible(false);
		Delivery.add(lblWP12);
		
		lblWP13 = new JLabel("13");
		lblWP13.setBounds(322, 10, 18, 15);
		lblWP13.setVisible(false);
		Delivery.add(lblWP13);
		
		lblWP14 = new JLabel("14");
		lblWP14.setBounds(322, 35, 18, 15);
		lblWP14.setVisible(false);
		Delivery.add(lblWP14);
		
		lblWP15 = new JLabel("15");
		lblWP15.setBounds(322, 60, 18, 15);
		lblWP15.setVisible(false);
		Delivery.add(lblWP15);
		
		lblWP16 = new JLabel("16");
		lblWP16.setBounds(322, 85, 18, 15);
		lblWP16.setVisible(false);
		Delivery.add(lblWP16);
		
		lblAD1 = new JLabel("");
		lblAD1.setBounds(115, 10, 55, 15);
		lblAD1.setVisible(false);
		Delivery.add(lblAD1);
		
		lblAD2 = new JLabel("");
		lblAD2.setBounds(115, 35, 55, 15);
		lblAD2.setVisible(false);
		Delivery.add(lblAD2);
		
		lblAD3 = new JLabel("");
		lblAD3.setBounds(115, 60, 55, 15);
		lblAD3.setVisible(false);
		Delivery.add(lblAD3);
		
		lblAD4 = new JLabel("");
		lblAD4.setBounds(115, 85, 55, 15);
		lblAD4.setVisible(false);
		Delivery.add(lblAD4);
		
		lblAD5 = new JLabel("");
		lblAD5.setBounds(187, 10, 55, 15);
		lblAD5.setVisible(false);
		Delivery.add(lblAD5);
		
		lblAD6 = new JLabel("");
		lblAD6.setBounds(187, 35, 55, 15);
		lblAD6.setVisible(false);
		Delivery.add(lblAD6);
		
		lblAD7 = new JLabel("");
		lblAD7.setBounds(187, 60, 55, 15);
		lblAD7.setVisible(false);
		Delivery.add(lblAD7);
		
		lblAD8 = new JLabel("");
		lblAD8.setBounds(187, 85, 55, 15);
		lblAD8.setVisible(false);
		Delivery.add(lblAD8);
		
		lblAD9 = new JLabel("");
		lblAD9.setBounds(265, 10, 55, 15);
		lblAD9.setVisible(false);
		Delivery.add(lblAD9);
		
		lblAD10 = new JLabel("");
		lblAD10.setBounds(265, 35, 55, 15);
		lblAD1.setVisible(false);
		Delivery.add(lblAD10);
		
		lblAD11 = new JLabel("");
		lblAD11.setBounds(265, 60, 55, 15);
		lblAD11.setVisible(false);
		Delivery.add(lblAD11);
		
		lblAD12 = new JLabel("");
		lblAD12.setBounds(265, 85, 55, 15);
		lblAD12.setVisible(false);
		Delivery.add(lblAD12);
		
		lblAD13 = new JLabel("");
		lblAD13.setBounds(345, 10, 55, 15);
		lblAD13.setVisible(false);
		Delivery.add(lblAD13);
		
		lblAD14 = new JLabel("");
		lblAD14.setBounds(345, 35, 55, 15);
		lblAD14.setVisible(false);
		Delivery.add(lblAD14);
		
		lblAD15 = new JLabel("");
		lblAD15.setBounds(345, 60, 55, 15);
		lblAD15.setVisible(false);
		Delivery.add(lblAD15);
		
		lblAD16 = new JLabel("");
		lblAD16.setBounds(345, 85, 55, 15);
		lblAD16.setVisible(false);
		Delivery.add(lblAD16);
	}

	@Override
	public Value call(ValueVector vv, final Context c) throws JessException {
		
		
		engine = c.getEngine();
		Iterator<Fact> itrFact = engine.listFacts();
		while(itrFact.hasNext())
		{
			Fact tmpFact = itrFact.next();
			if(tmpFact.getName().equals("MAIN::smr0.mrc.mrc.odometry")){
				odo = (Odometry)tmpFact.getSlotValue("OBJECT").javaObjectValue(c);
			}
		}
		
		new SwingWorker<Void, Double>() {
			@Override
			protected Void doInBackground() throws Exception {
				while(true)
				{
					setSpeed(odo.getVelocity());
					end_time = System.nanoTime();
					double difference = (end_time - start_time)/1e6;
					Iterator<Fact> itrFact2 = engine.listFacts();
					while(itrFact2.hasNext())
					{
						Fact tmpFact2 = itrFact2.next();
						if (tmpFact2.getName().equals("best")) {
							totDist = (double)tmpFact2.getSlotValue("dist").floatValue(c);
						}
					}
					double totTime = totDist/getSpeed();
					publish(totTime - difference);
				}
			}
			
			@Override
			protected void process(List<Double> chunks)
			{
				double mostRecent = chunks.get(chunks.size() - 1);
				System.out.println("process");
				
				txtETA.setText(String.valueOf(mostRecent));
			}
		}.execute();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					funGUI frame = new funGUI();
					frame.setVisible(true);
					frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		return jess.Funcall.NIL;
	}
}
