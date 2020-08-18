package ariiiii;
/**
 * symbol과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	String symbol;
	private int addr;
	private int section;
	
	/**초기화*/
	public SymbolTable() {
		symbol = "";
		addr = 0;
		section = 0;
	}
	
	/**--------------------------------------------------------------------------------------------
	 * 새로운 Symbol을 table에 추가한다.
	 * @param symbol : 새로 추가되는 symbol의 label
	 * @param addr : 해당 symbol이 가지는 주소값
	 * 주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	public void putSymbol(String symbol,int addr) {
		this.symbol = symbol;
		this.addr = addr;
	}
	
	
	
	/**---------------------------------------------------------------------------------------------
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * @param symbol : 변경을 원하는 symbol의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newLocation) {
		this.symbol = symbol;
		this.addr = newLocation;
	}
	
	
	
	/**-------------------------------------------------------------------------------------------
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
	 * @param i : 검색을 원하는 symbol의 index
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
	 */
	public int search(int i) {
		return addr;
	}
	
	
	/**저장된 심볼을 가져온다*/
	public String getSymbol() {
		return symbol;
	}


	/**영역*/
	public int getSection() {
		return section;
	}
	public void setSection(int section) {
		this.section = section;
	}	
	
}
