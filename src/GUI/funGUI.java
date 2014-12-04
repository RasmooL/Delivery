package GUI;

import jess.*;
import jessmw.*;

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
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.SwingWorker;

public class funGUI extends JFrame implements Userfunction {
	private String functionName = "GUI";
	static Rete engine;
	static Odometry odo;
	private JPanel contentPane;
	JPanel panel, waypoints, goStop, delivery;
	
	long start_time = 0, end_time = 0;
	boolean isRunning = false;
	double speed;
	int numCheckBox = 5;
	int[] goals = {2, 6, 9, 13, 15};
	
	JLabel goal;
	JCheckBox[] checkBoxes;
	
	JToggleButton btnStart, btnPause;
	ButtonGroup TButton;
	JLabel lblETA;
	static JLabel txtETA;
	static double totDist;
	
	JButton btnAccept, btnDecline;
	JLabel[] goalLabels, ADLabels;
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return functionName;
	}
	
	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	/**
	 * Create the frame.
	 */
	public funGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 270);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		// Check boxes for goal assigning
		waypoints = new JPanel();
		waypoints.setBounds(10, 10, 240, 95);
		panel.add(waypoints);
		waypoints.setLayout(null);
		
		goal = new JLabel("GOAL");
		goal.setFont(new Font("Dialog", Font.BOLD, 20));
		goal.setHorizontalAlignment(SwingConstants.CENTER);
		goal.setBounds(5, 5, 230, 25);
		waypoints.add(goal);

		checkBoxes = new JCheckBox[numCheckBox];
		for(int i = 0; i < numCheckBox; i++) {
			checkBoxes[i] = new JCheckBox("Goal " + (i+1));
			checkBoxes[i].addActionListener(new BoxListner(i));
			if (i < 3) {
				checkBoxes[i].setBounds(5 + 80*i, 35, 70, 25);
			} else {
				checkBoxes[i].setBounds(5 + 80*(i-3), 65, 70, 25);
			}
			waypoints.add(checkBoxes[i]);
		}
		
		// Toggle buttons for start and pause the operation
		goStop = new JPanel();
		goStop.setBounds(250, 10, 190, 95);
		panel.add(goStop);
		goStop.setLayout(null);
		
		TButton = new ButtonGroup();
		// ToggleButton Start
		btnStart = new JToggleButton("Start");
		TButton.add(btnStart);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(btnStart.isSelected()) {
					UIManager.put("ToggleButton.select", Color.GREEN);
					SwingUtilities.updateComponentTreeUI(btnStart);
					try {
						engine.assertString("(start)");
						start_time = System.nanoTime();
						txtETA.setVisible(true);
						System.out.println("Assert: (start)");
						System.out.println("start_time: " + start_time);
						
						Iterator<Fact> itrFact = engine.listFacts();
						while(itrFact.hasNext())
						{
							Fact tmpFact2 = itrFact.next();
							//System.out.println(tmpFact2.toString());
							if (tmpFact2.getName().equals("MAIN::route-length")) {
								totDist = Double.parseDouble(tmpFact2.get(0).toString());
								break;
							}
						}
						isRunning = true;
						timerWorker.execute();
					} catch (JessException e1) {
						// TODO Auto-generated catch block
						// e1.printStackTrace();
					}
					catch(NullPointerException e1)
					{
						System.out.println(e1.getMessage());
					}
					for(int i = 0; i < numCheckBox; i++) {
						checkBoxes[i].setEnabled(false);
					}
				}
			}
		});
		btnStart.setBounds(10, 5, 80, 25);
		goStop.add(btnStart);
		
		// ToggleButton Pause
		btnPause = new JToggleButton("Pause");
		TButton.add(btnPause);
		btnPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(btnPause.isSelected()) {
					UIManager.put("ToggleButton.select", Color.RED);
					SwingUtilities.updateComponentTreeUI(btnPause);
					isRunning = false;
					timerWorker.cancel(true);
					for(int i = 0; i < numCheckBox; i++) {
						checkBoxes[i].setEnabled(true);
					}
					try {
						engine.assertString("(pause)");
					} catch (JessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		btnPause.setBounds(100, 5, 80, 25);
		goStop.add(btnPause);
		
		// Estimated Time to arrive the final goal
		lblETA = new JLabel("Estimated Time Arrival");
		lblETA.setHorizontalAlignment(SwingConstants.CENTER);
		lblETA.setBounds(5, 35, 180, 25);
		goStop.add(lblETA);
		
		txtETA = new JLabel("00:00:00");
		txtETA.setHorizontalAlignment(SwingConstants.CENTER);
		txtETA.setBounds(5, 65, 180, 25);
		//txtETA.setVisible(false);
		goStop.add(txtETA);
		//txtETA.setColumns(10);
		
		// Acception or Declinination of delivered package
		delivery = new JPanel();
		delivery.setBounds(10, 95, 430, 150);
		panel.add(delivery);
		delivery.setLayout(null);
		
		btnAccept = new JButton("Accept");
		btnAccept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Display ADLabel of reached goal and set the text of ADLabel as "Accepted"
			}
		});
		btnAccept.setBounds(5, 50, 90, 25);
		delivery.add(btnAccept);
		
		btnDecline = new JButton("Decline");
		btnDecline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Display ADLabel of reached goal and set the text of ADLabel as "Declined"
			}
		});
		btnDecline.setBounds(5, 95, 90, 25);
		delivery.add(btnDecline);
		
		goalLabels = new JLabel[numCheckBox];
		ADLabels = new JLabel[numCheckBox];
		for(int i = 0; i < numCheckBox; i++) {
			goalLabels[i] = new JLabel("Goal " + (i+1));
			goalLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
			if (i < 3) {
				goalLabels[i].setBounds(110, 40 + 30*i, 50, 25);
			} else {
				goalLabels[i].setBounds(250, 40 + 30*(i-3), 50, 25);
			}
			
			goalLabels[i].setVisible(false);
			delivery.add(goalLabels[i]);
			
			ADLabels[i] = new JLabel("Accept");
			if (i%2 == 1) {
				ADLabels[i].setText("Declined");
			}
			if (i < 3) {
				ADLabels[i].setBounds(165, 40 + 30*i, 70, 25);				
			} else {
				ADLabels[i].setBounds(305, 40 + 30*(i-3), 70, 25);
			}
			ADLabels[i].setVisible(false);
			delivery.add(ADLabels[i]);
		}
	}

	@Override
	public Value call(ValueVector vv, Context c) throws JessException {
		engine = c.getEngine();
		Iterator<Fact> itrFact = engine.listFacts();
		while(itrFact.hasNext())
		{
			Fact tmpFact = itrFact.next();
			if(tmpFact.getName().equals("MAIN::smr0.mrc.mrc.odometry")){
				odo = (Odometry)tmpFact.getSlotValue("OBJECT").javaObjectValue(c);
			}
		}
		
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
	
	SwingWorker<Void, Double> timerWorker = new SwingWorker<Void, Double>() {
		@Override
		protected Void doInBackground() throws Exception {
			while(isRunning == true) {
				setSpeed(odo.getVelocity());
				end_time = System.nanoTime();
				double msdifference = (end_time - start_time)/1e6;
				double totMsTime = totDist/getSpeed()*1e3;
				publish(totMsTime - msdifference);
			}
			return null;
		}
		
		@Override
		protected void process(List<Double> chunks)
		{
			double mostRecent = chunks.get(chunks.size() - 1);
			
			int ms = ((int)mostRecent % 1000) / 10;
			int sec = ((int)mostRecent / 1000) % 60;
			int min = (int)mostRecent / 60000;
			String eta = (min < 10 ? "0" : "") + min + ":" +
					(sec < 10 ? "0" : "") + sec + ":" + (ms < 10 ? "0" : "") + ms;
			txtETA.setText(eta);
		}
	};
	
	private class BoxListner implements ActionListener {
		private final int i;
		public BoxListner(int i) {
			this.i = i;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if(checkBoxes[i].isSelected()) {
				System.out.println("Goal " + (i+1) + " checked!");
				try {
					engine.assertString("(goal (waypoint " + goals[i] + "))");
					System.out.println("Assert: (goal (waypoint " + goals[i] + "))");
					System.out.println("<Facts List>");
					Iterator<Fact> itrFact = engine.listFacts();
					while(itrFact.hasNext()) {
						Fact tmpFact = itrFact.next();
						System.out.println(tmpFact.toString());
					}
				} catch (JessException e1) {
					e1.printStackTrace();
				}						
				goalLabels[i].setVisible(true);
			} else {
				System.out.println("Goal " + (i+1) + " de-checked!");
				try {
					goalLabels[i].setVisible(false);
					ADLabels[i].setVisible(false);
					engine.retractString("(goal (waypoint " + goals[i] + "))");
					System.out.println("Retract: (goal (waypoint " + goals[i] + "))");
					System.out.println("<Facts List>");
					Iterator<Fact> itrFact = engine.listFacts();
					while(itrFact.hasNext()) {
						Fact tmpFact = itrFact.next();
						System.out.println(tmpFact.toString());
					}
				} catch (JessException e2) {
					e2.printStackTrace();
				}
				goalLabels[i].setVisible(false);
				ADLabels[i].setVisible(false);
			}
		}
	}
}
