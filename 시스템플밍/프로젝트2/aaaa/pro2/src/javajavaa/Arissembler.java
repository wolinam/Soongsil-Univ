package javajavaa;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */

//public class는 java파일 당 하나만. 이름도 같아야함. 그냥 class는 상관X
public class Arissembler { 
	
	final public int maxInst = 256;
	final public int maxLines = 5000;
	
	private InstTable instruction = new InstTable("inst.txt");
	private SymbolTable[] symbolUnit; //심볼
	private TokenTable[] tokenUnit; //토큰 테이블
	private TokenTable[] immediateData; //immediate Data의 순으로 지정된다.
	private Literal[] literal; //리터럴
	private Literal[] starLiteral; //label이 star인 리터럴 관리. 
	
	private boolean first; //시작을 확인한다.
	private int setImmediateIndex; //immediate Data의 인덱스 값 
	private int cntEqu; // EQU의 순서. 첫번째와 두번쨰 EQU의 처리를 다르게한다.
	private boolean firstMemoryExtent; // 메모리영역 처음 진입할때 확인
	private String csectLabel; //CSECT를 한 라벨을 저장한다.
	
	HashMap<String,Integer> instructionFormat = instruction.instructionFormat; //instruction Format을 불러온다.
	HashMap<String,Integer> opcode = instruction.opcode; //opcode 불러온다
	
	private HashMap<String, Integer> labelTable; // EQU등에서 사용하기 위한 LABEL가 LOC를 저장한다.
	private HashMap<String, Integer> absoluteLocCtr; //절대값을 저장해준다.(라벨과 절대값)
	LinkedHashMap<String, Integer> literalFormat; //literal Format을 저장한다.
	private ArrayList<Integer> sectionEndLength; //SEction별 길이
	private HashMap<Integer,Integer> sectionLength; //Section별 길이
	private ArrayList<String> sectionName; // 섹션이름
	ArrayList<String> extref; //EXTREF의 매칭을 위해 생성한다.
	
	int instIndex = instruction.instIndex; //instruction의 인덱스 개수
	String[] inputData = new String[maxLines]; // input Data의 정보
	int lineNum; //input의 라인 수 기록.
	int locctr; //누적시킨다.
	int literalIndex; //리터럴 인덱스
	int symbolIndex; //심볼 인덱스	
	int sectionIndex; // 심볼유닛 섹션 인덱스
	int tokenSectionIndex; //토큰 센션 인덱스
	int starLiteralIndex; //Label이 *인 인덱스
	
	
	
	/**초기화------------------------------------------------------------------------------------*/
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
	 * 어셈블러의 메인 루틴--------------------------------------------------------------------------
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

	
	
