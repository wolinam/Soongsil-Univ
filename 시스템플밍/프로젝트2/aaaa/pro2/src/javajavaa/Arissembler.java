package javajavaa;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Assembler : 
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. 
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. 
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. 
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) 
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) 
 * 
 * 
 * �ۼ����� ���ǻ��� : 
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)
 * 
 *     
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */

//public class�� java���� �� �ϳ���. �̸��� ���ƾ���. �׳� class�� ���X
public class Arissembler { 
	
	final public int maxInst = 256;
	final public int maxLines = 5000;
	
	private InstTable instruction = new InstTable("inst.txt");
	private SymbolTable[] symbolUnit; //�ɺ�
	private TokenTable[] tokenUnit; //��ū ���̺�
	private TokenTable[] immediateData; //immediate Data�� ������ �����ȴ�.
	private Literal[] literal; //���ͷ�
	private Literal[] starLiteral; //label�� star�� ���ͷ� ����. 
	
	private boolean first; //������ Ȯ���Ѵ�.
	private int setImmediateIndex; //immediate Data�� �ε��� �� 
	private int cntEqu; // EQU�� ����. ù��°�� �ι��� EQU�� ó���� �ٸ����Ѵ�.
	private boolean firstMemoryExtent; // �޸𸮿��� ó�� �����Ҷ� Ȯ��
	private String csectLabel; //CSECT�� �� ���� �����Ѵ�.
	
	HashMap<String,Integer> instructionFormat = instruction.instructionFormat; //instruction Format�� �ҷ��´�.
	HashMap<String,Integer> opcode = instruction.opcode; //opcode �ҷ��´�
	
	private HashMap<String, Integer> labelTable; // EQU��� ����ϱ� ���� LABEL�� LOC�� �����Ѵ�.
	private HashMap<String, Integer> absoluteLocCtr; //���밪�� �������ش�.(�󺧰� ���밪)
	LinkedHashMap<String, Integer> literalFormat; //literal Format�� �����Ѵ�.
	private ArrayList<Integer> sectionEndLength; //SEction�� ����
	private HashMap<Integer,Integer> sectionLength; //Section�� ����
	private ArrayList<String> sectionName; // �����̸�
	ArrayList<String> extref; //EXTREF�� ��Ī�� ���� �����Ѵ�.
	
	int instIndex = instruction.instIndex; //instruction�� �ε��� ����
	String[] inputData = new String[maxLines]; // input Data�� ����
	int lineNum; //input�� ���� �� ���.
	int locctr; //������Ų��.
	int literalIndex; //���ͷ� �ε���
	int symbolIndex; //�ɺ� �ε���	
	int sectionIndex; // �ɺ����� ���� �ε���
	int tokenSectionIndex; //��ū ���� �ε���
	int starLiteralIndex; //Label�� *�� �ε���
	
	
	
	/**�ʱ�ȭ------------------------------------------------------------------------------------*/
	public Arissembler() {
		symbolUnit = new SymbolTable[maxLines];
		tokenUnit = new TokenTable[maxLines];
		immediateData = new TokenTable[maxLines];
		literal = new Literal[maxLines];
		starLiteral = new Literal[maxLines];
		
		first = false;
		setImmediateIndex=0;
		cntEqu=1;
		firstMemoryExtent = true;
		
		labelTable = new HashMap<String,Integer>();
		absoluteLocCtr = new HashMap<String, Integer>();
		literalFormat = new LinkedHashMap<String, Integer>();
		extref = new ArrayList<String>();
		sectionEndLength = new ArrayList<Integer>();
		sectionLength = new HashMap<Integer, Integer>();
		sectionName = new ArrayList<String>();
		
		this.instIndex = 0;
		this.inputData = new String[maxLines];
		this.lineNum = 0;
		this.locctr = 0;
		this.literalIndex=0;
		this.symbolIndex = 0;
		this.sectionIndex = 0;
		this.tokenSectionIndex = 0;
		this.starLiteralIndex = 0;
	}
	
	
	/** 
	 * ������� ���� ��ƾ--------------------------------------------------------------------------
	 */
	public static void main(String[] args) {
		Arissembler ariassembler = new Arissembler();
		ariassembler.loadInputFile("input.txt");
		ariassembler.pass1();

		ariassembler.printSymbolTable("symtab_20180474.txt");
		//assembler.printLiteralTable("literaltab_00000000");
		ariassembler.printLiteralTable("literaltab_20180474.txt");
		ariassembler.pass2();
		//assembler.printObjectCode("output_00000000");
		ariassembler.makeObjectCode("output_20180474.txt");
	}

	
	
