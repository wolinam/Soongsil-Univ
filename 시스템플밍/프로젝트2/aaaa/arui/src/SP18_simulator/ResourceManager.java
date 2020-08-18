package SP18_simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * ResourceManager�� ��ǻ���� ���� ���ҽ����� �����ϰ� �����ϴ� Ŭ�����̴�.
 * ũ�� �װ����� ���� �ڿ� ������ �����ϰ�, �̸� ������ �� �ִ� �Լ����� �����Ѵ�.<br><br>
 * 
 * 1) ������� ���� �ܺ� ��ġ �Ǵ� device<br>
 * 2) ���α׷� �ε� �� ������ ���� �޸� ����. ���⼭�� 64KB�� �ִ밪���� ��´�.<br>
 * 3) ������ �����ϴµ� ����ϴ� �������� ����.<br>
 * 4) SYMTAB �� simulator�� ���� �������� ���Ǵ� �����͵��� ���� ������. 
 * <br><br>
 * 2���� simulator������ ����Ǵ� ���α׷��� ���� �޸𸮰����� �ݸ�,
 * 4���� simulator�� ������ ���� �޸� �����̶�� ������ ���̰� �ִ�.
 */
public class ResourceManager{
	/**
	 * deviceManager��  ����̽��� �̸��� �Է¹޾��� �� �ش� ����̽��� ���� ����� ���� Ŭ������ �����ϴ� ������ �Ѵ�.
	 * ���� ���, 'A1'�̶�� ����̽����� ������ read���� ������ ���, hashMap�� <"A1", scanner(A1)> ���� �������μ� �̸� ������ �� �ִ�.
	 * <br><br>
	 * ������ ���·� ����ϴ� �� ���� ����Ѵ�.<br>
	 * ���� ��� key������ String��� Integer�� ����� �� �ִ�.
	 * ���� ������� ���� ����ϴ� stream ���� �������� ����, �����Ѵ�.
	 * <br><br>
	 * �̰͵� �����ϸ� �˾Ƽ� �����ؼ� ����ص� �������ϴ�.
	 */
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	char[] memory = new char[65536]; 
	int[] register = new int[10];
	double register_F;
	
	String nowDev;
	List<String> progName;  //�� ���α׷��� �̸�
	List<Integer> progLength;  //�� ���α׷��� ����
	List<Integer> progAddr;  //�� ���α׷��� �ּ�

	SymbolTable symtabList;  //ESTAB�� ���
	
	ResourceManager() {
		symtabList = new SymbolTable();
		progName = new ArrayList<String>();
		progLength = new ArrayList<Integer>();
		progAddr = new ArrayList<Integer>();
	}
	
	

	/**
	 * �޸�, �������͵� ���� ���ҽ����� �ʱ�ȭ�Ѵ�.
	 */
	public void initializeResource(){    
		for(int i=0;i< memory.length;i++)
			memory[i]=0;
		
		for(int i=0;i< register.length;i++)
			register[i]=0;
		register_F=0;
	}
	
