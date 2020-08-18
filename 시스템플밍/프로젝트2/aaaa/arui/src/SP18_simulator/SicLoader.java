package SP18_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * SicLoader�� ���α׷��� �ؼ��ؼ� �޸𸮿� �ø��� ������ �����Ѵ�. �� �������� linker�� ���� ���� �����Ѵ�. 
 * <br><br>
 * SicLoader�� �����ϴ� ���� ���� ��� ������ ����.<br>
 * - program code�� �޸𸮿� �����Ű��<br>
 * - �־��� ������ŭ �޸𸮿� �� ���� �Ҵ��ϱ�<br>
 * - �������� �߻��ϴ� symbol, ���α׷� �����ּ�, control section �� ������ ���� ���� ���� �� ����
 */
public class SicLoader {
	ResourceManager rMgr;
	
	public SicLoader(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ
		setResourceManager(resourceManager);
	}

	/**
	 * Loader�� ���α׷��� ������ �޸𸮸� �����Ų��.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code�� �о load������ �����Ѵ�. load�� �����ʹ� resourceManager�� �����ϴ� �޸𸮿� �ö󰡵��� �Ѵ�.
	 * load�������� ������� symbol table �� �ڷᱸ�� ���� resourceManager�� �����Ѵ�.
	 * @param objectCode �о���� ����
	 */
	public void load(File objectCode){
		String line=null;
		int PROGADDR=0;   //os�� �ִ� ���α׷��� ���� �ּҸ� 0���� �����Ͽ� ���´�.
		int CSADDR=0;
		int loc=0;
		try { //pass1
			FileReader filereader = null;
			try {
				filereader = new FileReader(objectCode);  //���� ������ �б�
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			BufferedReader bufReader = new BufferedReader(filereader); // ������ ���پ� �о���̱� ���� bufferedReader�� ����Ѵ�.
			while((line=bufReader.readLine())!=null) {
				switch(line.charAt(0)) {
					case 'H':  //Header record
						int csLen = Integer.parseInt(line.substring(13, 19),16);
						String progName = line.substring(1, 7).trim();
						rMgr.progName.add(progName);  //simulator���� ����ϱ� ���� ���� ���α׷� ����,�̸�,�ּҸ� �����Ѵ�.
						rMgr.progLength.add(csLen);
						rMgr.progAddr.add(CSADDR);
						rMgr.symtabList.putSymbol(progName,CSADDR);
						CSADDR += csLen;  //���� ���α׷��� �����ּ�
						break;
					case 'D':  //Define record
						for(int i=1;i<line.length();i+=12) {  //��� ������ estab�� ������ ���´�
							rMgr.symtabList.putSymbol(line.substring(i,i+6),Integer.parseInt(line.substring(i+7, i+12),16));
						}
						break;
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		try {  //pass2
			CSADDR = 0;
			int csLen=0;
			
			FileReader filereader = null;
			try {
				filereader = new FileReader(objectCode);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			BufferedReader bufReader = new BufferedReader(filereader); // ������ ���پ� �о���̱� ���� bufferedReader�� ����Ѵ�.
			while((line=bufReader.readLine())!=null) {
				switch(line.charAt(0)) {
					case 'H':  //Header record
						CSADDR += csLen;   //�� ���α׷��� �����ּҸ� �����Ѵ�.
						csLen = Integer.parseInt(line.substring(13, 19),16);
						break;
					case 'T':
						int index=9;
						int cnt = 0;
						char num=0;
						int numIndex=0;
						loc = CSADDR + Integer.parseInt(line.substring(1, 7),16); //�� �ؽ�Ʈ ���ڵ��� ������ ������ �����ּҸ� ���Ѵ�.
						while(index<line.length()) {
							char c = line.charAt(index++);
							int tmp =0;
							tmp = c-0x30; //�ƽ�Ű �ڵ带 �Ϲ� ���ڷ� �ٲ۴�.
							if(tmp>9) tmp -= 0x7;
							if(cnt == 0) {  //�� ����Ʈ�� ù��° ���� 
								num = (char) (num+tmp);
								num = (char) (num<<4);
								cnt++;
							}
							else {  //�� ����Ʈ�� �ι�° ����
								num = (char) (num+tmp);
								cnt=0;
								char[] cArray = new char[1];
								cArray[0]=num;
								rMgr.setMemory(loc,cArray,1);
								loc++;
								num=0;
							}
						}
						break;
					case 'M':  //Modify record
						int addr = Integer.parseInt(line.substring(1, 7), 16);
						int mLen = Integer.parseInt(line.substring(8,9));
						String sym = line.substring(10, line.length());
						int plusV = rMgr.symtabList.search(sym);  //estab�� ã�� �� �ɺ��� �ּҰ��� �˾Ƴ���.
						
						char[] preV = rMgr.getMemory(CSADDR+addr,mLen); //������ �޸𸮿� �ִ� ��
						int preInt = 0;
						preInt += preV[0];
						preInt = preInt << 8;
						preInt += preV[1];
						preInt = preInt << 8;
						preInt += preV[2];
						
						if(line.charAt(9)=='+')  //���ϴ� M record �� ���
							preInt += plusV;
						else //���� M record �� ���
							preInt -= plusV;
						
						preV[2] = (char)(preInt&0xFF);
						preInt = preInt >> 8;
						preV[1] = (char)(preInt & 0xFF);
						preInt = preInt >> 8;
						preV[0] = (char) (preInt &0xFF);
						rMgr.setMemory(CSADDR+addr, preV, 3); //��� �� ���� �ٽ� �޸𸮿� �ø���.

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
	
	

}
