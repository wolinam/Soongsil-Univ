package ariiiii;
import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	/*����� ���� �� Ŭ���� ���---------------------------------------------------------------------------*/
	Arissembler assem = new Arissembler();
	private String label; //��
	private String operator; //���۷�����
	private String[] operand; //���۷���
	private String comment; //�ڸ�Ʈ
	private int locCtr; // locctr
	
	private String xAddress; //=X address
	private int machinOpcode; //���� Opcode
	private int machinOpIndex; //���� Index ����
	private int machineOpAddress; //���� �ּ�
	private int totalOpcode; //�����ּ�.
	
	private String oneLine; //�Ѱ��� ����
	private int section; //�� ������ section
	
	
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	
	
	/*������----------------------------------------------------------------------------------------*/
	public TokenTable(){
		label = null;
		operator = null;
		operand = new String[MAX_OPERAND];
		comment = null;
		locCtr = 0;
		section=0;
		xAddress="";
	}
	
	
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. ----------------------------------------------------------*/
	SymbolTable symTab;
	Literal literalTab;
	InstTable instTab;
	
	
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����.------------------------------------------------------------- */
	ArrayList<Token> tokenList;
	
	
	
	
	/**----------------------------------------------------------------------------------------------
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 * 
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
		//ȣ�⵵�� ����ó�� ������ ��� �߻��Ͽ� main routine���� ��ġ�� �Ű���ϴ�.
		//main routine������ �̸��� setimmediate
		
	}
	
	
	
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table literal table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param index
	 */
	public void makeObjectCode(int index){
		//ȣ�⵵�� ����ó�� ������ ��� �߻��Ͽ� main routine���� ��ġ�� �Ű���ϴ�.
	}
	
	/**
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	


	
	/*��Ÿ SET GET �Լ���----------------------------------------------------------------------------*/
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
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param instTab : instruction ���� ���ǵ� instTable
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
	 * �ʱ�ȭ�ϸ鼭 literalTable�� instTable�� ��ũ��Ų��.
	 * ��ũ�� ���� ��ġ�ϸ� 1 ��ȯ
	 * @param literalTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab : instruction ���� ���ǵ� instTable
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
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
	
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		//initialize �߰�
		parsing(line);
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		//ȣ�⵵�� ����ó�� ������ ��� �߻��Ͽ� main routine���� ��ġ�� �Ű���ϴ�.
	}
	
	/** 
	 * n,i,x,b,p,e flag�� �����Ѵ�. 
	 * 
	 * ��� �� : setFlag(nFlag, 1); 
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(char flag, int value) {
		nixbpe = flag;
	}
	
	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� 
	 * 
	 * ��� �� : getFlag(nFlag)
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
