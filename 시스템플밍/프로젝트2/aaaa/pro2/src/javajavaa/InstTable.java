package javajavaa;
import java.io.*;
import java.util.*;



/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;
	int instIndex=0;
	Instruction[] instruction = new Instruction[256];
	HashMap<String,Integer> opcode = new HashMap<String, Integer>(); //opcode����
	HashMap<String, Integer> instructionFormat = new HashMap<String, Integer>(); //format����
	
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 */
	public int openFile(String fileName) {
		try{
			FileReader fileReader = new FileReader(fileName); //File class - �ؽ�Ʈ������ �о����
			BufferedReader bufferedReader = new BufferedReader(fileReader); //bufferedreader ����
			String rowLine = null; //���پ� �д� rowline
			
			while((rowLine = bufferedReader.readLine()) != null){
				String[] token = rowLine.split("\t",4); //tab���� 4�� ����
				//System.out.println(rowLine);
				instruction[instIndex] = new Instruction();
				instruction[instIndex].inst = rowLine; //�� ������ �� ����
				Instruction(instruction[instIndex].inst);
				instIndex++;
				
			}
			bufferedReader.close();
		}
		
		catch(IOException e){
			e.printStackTrace();
			return -1;
		}
		
		return 1;
	}
	
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void Instruction(String line) {
		parsing(line);
	}
	
	
	
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
			String[] token = line.split("\t",4); //tab���� 4�� ����
		//	System.out.println(line);
			//instruction[instIndex] = new Instruction();
			//instruction[instIndex].inst = rowLine; //�� ������ �� ����
			instruction[instIndex].name = token[0]; //�̸� ����
			instruction[instIndex].format = Integer.parseInt(token[1]); //������ ����
			instruction[instIndex].op = Integer.parseInt(token[2],16);// op�� ����
			instruction[instIndex].ops = Integer.parseInt(token[3]); //ops�� ����
			
			//System.out.println(token[2]);
			opcode.put(token[0], Integer.parseInt(token[2],16)); //<��ɾ� �̸�, opcode 16����> ����
			opcode.put("+"+token[0], Integer.parseInt(token[2],16)); //4����
			instructionFormat.put(token[0], Integer.parseInt(token[1])); //<��ɾ� �̸�, format>
			instructionFormat.put("+"+token[0], 4); // + �� 4�������� ����
	}
}







/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	protected String inst; //instruction�� ����(line)�� ������ ��´�.
	protected String name; //inst str
	protected int op; //inst op
	protected int format; //inst format
	protected int ops; //inst ops
	
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public Instruction(){
		inst = "";
		name = "";
		op = 0;
		format = 0;
		ops = 0;
	}


	
	public String getStr() {
		return name;
	}

	public int getOp() {
		return op;
	}

	public int getFormat() {
		return format;
	}

	public int getOps() {
		return ops;
	}
	public String getInst() {
		return inst;
	}
}