	/**input file을 읽어옴-----------------------------------------------------------------------*/
	private void loadInputFile(String inputFile){
		try{
			FileReader fileReader = new FileReader(inputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String rowLine = null;
			int rowIndex = 0;
			
			while((rowLine = bufferedReader.readLine()) != null){
				String[] token = rowLine.split("\n",1); //개행문자를 구분자로 하여 한 줄 전체를 저장함
				inputData[rowIndex]=token[0]; // 해당 인덱스에 저장
				rowIndex++; //줄의 위치 1 증가.
			}
			lineNum=rowIndex; //총 라인 숫자 기록
			bufferedReader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	
	
	/** --------------------------------------------------------------------------------------
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	public void pass1(){
		for(int i=0; i < lineNum; i++){
			parsing(i);
			saveLocCtr(i);
		}
	}
	
	
	
	
	   /**---------------------------------------------------------------------------------------
	    * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	    * @param fileName : 저장되는 파일 이름
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
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
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
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	public void pass2(){
		//심볼생성
			makeSymbol();
		//기계어 변환
		for(int i=0; i< setImmediateIndex ; i++)
			changeMachineCode(i);
	}
	
	
	
	/**--------------------------------------------------------------------------------------
	 * SymbolTable을 만든다 - putsymbol을 호출
	 */
	public void makeSymbol(){
		for(int index=0; index< setImmediateIndex ; index++) {
			if(immediateData[index].getLabel() != ""){
				symbolUnit[symbolIndex] = new SymbolTable();
				
				//ltorg
				if(immediateData[index].getLabel() == "*"){
					starLiteral[starLiteralIndex] = new Literal();
					starLiteral[starLiteralIndex].setLiteral(immediateData[index].getOperator()); //operator의 저장
					starLiteral[starLiteralIndex].setLocCtr(immediateData[index].getLocCtr());
					starLiteralIndex++;
					}
				
				//symboltable에 하나씩 추가
				symbolUnit[symbolIndex].putSymbol(immediateData[index].getLabel(),immediateData[index].getLocCtr());//라벨저장
				symbolUnit[symbolIndex].setSection(sectionIndex);//영역저장
			
				//영역 구분
				if(immediateData[index].getOperator().contains("CSECT")){ //마지막에 CSECT 나옴ㄴ
					sectionIndex++; //인덱스 증가
					symbolUnit[symbolIndex].setSection(symbolIndex);//영역저장
					}
				symbolIndex++;
				}
			}
		}
	
	
	
	/**---------------------------------------------------------------------------------------
	 *기계어 코드인지를 검사하는 함수 - ObjectCode에서 T부분을 만들 때 필요
	 */
	private int changeMachineCode(int index){
		String operator = immediateData[index].getOperator(); //오퍼레이터
		int section = immediateData[index].getSection(); //위치
		String label = immediateData[index].getLabel(); //라벨
		Integer format = instructionFormat.get(operator);
		String operandOne = immediateData[index].getOperand(0); //오퍼랜드1
		String operandTwo = immediateData[index].getOperand(1); //오퍼랜드2
		String operandThree = immediateData[index].getOperand(2); //오퍼랜드3/
		int operatorSection = immediateData[index].getSection();
		String locCtr ="";
		if(searchOpcode(operator)==0)				
			return 0;
		//Symbol의 locCtr을 찾아준다.
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
					immediateData[index].leftMachinOpcode(); //8비트 왼쪽으로
				}
				immediateData[index].rightMachinOpcode(); //8비트 왼쪽으로
				immediateData[index].setTotalOpcode(immediateData[index].getMachinOpcode());
				//System.out.format("%04X%n",immediateData[index].getTotalOpcode());
				return 0;
			}
			else if(operator.contains("=X")){
				String tmpCat = ""; //문자열 연결용
				for(int i=3; operator.charAt(i) != '\'';i++){
					tmpCat+=operator.charAt(i);
				}
				immediateData[index].setxAddress(tmpCat);
				//System.out.println(immediateData[index].getxAddress());
				return 0;
			}
		}
		else if(operator.contains("BYTE")){
			String tmpCat = ""; //문자열 연결용
			for(int i=2; operandOne.charAt(i) != '\'';i++){
				tmpCat+=operandOne.charAt(i);
			}
			immediateData[index].setxAddress(tmpCat);
			//System.out.println(immediateData[index].getxAddress());
			return 0;
		}
		else if(operator.contains("WORD")){
			//-부호가 있으면(절대값 계산)
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
				//둘중 한개가 섹션의 영역이 없으면
				if((beforeSection != section) || (afterSection != section)){
					immediateData[index].setTotalOpcode(0); //0을 입력한다. 계산못하므로
				}
				
				//섹션이 두개다 일치하면
				else{
					immediateData[index].setTotalOpcode(absoluteLocCtr.get(label)); //계산 절대값으로 해준다.
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
				//둘중 한개가 섹션의 영역이 없으면
				if((beforeSection != section) || (afterSection != section)){
					immediateData[index].setTotalOpcode(0); //0을 입력한다. 계산못하므로
				}
				//섹션이 두개다 일치하면
				else{
					immediateData[index].setTotalOpcode(absoluteLocCtr.get(label)); //계산 절대값으로 해준다.
				}
			}
			// -나 +가 아니라면..
			else{
				immediateData[index].setTotalOpcode(Integer.parseInt(operandOne)); //계산 절대값으로 해준다.
			}
			//System.out.format("%06X\n", immediateData[index].getTotalOpcode());				

			return 0;
		}
		//1format
		if(instructionFormat.get(operator) ==1){
			int op=opcode.get(operator);
			immediateData[index].setTotalOpcode(op); //저장해준다.
		}
		//2format
		if(instructionFormat.get(operator) ==2){
			int op = opcode.get(operator);
			int cmp = op;
			cmp <<=4; //op의 위치를 옮겨준다.
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
			
			// index가 있을때
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
			immediateData[index].setTotalOpcode(cmp); //저장해준다.
			//System.out.format("%1X\n", immediateData[index].getTotalOpcode());				

		}
		
		//3format
		else if((instructionFormat.get(operator) == 3) && !operator.contains("+")){
			// op과 n,i의 영역
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
			
			// x, b, p, e 영역
			//RSUB일 때 특별한 처리..
			if(operator.contains("RSUB")){
				cmp<<=12;
				immediateData[index].setTotalOpcode(cmp); //저장해준다.
				//System.out.format("%06X\n", immediateData[index].getTotalOpcode());
				return 0;
			}
			//오퍼랜드에 #이 있다면..
			else if(operandOne.contains("#")){
				String tmpOperand ="";
				tmpOperand = operandOne.replaceFirst("#", ""); //# 제거
				cmp<<=12;
				cmp |= Integer.parseInt(tmpOperand);
				
				immediateData[index].setTotalOpcode(cmp); //저장해준다.
				//System.out.format("%06X\n", immediateData[index].getTotalOpcode());
				return 0;
			}
			//index 존재하는것
			//TODO : 샘플파일에 없으니, 4형식에 있는거 먼저 만들고 구현한다. 안전성을 위해서.
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
			
			//address 연산할 필요가 없다.
			if(operandOne.contains("#") || operandOne.contains("@")){
				immediateData[index].setTotalOpcode(cmp); //저장해준다.
				return 0;
			}
			//ADDRESS
			
			int symbolAddr = 0; //계산을 위한 심볼 주소
			for(int i=0; i<symbolIndex;i++){
				//operand와 section이 일치하는것을 골라준다.
				if(symbolUnit[i].getSymbol().contains(operandOne) &&
						symbolUnit[i].getSection() == section){
					symbolAddr = symbolUnit[i].search(i);
					//System.out.format("%06X%n", symbolAddr);
				}
			}
			
			//=C''인경우와 =X''인경우의 주소값 얻기
			if(operandOne.contains("=X") || operandOne.contains("=C")){
				for(int i=0; i<2;i++){
					if(starLiteral[i].getLiteral().contains(operandOne)){
						symbolAddr = starLiteral[i].getLocCtr();
					}
				}
			}
			
			int nextLocCtr = immediateData[index+1].getLocCtr(); //오퍼레이터
			int result = symbolAddr - nextLocCtr;
			//System.out.println(symbolAddr+" - " + nextLocCtr+" = " +result);
			//if(result <0){
			result &= 0xfff;
			//}
			cmp |=result;
			//System.out.format("%06X%n", cmp);
			immediateData[index].setTotalOpcode(cmp); //저장해준다.
			return 0;
		}
		
		//4format
		else if(instructionFormat.get(operator) ==4){
			//EXTREF 허용치 탐색.
			ArrayList<String> tmp = new ArrayList<String>();
			for(int i=0; i< setImmediateIndex;i++){
				if(immediateData[i].getOperator().contains("EXTREF")){
					tmp.add(immediateData[i].getOperand(0));
					tmp.add(immediateData[i].getOperand(1));
					tmp.add(immediateData[i].getOperand(2));
					break;
				}
			}
			
			// op과 n,i의 영역
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
			
			// n,i,x,b,p,e 중에서 e는 기본적으로 적용된다..
			cmp |=1;
			if(operandTwo !=""){
				cmp |=8; //X
			}

			for(int i=0; i<symbolIndex;i++){
				//심볼테이블에 있는지 확인
				if(symbolUnit[i].getSymbol().contains(operandOne)){
					//EXTDEF에 있으면
					if(tmp.contains(operandOne)){
						int addr = labelTable.get(operandOne);
						cmp<<=20;
						cmp|=addr;
						//System.out.format("%08X%n",cmp);
						immediateData[index].setTotalOpcode(cmp); //저장해준다.

						return 0;
					}
					//같은 섹션인지 확인
					if(symbolUnit[i].getSection() != section){
						cmp <<=20;
						immediateData[index].setTotalOpcode(cmp); //저장해준다.
						//System.out.format("%08X%n",cmp);
						immediateData[index].setTotalOpcode(cmp); //저장해준다.

						return 0;
					}
					//같은 섹션이면
					else{
						for(int j=0; j< setImmediateIndex;j++){
							if(immediateData[j].getLabel().contains(label)){
								cmp<<=20;
								cmp|=immediateData[j].getLocCtr();
								immediateData[index].setTotalOpcode(cmp); //저장해준다.

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
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * 원래 기존 양식에는 public class TokenTable에 있던 것
	 * @param lndex : 분리되지 않은 일반 문자열이 위치한 index
	 */
	public int parsing(int index){
		tokenUnit[index] = new TokenTable(); 
		tokenUnit[index+1] = new TokenTable(); 
		
		if(index <0) //인덱스 에러
			return -1;
		
		//해당 index에 있는 input file의 line을 input에 복사
		String input = inputData[index];
		//입력받은 line을 parsing하기위해 tab으로 구분하여 token에 각각을 저장
		String[] token = input.split("\t",4);
		

		
		//주석문이면 전부 공백
		if(input.equalsIgnoreCase(".") || token[0].matches(".")){
			tokenUnit[index].setLabel(".");
			tokenUnit[index].setOperator("");
			tokenUnit[index].setOperand(0, "");
			tokenUnit[index].setOperand(1, "");
			tokenUnit[index].setComment("\0");
			return 0;
		}
		
		//Label이 없을 때
		else if(!token[0].matches(".*[^a-z].*")){ 
			tokenUnit[index].setLabel(""); //Label이 들어가는 공간을 공백으로
			tokenUnit[index].setOperator(token[1]); //Operator넣기

			
			//token이 2개뿐일 때. 즉, operand가 없을 때
			if(token.length==2){
				tokenUnit[index].setOperand(0, "");
				tokenUnit[index].setOperand(1, "");
				tokenUnit[index].setComment(""); //operand 3개 다 비워두기
				return 0;
			}
			
			//아래는 Operand가 1개 이상일 때의 코드
			String[] commaToken = token[2].split(",",3); //콤마로 operand 구분
			
			//오퍼랜드가 하나면 하나만 저장하고 나머지 공백
			if(commaToken.length==1){ 
				tokenUnit[index].setOperand(0, commaToken[0]);
				tokenUnit[index].setOperand(1, "");
				
				if(commaToken[0].contains("=")){ //리터럴일때
					literal[literalIndex] = new Literal();
					literal[literalIndex].setLiteral(commaToken[0]);
					//literal.literalList.add(commaToken[0]);
					literalIndex++;
					
					//리터럴 크기 저장(key : Literal, value : Format)
					literalFormat.put(commaToken[0], instructionFormat.get(tokenUnit[index].getOperator()));
					
					//System.out.println(commaToken[0]);
				}

			}
			
			//오퍼랜드2개이상
			else if(commaToken.length>=2){ 
				tokenUnit[index].setOperand(0, commaToken[0]);
				tokenUnit[index].setOperand(1, commaToken[1]); //오퍼랜드 저장
				//EXTREF를 채운다.
				if(tokenUnit[index].getOperator().equalsIgnoreCase("EXTREF")){
					extref.add(commaToken[0]);
					extref.add(commaToken[1]);
						

				}
				if(commaToken.length==3){ //콤마가 3개라면
					tokenUnit[index].setOperand(2, commaToken[2]);
					//EXTREF
					if(tokenUnit[index].getOperator().equalsIgnoreCase("EXTREF")){
						extref.add(commaToken[2]);
						//extref.add(commaToken[1]);
					}
				}
			}
			
			
		}
		//Label이 있을 때
		else if(token[0].matches(".*[^a-z].*")){ 
			tokenUnit[index].setLabel(token[0]); //Label저장
			tokenUnit[index].setOperator(token[1]); //Operator저장
			//System.out.println(token[1]);
			
			//operand가 없을 때
			if(token.length==2){
				tokenUnit[index].setOperand(0, "");
				tokenUnit[index].setOperand(1, "");
				tokenUnit[index].setComment("");
				return 0;
			}

			//아래는 Operand가 1개 이상일 때의 코드
			String[] commaToken = token[2].split(",",3); //콤마로 오퍼랜드구분

			//오퍼랜드 하나
			if(commaToken.length==1){ 
				tokenUnit[index].setOperand(0, commaToken[0]);
				tokenUnit[index].setOperand(1, "");
				
				if(commaToken[0].contains("=")){ //리터럴 찾기
					literal[literalIndex] = new Literal();
					literal[literalIndex].setLiteral(commaToken[0]);
					//literal.literalList.add(commaToken[0]);
					literalIndex++;
					
					//리터럴 크기 저장(key : Literal, value : Format)
					literalFormat.put(commaToken[0], instructionFormat.get(tokenUnit[index].getOperator()));
				}
				
				//BUFEND-BUFFER 같은 연산 처리
				if(commaToken[0].contains("-")){
					if(token[1].contains("EQU")&&token[2].contains("-")){
						commaToken = token[2].split("-",2);
						tokenUnit[index].setOperand(0, commaToken[0]);
						tokenUnit[index].setOperand(1, "-");
						tokenUnit[index].setOperand(2, commaToken[1]);
					}
				}

			}
			
			//오퍼랜드 2개 이상
			else if(commaToken.length>=2){ 
				tokenUnit[index].setOperand(0, commaToken[0]);
				tokenUnit[index].setOperand(1, commaToken[1]);
				
				if(commaToken.length==3) //오퍼랜드가 3개
					tokenUnit[index].setOperand(2, commaToken[2]);

			}
			
			//Comment가 존재한다면
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
	 *기계어인지 지시어인지 검사-----------------------------------------------------------------------
	 */
	public int searchOpcode(String str){
		//ByTE, WORD를 대상에 포함
		if(str.contains("BYTE") || str.equals("WORD"))
			return 1;
		// 리터럴을 대상에 포함
		else if(str.contains("=C") || str.contains("=X"))
			return 1;
		//나머지 있는 것대상에 포함
		else if(instructionFormat.get(str)!=null)
			return 1;
		else
			return 0;
	}
	
	
			
	/**
	 * objectcode생성 및 출력---------------------------------------------------------------------
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
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
			
			//WORD는 수치에 상관없이 보이게한다.
			if(immediateData[i].getOperator().contains("WORD")){
				System.out.format("%06X\n",immediateData[i].getTotalOpcode());
				continue;
			}
			
			//옵코드가 0인것은 보이지않게 해준다.
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
		
		//EXTDEF, EXTREF 만들기
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
				
				//default sector 0에서 EXTDEF 처리
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

				//default sector 0에서 EXTREF 처리
				if(immediateData[index].getOperator().contains("EXTREF") &&
						immediateData[index].getSection()==0){
					buffer += "R";
					for(int i=0;i<tmpExtRef.size();i++){
						buffer += tmpExtRef.get(i);
						buffer += " ";
					}
					buffer += "\r\n";
				}
					
				//sector가 0이 아닌부분의 처리
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
		
				
				//sector의 시작부분
				if(tmpExtRef.contains(immediateData[index].getLabel()) &&
						immediateData[index].getOperator().contains("CSECT")){
					length =0; //초기화
					buffer += String.format(",H%s\t", immediateData[index].getLabel());
					buffer += " "; //띄어쓰기
					buffer += String.format("%06X", immediateData[index].getLocCtr());
					buffer += String.format("%06X", sectionLength.get(immediateData[index].getSection()+1)); //다음섹션거를 킨다
					buffer += "\r\n";
				}
				//기계어의 수행대상.
				String operator = immediateData[index].getOperator();
				if(searchOpcode(operator)==0)				
					continue;
				
				//구분자를 넣어서 토크화 한다.
				if(65<= length &&length<=69){
					buffer += ",";
				}
				//개행을 위해서 LTORG에 있는것 구분한다.
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
				//bufferWriter.write(buffer); //헤더부분 쓴다.

				
				if(length==0){
					//System.out.println(buffer);
					
					buffer += "T";
					buffer += String.format("%06X", immediateData[index].getLocCtr());
					buffer += ",";
					length+=7; //T와 시작주소의 크기
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
			//토큰화
			String[] token = buffer.split(",",100);
			String total ="";
			String totalLength = "";
			for(int i=0; i<token.length; i++){
				//System.out.println();
				//System.out.println(token[i]);
				for(int j=0; j<sectionName.size();j++){
					//토큰안에 섹션 이름이 있나 확인해서 그 중심으로 붙여준다.
					if(token[i].contains("H"+sectionName.get(j))){
						total += token[i]; //T을 합쳐준다.
						totalLength = String.format("%02X", (token[i+1].length())/2);
						total += totalLength;
						total += token[i+1];
						//제한범위내에서..
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

			//메모리 파트를 위한 토큰
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
	
	
	
	/**각 inst마다 LocCtr을 할당한다------------------------------------------------------------------*/
	public int saveLocCtr(int index){
		
		//주석처리
		if(tokenUnit[index].getLabel().contains(".")){
			setImmediate(tokenUnit[index].getLocCtr(), "", ".", "");
			return 0;
		}
		//START 처리
		if(tokenUnit[index].getOperator().contains("START")){
			locctr=(Integer.parseInt(tokenUnit[index].getOperand(0))); // 시작 주소 초기화
			tokenUnit[index].setLocCtr(Integer.parseInt(tokenUnit[index].getOperand(0))); //데이터 저장
			first = true; // 시작했음을 알린다. 이것으로 처음 나오는 operator는 처리하지 않는다.
			
			//locCtr 순으로 저장
			setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
			sectionName.add(tokenUnit[index].getLabel()); //토큰의 이름을 저장한다.
			return 0;
		}
		
		//END 처리
		if(tokenUnit[index].getOperator().contains("END")){
			setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));

			int literalIndex =0;
			boolean first = true; // LTORG 첫번쨰꺼는 이전 2칸 전것의 locCtr을 읽는다.
			
			// HashMap에 저장되어있는 Literal을 모두 꺼내어 locCtr을 저장해준다.
			Set literalKey =  literalFormat.keySet();
			for(Iterator iterator = literalKey.iterator(); iterator.hasNext();){
				String keyLiteral = (String)iterator.next(); //Hashmap Key
				Integer valueFormat = literalFormat.get(keyLiteral); //Hashmap value
				
				//locCtr 순으로 저장
				//setImmediate(tokenUnit[index].getLocCtr(),"*",keyLiteral,"");

				if(first==true){
					first = false; //첫번째것만 2칸 전것의 locCtr을 읽고 false로 바꾼다.
					int before = instructionFormat.get(immediateData[setImmediateIndex-2].getOperator()); // 현재 Operator의 이전 Operator에서 format
					locctr+=3;
					setImmediate(locctr,"*",keyLiteral,"");
				}
				//첫번째가 아닌 나머지의 경우
				else{
					locctr+=3;
					if(immediateData[setImmediateIndex-1].getOperator().contains("=C'")){ //C'' 구조일때
						int before = immediateData[setImmediateIndex-1].getOperator().length()-4; //문자의 =C'' 4개 제거
						locctr+=3;
						setImmediate(locctr,"*",keyLiteral,"");
					}
					if(immediateData[setImmediateIndex-1].getOperator().contains("=X'")){ //X'' 구조일때
						int before = (immediateData[setImmediateIndex-1].getOperator().length()-4)/2; //문자의 =C'' 4개 제거
						locctr+=before;
						setImmediate(locctr,"*",keyLiteral,"");
					}
				}
			}
			//파일의 끝을 저장한다.
			if(immediateData[setImmediateIndex-1].getLabel().contains("*")){
				int result = immediateData[setImmediateIndex-1].getLocCtr()+3;
				sectionEndLength.add(result);
				sectionLength.put(immediateData[setImmediateIndex-1].getSection(),result);
			}
			else if(immediateData[setImmediateIndex-1].getOperator().contains("RSUB")){
				sectionEndLength.add(immediateData[setImmediateIndex-1].getLocCtr()+3);
				sectionLength.put(immediateData[setImmediateIndex-1].getSection(), immediateData[setImmediateIndex-1].getLocCtr()+3);
				
			}


			literalFormat.clear(); //다 사용하였으므로 초기화해준다.
			return 0;
		}
		
		//EXTDEF, EXTREF 처리
		if(tokenUnit[index].getOperator().contains("EXTDEF") || 
				tokenUnit[index].getOperator().contains("EXTREF")){
			first=true; //시작 한칸 딜레이
				//operand가 1개면
			if(tokenUnit[index].getOperand(1)==null)
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
			
			//operand가 2개면
			else if(tokenUnit[index].getOperand(2)==null)
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1));
			
			//operand가 2개 이상
			else
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
			
			return 0;
		}
		
		//START를 제외한 처음 시작의 operator
		if(first == true){
			first = false; //시작이 지났으므로 다음문구에는 사용하지 않는다.
			
			tokenUnit[index].setLocCtr(locctr); //데이터 저장
			//csectLabel
			//locCtr 순으로 저장
			
			//각 처음의 라벨을 저장한다. RDREC, WRREC 같은 것. default sector는 저장안함.
			if(tokenUnit[index].getLabel().isEmpty()){
				setImmediate(tokenUnit[index].getLocCtr(), csectLabel, tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
			}
			else{
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
			}
			return 0;
		}
		// Operator와 Format이 정해져 있는 경우
		// + 포맷인 경우
		if((instructionFormat.get(tokenUnit[index].getOperator()) != null)||
				tokenUnit[index].getOperator().contains("+")){
			//4형식일 떄
			if(immediateData[setImmediateIndex-1].getOperator().contains("+")){
				locctr+=4; //format 4를 더해준다
				tokenUnit[index].setLocCtr(locctr); // locCtr 저장
				//locCtr 순으로 저장
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
				return 0;
			}
			//나머지의 경우 (맨 아래에 있어야 한다.)
			if(instructionFormat.get(tokenUnit[index-1].getOperator())!=null){
				int before = instructionFormat.get(immediateData[setImmediateIndex-1].getOperator()); // 현재 Operator의 이전 Operator에서 format
				locctr+=before;
				tokenUnit[index].setLocCtr(locctr); // locCtr 저장
				
				//locCtr 순으로 저장
				setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
				return 0;
			}
		}// Operator와 Format이 정해져 있지 않은 경우		
		else if(instructionFormat.get(tokenUnit[index].getOperator()) == null){
			
			//CSECT인 경우
			if(tokenUnit[index].getOperator().contains("CSECT")){
				//파일의 끝을 저장한다.
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
				//섹션 증가
				tokenSectionIndex++;
				sectionName.add(tokenUnit[index].getLabel());
				//라벨 저장
				csectLabel = tokenUnit[index].getLabel();
				//TODO : 처음진입 초기화
				firstMemoryExtent = true; //다음 sect에서 메모리 영역 처음 진입할 수 있게 해줌.
				first = false; //첫번째것만 2칸 전것의 locCtr을 읽고 false로 바꾼다.
				
				return 0;
			}
			
			//메모리 영역 처음 진입할 때
			if(tokenUnit[index].getOperator().contains("RESW") ||
					tokenUnit[index].getOperator().contains("RESB") ||
					tokenUnit[index].getOperator().contains("WORD") ||
					tokenUnit[index].getOperator().contains("BYTE") || 
					tokenUnit[index].getOperator().contains("LTORG") || 
					tokenUnit[index].getOperator().contains("EQU")){
				//메모리 영역에 처음 진입할 때 Operator를 처리해줘야한다.
				//TODO : FIRST
				if(firstMemoryExtent == true){
					// RESW, RESB, WORD, BYTE
					firstMemoryExtent = false; //2번째부터는 메모리 인식.

					if(tokenUnit[index].getOperator().contains("RESW") ||
							tokenUnit[index].getOperator().contains("RESB") ||
							tokenUnit[index].getOperator().contains("WORD") ||
							tokenUnit[index].getOperator().contains("BYTE")){
						int before = instructionFormat.get(immediateData[setImmediateIndex-1].getOperator()); // 현재 Operator의 이전 Operator에서 format
						locctr+=before;
						tokenUnit[index].setLocCtr(locctr); // locCtr 저장
						
						//locCtr 순으로 저장
						setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
					}
					
					//LTORG일때의 처리
					if(tokenUnit[index].getOperator().contains("LTORG")){
						setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
						int literalIndex =0;
						boolean first = true; // LTORG 첫번쨰꺼는 이전 2칸 전것의 locCtr을 읽는다.
						
						// HashMap에 저장되어있는 Literal을 모두 꺼내어 locCtr을 저장해준다.
						Set literalKey =  literalFormat.keySet();
						for(Iterator iterator = literalKey.iterator(); iterator.hasNext();){
							String keyLiteral = (String)iterator.next(); //Hashmap Key
							Integer valueFormat = literalFormat.get(keyLiteral); //Hashmap value
							
							//locCtr 순으로 저장
							//setImmediate(tokenUnit[index].getLocCtr(),"*",keyLiteral,"");
						
							if(first==true){
								first = false; //첫번째것만 2칸 전것의 locCtr을 읽고 false로 바꾼다.
								int before = instructionFormat.get(immediateData[setImmediateIndex-2].getOperator()); // 현재 Operator의 이전 Operator에서 format
								locctr+=before;
								setImmediate(locctr,"*",keyLiteral,"");
							}
							//첫번째가 아닌 나머지의 경우
							else{
								if(immediateData[setImmediateIndex-1].getOperator().contains("=C'")){ //C'' 구조일때
									int before = immediateData[setImmediateIndex-1].getOperator().length()-4; //문자의 =C'' 4개 제거
									locctr+=before;
									setImmediate(locctr,"*",keyLiteral,"");
								}
								if(immediateData[setImmediateIndex-1].getOperator().contains("=X'")){ //X'' 구조일때
									int before = (immediateData[setImmediateIndex-1].getOperator().length()-4)/2; //문자의 =C'' 4개 제거
									locctr+=before;
									setImmediate(locctr,"*",keyLiteral,"");
								}
							}
						}
						literalFormat.clear(); //다 사용하였으므로 초기화해준다.
						return 0;
					}

					
					//EQU일때의 처리
					if(tokenUnit[index].getOperator().contains("EQU")){
						//첫번째 EQU
						if(cntEqu==1){
							//파일의 끝을 저장한다.

							cntEqu++; //다음부터는 2번째로
							firstMemoryExtent = true; //EQU의 한번 더 진입을 허용해준다.

							int before = instructionFormat.get(immediateData[setImmediateIndex-1].getOperator()); // 현재 Operator의 이전 Operator에서 format
							setImmediate(before, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							//System.out.format("%04X%n",labelTable.get(tokenUnit[index].getLabel())); //출력 테스트
							sectionEndLength.add(immediateData[setImmediateIndex].getLocCtr());
							sectionLength.put(immediateData[setImmediateIndex].getSection(), immediateData[setImmediateIndex].getLocCtr());
							return 0;
						}
						//두번째의 EQU
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
					//RESW 일때
					if(tokenUnit[index].getOperator().contains("RESW") ||
							tokenUnit[index].getOperator().contains("RESB") ||
							tokenUnit[index].getOperator().contains("WORD") ||
							tokenUnit[index].getOperator().contains("BYTE")){
						// 앞이 RESW 일때
						if(immediateData[setImmediateIndex-1].getOperator().contains("RESW")){
							locctr+=(Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0))*3);
							tokenUnit[index].setLocCtr(locctr); // locCtr 저장

							//locCtr 순으로 저장
							setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							return 0;
						}
						
						//앞이 RESB일떄
						if(immediateData[setImmediateIndex-1].getOperator().contains("RESB")){
							locctr+=(Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0)));
							tokenUnit[index].setLocCtr(locctr); // locCtr 저장

							//locCtr 순으로 저장
							setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							return 0;

						}
						
						//앞이 WORD일떄
						if(immediateData[setImmediateIndex-1].getOperator().contains("WORD")){
							locctr+=3;
							tokenUnit[index].setLocCtr(locctr); // locCtr 저장
							
							//locCtr 순으로 저장
							setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							return 0;
						}
						
						//앞이 BYTE일때
						if(immediateData[setImmediateIndex-1].getOperator().contains("BYTE")){
							// 콤마있어서 문자가 저장됐을때
							if(immediateData[setImmediateIndex-1].getOperand(0).contains("C'")){ //C'' 구조일때
								locctr+=((immediateData[setImmediateIndex-1].getOperand(0).length()-3)); //C'' 혹은 X''을 제외하고 2나눈다..
								tokenUnit[index].setLocCtr(locctr); // locCtr 저장

								//locCtr 순으로 저장
								setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
								return 0;
							}

							if(immediateData[setImmediateIndex-1].getOperand(0).contains("X'")){ //C'' 구조일때
								locctr+=((immediateData[setImmediateIndex-1].getOperand(0).length()-3)/2); 
								tokenUnit[index].setLocCtr(locctr); // locCtr 저장

								//locCtr 순으로 저장
								setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
								return 0;

							}
						}

						//TODO : LABLE을 봐서 *이면 LTORG이므로의 처리
						//LTORG인 경우를 처리하는 것이다.
						if(immediateData[setImmediateIndex-1].getLabel().contains("*")){
							int before = immediateData[setImmediateIndex-1].getOperator().length()-4; //문자의 =C'' 4개 제거
							locctr+=before;
							//locCtr 순으로 저장
							setImmediate(locctr, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
						}
					}
					
					//LTORG일떄
					if(tokenUnit[index].getOperator().contains("LTORG")){
						setImmediate(locctr, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
						
						if(immediateData[setImmediateIndex-1].getOperator().contains("LTORG")){
							
							
							//setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
							int literalIndex =0;
							boolean first = true; // LTORG 첫번쨰꺼는 이전 2칸 전것의 locCtr을 읽는다.
							
							// HashMap에 저장되어있는 Literal을 모두 꺼내어 locCtr을 저장해준다.
							Set literalKey =  literalFormat.keySet();
							for(Iterator iterator = literalKey.iterator(); iterator.hasNext();){
								String keyLiteral = (String)iterator.next(); //Hashmap Key
								Integer valueFormat = literalFormat.get(keyLiteral); //Hashmap value

								if(first==true){
									first = false; //첫번째것만 2칸 전것의 locCtr을 읽고 false로 바꾼다.
									
									//LTORG 위에가 RESW일때
									if(immediateData[setImmediateIndex-2].getOperator().contains("RESW")){
										locctr+=(Integer.parseInt(immediateData[setImmediateIndex-2].getOperand(0))*3);	
										setImmediate(locctr,"*",keyLiteral,"");
									}
									//LTORG 위에가 RESB일때
									else if(immediateData[setImmediateIndex-2].getOperator().contains("RESB")){
										locctr+=(Integer.parseInt(immediateData[setImmediateIndex-2].getOperand(0)));	
										setImmediate(locctr,"*",keyLiteral,"");
									}
									//LTORG 위에가 WORD일때
									else if(immediateData[setImmediateIndex-2].getOperator().contains("WORD")){
										locctr+=3;	
										setImmediate(locctr,"*",keyLiteral,"");
									}
									//LTORG 위에가 BYTE일때
									else if(immediateData[setImmediateIndex-2].getOperator().contains("BYTE")){
										if(immediateData[setImmediateIndex-2].getOperand(0).contains("C'")){ //C'' 일때
											locctr+=((immediateData[setImmediateIndex-2].getOperand(0).length()-3));
											setImmediate(locctr,"*",keyLiteral,"");
										}
										if(immediateData[setImmediateIndex-2].getOperand(0).contains("X'")){ //X'' 일때
											locctr+=((immediateData[setImmediateIndex-2].getOperand(0).length()-3)/2);
											setImmediate(locctr,"*",keyLiteral,"");
										}
									}
									else{
									//수식오류방지
									setImmediate(tokenUnit[index].getLocCtr(), tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0));
									}
								}
								//첫번째가 아닌 나머지의 경우
								else{
									int before = immediateData[setImmediateIndex-1].getOperator().length()-4; //문자의 =C'' 4개 제거
									locctr+=before;
									setImmediate(locctr,"*",keyLiteral,"");
								}
							}
							literalFormat.clear(); //다 사용하였으므로 초기화해준다.
							return 0;
						}
					}
					
					//EQU일때
					if(tokenUnit[index].getOperator().contains("EQU")){
						if(cntEqu==1){
							//파일의 끝을 저장한다.
							
							cntEqu++; //다음부터는 2번째로

							//LTORG 위에가 RESW일때
							if(immediateData[setImmediateIndex-1].getOperator().contains("RESW")){
								locctr+=(Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0))*3);	
								setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
							}
							//LTORG 위에가 RESB일때
							else if(immediateData[setImmediateIndex-1].getOperator().contains("RESB")){
								locctr+=(Integer.parseInt(immediateData[setImmediateIndex-1].getOperand(0)));	
								setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
							}
							//LTORG 위에가 WORD일때
							else if(immediateData[setImmediateIndex-1].getOperator().contains("WORD")){
								locctr+=3;	
								setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
							}
							//LTORG 위에가 BYTE일때
							else if(immediateData[setImmediateIndex-1].getOperator().contains("BYTE")){
								if(immediateData[setImmediateIndex-1].getOperand(0).contains("C'")){ //C'' 일때
									locctr+=((immediateData[setImmediateIndex-1].getOperand(0).length()-3));
									setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
								}
								if(immediateData[setImmediateIndex-1].getOperand(0).contains("X'")){ //X'' 일때
									locctr+=((immediateData[setImmediateIndex-1].getOperand(0).length()-3)/2);
									setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
								}
							}
							
							//TODO : 라벨일때.. *..
							//수식오류방지
							else{
								setImmediate(locctr,tokenUnit[index].getLabel(),tokenUnit[index].getOperator(),tokenUnit[index].getOperand(0));
							}
							sectionEndLength.add(immediateData[setImmediateIndex-1].getLocCtr());
							sectionLength.put(immediateData[setImmediateIndex-1].getSection(),immediateData[setImmediateIndex-1].getLocCtr());
							return 0;
						}
						//두번째의 EQU
						else if(cntEqu==2){
							cntEqu=1;
							if(tokenUnit[index].getOperand(1)=="-"){
								if((labelTable.get(tokenUnit[index].getOperand(0)) != null) && (labelTable.get(tokenUnit[index].getOperand(2)) != null)){
									int result = labelTable.get(tokenUnit[index].getOperand(0)) - labelTable.get(tokenUnit[index].getOperand(2));
									setImmediate(result, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
									absoluteLocCtr.put(tokenUnit[index].getLabel(),result); //절대값 계산 결과를 저장해준다.
									return 0;
								}
							}
							if(tokenUnit[index].getOperand(1)=="+"){
								if((labelTable.get(tokenUnit[index].getOperand(0)) != null) && (labelTable.get(tokenUnit[index].getOperand(2)) != null)){
									int result = labelTable.get(tokenUnit[index].getOperand(0)) + labelTable.get(tokenUnit[index].getOperand(2));
									setImmediate(result, tokenUnit[index].getLabel(), tokenUnit[index].getOperator(), tokenUnit[index].getOperand(0),tokenUnit[index].getOperand(1),tokenUnit[index].getOperand(2));
									absoluteLocCtr.put(tokenUnit[index].getLabel(),result); //절대값 계산 결과를 저장해준다.
									
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
	
	
	/**Instruction의 각 토큰을 토큰테이블에 저장 -> class tokentable-----------------------------------*/
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
		//EQU를 위한 라벨을 추가
		if(label != "") 
			labelTable.put(label, immediateData[setImmediateIndex].getLocCtr());
		//System.out.print(" | " + immediateData[setImmediateIndex].getLabel()+" | " + immediateData[setImmediateIndex].getOperator()+" | " + immediateData[setImmediateIndex].getOperand(0)+ " | ");
		//System.out.format("%04X%n", immediateData[setImmediateIndex].getLocCtr()); //출력 테스트
		setImmediateIndex++;
	}
	/**위와 동일. 오퍼랜드 두 개일 때-----------------------------------------------------------------*/
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
		//EQU를 위한 라벨을 추가
		if(label != "") 
			labelTable.put(label, immediateData[setImmediateIndex].getLocCtr());
		//System.out.print(" | " + immediateData[setImmediateIndex].getLabel()+" | " + immediateData[setImmediateIndex].getOperator()+" | " + immediateData[setImmediateIndex].getOperand(0) +" | " + immediateData[setImmediateIndex].getOperand(1)+ " | ");
		//System.out.format("%04X%n", immediateData[setImmediateIndex].getLocCtr()); //출력 테스트
		setImmediateIndex++;
	}
	
	/**위와 동일. 오퍼랜드 세 개일 때-----------------------------------------------------------------*/
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
		//EQU를 위한 라벨을 추가
		if(label != "") 
			labelTable.put(label, immediateData[setImmediateIndex].getLocCtr());
		//System.out.print(" | " + immediateData[setImmediateIndex].getLabel()+" | " + immediateData[setImmediateIndex].getOperator()+" | " + immediateData[setImmediateIndex].getOperand(0) +" | " + immediateData[setImmediateIndex].getOperand(1)+" | " + immediateData[setImmediateIndex].getOperand(2)+ " | ");
		//System.out.format("%04X%n", immediateData[setImmediateIndex].getLocCtr()); //출력 테스트
		setImmediateIndex++;
	}
}
