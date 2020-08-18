package SP18_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;
	
	public SicLoader(ResourceManager resourceManager) {
		// 필요하다면 초기화
		setResourceManager(resourceManager);
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode){
		String line=null;
		int PROGADDR=0;   //os가 주는 프로그램의 시작 주소를 0으로 가정하여 놓는다.
		int CSADDR=0;
		int loc=0;
		try { //pass1
			FileReader filereader = null;
			try {
				filereader = new FileReader(objectCode);  //목적 파일을 읽기
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			BufferedReader bufReader = new BufferedReader(filereader); // 파일을 한줄씩 읽어들이기 위해 bufferedReader을 사용한다.
			while((line=bufReader.readLine())!=null) {
				switch(line.charAt(0)) {
					case 'H':  //Header record
						int csLen = Integer.parseInt(line.substring(13, 19),16);
						String progName = line.substring(1, 7).trim();
						rMgr.progName.add(progName);  //simulator에서 사용하기 위해 따로 프로그램 길이,이름,주소를 저장한다.
						rMgr.progLength.add(csLen);
						rMgr.progAddr.add(CSADDR);
						rMgr.symtabList.putSymbol(progName,CSADDR);
						CSADDR += csLen;  //다음 프로그램의 시작주소
						break;
					case 'D':  //Define record
						for(int i=1;i<line.length();i+=12) {  //모든 변수를 estab에 저장해 놓는다
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
			
			BufferedReader bufReader = new BufferedReader(filereader); // 파일을 한줄씩 읽어들이기 위해 bufferedReader을 사용한다.
			while((line=bufReader.readLine())!=null) {
				switch(line.charAt(0)) {
					case 'H':  //Header record
						CSADDR += csLen;   //각 프로그램의 시작주소를 저장한다.
						csLen = Integer.parseInt(line.substring(13, 19),16);
						break;
					case 'T':
						int index=9;
						int cnt = 0;
						char num=0;
						int numIndex=0;
						loc = CSADDR + Integer.parseInt(line.substring(1, 7),16); //각 텍스트 레코드의 정보를 참고해 시작주소를 정한다.
						while(index<line.length()) {
							char c = line.charAt(index++);
							int tmp =0;
							tmp = c-0x30; //아스키 코드를 일반 숫자로 바꾼다.
							if(tmp>9) tmp -= 0x7;
							if(cnt == 0) {  //한 바이트의 첫번째 숫자 
								num = (char) (num+tmp);
								num = (char) (num<<4);
								cnt++;
							}
							else {  //한 바이트의 두번째 숫자
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
						int plusV = rMgr.symtabList.search(sym);  //estab를 찾아 그 심볼의 주소값을 알아낸다.
						
						char[] preV = rMgr.getMemory(CSADDR+addr,mLen); //기존에 메모리에 있던 값
						int preInt = 0;
						preInt += preV[0];
						preInt = preInt << 8;
						preInt += preV[1];
						preInt = preInt << 8;
						preInt += preV[2];
						
						if(line.charAt(9)=='+')  //더하는 M record 일 경우
							preInt += plusV;
						else //빼는 M record 일 경우
							preInt -= plusV;
						
						preV[2] = (char)(preInt&0xFF);
						preInt = preInt >> 8;
						preV[1] = (char)(preInt & 0xFF);
						preInt = preInt >> 8;
						preV[0] = (char) (preInt &0xFF);
						rMgr.setMemory(CSADDR+addr, preV, 3); //계산 뒤 값을 다시 메모리에 올린다.

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
	
	

}
