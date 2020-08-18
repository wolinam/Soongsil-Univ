package SP18_simulator;
import java.util.ArrayList;


public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> addressList;
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param address : �ش� symbol�� ������ �ּҰ�
	 * <br><br>
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
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
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newaddress : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newaddress) { 
		for(int i=0;i<symbolList.size();i++) {
			if(symbolList.get(i).equals(symbol)) {
				addressList.set(i, newaddress);
			}
		}
	}
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
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
