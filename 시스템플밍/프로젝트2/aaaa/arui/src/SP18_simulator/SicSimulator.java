package SP18_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class SicSimulator {
	ResourceManager rMgr;
	int loc;
	List<String> logList;  //VisulaSimulator의 log를 위한 리스트
	List<String> instList; //VisulaSimulator의 instruction를 위한 리스트
	String targetAddr;  //VisulaSimulator의 targetAddress를 위한 리스트
	Map<Integer,Integer> instIndexMap; //주소가 key, instList의 index가 value인 맵
	int nowIndex;

	public SicSimulator(ResourceManager resourceManager) {
		// 필요하다면 초기화 과정 추가
		this.rMgr = resourceManager;
		logList = new ArrayList<String>();
		instList = new ArrayList<String>();
		instIndexMap = new HashMap<Integer,Integer>();  
	}

	/**
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행.
	 * 단, object code의 메모리 적재 및 해석은 SicLoader에서 수행하도록 한다. 
	 */
	public void load(File program) {
		/* 메모리 초기화, 레지스터 초기화 등*/
		rMgr.initializeResource();
	}
	
	public List<String> loadInstruction() {  //VisualSimulator의 inst 리스트를 완성하는 함수
		instIndexMap.put(loc, instList.size());
		char[] tmp = rMgr.getMemory(loc, 3); //현재 주소의 메모리를 받아온다.
		instList.add(charToString(tmp,3));
		loc+=3;
		int csNum = 0;  //현재 몇번째 컨트롤 섹션인지
		int i=0;
		while(true) {
			tmp = rMgr.getMemory(loc, 2);
			int opcode = tmp[0] & 0xFC;
			int e = tmp[1]&0x10;   //extend
			String op = Integer.toHexString(opcode).toUpperCase();
			String inst=null;
			instIndexMap.put(loc, instList.size());
			if(op.equals("B4")||op.equals("A0")||op.equals("B8")) {
				tmp = rMgr.getMemory(loc, 2); //현재 주소의 메모리를 받아온다.
				inst = charToString(tmp,2);
				loc+=2; //2형식일 경우
			}
			else if(e==16) {
				tmp = rMgr.getMemory(loc, 4); //현재 주소의 메모리를 받아온다.
				inst = charToString(tmp,4);
				loc+=4;  //4형식일 경우
			}
			else {
				tmp = rMgr.getMemory(loc, 3); //현재 주소의 메모리를 받아온다.
				inst = charToString(tmp,3);
				while(inst.length()<6) inst+="0";
				while(inst.length()>6) inst = inst.substring(0,inst.length()-1);
				loc+=3;  //3형식일 경우
			}
			if(inst.equals("4F0000")) {
				if(csNum==2) {  //맨 마지막 명령어이다
					instList.add(inst);
					break;
				}
				else loc= rMgr.progAddr.get(++csNum);
			}
			if(inst.equals("000000")) { //instruction이 없는 빈 공간이므로 넘어간다.
				loc= rMgr.progAddr.get(++csNum);
				continue;
			}

			instList.add(inst);
		}
		loc = 0;
		return instList;
	}

	/**
	 * 1개의 instruction이 수행된 모습을 보인다. 
	 */
	public void oneStep() {
		char[] tmp = rMgr.getMemory(loc, 2);
		int opcode = tmp[0] & 0xFC;
		int e = tmp[1]&0x10;   //extend
		int im = tmp[0]&0x01;  //immediate
		int indirect = tmp[0]&0x02;  //indirect
		String op = Integer.toHexString(opcode).toUpperCase();
		nowIndex = instIndexMap.get(loc);
		
		if(op.equals("14")) { //STL 
			addLog("STL");
			tmp = rMgr.getMemory(loc, 3); //현재 주소의 메모리를 받아온다.
			loc += 3;  //pc값을 올려준다.
			int target = getAddress(tmp,3);  //타겟 주소를 계산해 받아온다.
			int regV = rMgr.getRegister(2);  //L레지스터 값
			char[] data = rMgr.intToChar(regV);
			rMgr.setMemory(loc+target, data, 3);  //해당 메모리에 L레지스터 값을 올린다.
			targetAddr = Integer.toHexString(loc+target); //Simulator을 위한 targetAddress값 변화
		}
		else if(op.equals("48")) { //JSUB
			addLog("+JSUB");
			tmp = rMgr.getMemory(loc, 4);
			int target = getAddress(tmp,4);
			loc += 4;
			rMgr.setRegister(2, loc);  //돌아올 수 있도록 현재 pc값을 L레지스터에 저장
			loc = target;  //target address로 점프한다.
			targetAddr = Integer.toHexString(loc);
		}
		else if(op.equals("B4")) {  //CLEAR
			addLog("CLEAR");
			tmp = rMgr.getMemory(loc, 2);
			int target = getAddress(tmp,2);
			target = target & 0xF0;   //몇번째 레지스터인지 뽑아낸다.
			target = target >> 4;
			rMgr.setRegister(target, 0);
			loc = loc+2;
			targetAddr = "x";  //target Address는 존재하지 않는다.
		}
		else if(op.equals("74")) {  //LDT
			if(e == 0) {  //3형식
				addLog("LDT");
				tmp = rMgr.getMemory(loc, 3);
				loc += 3;
				int target = getAddress(tmp,3);
				tmp = rMgr.getMemory(loc+target, 3);
				int mem = rMgr.byteToInt(tmp);
				rMgr.setRegister(5, mem);
				targetAddr = Integer.toHexString(loc+target);
			}
			else {  //4형식
				addLog("+LDT");
				tmp = rMgr.getMemory(loc, 4);
				int target = getAddress(tmp,4);
				loc += 4;
				tmp = rMgr.getMemory(target, 3);
				int mem = rMgr.byteToInt(tmp);
				rMgr.setRegister(5, mem);
				targetAddr = Integer.toHexString(target);
			}
		}
		else if(op.equals("E0")) { //TD
			addLog("TD");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);
			loc += 3;
			tmp = rMgr.getMemory(loc+target, 1);  //어떤 device를 사용할 건지 받아오기
			String dev = ""+(int)tmp[0];
			if(dev.equals("241")) //16진수로 F1 
				dev = "F1";  //입력
			else if(dev.equals("5")) 
				dev = "05";  //출력
			rMgr.testDevice(dev);  
			targetAddr = Integer.toHexString(loc+target);
		}
		else if(op.equals("30")) { //JEQ
			addLog("JEQ");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);
			if(rMgr.getRegister(9)==0) {  //SW reg가 0이면 비교하는 둘이 같을 경우이므로 jump한다.  
				loc = target+loc;
			}
			loc += 3;
			targetAddr = Integer.toHexString(loc+target);
		}
		else if(op.equals("D8")) {  //RD
			addLog("RD");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);
			loc+=3;
			tmp = rMgr.getMemory(loc+target, 1);  //어떤 인풋디바이스를 사용하는지
			String dev = ""+(int)tmp[0];
			if(dev.equals("241")) dev = "F1";
			rMgr.setRegister(0, rMgr.readDevice(dev, 1));  //한바이트씩 읽어서 A 레지스터에 집어넣는다.
			targetAddr = Integer.toHexString(loc+target);
		}
		else if(op.equals("A0")) {  //COMPR
			addLog("COMPR");
			tmp = rMgr.getMemory(loc, 2);
			int target = getAddress(tmp,2);
			int reg1 = target & 0xF0;  //비교하려는 두 레지스터를 뽑아낸다.
			int reg2 = target & 0xF;
			loc+=2;
			if(rMgr.getRegister(reg1) == 65535)  //rMgr.getRegister(reg2))  
				rMgr.setRegister(9, 0);  //같으면 CC reg를 0로 만들어준다.
			//보통 텍스트 파일에서 파일의 끝은 -1이어서 책처럼 0이 들어있는 S와 비교해서는 파일의 끝을 알 수가 없어 다르게 구현 
			else
				rMgr.setRegister(9, 1);
			targetAddr = "x";
		}
		else if(op.equals("54")) {  //+STCH
			addLog("+STCH");
			tmp = rMgr.getMemory(loc, 4);
			int target = getAddress(tmp,4);
			loc += 4;
			char[] data = new char[1];
			int regData = rMgr.getRegister(0);  //A레지스터의 값을 받아온다.
			data[0] = (char)regData;
			rMgr.setMemory(target+rMgr.getRegister(1), data, 1);   //x 레지스터 값 참고하여 A레지스터 데이터를 x번째 바이트에 저장
			targetAddr = Integer.toHexString(target+rMgr.getRegister(1));
		}
		else if(op.equals("B8")) {  //TIXR
			addLog("TIXR");
			tmp = rMgr.getMemory(loc, 2);
			int target = getAddress(tmp,2);
			loc +=2;
			target = (target & 0xF0) / 16;  //target 레지스터를 구한다.
			rMgr.setRegister(1,rMgr.getRegister(1)+1);  //x레지스터의 값을 1 증가시킨다.
			if(rMgr.getRegister(1)<rMgr.getRegister(target)) {  //X 레지스터 값과 target 레지스터 값을 비교한다.
				rMgr.setRegister(9, -1);  //SW가 less than이라는 표시
			}
			else rMgr.setRegister(9, 0);
		}
		else if(op.equals("38")) {  //JLT
			addLog("JLT");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);
			loc+=3;
			if(rMgr.getRegister(9)==-1) {  //SW를 참고해 less일 경우 jump한다.
				loc = loc + target;
				targetAddr = Integer.toHexString(loc);
			}
			else targetAddr = Integer.toHexString(loc+target);
		}
		else if(op.equals("10")) {  //STX
			addLog("+STX");
			tmp = rMgr.getMemory(loc, 4);
			int target = getAddress(tmp,4);
			loc += 4;
			tmp = rMgr.intToChar(rMgr.getRegister(1));  //x레지스터 값을 가져온다.
			rMgr.setMemory(target, tmp, 3);
			targetAddr = Integer.toHexString(target);
		}
		else if(op.equals("4C")) {  //RSUB
			addLog("RSUB");
			tmp = rMgr.getMemory(loc, 3);
			loc = rMgr.getRegister(2);  //L레지스터를 보고 jump를 호출한 주소로 돌아간다.
			targetAddr = Integer.toHexString(rMgr.getRegister(2));
		}
		else if(op.equals("0")) {  //LDA
			addLog("LDA");
			if(im == 1 && indirect == 0) {  //LDA immediate 일 경우
				tmp = rMgr.getMemory(loc, 3);
				int target = getAddress(tmp,3);  //immediate값을 가져온다.
				loc+=3;
				rMgr.setRegister(0, target);  //immediate값을 A register에 저장한ㄷ,
				targetAddr = Integer.toHexString(target+loc);
			}
			else {  //기본 LDA일 경우
				tmp = rMgr.getMemory(loc, 3);
				int target = getAddress(tmp,3);
				loc+=3;
				tmp = rMgr.getMemory(loc+target, 3);
				int mem = rMgr.byteToInt(tmp);
				rMgr.setRegister(0, mem);
				targetAddr = Integer.toHexString(target);
			}
		}
		else if(op.equals("28")) {  //COMP
			addLog("COMP");
			tmp = rMgr.getMemory(loc, 3);
			if(im==1&&indirect==0) {  //immediate COMP  
				int imNum = tmp[1];
				imNum = tmp[1]<<8;
				imNum += tmp[2];
				if(rMgr.getRegister(0)==imNum)  //immediate 숫자와 A reg값이 같을 경우
					rMgr.setRegister(9, 0); //SW reg를 0으로 만든다.
				else 
					rMgr.setRegister(9, 1);
				loc+=3;
			}
			else {  //기본 COMP
			int target = getAddress(tmp,3);
			loc+=3;
			tmp = rMgr.getMemory(loc+target, 3);  //비교할 메모리 값을 가져오기
			int mem = rMgr.byteToInt(tmp); 
			if(rMgr.getRegister(0)==mem)
				rMgr.setRegister(9, 0);
			else 
				rMgr.setRegister(9, 1);
			targetAddr = "x";
			}
		}
		else if(op.equals("50")) {  //LDCH
			addLog("+LDCH");
			tmp = rMgr.getMemory(loc, 4);
			int target = getAddress(tmp,4);
			loc += 4;
			tmp = rMgr.getMemory(target+rMgr.getRegister(1), 1);  //메모리에서 타겟주소의 한 바이트를 가져오기
			int mem = rMgr.byteToInt(tmp);
			rMgr.setRegister(0, mem);  //가져온 한 바이트를 A reg에 집어넣기
			targetAddr = Integer.toHexString(target);
		}
		else if(op.equals("DC")) {  //WD
			addLog("WD");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);
			loc+=3;
			tmp = rMgr.getMemory(loc+target, 1);  //무슨 output device인지 메모리에서 가져오기
			String dev = ""+(int)tmp[0];
			if(dev.equals("5")) dev = "05";
			rMgr.writeDevice(dev, (char)rMgr.getRegister(0), 1);  //한 바이트를 읽어서 A에 집어넣기
			targetAddr = Integer.toHexString(target+loc);
		}
		else if(op.equals("3C")) {  //J
			addLog("J");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);  
			loc+=3;
			if(indirect==2&&im==0) {  //indirect Jump일 경우
				loc = target;
				targetAddr = ""+target;
			}
			else{  //기본 Jump일 경우
				loc+=target;
				targetAddr = Integer.toHexString(target+loc);
			}
		}
		else if(op.equals("C")) {  //STA
			addLog("STA");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);
			loc+=3;
			char[] data = new char[3];
			int regV = rMgr.getRegister(0);  // A레지스터의 값을 가져온다.
			data = rMgr.intToChar(regV);
			rMgr.setMemory(loc+target, data, 3);  //그 값을 메모리에 저장한다.
			targetAddr = Integer.toHexString(target+loc);
		}
		rMgr.setRegister(8, loc);  //PC값을 매번 변화시킨다.
	}
	
	String charToString(char[] data, int num) {  //char배열을 스트링으로 만든다. instList에 명령어들을 넣을때 사용한다.
		String result = ""; 
 		for(int i = 0; i < num; i++){ 
 			int temp = 0; 
			temp = data[i]; 
 			result += Integer.toHexString(temp).toUpperCase(); 
 		} 
 		if(result.length()%2 == 1 && result.length() <= 6){ 
 			result = result.replace("0", "00"); 
 		} 
 		result = result.replace("FFFFFF", ""); 
 		return result.toUpperCase(); 

	}
	
	/**
	 * 남은 모든 instruction이 수행된 모습을 보인다.
	 */
	public void allStep() {
		while(true) {
			oneStep();
			if(rMgr.getRegister(8)==0) break;
		}
	}
	
	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
	 */
	public void addLog(String log) {
		logList.add(log);
	}	
	
	public int getAddress(char[] data,int type){  //주소를 간단하게 찾기 위한 함수
		int result = 0;
		if(type == 2){ //2형식
			result += data[1];
		}
		else if(type == 3){ //3형식
			if((data[1]&0xF)==0xF) {   //타겟 주소 상대값이 마이너스인 경우
				result += data[1];
				result = result << 8;
				result += data[2];
				result = result &0x0FFF;	 
				result = (int)((char)result)|0xFFFFF000;
			}
			else { //타겟 주소 상대값이 플러스인 경우
				result += data[1];
				result = result << 8;
				result += data[2];
				result = result &0x0FFF;
			}
		}
		else if(type ==4){  //4형식
			result += data[1];
			result = result << 8;
			result += data[2];
			result = result << 8;
			result += data[3];
			result = result &0x0FFFFF;
		}
		return result;
	}

}