	/**input file�� �о��-----------------------------------------------------------------------*/
	private void loadInputFile(String inputFile){
		try{
			FileReader fileReader = new FileReader(inputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String rowLine = null;
			int rowIndex = 0;
			
			while((rowLine = bufferedReader.readLine()) != null){
				String[] token = rowLine.split("\n",1); //���๮�ڸ� �����ڷ� �Ͽ� �� �� ��ü�� ������
				inputData[rowIndex]=token[0]; // �ش� �ε����� ����
				rowIndex++; //���� ��ġ 1 ����.
			}
			lineNum=rowIndex; //�� ���� ���� ���
			bufferedReader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	
	
	/** --------------------------------------------------------------------------------------
	 * pass1 ������ �����Ѵ�.
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����
	 *   2) label�� symbolTable�� ����
	 *   
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	public void pass1(){
		for(int i=0; i < lineNum; i++){
			parsing(i);
			saveLocCtr(i);
		}
	}
	
	
	
	
	   /**---------------------------------------------------------------------------------------
	    * �ۼ��� LiteralTable���� ������¿� �°� ����Ѵ�.
	    * @param fileName : ����Ǵ� ���� �̸�
	    */
	   private void printLiteralTable(String Filename) {
	      String[] token = new String[10];
	      String literprint = "";
	      for(int index=0; index< setImmediateIndex ; index++) {
	         if(immediateData[index].getLabel() == "*") {
	            if(immediateData[index].getOperator() == "=C'EOF'"||immediateData[index].getOperator() == "=X'05'")
	               token = (immediateData[index].getOperator()).split("'",3);
	            else
	               token = (immediateData[index].getOperator()).split("=",1);
	      //   System.out.print(token[1]+"\t");
	      //   System.out.printf("%X",immediateData[index].getLocCtr());
	      //   System.out.printf("\n");
	         literprint += token[0];
	         literprint += "\t";
	         literprint += String.format("%X",immediateData[index].getLocCtr());
	         literprint +="\r\n";
	         }
	      }
	      try {
	         BufferedWriter bfWriter = new BufferedWriter(new FileWriter(Filename));
	         bfWriter.write(literprint);
	         bfWriter.close();
	      }catch(IOException e){
	         e.printStackTrace();
	      }
	   }
	
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		String symprint = "";
		for(int index=0; index< setImmediateIndex ; index++) {
		if((immediateData[index].getLabel() != "")&&(immediateData[index].getLabel() != "*")) {
		//	System.out.print(immediateData[index].getLabel()+"\t");
		//	System.out.printf("%X",immediateData[index].getLocCtr());
		//	System.out.printf("\n");
			symprint+=(immediateData[index].getLabel());
			symprint+="\t";
			symprint+=String.format("%X",immediateData[index].getLocCtr());
			symprint+="\r\n";
			}
		}
		try {
			BufferedWriter bfWriter = new BufferedWriter(new FileWriter(fileName));
			bfWriter.write(symprint);
			bfWriter.close();
		}catch(IOException e){
			e.printStackTrace();
			}
		}
	
	
		
	/**---------------------------------------------------------------------------------------
	 * pass2 ������ �����Ѵ�.
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	public void pass2(){
		//�ɺ�����
			makeSymbol();
		//���� ��ȯ
		for(int i=0; i< setImmediateIndex ; i++)
			changeMachineCode(i);
	}
	
	
	
	/**--------------------------------------------------------------------------------------
	 * SymbolTable�� ����� - putsymbol�� ȣ��
	 */
	public void makeSymbol(){
		for(int index=0; index< setImmediateIndex ; index++) {
			if(immediateData[index].getLabel() != ""){
				symbolUnit[symbolIndex] = new SymbolTable();
				
				//ltorg
				if(immediateData[index].getLabel() == "*"){
					starLiteral[starLiteralIndex] = new Literal();
					starLiteral[starLiteralIndex].setLiteral(immediateData[index].getOperator()); //operator�� ����
					starLiteral[starLiteralIndex].setLocCtr(immediateData[index].getLocCtr());
					starLiteralIndex++;
					}
				
				//symboltable�� �ϳ��� �߰�
				symbolUnit[symbolIndex].putSymbol(immediateData[index].getLabel(),immediateData[index].getLocCtr());//������
				symbolUnit[symbolIndex].setSection(sectionIndex);//��������
			
				//���� ����
				if(immediateData[index].getOperator().contains("CSECT")){ //�������� CSECT ���Ȥ�
					sectionIndex++; //�ε��� ����
					symbolUnit[symbolIndex].setSection(symbolIndex);//��������
					}
				symbolIndex++;
				}
			}
		}
	
	
	
	/**---------------------------------------------------------------------------------------
	 *���� �ڵ������� �˻��ϴ� �Լ� - ObjectCode���� T�κ��� ���� �� �ʿ�
	 */
	private int changeMachineCode(int index){
		String operator = immediateData[index].getOperator(); //���۷�����
		int section = immediateData[index].getSection(); //��ġ
		String label = immediateData[index].getLabel(); //��
		Integer format = instructionFormat.get(operator);
		String operandOne = immediateData[index].getOperand(0); //���۷���1
		String operandTwo = immediateData[index].getOperand(1); //���۷���2
		String operandThree = immediateData[index].getOperand(2); //���۷���3/
		int operatorSection = immediateData[index].getSection();
		String locCtr ="";
		if(searchOpcode(operator)==0)				
			return 0;
		//Symbol�� locCtr�� ã���ش�.
		for(int i=0; i<symbolIndex; i++){
			if(symbolUnit[i].getSymbol().contains(operandOne)){
				locCtr = symbolUnit[i].getSymbol(); // locCtr
			}
		}
		//System.out.println(label+" , " + operator +" , " + format + " , "+ operandOne+" , "+ operandTwo+" , "+ operandThree + " , " + operatorSection);

		if(label.contains("*")){
			if(operator.contains("=C")){
				for(int i=3; operator.charAt(i) != '\'';i++){
					immediateData[index].setMachinOpcode(operator.charAt(i));
					immediateData[index].leftMachinOpcode(); //8��Ʈ ��������
				}
				immediateData[index].rightMachinOpcode(); //8��Ʈ ��������
				immediateData[index].setTotalOpcode(immediateData[index].getMachinOpcode());
				//System.out.format("%04X%n",immediateData[index].getTotalOpcode());
				return 0;
			}
			else if(operator.contains("=X")){
				String tmpCat = ""; //���ڿ� �����
				for(int i=3; operator.charAt(i) != '\'';i++){
					tmpCat+=operator.charAt(i);
				}
				immediateData[index].setxAddress(tmpCat);
				//System.out.println(immediateData[index].getxAddress());
				return 0;
			}
		}
		else if(operator.contains("BYTE")){
			String tmpCat = ""; //���ڿ� �����
			for(int i=2; operandOne.charAt(i) != '\'';i++){
				tmpCat+=operandOne.charAt(i);
			}
			immediateData[index].setxAddress(tmpCat);
			//System.out.println(immediateData[index].getxAddress());
			return 0;
		}
		else if(operator.contains("WORD")){
			//-��ȣ�� ������(���밪 ���)
			if(operandOne.contains("-")){
				String[] token = operandOne.split("-",2);
				int beforeSection = 0;
				int afterSection = 0;
				for(int i=0; i<symbolIndex;i++){
					if(symbolUnit[i].getSymbol().contains(token[0])){
						beforeSection = symbolUnit[i].getSection();
						afterSection = symbolUnit[i].getSection();
					}
				}
				//���� �Ѱ��� ������ ������ ������
				if((beforeSection != section) || (afterSection != section)){
					immediateData[index].setTotalOpcode(0); //0�� �Է��Ѵ�. �����ϹǷ�
				}
				
				//������ �ΰ��� ��ġ�ϸ�
				else{
					immediateData[index].setTotalOpcode(absoluteLocCtr.get(label)); //��� ���밪���� ���ش�.
				}
			}
			else if(operandOne.contains("+")){
				String[] token = operandOne.split("+",2);
				int beforeSection = 0;
				int afterSection = 0;
				for(int i=0; i<symbolIndex;i++){
					if(symbolUnit[i].getSymbol().contains(token[0])){
						beforeSection = symbolUnit[i].getSection();
						afterSection = symbolUnit[i].getSection();
					}
				}
				//���� �Ѱ��� ������ ������ ������
				if((beforeSection != section) || (afterSection != section)){
					immediateData[index].setTotalOpcode(0); //0�� �Է��Ѵ�. �����ϹǷ�
				}
				//������ �ΰ��� ��ġ�ϸ�
				else{
					immediateData[index].setTotalOpcode(absoluteLocCtr.get(label)); //��� ���밪���� ���ش�.
				}
			}
			// -�� +�� �ƴ϶��..
			else{
				immediateData[index].setTotalOpcode(Integer.parseInt(operandOne)); //��� ���밪���� ���ش�.
			}
			//System.out.format("%06X\n", immediateData[index].getTotalOpcode());				

			return 0;
		}
		//1format
		if(instructionFormat.get(operator) ==1){
			int op=opcode.get(operator);
			immediateData[index].setTotalOpcode(op); //�������ش�.
		}
		//2format
		if(instructionFormat.get(operator) ==2){
			int op = opcode.get(operator);
			int cmp = op;
			cmp <<=4; //op�� ��ġ�� �Ű��ش�.
			if(operandOne.contains("A")){
				cmp |= 0;
				cmp <<= 4;
			}
			else if(operandOne.contains("X")){
				cmp |= 1;
				cmp <<= 4;
			}
			else if(operandOne.contains("L")){
				cmp |= 2;
				cmp <<= 4;
			}
			else if(operandOne.contains("B")){
				cmp |= 3;
				cmp <<= 4;
			}
			else if(operandOne.contains("S")){
				cmp |= 4;
				cmp <<= 4;
			}
			else if(operandOne.contains("T")){
				cmp |= 5;
				cmp <<= 4;
			}
			else if(operandOne.contains("F")){
				cmp |= 6;
				cmp <<= 4;
			}
			else if(operandOne.contains("PC")){
				cmp |= 8;
				cmp <<= 4;
			}
			else if(operandOne.contains("SW")){
				cmp |= 9;
				cmp <<= 4;
			}
			
			// index�� ������
			if(operandTwo.contains("A")||operandTwo.contains("X")||operandTwo.contains("L")||
					operandTwo.contains("B")||operandTwo.contains("S")||operandTwo.contains("T")||
					operandTwo.contains("F")||operandTwo.contains("PC") || operandTwo.contains("SW")){
				if(operandTwo.contains("A")){
					cmp |= 0;
				}
				else if(operandTwo.contains("X")){
					cmp |= 1;
				}
				else if(operandTwo.contains("L")){
					cmp |= 2;
				}
				else if(operandTwo.contains("B")){
					cmp |= 3;
				}
				else if(operandTwo.contains("S")){
					cmp |= 4;
				}
				else if(operandTwo.contains("T")){
					cmp |= 5;
				}
				else if(operandTwo.contains("F")){
					cmp |= 6;
				}
				else if(operandTwo.contains("PC")){
					cmp |= 8;
				}
				else if(operandTwo.contains("SW")){
					cmp |= 9;
				}
			}
			immediateData[index].setTotalOpcode(cmp); //�������ش�.
			//System.out.format("%1X\n", immediateData[index].getTotalOpcode());				

		}
		
		//3format
		else if((instructionFormat.get(operator) == 3) && !operator.contains("+")){
			// op�� n,i�� ����
			int cmp =0;
			int op = opcode.get(operator);
			cmp |= op;
			if(operandOne.contains("#")){
				cmp|=01;
			}
			else if(operandOne.contains("@")){
				cmp |=02;
			}
			else{
				cmp |=03;
			}
			cmp<<=4;
			
			// x, b, p, e ����
			//RSUB�� �� Ư���� ó��..
			if(operator.contains("RSUB")){
				cmp<<=12;
				immediateData[index].setTotalOpcode(cmp); //�������ش�.
				//System.out.format("%06X\n", immediateData[index].getTotalOpcode());
				return 0;
			}
			//���۷��忡 #�� �ִٸ�..
			else if(operandOne.contains("#")){
				String tmpOperand ="";
				tmpOperand = operandOne.replaceFirst("#", ""); //# ����
				cmp<<=12;
				cmp |= Integer.parseInt(tmpOperand);
				
				immediateData[index].setTotalOpcode(cmp); //�������ش�.
				//System.out.format("%06X\n", immediateData[index].getTotalOpcode());
				return 0;
			}
			//index �����ϴ°�
			//TODO : �������Ͽ� ������, 4���Ŀ� �ִ°� ���� ����� �����Ѵ�. �������� ���ؼ�.
			if(operandTwo !=""){
				cmp |=8; //X
				cmp |=2; //P
			}
			//PC
			else{
				cmp |= 2;
			}
			//cmp<<=4;
			
			cmp<<=12;
			
			//address ������ �ʿ䰡 ����.
			if(operandOne.contains("#") || operandOne.contains("@")){
				immediateData[index].setTotalOpcode(cmp); //�������ش�.
				return 0;
			}
			//ADDRESS
			
			int symbolAddr = 0; //����� ���� �ɺ� �ּ�
			for(int i=0; i<symbolIndex;i++){
				//operand�� section�� ��ġ�ϴ°��� ����ش�.
				if(symbolUnit[i].getSymbol().contains(operandOne) &&
						symbolUnit[i].getSection() == section){
					symbolAddr = symbolUnit[i].search(i);
					//System.out.format("%06X%n", symbolAddr);
				}
			}
			
			//=C''�ΰ��� =X''�ΰ���� �ּҰ� ���
			if(operandOne.contains("=X") || operandOne.contains("=C")){
				for(int i=0; i<2;i++){
					if(starLiteral[i].getLiteral().contains(operandOne)){
						symbolAddr = starLiteral[i].getLocCtr();
					}
				}
			}
			
			int nextLocCtr = immediateData[index+1].getLocCtr(); //���۷�����
			int result = symbolAddr - nextLocCtr;
			//System.out.println(symbolAddr+" - " + nextLocCtr+" = " +result);
			//if(result <0){
			result &= 0xfff;
			//}
			cmp |=result;
			//System.out.format("%06X%n", cmp);
			immediateData[index].setTotalOpcode(cmp); //�������ش�.
			return 0;
		}
		
		//4format
		else if(instructionFormat.get(operator) ==4){
			//EXTREF ���ġ Ž��.
			ArrayList<String> tmp = new ArrayList<String>();
			for(int i=0; i< setImmediateIndex;i++){
				if(immediateData[i].getOperator().contains("EXTREF")){
					tmp.add(immediateData[i].getOperand(0));
					tmp.add(immediateData[i].getOperand(1));
					tmp.add(immediateData[i].getOperand(2));
					break;
				}
			}
			
			// op�� n,i�� ����
			int cmp =0;
			int op = opcode.get(operator);
			cmp |= op;
			if(operandOne.contains("#")){
				cmp|=01;
			}
			else if(operandOne.contains("@")){
				cmp |=02;
			}
			else{
				cmp |=03;
			}
			cmp<<=4;
			
			// n,i,x,b,p,e �߿��� e�� �⺻������ ����ȴ�..
			cmp |=1;
			if(operandTwo !=""){
				cmp |=8; //X
			}

			for(int i=0; i<symbolIndex;i++){
				//�ɺ����̺� �ִ��� Ȯ��
				if(symbolUnit[i].getSymbol().contains(operandOne)){
					//EXTDEF�� ������
					if(tmp.contains(operandOne)){
						int addr = labelTable.get(operandOne);
						cmp<<=20;
						cmp|=addr;
						//System.out.format("%08X%n",cmp);
						immediateData[index].setTotalOpcode(cmp); //�������ش�.

						return 0;
					}
					//���� �������� Ȯ��
					if(symbolUnit[i].getSection() != section){
						cmp <<=20;
						immediateData[index].setTotalOpcode(cmp); //�������ش�.
						//System.out.format("%08X%n",cmp);
						immediateData[index].setTotalOpcode(cmp); //�������ش�.

						return 0;
					}
					//���� �����̸�
					else{
						for(int j=0; j< setImmediateIndex;j++){
							if(immediateData[j].getLabel().contains(label)){
								cmp<<=20;
								cmp|=immediateData[j].getLocCtr();
								immediateData[index].setTotalOpcode(cmp); //�������ش�.

								return 0;
							}
						}

					}
				}
			}


		}
		
		
		return 0;
	}
	
	
	
	/**----------------------------------------------------------------------------------------------
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * ���� ���� ��Ŀ��� public class TokenTable�� �ִ� ��
	 * @param lndex : �и����� ���� �Ϲ� ���ڿ��� ��ġ�� index
	 */
	public int parsing(int index){
		tokenUnit[index] = new TokenTable(); 
		tokenUnit[index+1] = new TokenTable(); 
		
		if(index <0) //�ε��� ����
			return -1;
		
		//�ش� index�� �ִ� input file�� line�� input�� ����
		String input = inputData[index];
		//�Է¹��� line�� parsing�ϱ����� tab���� �����Ͽ� token�� ������ ����
		String[] token = input.split("\t",4);
		

		
		//�ּ����̸� ���� ����
		if(input.equalsIgnoreCase(".") || token[0].matches(".")){
			tokenUnit[index].setLabel(".");
			tokenUnit[index].setOperator("");
			tokenUnit[index].setOperand(0, "");
			tokenUnit[index].setOperand(1, "");
			tokenUnit[index].setComment("\0");
			return 0;
		}
		
		//Label�� ���� ��
		else if(!token[0].matches(".*[^a-z].*")){ 
			tokenUnit[index].setLabel(""); //Label�� ���� ������ ��������
			tokenUnit[index].setOperator(token[1]); //Operator�ֱ�

			
			//token�� 2������ ��. ��, operand�� ���� ��
			if(token.length==2){
				tokenUnit[index].setOperand(0, "");
				tokenUnit[index].setOperand(1, "");
				tokenUnit[index].setComment(""); //operand 3�� �� ����α�
				return 0;
			}
			
			//�Ʒ��� Operand�� 1�� �̻��� ���� �ڵ�
			String[] commaToken = token[2].split(",",3); //�޸��� operand ����
			
			//���۷��尡 �ϳ��� �ϳ��� �����ϰ� ������ ����
			if(commaToken.length==1){ 
				tokenUnit[index].setOperand(0, commaToken[0]);
				tokenUnit[index].setOperand(1, "");
				
				if(commaToken[0].contains("=")){ //���ͷ��϶�
					literal[literalIndex] = new Literal();
					literal[literalIndex].setLiteral(commaToken[0]);
					//literal.literalList.add(commaToken[0]);
					literalIndex++;
					
					//���ͷ� ũ�� ����(key : Literal, value : Format)
					literalFormat.put(commaToken[0], instructionFormat.get(tokenUnit[index].getOperator()));
					
					//System.out.println(commaToken[0]);
				}

			}
			
			//���۷���2���̻�
			else if(commaToken.length>=2){ 
				tokenUnit[index].setOperand(0, commaToken[0]);
				tokenUnit[index].setOperand(1, commaToken[1]); //���۷��� ����
				//EXTREF�� ä���.
				if(tokenUnit[index].getOperator().equalsIgnoreCase("EXTREF")){
					extref.add(commaToken[0]);
					extref.add(commaToken[1]);
						

				}
				if(commaToken.length==3){ //�޸��� 3�����
					tokenUnit[index].setOperand(2, commaToken[2]);
					//EXTREF
					if(tokenUnit[index].getOperator().equalsIgnoreCase("EXTREF")){
						extref.add(commaToken[2]);
						//extref.add(commaToken[1]);
					}
				}
			}
			
			
		}
		//Label�� ���� ��
		else if(token[0].matches(".*[^a-z].*")){ 
			tokenUnit[index].setLabel(token[0]); //Label����
			tokenUnit[index].setOperator(token[1]); //Operator����
			//System.out.println(token[1]);
			
			//operand�� ���� ��
			if(token.length==2){
				tokenUnit[index].setOperand(0, "");
				tokenUnit[index].setOperand(1, "");
				tokenUnit[index].setComment("");
				return 0;
			}

			//�Ʒ��� Operand�� 1�� �̻��� ���� �ڵ�
			String[] commaToken = token[2].split(",",3); //�޸��� ���۷��屸��

			//���۷��� �ϳ�
			if(commaToken.length==1){ 
				tokenUnit[index].setOperand(0, commaToken[0]);
				tokenUnit[index].setOperand(1, "");
				
				if(commaToken[0].contains("=")){ //���ͷ� ã��
					literal[literalIndex] = new Literal();
					literal[literalIndex].setLiteral(commaToken[0]);
					//literal.literalList.add(commaToken[0]);
					literalIndex++;
					
					//���ͷ� ũ�� ����(key : Literal, value : Format)
					literalFormat.put(commaToken[0], instructionFormat.get(tokenUnit[index].getOperator()));
				}
				
				//BUFEND-BUFFER ���� ���� ó��
				if(commaToken[0].contains("-")){
					if(token[1].contains("EQU")&&token[2].contains("-")){
						commaToken = token[2].split("-",2);
						tokenUnit[index].setOperand(0, commaToken[0]);
						tokenUnit[index].setOperand(1, "-");
						tokenUnit[index].setOperand(2, commaToken[1]);
					}
				}

			}
			
			//���۷��� 2�� �̻�
			else if(commaToken.length>=2){ 
				tokenUnit[index].setOperand(0, commaToken[0]);
				tokenUnit[index].setOperand(1, commaToken[1]);
				
				if(commaToken.length==3) //���۷��尡 3��
					tokenUnit[index].setOperand(2, commaToken[2]);

			}
			
			//Comment�� �����Ѵٸ�
			if(token.length == 4){
				tokenUnit[index].setComment(token[3]);
				return 0;
			}
			else if(!(token.length == 4)){
				tokenUnit[index].setComment("");
				return 0;
			}
		}
		
		return 1;
	}
	
	
	
	/**
	 *�������� ���þ����� �˻�-----------------------------------------------------------------------
	 */
	public int searchOpcode(String str){
		//ByTE, WORD�� ��� ����
		if(str.contains("BYTE") || str.equals("WORD"))
			return 1;
		// ���ͷ��� ��� ����
		else if(str.contains("=C") || str.contains("=X"))
			return 1;
		//������ �ִ� �ʹ�� ����
		else if(instructionFormat.get(str)!=null)
			return 1;
		else
			return 0;
	}
	
	
			
	/**
	 * objectcode���� �� ���---------------------------------------------------------------------
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table literal table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param fileName
	 */
	public void makeObjectCode(String fileName){
		for(int i=0; i< setImmediateIndex;i++){
			if(immediateData[i].getOperator().contains(".")){
				System.out.println(immediateData[i].getOperator());
				continue;
					
			}
			if(!(immediateData[i].getOperator().contains("EXTDEF")||
					immediateData[i].getOperator().contains("EXTREF")||
					immediateData[i].getOperator().contains("LTORG")||
					immediateData[i].getOperator().contains("END") ||
					immediateData[i].getOperator().contains("CSECT"))){
				System.out.format("%04X",immediateData[i].getLocCtr());
			}
			
			System.out.print("\t"+immediateData[i].getLabel());
			System.out.print("\t"+immediateData[i].getOperator());
			System.out.print("\t"+immediateData[i].getOperand(0));
			if(immediateData[i].getOperand(1).isEmpty()){
				System.out.print("\t");
			}
			if(immediateData[i].getOperator().contains("+")){
				if(immediateData[i].getOperand(1).equals("")){
				}else{
					System.out.print(","+immediateData[i].getOperand(1)+"\t");
				}
			}
			if(immediateData[i].getOperator().contains("EXTDEF")||
					immediateData[i].getOperator().contains("EXTREF")){
				if(!immediateData[i].getOperand(1).isEmpty()){
					System.out.print(","+immediateData[i].getOperand(1));
				}
				if(!immediateData[i].getOperand(2).isEmpty()){
					System.out.print(","+immediateData[i].getOperand(2));
				}
			}
			if(immediateData[i].getOperator().contains("EQU")){
				System.out.print(immediateData[i].getOperand(1));
				System.out.println(immediateData[i].getOperand(2));
				continue;
			}
			if(instructionFormat.get(immediateData[i].getOperator())==null){
			}
			else if(instructionFormat.get(immediateData[i].getOperator())==2){
				if(!immediateData[i].getOperand(1).isEmpty())
				System.out.print(","+immediateData[i].getOperand(1)+"\t");
			}
			if(immediateData[i].getOperator().contains("=X")||
					immediateData[i].getOperator().contains("BYTE")){
				System.out.println(immediateData[i].getxAddress());
				continue;
			}
			
			//WORD�� ��ġ�� ������� ���̰��Ѵ�.
			if(immediateData[i].getOperator().contains("WORD")){
				System.out.format("%06X\n",immediateData[i].getTotalOpcode());
				continue;
			}
			
			//���ڵ尡 0�ΰ��� �������ʰ� ���ش�.
			if(immediateData[i].getTotalOpcode()==0){
				System.out.println("");
				continue;
			}
			else if(instructionFormat.get(immediateData[i].getOperator())==null){
				System.out.format("%06X\n",immediateData[i].getTotalOpcode());

			}
			else if(instructionFormat.get(immediateData[i].getOperator())==2){
				System.out.format("%04X\n",immediateData[i].getTotalOpcode());
			}
			else if(instructionFormat.get(immediateData[i].getOperator())==3){
				System.out.format("%06X\n",immediateData[i].getTotalOpcode());
			}
			else{
				System.out.format("%06X\n",immediateData[i].getTotalOpcode());
			}
		}
		
		//EXTDEF, EXTREF �����
		HashMap<String,Integer> tmpExtDef=new HashMap<String,Integer>();
		ArrayList<String> tmpExtRef = new ArrayList<String>();
		for(int i=0; i<setImmediateIndex;i++){
			if(immediateData[i].getOperator().contains("EXTDEF") &&
					immediateData[i].getSection()==0){
				String operandOne = immediateData[i].getOperand(0);
				String operandTwo = immediateData[i].getOperand(1);
				String operandThree = immediateData[i].getOperand(2);
				for(int j=0; j<setImmediateIndex; j++){
					if(immediateData[j].getLabel().contains(operandOne)){
						tmpExtDef.put(operandOne, immediateData[j].getLocCtr());
					}
				}
				for(int j=0; j<setImmediateIndex; j++){
					if(immediateData[j].getLabel().contains(operandTwo)){
						tmpExtDef.put(operandTwo, immediateData[j].getLocCtr());
					}
				}
				for(int j=0; j<setImmediateIndex; j++){
					if(immediateData[j].getLabel().contains(operandThree)){
						tmpExtDef.put(operandThree, immediateData[j].getLocCtr());
					}
				}
				continue;
			}
			if(immediateData[i].getOperator().contains("EXTREF") &&
					immediateData[i].getSection()==0){
				String operandOne = immediateData[i].getOperand(0);
				String operandTwo = immediateData[i].getOperand(1);
				String operandThree = immediateData[i].getOperand(2);
				tmpExtRef.add(operandOne);
				tmpExtRef.add(operandTwo);
				tmpExtRef.add(operandThree);
				continue;
			}
		}
		//System.out.println(tmpExtRef);
		try {
			BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(fileName));
			int length=0;
			String buffer ="";
			for(int index = 0; index <setImmediateIndex; index++){
				//START
				if(immediateData[index].getOperator().contains("START")){
					buffer += String.format("H%s\t", immediateData[index].getLabel());
					buffer += " ";
					buffer += String.format("%06X", immediateData[index].getLocCtr());
					buffer += String.format("%06X", sectionEndLength.get(immediateData[index].getSection()));
					buffer += "\r\n";
				}
				
				//default sector 0���� EXTDEF ó��
				if(immediateData[index].getOperator().contains("EXTDEF") &&
						immediateData[index].getSection()==0){
					buffer += "D";
					buffer += immediateData[index].getOperand(0);
					buffer += String.format("%06X",tmpExtDef.get(immediateData[index].getOperand(0)));
					if(!(immediateData[index].getOperand(1)=="")){
						buffer += immediateData[index].getOperand(1);
						buffer += String.format("%06X",tmpExtDef.get(immediateData[index].getOperand(1)));
					}
					if(!(immediateData[index].getOperand(2)=="")){
						buffer += immediateData[index].getOperand(2);
						buffer += String.format("%06X",tmpExtDef.get(immediateData[index].getOperand(2)));
					}
					buffer += "\r\n";
				}

				//default sector 0���� EXTREF ó��
				if(immediateData[index].getOperator().contains("EXTREF") &&
						immediateData[index].getSection()==0){
					buffer += "R";
					for(int i=0;i<tmpExtRef.size();i++){
						buffer += tmpExtRef.get(i);
						buffer += " ";
					}
					buffer += "\r\n";
				}
					
				//sector�� 0�� �ƴѺκ��� ó��
				if(immediateData[index].getOperator().contains("EXTREF") &&
						immediateData[index].getSection()!=0){
					String operandOne = immediateData[index].getOperand(0);
					String operandTwo = immediateData[index].getOperand(1);
					String operandThree = immediateData[index].getOperand(2);
					tmpExtRef.add(operandOne);
					buffer += "R";
					buffer += operandOne;
					if(!(immediateData[index].getOperand(1)=="")){
						buffer += operandTwo;
						tmpExtRef.add(operandTwo);
						
					}
					if(!(immediateData[index].getOperand(2)=="")){
						buffer += operandThree;
						tmpExtRef.add(operandThree);
						}
					buffer += "\r\n";
				}
		
				
				//sector�� ���ۺκ�
				if(tmpExtRef.contains(immediateData[index].getLabel()) &&
						immediateData[index].getOperator().contains("CSECT")){
					length =0; //�ʱ�ȭ
					buffer += String.format(",H%s\t", immediateData[index].getLabel());
					buffer += " "; //����
					buffer += String.format("%06X", immediateData[index].getLocCtr());
					buffer += String.format("%06X", sectionLength.get(immediateData[index].getSection()+1)); //�������ǰŸ� Ų��
					buffer += "\r\n";
				}
				//������ ������.
				String operator = immediateData[index].getOperator();
				if(searchOpcode(operator)==0)				
					continue;
				
				//�����ڸ� �־ ��ũȭ �Ѵ�.
				if(65<= length &&length<=69){
					buffer += ",";
				}
				//������ ���ؼ� LTORG�� �ִ°� �����Ѵ�.
				if(immediateData[index-1].getOperator().contains("LTORG") &&
						immediateData[index].getLabel().contains("*")){
					buffer += ",";
					buffer += String.format("%06X",immediateData[index].getTotalOpcode());
				}
				if(immediateData[index-2].getOperator().contains("LTORG") &&
						immediateData[index].getLabel().contains("*")){
					buffer += ",";
					buffer += String.format("%06X",immediateData[index].getTotalOpcode());
				}
				if(immediateData[index-3].getOperator().contains("LTORG") &&
						immediateData[index].getLabel().contains("*")){
					buffer += ",";
					buffer += String.format("%06X",immediateData[index].getTotalOpcode());
					
				}
				//bufferWriter.write(buffer); //����κ� ����.

				
				if(length==0){
					//System.out.println(buffer);
					
					buffer += "T";
					buffer += String.format("%06X", immediateData[index].getLocCtr());
					buffer += ",";
					length+=7; //T�� �����ּ��� ũ��
					length+=2;
					if(instructionFormat.get(immediateData[index].getOperator()) ==null){
						continue;
					}
					else{
						if(instructionFormat.get(immediateData[index].getOperator()) ==1){
							length+=2;
							buffer += String.format("%02X", immediateData[index].getTotalOpcode());
						}
						if(instructionFormat.get(immediateData[index].getOperator()) ==2){
							length+=4;
							buffer += String.format("%04X", immediateData[index].getTotalOpcode());
						}
						if(instructionFormat.get(immediateData[index].getOperator()) ==3){
							length+=6;
							buffer += String.format("%06X", immediateData[index].getTotalOpcode());
						}
						if(immediateData[index].getOperator() =="=0"){
							buffer += String.format("000000");
							length+=6;
						}
						if(immediateData[index].getOperator() =="=3"){
							buffer += String.format("000003");
							length+=6;
						}
						if(instructionFormat.get(immediateData[index].getOperator()) ==4){
							buffer += String.format("%08X", immediateData[index].getTotalOpcode());
							length+=8;
						}
					}
					//System.out.println("\n"+length+"\n");
				}
				
				else{
					if(instructionFormat.get(immediateData[index].getOperator())==null){
						if(immediateData[index].getOperator().contains("=X")){
							buffer += immediateData[index].getxAddress();
							length+=(immediateData[index].getOperator().length()-4);
						}
						if(immediateData[index].getOperator().contains("BYTE")){
							
							buffer += immediateData[index].getxAddress();
							length+=(immediateData[index].getOperand(0).length()-3);
						}
						if(immediateData[index].getOperator().contains("WORD")){
							buffer += String.format("%06X", immediateData[index].getTotalOpcode());
							length+=6;
						}
						continue;
					}
					if(instructionFormat.get(immediateData[index].getOperator())==1){
						buffer += String.format("%02X", immediateData[index].getTotalOpcode());
						length+=2;
					}
					else if(instructionFormat.get(immediateData[index].getOperator())==2){
						buffer += String.format("%04X", immediateData[index].getTotalOpcode());
						length+=4;
					}
					
					else if(instructionFormat.get(immediateData[index].getOperator())==3){
						buffer += String.format("%06X", immediateData[index].getTotalOpcode());
						length+=6;
					}
					if(immediateData[index].getOperator() =="=0"){
						
						buffer += String.format("000000");length+=6;
					}
					if(immediateData[index].getOperator() =="=3"){
						buffer += String.format("000003");length+=6;
					}
					else if(instructionFormat.get(immediateData[index].getOperator())==4){
						buffer += String.format("%08X", immediateData[index].getTotalOpcode());
						length+=8;
					}
					
				}
			}
			//��ūȭ
			String[] token = buffer.split(",",100);
			String total ="";
			String totalLength = "";
			for(int i=0; i<token.length; i++){
				//System.out.println();
				//System.out.println(token[i]);
				for(int j=0; j<sectionName.size();j++){
					//��ū�ȿ� ���� �̸��� �ֳ� Ȯ���ؼ� �� �߽����� �ٿ��ش�.
					if(token[i].contains("H"+sectionName.get(j))){
						total += token[i]; //T�� �����ش�.
						totalLength = String.format("%02X", (token[i+1].length())/2);
						total += totalLength;
						total += token[i+1];
						//���ѹ���������..
						if(i+3 < token.length){
							if(!token[i+2].contains("T00")){
								total += "\r\n";
								total += "T";
								totalLength = String.format("%06X", (token[i+1].length())/2);
								total += totalLength;
								totalLength = String.format("%02X", (token[i+2].length())/2);
								total += totalLength;
								total += token[i+2];
							}
							if(!token[i+3].contains("T00")){
								total += "\r\n";

								total+="T";
 								for(int k =0; k< setImmediateIndex ;k++){
									
									if(immediateData[k].getOperator().contains("=0")){
										total += "00003009";
										total+= String.format("000000");
										
									//	break;
									}
									if(immediateData[k].getLabel().contains("*")
											&&immediateData[k].getOperator().contains("=C")){
										total+= String.format("%06X", immediateData[k].getTotalOpcode());
									//	total += totalLength;
										//break;
									}
									if(immediateData[k].getOperator().contains("=3")){
										total += String.format("000003");
									//	break;
									}
								}
							//	System.out.println(totalLength);
							//	totalLength = String.format("%02X", (token[i+2].length()/2));
							//	total += totalLength;
							//	total += token[i+3];
								
							}
						}
						total+="\r\n";

						total+=",";
						break;
					}

				}
			}

			//�޸� ��Ʈ�� ���� ��ū
			String[] token2 = total.split(",",100);
			String writeTotal ="";
			//System.out.print(token2[0]);
			//System.out.print(token2[1]);
			//System.out.print(token2[2]);
			ArrayList<String>extOne = new ArrayList<String>();
			ArrayList<String>extTwo = new ArrayList<String>();
			ArrayList<String>extThree = new ArrayList<String>();

			
			//writeTotal += "\r\n";
			writeTotal += token2[0];
			for(int index = 0; index <setImmediateIndex; index++){
				if(immediateData[index].getOperator().contains("EXTREF")){
					if(immediateData[index].getSection() ==0){
						extOne.add(immediateData[index].getOperand(0));
						if(immediateData[index].getOperand(1)!=""){
							extOne.add(immediateData[index].getOperand(1));
						}
						if(immediateData[index].getOperand(2)!=""){
							extOne.add(immediateData[index].getOperand(2));
						}
					}
				}	
				if(immediateData[index].getSection() ==0){
					if(extOne.contains(immediateData[index].getOperand(0))&&
							!immediateData[index].getOperator().contains("EXTREF")&&
							(immediateData[index].getOperand(0)!="")&&
							!immediateData[index].getOperator().contains("RSUB")){
						writeTotal += "M";
						writeTotal+=String.format("%06X",immediateData[index].getLocCtr()+1);
						writeTotal+=String.format("%02X",5);
						if(immediateData[index].getOperand(1) == "-"){
							writeTotal+= "-";
							writeTotal+= immediateData[index].getOperand(0);
						}
						if(immediateData[index].getOperand(1) == ""){
							writeTotal+= "+";
							writeTotal+= immediateData[index].getOperand(0);
						}
						else{
							writeTotal+= "+";
							writeTotal+= immediateData[index].getOperand(0);
						}
						writeTotal += "\r\n";
						//System.out.println(immediateData[index].getLocCtr());
						continue;
					}
					else if(immediateData[index].getOperator().contains("WORD")){
						if(immediateData[index].getOperand(0).contains("-")){
							String[] tmp = immediateData[index].getOperand(0).split("-",100);
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="+";
							writeTotal += tmp[0];
							writeTotal += "\r\n";
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="-";
							writeTotal += tmp[1];
							writeTotal += "\r\n";
						}
						if(immediateData[index].getOperand(0).contains("+")){
							String[] tmp = immediateData[index].getOperand(0).split("+",100);
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="+";
							writeTotal += tmp[0];
							writeTotal += "\r\n";
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="+";
							writeTotal += tmp[1];
							writeTotal += "\r\n";
						}
						
					}
				}
				
			}
			writeTotal += "E";
			writeTotal+=String.format("%06X",immediateData[0].getLocCtr());
			
			
			writeTotal += "\r\n";
			writeTotal += token2[1];
			for(int index = 0; index <setImmediateIndex; index++){
				if(immediateData[index].getOperator().contains("EXTREF")){
					if(immediateData[index].getSection() ==1){
						extTwo.add(immediateData[index].getOperand(0));
						if(immediateData[index].getOperand(1)!=""){
							extTwo.add(immediateData[index].getOperand(1));
						}
						if(immediateData[index].getOperand(2)!=""){
							extTwo.add(immediateData[index].getOperand(2));
						}
					}
				}	
				if(immediateData[index].getSection() ==1){
					if(extTwo.contains(immediateData[index].getOperand(0))&&
							!immediateData[index].getOperator().contains("EXTREF")&&
							(immediateData[index].getOperand(0)!="")&&
							!immediateData[index].getOperator().contains("RSUB")){
						writeTotal += "M";
						writeTotal+=String.format("%06X",immediateData[index].getLocCtr()+1);
						writeTotal+=String.format("%02X",5);
						if(immediateData[index].getOperand(1) == "-"){
							writeTotal+= "-";
							writeTotal+= immediateData[index].getOperand(0);
						}
						if(immediateData[index].getOperand(1) == ""){
							writeTotal+= "+";
							writeTotal+= immediateData[index].getOperand(0);
						}
						else{
							writeTotal+= "+";
							writeTotal+= immediateData[index].getOperand(0);
						}
						writeTotal += "\r\n";
						
						
					}
					else if(immediateData[index].getOperator().contains("WORD")){
						if(immediateData[index].getOperand(0).contains("-")){
							String[] tmp = immediateData[index].getOperand(0).split("-",100);
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="+";
							writeTotal += tmp[0];
							writeTotal += "\r\n";
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="-";
							writeTotal += tmp[1];
							writeTotal += "\r\n";
						}
						if(immediateData[index].getOperand(0).contains("+")){
							String[] tmp = immediateData[index].getOperand(0).split("+",100);
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="+";
							writeTotal += tmp[0];
							writeTotal += "\r\n";
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="+";
							writeTotal += tmp[1];
							writeTotal += "\r\n";
						}
						
						//writeTotal+=immediateData[index].getOperand(0);
					}
				}
			}
			writeTotal += "E";
			
			writeTotal += "\r\n";
			writeTotal += token2[2];

			for(int index = 0; index <setImmediateIndex; index++){
				if(immediateData[index].getOperator().contains("EXTREF")){
					if(immediateData[index].getSection() ==2){
						extThree.add(immediateData[index].getOperand(0));
						if(immediateData[index].getOperand(1)!=""){
							extThree.add(immediateData[index].getOperand(1));
						}
						if(immediateData[index].getOperand(2)!=""){
							extThree.add(immediateData[index].getOperand(2));
						}
					}
				}	

				if(immediateData[index].getSection() ==2){
					if(extThree.contains(immediateData[index].getOperand(0))&&
							!immediateData[index].getOperator().contains("EXTREF")&&
							(immediateData[index].getOperand(0)!="") &&
							!immediateData[index].getOperator().contains("RSUB")){
						//System.out.println(extThree);
						writeTotal += "M";
						writeTotal+=String.format("%06X",immediateData[index].getLocCtr()+1);
						writeTotal+=String.format("%02X",5);
						//System.out.println(immediateData[index].getOperator());
						if(immediateData[index].getOperand(1) == "-"){
							writeTotal+= "-";
							writeTotal+= immediateData[index].getOperand(0);
						}
						if(immediateData[index].getOperand(1) == ""){
							writeTotal+= "+";
							writeTotal+= immediateData[index].getOperand(0);
						}
						else{
							writeTotal+= "+";
							writeTotal+= immediateData[index].getOperand(0);
						}
						writeTotal += "\r\n";
					}
					else if(immediateData[index].getOperator().contains("WORD")){
						if(immediateData[index].getOperand(0).contains("-")){
							String[] tmp = immediateData[index].getOperand(0).split("-",100);
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="+";
							writeTotal += tmp[0];
							writeTotal += "\r\n";
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="-";
							writeTotal += tmp[1];
							writeTotal += "\r\n";
						}
						if(immediateData[index].getOperand(0).contains("+")){
							String[] tmp = immediateData[index].getOperand(0).split("+",100);
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="+";
							writeTotal += tmp[0];
							writeTotal += "\r\n";
							writeTotal +="M";
							writeTotal+=String.format("%06X",immediateData[index].getLocCtr());
							writeTotal+=String.format("%02X",6);
							writeTotal +="+";
							writeTotal += tmp[1];
							writeTotal += "\r\n";
						}
						
						//writeTotal+=immediateData[index].getOperand(0);
					}
				}
			}
			writeTotal += "E";
					
		
			//System.out.println(total);
			//System.out.println(writeTotal);
			
			bufferWriter.write(writeTotal);
			bufferWriter.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	/**�� inst���� LocCtr�� �Ҵ��Ѵ�------------------------------------------------------------------*/
	public int saveLocCtr(int index){
		
		//�ּ�ó��
		if(tokenUnit[index].getLabel().contains(".")){
			setImmediate(tokenUnit[index].getLocCtr(), "", ".", "");
			return 0;
		}
		//START ó��
		if(tokenUnit[index].getOperator().contains("START")){
			locctr=(Integer.parseInt(tokenUnit[index].getOperand(0))); // ���� �ּ� �ʱ�ȭ
			tokenUnit[index].setLocCtr(Integer.parseInt(tokenUnit[index].getOperand(0))); //������ ����
			first = true; // ���������� �˸���. �̰����� ó�� ������ operator�� ó������ �ʴ´�.
			
			//locCtr ������ ����
			setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
			sectionName.add(tokenUnit[index].getLabel()); //��ū�� �̸��� �����Ѵ�.
			return 0;
		}
		
		//END ó��
		if(tokenUnit[index].getOperator().contains("END")){
			setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));

			int literalIndex =0;
			boolean first = true; // LTORG ù�������� ���� 2ĭ ������ locCtr�� �д´�.
			
			// HashMap�� ����Ǿ��ִ� Literal�� ��� ������ locCtr�� �������ش�.
			Set literalKey =  literalFormat.keySet();
			for(Iterator iterator = literalKey.iterator(); iterator.hasNext();){
				String keyLiteral = (String)iterator.next(); //Hashmap Key
				Integer valueFormat = literalFormat.get(keyLiteral); //Hashmap value
				
				//locCtr ������ ����
				//setImmediate(tokenUnit[index].getLocCtr(),"*",keyLiteral,"");

				if(first==true){
					first = false; //ù��°�͸� 2ĭ ������ locCtr�� �а� false�� �ٲ۴�.
					int before = instructionFormat.get(immediateData[setImmediateIndex-2].getOperator()); // ���� Operator�� ���� Operator���� format
					locctr+=3;
					setImmediate(locctr,"*",keyLiteral,"");
				}
				//ù��°�� �ƴ� �������� ���
				else{
					locctr+=3;
					if(immediateData[setImmediateIndex-1].getOperator().contains("=C'")){ //C'' �����϶�
						int before = immediateData[setImmediateIndex-1].getOperator().length()-4; //������ =C'' 4�� ����
						locctr+=3;
						setImmediate(locctr,"*",keyLiteral,"");
					}
					if(immediateData[setImmediateIndex-1].getOperator().contains("=X'")){ //X'' �����϶�
						int before = (immediateData[setImmediateIndex-1].getOperator().length()-4)/2; //������ =C'' 4�� ����
						locctr+=before;
						setImmediate(locctr,"*",keyLiteral,"");
					}
				}
			}
			//������ ���� �����Ѵ�.
			if(immediateData[setImmediateIndex-1].getLabel().contains("*")){
				int result = immediateData[setImmediateIndex-1].getLocCtr()+3;
				sectionEndLength.add(result);
				sectionLength.put(immediateData[setImmediateIndex-1].getSection(),result);
			}
			else if(immediateData[setImmediateIndex-1].getOperator().contains("RSUB")){
				sectionEndLength.add(immediateData[setImmediateIndex-1].getLocCtr()+3);
				sectionLength.put(immediateData[setImmediateIndex-1].getSection(), immediateData[setImmediateIndex-1].getLocCtr()+3);
				
			}


			literalFormat.clear(); //�� ����Ͽ����Ƿ� �ʱ�ȭ���ش�.
			return 0;
		}
		
		//EXTDEF, EXTREF ó��
		if(tokenUnit[index].getOperator().contains("EXTDEF") || 
				tokenUnit[index].getOperator().contains("EXTREF")){
			first=true; //���� ��ĭ ������
				//operand�� 1����
			if(tokenUnit[index].getOperand(1)==null)
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
			
			//operand�� 2����
			else if(tokenUnit[index].getOperand(2)==null)
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1));
			
			//operand�� 2�� �̻�
			else
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
			
			return 0;
		}
		
