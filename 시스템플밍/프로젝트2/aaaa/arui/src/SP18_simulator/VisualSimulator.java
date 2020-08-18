package SP18_simulator;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.jgoodies.forms.factories.DefaultComponentFactory;

import net.miginfocom.swing.MigLayout;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator extends JPanel {
	static ResourceManager resourceManager = new ResourceManager();
	static SicLoader sicLoader = new SicLoader(resourceManager);
	static SicSimulator sicSimulator = new SicSimulator(resourceManager);
	static boolean finished;  //프로그램이 완료되었는지 체크하는 변수
	
	static JFrame frame;
	static File selectedFile;  
	
	public VisualSimulator() {
		initComponents();
		finished=false;
	}

	private void openMouseClicked(MouseEvent e) {
		JFileChooser fileChooser = new JFileChooser();    //파일을 열기 위해 JFileChooser을 사용
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(frame);
		if(result == JFileChooser.APPROVE_OPTION) {    //사용자가 파일을 선택할 경우 그 파일을 변수에 저장해 놓음
			selectedFile = fileChooser.getSelectedFile();
			open.setText(selectedFile.getName());
			step1Button.setEnabled(true); //파일을 선택한 뒤에는 버튼을 활성화
			stepAllButton.setEnabled(true);
	        load(selectedFile);  //목적 파일을 로드한다.
	        List<String> instList = sicSimulator.loadInstruction(); //inst리스트를 먼저 완성해놓는다.
	        String[] instStringList = instList.toArray(new String[instList.size()]);
	        
	       Inst.setListData(instStringList);
		}
	}
	
	private void step1MouseClicked(MouseEvent e) {
		oneStep();
	}
	
	private void stepAllMouseClicked(MouseEvent e) {
		allStep();
	}
	
	private void finishMouseClicked(MouseEvent e) {
		System.exit(0);
	}

	private void initComponents() {
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		label1 = new JLabel();
		open = new JTextField();
		openButton = new JButton();
		separator1 = compFactory.createSeparator("H(Header Record)");
		separator2 = compFactory.createSeparator("E(End Record)");
		label3 = new JLabel();
		name = new JTextField();
		label8 = new JLabel();
		label4 = new JLabel();
		label9 = new JLabel();
		firstInst = new JTextField();
		separator7 = new JSeparator();
		label6 = new JLabel();
		start = new JTextField();
		label10 = new JLabel();
		firstAddress = new JTextField();
		label5 = new JLabel();
		length = new JTextField();
		label11 = new JLabel();
		targetAddress = new JTextField();
		separator9 = new JSeparator();
		separator3 = new JToolBar.Separator();
		separator4 = compFactory.createSeparator("Register");
		label12 = new JLabel();
		label21 = new JLabel();
		label20 = new JLabel();
		scrollPane1 = new JScrollPane();
		Inst = new JList<String>(new DefaultListModel<String>());
		label13 = new JLabel();
		label15 = new JLabel();
		aDec = new JTextField();
		aHex = new JTextField();
		machineName = new JTextField();
		label16 = new JLabel();
		xDec = new JTextField();
		xHex = new JTextField();
		label17 = new JLabel();
		label2 = new JLabel();
		lDec = new JTextField();
		lHex = new JTextField();
		label7 = new JLabel();
		label18 = new JLabel();
		pcDec = new JTextField();
		pcHex = new JTextField();
		label19 = new JLabel();
		swDec = new JTextField();
		step1Button = new JButton();
		separator5 = new JSeparator();
		separator8 = compFactory.createSeparator("Register(for XE)");
		stepAllButton = new JButton();
		label22 = new JLabel();
		bDec = new JTextField();
		bHex = new JTextField();
		finishButton = new JButton();
		label23 = new JLabel();
		sDec = new JTextField();
		sHex = new JTextField();
		label24 = new JLabel();
		tDec = new JTextField();
		tHex = new JTextField();
		label25 = new JLabel();
		fDec = new JTextField();
		separator6 = new JSeparator();
		label14 = new JLabel();
		scrollPane2 = new JScrollPane();
		log = new JList<String>(new DefaultListModel<String>());
		

		step1Button.setEnabled(false);
		stepAllButton.setEnabled(false);

		

		setMaximumSize(new Dimension(1000000000, 1000000000));
		setLayout(new MigLayout(
			"hidemode 3",
			// columns
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]",
			// rows
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]"));

		//---- label1 ----
		label1.setText("FileName");
		add(label1, "cell 0 0");
		add(open, "cell 1 0 7 1");

		//---- openButton ----
		openButton.setText("open");
		openButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				openMouseClicked(e);
			}
		});
		
		step1Button.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				step1MouseClicked(e);
			}
		});
		
		stepAllButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				stepAllMouseClicked(e);
			}
		});
		
		finishButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				finishMouseClicked(e);
			}
		});
		
		add(openButton, "cell 8 0");
		add(separator1, "cell 0 1 7 1");
		add(separator2, "cell 7 1 2 1");

		//---- label3 ----
		label3.setText("Program Name:");
		add(label3, "cell 0 2");
		add(name, "cell 1 2 6 1");

		//---- label8 ----
		label8.setText("Address of First Instruction");
		add(label8, "cell 7 2");

		//---- label4 ----
		label4.setText("Start Address of");
		add(label4, "cell 0 3");

		//---- label9 ----
		label9.setText("    In Object Program: ");
		add(label9, "cell 7 3");
		add(firstInst, "cell 8 3");
		add(separator7, "cell 7 4 2 1");

		//---- label6 ----
		label6.setText("    Object Program:");
		add(label6, "cell 0 5");
		add(start, "cell 1 5 6 1");

		//---- label10 ----
		label10.setText("Start Address in Memory:");
		add(label10, "cell 7 5");
		add(firstAddress, "cell 8 5");

		//---- label5 ----
		label5.setText("Length of Program");
		add(label5, "cell 0 6");
		add(length, "cell 1 6 6 1");

		//---- label11 ----
		label11.setText("Target Address:");
		add(label11, "cell 7 6");
		add(targetAddress, "cell 8 6");
		add(separator9, "cell 0 7 7 1");
		add(separator3, "cell 0 8 7 1");
		add(separator4, "cell 0 9 7 1");

		//---- label12 ----
		label12.setText("Instructions:");
		add(label12, "cell 7 9");

		//---- label21 ----
		label21.setText("  Dec");
		add(label21, "cell 1 10");

		//---- label20 ----
		label20.setText("        Hex");
		add(label20, "cell 2 10 5 1");

		//======== scrollPane1 ========
		{

			//---- Inst ----
			Inst.setVisibleRowCount(14);
			scrollPane1.setViewportView(Inst);
		}
		add(scrollPane1, "cell 7 10 1 12,aligny top,growy 0");

		//---- label13 ----
		label13.setText("\uc0ac\uc6a9\uc911\uc778 \uc7a5\uce58");
		add(label13, "cell 8 10");

		//---- label15 ----
		label15.setText("A(#0)");
		add(label15, "cell 0 11 2 1");
		add(aDec, "cell 1 11 3 1");
		add(aHex, "cell 4 11 3 1");
		add(machineName, "cell 8 11");

		//---- label16 ----
		label16.setText("X(#1)");
		add(label16, "cell 0 12");
		add(xDec, "cell 1 12 3 1");
		add(xHex, "cell 4 12 3 1");

		//---- label17 ----
		label17.setText("L(#2)");
		add(label17, "cell 0 13");
		add(label2, "cell 0 13");
		add(lDec, "cell 1 13 3 1");
		add(lHex, "cell 4 13 3 1");
		add(label7, "cell 7 13");

		//---- label18 ----
		label18.setText("PC(#8)");
		add(label18, "cell 0 14");
		add(pcDec, "cell 1 14 3 1");
		add(pcHex, "cell 4 14 3 1");

		//---- label19 ----
		label19.setText("SW(#9)");
		add(label19, "cell 0 15");
		add(swDec, "cell 1 15 6 1");

		//---- step1Button ----
		step1Button.setText("\uc2e4\ud589(1 Step)");
		add(step1Button, "cell 8 15");
		add(separator5, "cell 0 16 7 1");
		add(separator8, "cell 0 17 7 1");

		//---- stepAllButton ----
		stepAllButton.setText("\uc2e4\ud589 (All)");
		add(stepAllButton, "cell 8 17");

		//---- label22 ----
		label22.setText("B(#3)");
		add(label22, "cell 0 18");
		add(bDec, "cell 1 18 3 1");
		add(bHex, "cell 4 18 3 1");

		//---- finishButton ----
		finishButton.setText("\uc885\ub8cc");
		add(finishButton, "cell 8 18");

		//---- label23 ----
		label23.setText("S(#4)");
		add(label23, "cell 0 19");
		add(sDec, "cell 1 19 3 1");
		add(sHex, "cell 4 19 3 1");

		//---- label24 ----
		label24.setText("T(#5)");
		add(label24, "cell 0 20");
		add(tDec, "cell 1 20 3 1");
		add(tHex, "cell 4 20 3 1");

		//---- label25 ----
		label25.setText("F(#6)");
		add(label25, "cell 0 21");
		add(fDec, "cell 1 21 6 1");
		add(separator6, "cell 0 22 7 1");

		//---- label14 ----
		label14.setText("Log(\uba85\ub839\uc5b4 \uc218\ud589 \uad00\ub828)");
		add(label14, "cell 0 23");

		//======== scrollPane2 ========
		{
			scrollPane2.setViewportView(log);
		}
		add(scrollPane2, "cell 0 24 9 9,aligny top,growy 0");
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - p
	private JLabel label1;
	private JTextField open;
	private JButton openButton;
	private JComponent separator1;
	private JComponent separator2;
	private JLabel label3;
	private static JTextField name;
	private JLabel label8;
	private JLabel label4;
	private JLabel label9;
	private static JTextField firstInst;
	private JSeparator separator7;
	private JLabel label6;
	private static JTextField start;
	private JLabel label10;
	private static JTextField firstAddress;
	private JLabel label5;
	private static JTextField length;
	private JLabel label11;
	private static JTextField targetAddress;
	private JSeparator separator9;
	private JToolBar.Separator separator3;
	private JComponent separator4;
	private JLabel label12;
	private JLabel label21;
	private JLabel label20;
	private JScrollPane scrollPane1;
	private static JList Inst;
	private JLabel label13;
	private JLabel label15;
	private static JTextField aDec;
	private static JTextField aHex;
	private static JTextField machineName;
	private JLabel label16;
	private static JTextField xDec;
	private static JTextField xHex;
	private JLabel label17;
	private JLabel label2;
	private static JTextField lDec;
	private static JTextField lHex;
	private JLabel label7;
	private JLabel label18;
	private static JTextField pcDec;
	private static JTextField pcHex;
	private JLabel label19;
	private static JTextField swDec;
	private static JButton step1Button;
	private JSeparator separator5;
	private JComponent separator8;
	private static JButton stepAllButton;
	private JLabel label22;
	private static JTextField bDec;
	private static JTextField bHex;
	private JButton finishButton;
	private JLabel label23;
	private static JTextField sDec;
	private static JTextField sHex;
	private JLabel label24;
	private static JTextField tDec;
	private static JTextField tHex;
	private JLabel label25;
	private static JTextField fDec;
	private JSeparator separator6;
	private JLabel label14;
	private JScrollPane scrollPane2;
	private static JList log;
	
	public static void main(String[] args) {
		frame = new JFrame("SimulatorGUI");
        frame.setContentPane(new VisualSimulator());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
     
		aDec.setText("0");
		xDec.setText("0");
		lDec.setText("0");
		bDec.setText("0");
		sDec.setText("0");
		tDec.setText("0");
		fDec.setText("0");
		pcDec.setText("0");
		swDec.setText("0");
		
		aHex.setText("0");
		xHex.setText("0");
		lHex.setText("0");
		pcHex.setText("0");
		bHex.setText("0");
		sHex.setText("0");
		tHex.setText("0");
		
		firstInst.setText("000000");
		firstAddress.setText("000000");
		start.setText("000000");
		frame.setVisible(true);
	}
	
	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public static void load(File program){  //메모리를 초기화 한 뒤 거기에 로드를 진행한다.
		//...
		sicSimulator.load(program);
		sicLoader.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public static void oneStep(){
		sicSimulator.oneStep();
		update();  //한 스탭마다 화면을 업데이트한다.
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep(){
		while(true) {
			sicSimulator.oneStep();
			update();
			if(finished) break;  //프로그램이 끝나기 전까지 oneStep을 수행한다.
		}
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public static void update(){
		targetAddress.setText(sicSimulator.targetAddr);
		
		aDec.setText(Integer.toString(resourceManager.getRegister(0)));
		xDec.setText(Integer.toString(resourceManager.getRegister(1)));
		lDec.setText(Integer.toString(resourceManager.getRegister(2)));
		bDec.setText(Integer.toString(resourceManager.getRegister(3)));
		sDec.setText(Integer.toString(resourceManager.getRegister(4)));
		tDec.setText(Integer.toString(resourceManager.getRegister(5)));
		fDec.setText(Integer.toString(resourceManager.getRegister(6)));
		pcDec.setText(Integer.toString(resourceManager.getRegister(8)));
		swDec.setText(Integer.toString(resourceManager.getRegister(9)));
		
		aHex.setText(Integer.toHexString(resourceManager.getRegister(0)));
		xHex.setText(Integer.toHexString(resourceManager.getRegister(1)));
		lHex.setText(Integer.toHexString(resourceManager.getRegister(2)));
		pcHex.setText(Integer.toHexString(resourceManager.getRegister(8)));
		bHex.setText(Integer.toHexString(resourceManager.getRegister(3)));
		sHex.setText(Integer.toHexString(resourceManager.getRegister(4)));
		tHex.setText(Integer.toHexString(resourceManager.getRegister(5)));
		
		int check=0;
		for(int i=0;i<resourceManager.progName.size()-1;i++) {  //각 프로그램의 시작주소와 현재주소를 비교해 어느 프로그램에 위치해있는지 알아와 textField를 변화시킨다.
			if(sicSimulator.loc>=resourceManager.progAddr.get(i)
					&&sicSimulator.loc<resourceManager.progAddr.get(i+1)) {
				name.setText(resourceManager.progName.get(i));
				length.setText(Integer.toHexString(resourceManager.progLength.get(i)));
				check=1;
			}
		}
		if(check==0) {  //앞의 섹션들에 속해있지 않을 경우 마지막 세션에 속한다.
			name.setText(resourceManager.progName.get(2));
			length.setText(Integer.toHexString(resourceManager.progLength.get(2)));
		}
		
		machineName.setText(resourceManager.nowDev);
		
		Inst.setSelectedIndex(sicSimulator.nowIndex);
		
		if(sicSimulator.logList.size()!=0) {
			DefaultListModel lm = (DefaultListModel)log.getModel();
			lm.addElement(sicSimulator.logList.get(sicSimulator.logList.size()-1));
			log.setSelectedIndex(sicSimulator.logList.size()-1);
		}
		if(sicSimulator.instList.get(sicSimulator.nowIndex).equals("3E2000")) {  //마지막 명령어일 경우
			step1Button.setEnabled(false);
			stepAllButton.setEnabled(false);
			try {
				resourceManager.closeDevice();  
			} catch (IOException e) {
				e.printStackTrace();
			}
			finished=true;  //프로그램이 끝났다고 체크한다.
		}
	};
	
}