	/**
	 * deviceManager�� �����ϰ� �ִ� ���� ����� stream���� ���� �����Ű�� ����.
	 * ���α׷��� �����ϰų� ������ ���� �� ȣ���Ѵ�.
	 * @throws IOException 
	 */
	public void closeDevice() throws IOException {
		BufferedReader in = (BufferedReader) deviceManager.get("F1");
		in.close();
		
		BufferedWriter out = (BufferedWriter) deviceManager.get("05");
		out.flush();
		out.close();
	}
		
	
	/**
	 * ����̽��� ����� �� �ִ� ��Ȳ���� üũ. TD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * ����� stream�� ���� deviceManager�� ���� ������Ų��.
	 * @param devName Ȯ���ϰ��� �ϴ� ����̽��� ��ȣ,�Ǵ� �̸�
	 */
	public void testDevice(String devName) {
		BufferedReader bufReader =null;
		BufferedWriter bufWriter = null;

		if(devName.equals("F1")&&deviceManager.get("F1")==null) {  //F1 ����̽��� �̹� �������� �н��ϰ�, �ƴϸ� �ִ´�.
			FileReader filereader = null;
			try {
				filereader = new FileReader(devName+".txt");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			bufReader = new BufferedReader(filereader);
			if(bufReader!=null) deviceManager.put(devName, bufReader);
		}
		else if(devName.equals("05")&&deviceManager.get("05")==null) {  ////05 ����̽��� �̹� �������� �н��ϰ�, �ƴϸ� �ִ´�.
			Writer filewriter = null;
			try {
				filewriter = new FileWriter(new File(devName+".txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			bufWriter = new BufferedWriter(filewriter);
			if(bufWriter!=null) deviceManager.put(devName, bufWriter);
		}
		setRegister(9,-1);   //sw register�� less���·� �ٲ� RD,WD�� �����ϰ� �Ѵ�.
	}

	/**
	 * ����̽��κ��� ���ϴ� ������ŭ�� ���ڸ� �о���δ�. RD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param num �������� ������ ����
	 * @return ������ ������
	 */
	public char readDevice(String devName, int num){   //RD��ɾ�� �� ���ھ��ۿ� ���о���̹Ƿ� return�� char�� �ϱ�
		int read = 0;
		try {
			BufferedReader buf = (BufferedReader) deviceManager.get(devName);
			if(buf!=null)
			read = buf.read();
			nowDev=devName;
		}catch(IOException er) {
			er.printStackTrace();
		}
		if(read==-1) nowDev = "";  //������ ���� ������ ��� simulator�� ������� ��ġ �׸��� ����
		return (char)read;
	}

	/**
	 * ����̽��� ���ϴ� ���� ��ŭ�� ���ڸ� ����Ѵ�. WD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param data ������ ������
	 * @param num ������ ������ ����
	 */
	public void writeDevice(String devName, char data, int num){  //�̰͵� WD��ɾ�� �� ���ھ��ۿ� ���о���̹Ƿ� return�� char�� �ϱ�
		try {
			BufferedWriter buf = (BufferedWriter) deviceManager.get(devName);
			buf.write(data);
			nowDev=devName;
		}catch(IOException e) {
			e.printStackTrace();
		}
		if(register[5]-1==register[1]) nowDev=""; //�� ������ ���ڿ� �����ϸ� simulator�� ������� ��ġ �׸��� ����
	}
	
	/**
	 * �޸��� Ư�� ��ġ���� ���ϴ� ������ŭ�� ���ڸ� �����´�.
	 * @param location �޸� ���� ��ġ �ε���
	 * @param num ������ ����
	 * @return �������� ������
	 */
	public char[] getMemory(int location, int num){
		int rIndex=0;
		char[] res = new char[num];
		for(int i=location;i<location+num;i++) {
			res[rIndex++] = memory[i];
		}
		return res;
	}

	/**
	 * �޸��� Ư�� ��ġ�� ���ϴ� ������ŭ�� �����͸� �����Ѵ�. 
	 * @param locate ���� ��ġ �ε���
	 * @param data �����Ϸ��� ������
	 * @param num �����ϴ� �������� ����
	 */
	public void setMemory(int locate, char[] data, int num){
		int iIndex = 0;
		for(int i=locate;i<locate+num;i++)
			memory[i]=data[iIndex++];
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		return register[regNum];
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� char[]���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public char[] intToChar(int data){
		char[] temp = new char[3]; 
 		for(int i = 2; i > 0; i--){ 
 			temp[i] = (char)(data & 0xFF); 
	 		data = data >> 8; 
	 	} 
 		temp[0] = (char)(data & 0xFF); 
		return temp; 

	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. char[]���� int���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public int byteToInt(char[] data){   //�ּ��� char[]���� int���·� ��ȯ�Ѵٰ� �Ǿ� �־� 
											//������ byte[]�� char[]�� �ٲ۴�.
		int num = data.length;
		int temp = 0;
		for(int i=0;i<num-1;i++) {
			temp += data[i];
			temp = temp << 8;
		}
		temp += data[num-1];
		return temp;
	}
}