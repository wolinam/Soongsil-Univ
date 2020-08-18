package javajavaa;
import java.io.*;
import java.util.*;



/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/** 
	 * inst.data 파일을 불러와 저장하는 공간.
	 *  명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;
	int instIndex=0;
	Instruction[] instruction = new Instruction[256];
	HashMap<String,Integer> opcode = new HashMap<String, Integer>(); //opcode저장
	HashMap<String, Integer> instructionFormat = new HashMap<String, Integer>(); //format저장
	
	
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public int openFile(String fileName) {
		try{
			FileReader fileReader = new FileReader(fileName); //File class - 텍스트파일을 읽어들임
			BufferedReader bufferedReader = new BufferedReader(fileReader); //bufferedreader 선언
			String rowLine = null; //한줄씩 읽는 rowline
			
			while((rowLine = bufferedReader.readLine()) != null){
				String[] token = rowLine.split("\t",4); //tab으로 4개 구분
				//System.out.println(rowLine);
				instruction[instIndex] = new Instruction();
				instruction[instIndex].inst = rowLine; //한 라인을 다 저장
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
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void Instruction(String line) {
		parsing(line);
	}
	
	
	
	
	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
			String[] token = line.split("\t",4); //tab으로 4개 구분
		//	System.out.println(line);
			//instruction[instIndex] = new Instruction();
			//instruction[instIndex].inst = rowLine; //한 라인을 다 저장
			instruction[instIndex].name = token[0]; //이름 저장
			instruction[instIndex].format = Integer.parseInt(token[1]); //포멧의 저장
			instruction[instIndex].op = Integer.parseInt(token[2],16);// op의 저장
			instruction[instIndex].ops = Integer.parseInt(token[3]); //ops의 저장
			
			//System.out.println(token[2]);
			opcode.put(token[0], Integer.parseInt(token[2],16)); //<명령어 이름, opcode 16진수> 넣음
			opcode.put("+"+token[0], Integer.parseInt(token[2],16)); //4형식
			instructionFormat.put(token[0], Integer.parseInt(token[1])); //<명령어 이름, format>
			instructionFormat.put("+"+token[0], 4); // + 는 4형식으로 넣음
	}
}







/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {
	protected String inst; //instruction의 한줄(line)의 내용을 담는다.
	protected String name; //inst str
	protected int op; //inst op
	protected int format; //inst format
	protected int ops; //inst ops
	
	
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
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