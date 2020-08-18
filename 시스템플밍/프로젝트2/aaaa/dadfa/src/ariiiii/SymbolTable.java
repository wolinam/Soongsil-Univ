package ariiiii;
/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	String symbol;
	private int addr;
	private int section;
	
	/**�ʱ�ȭ*/
	public SymbolTable() {
		symbol = "";
		addr = 0;
		section = 0;
	}
	
	/**--------------------------------------------------------------------------------------------
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param addr : �ش� symbol�� ������ �ּҰ�
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol,int addr) {
		this.symbol = symbol;
		this.addr = addr;
	}
	
	
	
	/**---------------------------------------------------------------------------------------------
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		this.symbol = symbol;
		this.addr = newLocation;
	}
	
	
	
	/**-------------------------------------------------------------------------------------------
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param i : �˻��� ���ϴ� symbol�� index
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(int i) {
		return addr;
	}
	
	
	/**����� �ɺ��� �����´�*/
	public String getSymbol() {
		return symbol;
	}


	/**����*/
	public int getSection() {
		return section;
	}
	public void setSection(int section) {
		this.section = section;
	}	
	
}
