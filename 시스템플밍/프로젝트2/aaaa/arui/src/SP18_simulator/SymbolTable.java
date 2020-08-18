package SP18_simulator;
import java.util.ArrayList;


public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> addressList;
	/**
	 * 새로운 Symbol을 table에 추가한다.
	 * @param symbol : 새로 추가되는 symbol의 label
	 * @param address : 해당 symbol이 가지는 주소값
	 * <br><br>
	 * 주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	
	public SymbolTable(){   
		symbolList=new ArrayList<String>();
		addressList = new ArrayList<Integer>();
	}
	void putSymbol(String n,int a){
		symbolList.add(n);
		addressList.add(a);
	}
	
	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * @param symbol : 변경을 원하는 symbol의 label
	 * @param newaddress : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newaddress) { 
		for(int i=0;i<symbolList.size();i++) {
			if(symbolList.get(i).equals(symbol)) {
				addressList.set(i, newaddress);
			}
		}
	}
	
	/**
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
	 * @param symbol : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
	 */
	public int search(String symbol) {
		for(int i=0;i<symbolList.size();i++) {
			if(symbolList.get(i) !=null &&symbolList.get(i).equals(symbol)) {
				return addressList.get(i);
			}
		}
		return -1;
	}
	
	
	
}