		//START�� ������ ó�� ������ operator
		if(first == true){
			first = false; //������ �������Ƿ� ������������ ������� �ʴ´�.
			
			tokenUnit[index].setLocCtr(locctr); //������ ����
			//csectLabel
			//locCtr ������ ����
			
			//�� ó���� ���� �����Ѵ�. RDREC, WRREC ���� ��. default sector�� �������.
			if(tokenUnit[index].getLabel().isEmpty()){
				setImmediate(tokenUnit[index].getLocCtr(), csectLabel, tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
			}
			else{
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
			}
			return 0;
		}
		// Operator�� Format�� ������ �ִ� ���
		// + ������ ���
		if((instructionFormat.get(tokenUnit[index].getOperator()) != null)||
				tokenUnit[index].getOperator().contains("+")){
			//4������ ��
			if(immediateData[setImmediateIndex-1].getOperator().contains("+")){
				locctr+=4; //format 4�� �����ش�
				tokenUnit[index].setLocCtr(locctr); // locCtr ����
				//locCtr ������ ����
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
				return 0;
			}
			//�������� ��� (�� �Ʒ��� �־�� �Ѵ�.)
			if(instructionFormat.get(tokenUnit[index-1].getOperator())!=null){
				int before = instructionFormat.get(immediateData[setImmediateIndex-1].getOperator()); // ���� Operator�� ���� Operator���� format
				locctr+=before;
				tokenUnit[index].setLocCtr(locctr); // locCtr ����
				
				//locCtr ������ ����
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
				return 0;
			}
		}// Operator�� Format�� ������ ���� ���� ���		
		else if(instructionFormat.get(tokenUnit[index].getOperator()) == null){
			
			//CSECT�� ���
			if(tokenUnit[index].getOperator().contains("CSECT")){
				//������ ���� �����Ѵ�.
				if(immediateData[setImmediateIndex-1].getOperator().contains("WORD")){
					sectionEndLength.add(immediateData[setImmediateIndex-1].getLocCtr()+3);
					sectionLength.put(immediateData[setImmediateIndex-1].getSection(), immediateData[setImmediateIndex-1].getLocCtr()+3);
				}
				else if(immediateData[setImmediateIndex-1].getOperator().contains("RESW")){
					sectionEndLength.add(Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0))*3);
					sectionLength.put(immediateData[setImmediateIndex-1].getSection(), Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0))*3);
				}
				else if(immediateData[setImmediateIndex-1].getOperator().contains("RESB")){
					sectionEndLength.add(immediateData[setImmediateIndex-1].getLocCtr()+Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0)));
					sectionLength.put(immediateData[setImmediateIndex-1].getSection(), immediateData[setImmediateIndex-1].getLocCtr()+Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0)));

				}
				else if(immediateData[setImmediateIndex-1].getOperator().contains("BYTE")){
					if(immediateData[setImmediateIndex-1].getOperand(0).contains("X'") ||
							immediateData[setImmediateIndex-1].getOperand(0).contains("C'")){
						sectionEndLength.add((immediateData[setImmediateIndex-1].getOperand(0).length()-3)/2);
						sectionLength.put(immediateData[setImmediateIndex-1].getSection(),(immediateData[setImmediateIndex-1].getOperand(0).length()-3)/2);

					}
				}
				locctr=(Integer.parseInt(tokenUnit[0].getOperand(0)));
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
				//���� ����
				tokenSectionIndex++;
				sectionName.add(tokenUnit[index].getLabel());
				//�� ����
				csectLabel = tokenUnit[index].getLabel();
				//TODO : ó������ �ʱ�ȭ
				firstMemoryExtent = true; //���� sect���� �޸� ���� ó�� ������ �� �ְ� ����.
				first = false; //ù��°�͸� 2ĭ ������ locCtr�� �а� false�� �ٲ۴�.
				
				return 0;
			}
			
			//�޸� ���� ó�� ������ ��
			if(tokenUnit[index].getOperator().contains("RESW") ||
					tokenUnit[index].getOperator().contains("RESB") ||
					tokenUnit[index].getOperator().contains("WORD") ||
					tokenUnit[index].getOperator().contains("BYTE") || 
					tokenUnit[index].getOperator().contains("LTORG") || 
					tokenUnit[index].getOperator().contains("EQU")){
				//�޸� ������ ó�� ������ �� Operator�� ó��������Ѵ�.
				//TODO : FIRST
				if(firstMemoryExtent == true){
					// RESW, RESB, WORD, BYTE
					firstMemoryExtent = false; //2��°���ʹ� �޸� �ν�.

					if(tokenUnit[index].getOperator().contains("RESW") ||
							tokenUnit[index].getOperator().contains("RESB") ||
							tokenUnit[index].getOperator().contains("WORD") ||
							tokenUnit[index].getOperator().contains("BYTE")){
						int before = instructionFormat.get(immediateData[setImmediateIndex-1].getOperator()); // ���� Operator�� ���� Operator���� format
						locctr+=before;
						tokenUnit[index].setLocCtr(locctr); // locCtr ����
						
						//locCtr ������ ����
						setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
					}
					
					//LTORG�϶��� ó��
					if(tokenUnit[index].getOperator().contains("LTORG")){
						setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
						int literalIndex =0;
						boolean first = true; // LTORG ù�������� ���� 2ĭ ������ locCtr�� �д´�.
						
						// HashMap�� ����Ǿ��ִ� Literal�� ��� ������ locCtr�� �������ش�.
						Set literalKey =  literalFormat.keySet();
						for(Iterator iterator = literalKey.iterator(); iterator.hasNext();){
							String keyLiteral = (String)iterator.next(); //Hashmap Key
							Integer valueFormat = literalFormat.get(keyLiteral); //Hashmap value
							
							//locCtr ������ ����
							//setImmediate(tokenUnit[index].getLocCtr(),"*",keyLiteral,"");
						
							if(first==true){
								first = false; //ù��°�͸� 2ĭ ������ locCtr�� �а� false�� �ٲ۴�.
								int before = instructionFormat.get(immediateData[setImmediateIndex-2].getOperator()); // ���� Operator�� ���� Operator���� format
								locctr+=before;
								setImmediate(locctr,"*",keyLiteral,"");
							}
							//ù��°�� �ƴ� �������� ���
							else{
								if(immediateData[setImmediateIndex-1].getOperator().contains("=C'")){ //C'' �����϶�
									int before = immediateData[setImmediateIndex-1].getOperator().length()-4; //������ =C'' 4�� ����
									locctr+=before;
									setImmediate(locctr,"*",keyLiteral,"");
								}
								if(immediateData[setImmediateIndex-1].getOperator().contains("=X'")){ //X'' �����϶�
									int before = (immediateData[setImmediateIndex-1].getOperator().length()-4)/2; //������ =C'' 4�� ����
									locctr+=before;
									setImmediate(locctr,"*",keyLiteral,"");
								}
							}
						}
						literalFormat.clear(); //�� ����Ͽ����Ƿ� �ʱ�ȭ���ش�.
						return 0;
					}

					
					//EQU�϶��� ó��
					if(tokenUnit[index].getOperator().contains("EQU")){
						//ù��° EQU
						if(cntEqu==1){
							//������ ���� �����Ѵ�.

							cntEqu++; //�������ʹ� 2��°��
							firstMemoryExtent = true; //EQU�� �ѹ� �� ������ ������ش�.

							int before = instructionFormat.get(immediateData[setImmediateIndex-1].getOperator()); // ���� Operator�� ���� Operator���� format
							setImmediate(before, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							//System.out.format("%04X%n",labelTable.get(tokenUnit[index].getLabel())); //��� �׽�Ʈ
							sectionEndLength.add(immediateData[setImmediateIndex].getLocCtr());
							sectionLength.put(immediateData[setImmediateIndex].getSection(), immediateData[setImmediateIndex].getLocCtr());
							return 0;
						}
						//�ι�°�� EQU
						else if(cntEqu==2){
							cntEqu=1;
							if(tokenUnit[index].getOperand(1)=="-"){
								if((labelTable.get(tokenUnit[index].getOperand(0)) != null) && (labelTable.get(tokenUnit[index].getOperand(2)) != null)){
									int result = labelTable.get(tokenUnit[index].getOperand(0)) - labelTable.get(tokenUnit[index].getOperand(2));
									setImmediate(result, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
								}
							}
							if(tokenUnit[index].getOperand(1)=="+"){
								if((labelTable.get(tokenUnit[index].getOperand(0)) != null) && (labelTable.get(tokenUnit[index].getOperand(2)) != null)){
									int result = labelTable.get(tokenUnit[index].getOperand(0)) + labelTable.get(tokenUnit[index].getOperand(2));
									setImmediate(result, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));								
								}
							}
						}
					}
				}
				
				//TODO : AFTER FIRST
				else if(firstMemoryExtent == false){
					//RESW �϶�
					if(tokenUnit[index].getOperator().contains("RESW") ||
							tokenUnit[index].getOperator().contains("RESB") ||
							tokenUnit[index].getOperator().contains("WORD") ||
							tokenUnit[index].getOperator().contains("BYTE")){
						// ���� RESW �϶�
						if(immediateData[setImmediateIndex-1].getOperator().contains("RESW")){
							locctr+=(Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0))*3);
							tokenUnit[index].setLocCtr(locctr); // locCtr ����

							//locCtr ������ ����
							setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							return 0;
						}
						
						//���� RESB�ϋ�
						if(immediateData[setImmediateIndex-1].getOperator().contains("RESB")){
							locctr+=(Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0)));
							tokenUnit[index].setLocCtr(locctr); // locCtr ����

							//locCtr ������ ����
							setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							return 0;

						}
						
						//���� WORD�ϋ�
						if(immediateData[setImmediateIndex-1].getOperator().contains("WORD")){
							locctr+=3;
							tokenUnit[index].setLocCtr(locctr); // locCtr ����
							
							//locCtr ������ ����
							setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							return 0;
						}
						
						//���� BYTE�϶�
						if(immediateData[setImmediateIndex-1].getOperator().contains("BYTE")){
							// �޸��־ ���ڰ� ���������
							if(immediateData[setImmediateIndex-1].getOperand(0).contains("C'")){ //C'' �����϶�
								locctr+=((immediateData[setImmediateIndex-1].getOperand(0).length()-3)); //C'' Ȥ�� X''�� �����ϰ� 2������..
								tokenUnit[index].setLocCtr(locctr); // locCtr ����

								//locCtr ������ ����
								setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
								return 0;
							}

							if(immediateData[setImmediateIndex-1].getOperand(0).contains("X'")){ //C'' �����϶�
								locctr+=((immediateData[setImmediateIndex-1].getOperand(0).length()-3)/2); 
								tokenUnit[index].setLocCtr(locctr); // locCtr ����

								//locCtr ������ ����
								setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
								return 0;

							}
						}

						//TODO : LABLE�� ���� *�̸� LTORG�̹Ƿ��� ó��
						//LTORG�� ��츦 ó���ϴ� ���̴�.
						if(immediateData[setImmediateIndex-1].getLabel().contains("*")){
							int before = immediateData[setImmediateIndex-1].getOperator().length()-4; //������ =C'' 4�� ����
							locctr+=before;
							//locCtr ������ ����
							setImmediate(locctr, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
						}
					}
					
					//LTORG�ϋ�
					if(tokenUnit[index].getOperator().contains("LTORG")){
						setImmediate(locctr, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
						
						if(immediateData[setImmediateIndex-1].getOperator().contains("LTORG")){
							
							
							//setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							int literalIndex =0;
							boolean first = true; // LTORG ù�������� ���� 2ĭ ������ locCtr�� �д´�.
							
							// HashMap�� ����Ǿ��ִ� Literal�� ��� ������ locCtr�� �������ش�.
							Set literalKey =  literalFormat.keySet();
							for(Iterator iterator = literalKey.iterator(); iterator.hasNext();){
								String keyLiteral = (String)iterator.next(); //Hashmap Key
								Integer valueFormat = literalFormat.get(keyLiteral); //Hashmap value

								if(first==true){
									first = false; //ù��°�͸� 2ĭ ������ locCtr�� �а� false�� �ٲ۴�.
									
									//LTORG ������ RESW�϶�
									if(immediateData[setImmediateIndex-2].getOperator().contains("RESW")){
										locctr+=(Integer.parseInt(immediateData[setImmediateIndex-2].getOperand(0))*3);	
										setImmediate(locctr,"*",keyLiteral,"");
									}
									//LTORG ������ RESB�϶�
									else if(immediateData[setImmediateIndex-2].getOperator().contains("RESB")){
										locctr+=(Integer.parseInt(immediateData[setImmediateIndex-2].getOperand(0)));	
										setImmediate(locctr,"*",keyLiteral,"");
									}
									//LTORG ������ WORD�϶�
									else if(immediateData[setImmediateIndex-2].getOperator().contains("WORD")){
										locctr+=3;	
										setImmediate(locctr,"*",keyLiteral,"");
									}
									//LTORG ������ BYTE�϶�
									else if(immediateData[setImmediateIndex-2].getOperator().contains("BYTE")){
										if(immediateData[setImmediateIndex-2].getOperand(0).contains("C'")){ //C'' �϶�
											locctr+=((immediateData[setImmediateIndex-2].getOperand(0).length()-3));
											setImmediate(locctr,"*",keyLiteral,"");
										}
										if(immediateData[setImmediateIndex-2].getOperand(0).contains("X'")){ //X'' �϶�
											locctr+=((immediateData[setImmediateIndex-2].getOperand(0).length()-3)/2);
											setImmediate(locctr,"*",keyLiteral,"");
										}
									}
									else{
									//���Ŀ�������
									setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
									}
								}
								//ù��°�� �ƴ� �������� ���
								else{
									int before = immediateData[setImmediateIndex-1].getOperator().length()-4; //������ =C'' 4�� ����
									locctr+=before;
									setImmediate(locctr,"*",keyLiteral,"");
								}
							}
							literalFormat.clear(); //�� ����Ͽ����Ƿ� �ʱ�ȭ���ش�.
							return 0;
						}
					}
					
					//EQU�϶�
					if(tokenUnit[index].getOperator().contains("EQU")){
						if(cntEqu==1){
							//������ ���� �����Ѵ�.
							
							cntEqu++; //�������ʹ� 2��°��

							//LTORG ������ RESW�϶�
							if(immediateData[setImmediateIndex-1].getOperator().contains("RESW")){
								locctr+=(Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0))*3);	
								setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
							}
							//LTORG ������ RESB�϶�
							else if(immediateData[setImmediateIndex-1].getOperator().contains("RESB")){
								locctr+=(Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0)));	
								setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
							}
							//LTORG ������ WORD�϶�
							else if(immediateData[setImmediateIndex-1].getOperator().contains("WORD")){
								locctr+=3;	
								setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
							}
							//LTORG ������ BYTE�϶�
							else if(immediateData[setImmediateIndex-1].getOperator().contains("BYTE")){
								if(immediateData[setImmediateIndex-1].getOperand(0).contains("C'")){ //C'' �϶�
									locctr+=((immediateData[setImmediateIndex-1].getOperand(0).length()-3));
									setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
								}
								if(immediateData[setImmediateIndex-1].getOperand(0).contains("X'")){ //X'' �϶�
									locctr+=((immediateData[setImmediateIndex-1].getOperand(0).length()-3)/2);
									setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
								}
							}
							
							//TODO : ���϶�.. *..
							//���Ŀ�������
							else{
								setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
							}
							sectionEndLength.add(immediateData[setImmediateIndex-1].getLocCtr());
							sectionLength.put(immediateData[setImmediateIndex-1].getSection(),immediateData[setImmediateIndex-1].getLocCtr());
							return 0;
						}
						//�ι�°�� EQU
						else if(cntEqu==2){
							cntEqu=1;
							if(tokenUnit[index].getOperand(1)=="-"){
								if((labelTable.get(tokenUnit[index].getOperand(0)) != null) && (labelTable.get(tokenUnit[index].getOperand(2)) != null)){
									int result = labelTable.get(tokenUnit[index].getOperand(0)) - labelTable.get(tokenUnit[index].getOperand(2));
									setImmediate(result, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
									absoluteLocCtr.put(tokenUnit[index].getLabel(),result); //���밪 ��� ����� �������ش�.
									return 0;
								}
							}
							if(tokenUnit[index].getOperand(1)=="+"){
								if((labelTable.get(tokenUnit[index].getOperand(0)) != null) && (labelTable.get(tokenUnit[index].getOperand(2)) != null)){
									int result = labelTable.get(tokenUnit[index].getOperand(0)) + labelTable.get(tokenUnit[index].getOperand(2));
									setImmediate(result, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
									absoluteLocCtr.put(tokenUnit[index].getLabel(),result); //���밪 ��� ����� �������ش�.
									
									return 0;
								}
							}
						}
					}
				}
			}
		}
		return 0;
	}
	
	
	/**Instruction�� �� ��ū�� ��ū���̺� ���� -> class tokentable-----------------------------------*/
	private void setImmediate(int locCtr, String label, String operator,
			String operand){
		immediateData[setImmediateIndex] = new TokenTable();
		immediateData[setImmediateIndex].setLocCtr(locCtr);
		immediateData[setImmediateIndex].setLabel(label);
		immediateData[setImmediateIndex].setOperator(operator);
		immediateData[setImmediateIndex].setOperand(0, operand);
		immediateData[setImmediateIndex].setOperand(1, "");
		immediateData[setImmediateIndex].setOperand(2, "");
		immediateData[setImmediateIndex].setSection(tokenSectionIndex);
		//EQU�� ���� ���� �߰�
		if(label != "") 
			labelTable.put(label, immediateData[setImmediateIndex].getLocCtr());
		//System.out.print(" | " + immediateData[setImmediateIndex].getLabel()+" | " + immediateData[setImmediateIndex].getOperator()+" | " + immediateData[setImmediateIndex].getOperand(0)+ " | ");
		//System.out.format("%04X%n", immediateData[setImmediateIndex].getLocCtr()); //��� �׽�Ʈ
		setImmediateIndex++;
	}
	/**���� ����. ���۷��� �� ���� ��-----------------------------------------------------------------*/
	private void setImmediate(int locCtr, String label, String operator,
			String operandOne, String operandTwo){
		immediateData[setImmediateIndex] = new TokenTable();
		immediateData[setImmediateIndex].setLocCtr(locCtr);
		immediateData[setImmediateIndex].setLabel(label);
		immediateData[setImmediateIndex].setOperator(operator);
		immediateData[setImmediateIndex].setOperand(0, operandOne);
		immediateData[setImmediateIndex].setOperand(1, operandTwo);
		immediateData[setImmediateIndex].setOperand(2, "");
		immediateData[setImmediateIndex].setSection(tokenSectionIndex);
		//EQU�� ���� ���� �߰�
		if(label != "") 
			labelTable.put(label, immediateData[setImmediateIndex].getLocCtr());
		//System.out.print(" | " + immediateData[setImmediateIndex].getLabel()+" | " + immediateData[setImmediateIndex].getOperator()+" | " + immediateData[setImmediateIndex].getOperand(0) +" | " + immediateData[setImmediateIndex].getOperand(1)+ " | ");
		//System.out.format("%04X%n", immediateData[setImmediateIndex].getLocCtr()); //��� �׽�Ʈ
		setImmediateIndex++;
	}
	
	/**���� ����. ���۷��� �� ���� ��-----------------------------------------------------------------*/
	private void setImmediate(int locCtr, String label, String operator,
			String operandOne, String operandTwo, String operandThree){

		immediateData[setImmediateIndex] = new TokenTable();
		immediateData[setImmediateIndex].setLocCtr(locCtr);
		immediateData[setImmediateIndex].setLabel(label);
		immediateData[setImmediateIndex].setOperator(operator);
		immediateData[setImmediateIndex].setOperand(0, operandOne);
		immediateData[setImmediateIndex].setOperand(1, operandTwo);
		immediateData[setImmediateIndex].setOperand(2, operandThree);
		immediateData[setImmediateIndex].setSection(tokenSectionIndex);
		//EQU�� ���� ���� �߰�
		if(label != "") 
			labelTable.put(label, immediateData[setImmediateIndex].getLocCtr());
		//System.out.print(" | " + immediateData[setImmediateIndex].getLabel()+" | " + immediateData[setImmediateIndex].getOperator()+" | " + immediateData[setImmediateIndex].getOperand(0) +" | " + immediateData[setImmediateIndex].getOperand(1)+" | " + immediateData[setImmediateIndex].getOperand(2)+ " | ");
		//System.out.format("%04X%n", immediateData[setImmediateIndex].getLocCtr()); //��� �׽�Ʈ
		setImmediateIndex++;
	}
}
