package ariiiii;
import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	/*사용할 변수 및 클래스 등등---------------------------------------------------------------------------*/
	Arissembler assem = new Arissembler();
	private String label; //라벨
	private String operator; //오퍼레이터
	private String[] operand; //오퍼랜드
	private String comment; //코멘트
	private int locCtr; // locctr
	
	private String xAddress; //=X address
	private int machinOpcode; //기계어 Opcode
	private int machinOpIndex; //기계어 Index 유무
	private int machineOpAddress; //기계어 주소
	private int totalOpcode; //최종주소.
	
	private String oneLine; //한개의 라인
	private int section; //각 라인의 section
	
	
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	
	
	/*생성자----------------------------------------------------------------------------------------*/
	public TokenTable(){
		label = null;
		operator = null;
		operand = new String[MAX_OPERAND];
		comment = null;
		locCtr = 0;
		section=0;
		xAddress="";
	}
	
	
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. ----------------------------------------------------------*/
	SymbolTable symTab;
	Literal literalTab;
	InstTable instTab;
	
	
	/** 각 line을 의미별로 분할하고 분석하는 공간.------------------------------------------------------------- */
	ArrayList<Token> tokenList;
	
	
	
	
	/**----------------------------------------------------------------------------------------------
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 * 
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
		//호출도중 예외처리 오류가 계속 발생하여 main routine으로 위치를 옮겼습니다.
		//main routine에서의 이름은 setimmediate
		
	}
	
	
	
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index){
		//호출도중 예외처리 오류가 계속 발생하여 main routine으로 위치를 옮겼습니다.
	}
	
	/**
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	


	
	/*기타 SET GET 함수들----------------------------------------------------------------------------*/
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOperand(int index) {
		return operand[index];
	}

	public void setOperand(int index, String data) {
		this.operand[index] = data;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getOneLine() {
		return oneLine;
	}

	public void setOneLine(String oneLine) {
		this.oneLine = oneLine;
	}

	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}

	public int getMaxOperand() {
		return MAX_OPERAND;
	}


	public int getLocCtr() {
		return locCtr;
	}


	public void setLocCtr(int locCtr) {
		this.locCtr = locCtr;
	}


	public String[] getOperand() {
		return operand;
	}


	public void setOperand(String[] operand) {
		this.operand = operand;
	}


	public int getMachinOpcode() {
		return machinOpcode;
	}


	public void setMachinOpcode(int machinOpcode) {
		this.machinOpcode |= machinOpcode;
	}


	public int getMachinOpIndex() {
		return machinOpIndex;
	}


	public void setMachinOpIndex(int machinOpIndex) {
		this.machinOpIndex = machinOpIndex;
	}

	public void leftMachinOpcode(){
		this.machinOpcode <<=8;
	}
	
	public void rightMachinOpcode(){
		this.machinOpcode >>=8;
	}


	public String getxAddress() {
		return xAddress;
	}


	public void setxAddress(String xAddress) {
		this.xAddress = xAddress;
	}


	public int getTotalOpcode() {
		return totalOpcode;
	}


	public void setTotalOpcode(int totalOpcode) {
		this.totalOpcode = totalOpcode;
		//System.out.format("%08X%n",totalOpcode);

	}
	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public int TokenTable(SymbolTable symTab, InstTable instTab) {
		Instruction[] instruction = new Instruction[256];
			for(int j = 0; j<assem.lineNum;j++) {
				if(symTab.symbol==instruction[j].name) {
					return 1;
				}
		}
		return 1;
	}
	/**
	 * 초기화하면서 literalTable과 instTable을 링크시킨다.
	 * 링크된 것이 일치하면 1 반환
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public int TokenTable(Literal literalTab[], InstTable instTab[]) {
		Instruction[] instruction = new Instruction[256];
		for(int index=0; index< 5 ; index++) {
			for(int j = 0; j<assem.lineNum;j++) {
				if(literalTab[index].getLiteral()==instruction[j].name) {
					return 1;
				}		
			}
		}
		return 1;
	}
}




/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		//initialize 추가
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		//호출도중 예외처리 오류가 계속 발생하여 main routine으로 위치를 옮겼습니다.
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(char flag, int value) {
		nixbpe = flag;
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
