package SP18_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ����
 * ResourceManager�� �����Ͽ� �۾��� �����Ѵ�.  
 * 
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� ������ ��.<br>
 *  2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class SicSimulator {
	ResourceManager rMgr;
	int loc;
	List<String> logList;  //VisulaSimulator�� log�� ���� ����Ʈ
	List<String> instList; //VisulaSimulator�� instruction�� ���� ����Ʈ
	String targetAddr;  //VisulaSimulator�� targetAddress�� ���� ����Ʈ
	Map<Integer,Integer> instIndexMap; //�ּҰ� key, instList�� index�� value�� ��
	int nowIndex;

	public SicSimulator(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ ���� �߰�
		this.rMgr = resourceManager;
		logList = new ArrayList<String>();
		instList = new ArrayList<String>();
		instIndexMap = new HashMap<Integer,Integer>();  
	}

	/**
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����.
	 * ��, object code�� �޸� ���� �� �ؼ��� SicLoader���� �����ϵ��� �Ѵ�. 
	 */
	public void load(File program) {
		/* �޸� �ʱ�ȭ, �������� �ʱ�ȭ ��*/
		rMgr.initializeResource();
	}
	
	public List<String> loadInstruction() {  //VisualSimulator�� inst ����Ʈ�� �ϼ��ϴ� �Լ�
		instIndexMap.put(loc, instList.size());
		char[] tmp = rMgr.getMemory(loc, 3); //���� �ּ��� �޸𸮸� �޾ƿ´�.
		instList.add(charToString(tmp,3));
		loc+=3;
		int csNum = 0;  //���� ���° ��Ʈ�� ��������
		int i=0;
		while(true) {
			tmp = rMgr.getMemory(loc, 2);
			int opcode = tmp[0] & 0xFC;
			int e = tmp[1]&0x10;   //extend
			String op = Integer.toHexString(opcode).toUpperCase();
			String inst=null;
			instIndexMap.put(loc, instList.size());
			if(op.equals("B4")||op.equals("A0")||op.equals("B8")) {
				tmp = rMgr.getMemory(loc, 2); //���� �ּ��� �޸𸮸� �޾ƿ´�.
				inst = charToString(tmp,2);
				loc+=2; //2������ ���
			}
			else if(e==16) {
				tmp = rMgr.getMemory(loc, 4); //���� �ּ��� �޸𸮸� �޾ƿ´�.
				inst = charToString(tmp,4);
				loc+=4;  //4������ ���
			}
			else {
				tmp = rMgr.getMemory(loc, 3); //���� �ּ��� �޸𸮸� �޾ƿ´�.
				inst = charToString(tmp,3);
				while(inst.length()<6) inst+="0";
				while(inst.length()>6) inst = inst.substring(0,inst.length()-1);
				loc+=3;  //3������ ���
			}
			if(inst.equals("4F0000")) {
				if(csNum==2) {  //�� ������ ��ɾ��̴�
					instList.add(inst);
					break;
				}
				else loc= rMgr.progAddr.get(++csNum);
			}
			if(inst.equals("000000")) { //instruction�� ���� �� �����̹Ƿ� �Ѿ��.
				loc= rMgr.progAddr.get(++csNum);
				continue;
			}

			instList.add(inst);
		}
		loc = 0;
		return instList;
	}

	/**
	 * 1���� instruction�� ����� ����� ���δ�. 
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
			tmp = rMgr.getMemory(loc, 3); //���� �ּ��� �޸𸮸� �޾ƿ´�.
			loc += 3;  //pc���� �÷��ش�.
			int target = getAddress(tmp,3);  //Ÿ�� �ּҸ� ����� �޾ƿ´�.
			int regV = rMgr.getRegister(2);  //L�������� ��
			char[] data = rMgr.intToChar(regV);
			rMgr.setMemory(loc+target, data, 3);  //�ش� �޸𸮿� L�������� ���� �ø���.
			targetAddr = Integer.toHexString(loc+target); //Simulator�� ���� targetAddress�� ��ȭ
		}
		else if(op.equals("48")) { //JSUB
			addLog("+JSUB");
			tmp = rMgr.getMemory(loc, 4);
			int target = getAddress(tmp,4);
			loc += 4;
			rMgr.setRegister(2, loc);  //���ƿ� �� �ֵ��� ���� pc���� L�������Ϳ� ����
			loc = target;  //target address�� �����Ѵ�.
			targetAddr = Integer.toHexString(loc);
		}
		else if(op.equals("B4")) {  //CLEAR
			addLog("CLEAR");
			tmp = rMgr.getMemory(loc, 2);
			int target = getAddress(tmp,2);
			target = target & 0xF0;   //���° ������������ �̾Ƴ���.
			target = target >> 4;
			rMgr.setRegister(target, 0);
			loc = loc+2;
			targetAddr = "x";  //target Address�� �������� �ʴ´�.
		}
		else if(op.equals("74")) {  //LDT
			if(e == 0) {  //3����
				addLog("LDT");
				tmp = rMgr.getMemory(loc, 3);
				loc += 3;
				int target = getAddress(tmp,3);
				tmp = rMgr.getMemory(loc+target, 3);
				int mem = rMgr.byteToInt(tmp);
				rMgr.setRegister(5, mem);
				targetAddr = Integer.toHexString(loc+target);
			}
			else {  //4����
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
			tmp = rMgr.getMemory(loc+target, 1);  //� device�� ����� ���� �޾ƿ���
			String dev = ""+(int)tmp[0];
			if(dev.equals("241")) //16������ F1 
				dev = "F1";  //�Է�
			else if(dev.equals("5")) 
				dev = "05";  //���
			rMgr.testDevice(dev);  
			targetAddr = Integer.toHexString(loc+target);
		}
		else if(op.equals("30")) { //JEQ
			addLog("JEQ");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);
			if(rMgr.getRegister(9)==0) {  //SW reg�� 0�̸� ���ϴ� ���� ���� ����̹Ƿ� jump�Ѵ�.  
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
			tmp = rMgr.getMemory(loc+target, 1);  //� ��ǲ����̽��� ����ϴ���
			String dev = ""+(int)tmp[0];
			if(dev.equals("241")) dev = "F1";
			rMgr.setRegister(0, rMgr.readDevice(dev, 1));  //�ѹ���Ʈ�� �о A �������Ϳ� ����ִ´�.
			targetAddr = Integer.toHexString(loc+target);
		}
		else if(op.equals("A0")) {  //COMPR
			addLog("COMPR");
			tmp = rMgr.getMemory(loc, 2);
			int target = getAddress(tmp,2);
			int reg1 = target & 0xF0;  //���Ϸ��� �� �������͸� �̾Ƴ���.
			int reg2 = target & 0xF;
			loc+=2;
			if(rMgr.getRegister(reg1) == 65535)  //rMgr.getRegister(reg2))  
				rMgr.setRegister(9, 0);  //������ CC reg�� 0�� ������ش�.
			//���� �ؽ�Ʈ ���Ͽ��� ������ ���� -1�̾ åó�� 0�� ����ִ� S�� ���ؼ��� ������ ���� �� ���� ���� �ٸ��� ���� 
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
			int regData = rMgr.getRegister(0);  //A���������� ���� �޾ƿ´�.
			data[0] = (char)regData;
			rMgr.setMemory(target+rMgr.getRegister(1), data, 1);   //x �������� �� �����Ͽ� A�������� �����͸� x��° ����Ʈ�� ����
			targetAddr = Integer.toHexString(target+rMgr.getRegister(1));
		}
		else if(op.equals("B8")) {  //TIXR
			addLog("TIXR");
			tmp = rMgr.getMemory(loc, 2);
			int target = getAddress(tmp,2);
			loc +=2;
			target = (target & 0xF0) / 16;  //target �������͸� ���Ѵ�.
			rMgr.setRegister(1,rMgr.getRegister(1)+1);  //x���������� ���� 1 ������Ų��.
			if(rMgr.getRegister(1)<rMgr.getRegister(target)) {  //X �������� ���� target �������� ���� ���Ѵ�.
				rMgr.setRegister(9, -1);  //SW�� less than�̶�� ǥ��
			}
			else rMgr.setRegister(9, 0);
		}
		else if(op.equals("38")) {  //JLT
			addLog("JLT");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);
			loc+=3;
			if(rMgr.getRegister(9)==-1) {  //SW�� ������ less�� ��� jump�Ѵ�.
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
			tmp = rMgr.intToChar(rMgr.getRegister(1));  //x�������� ���� �����´�.
			rMgr.setMemory(target, tmp, 3);
			targetAddr = Integer.toHexString(target);
		}
		else if(op.equals("4C")) {  //RSUB
			addLog("RSUB");
			tmp = rMgr.getMemory(loc, 3);
			loc = rMgr.getRegister(2);  //L�������͸� ���� jump�� ȣ���� �ּҷ� ���ư���.
			targetAddr = Integer.toHexString(rMgr.getRegister(2));
		}
		else if(op.equals("0")) {  //LDA
			addLog("LDA");
			if(im == 1 && indirect == 0) {  //LDA immediate �� ���
				tmp = rMgr.getMemory(loc, 3);
				int target = getAddress(tmp,3);  //immediate���� �����´�.
				loc+=3;
				rMgr.setRegister(0, target);  //immediate���� A register�� �����Ѥ�,
				targetAddr = Integer.toHexString(target+loc);
			}
			else {  //�⺻ LDA�� ���
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
				if(rMgr.getRegister(0)==imNum)  //immediate ���ڿ� A reg���� ���� ���
					rMgr.setRegister(9, 0); //SW reg�� 0���� �����.
				else 
					rMgr.setRegister(9, 1);
				loc+=3;
			}
			else {  //�⺻ COMP
			int target = getAddress(tmp,3);
			loc+=3;
			tmp = rMgr.getMemory(loc+target, 3);  //���� �޸� ���� ��������
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
			tmp = rMgr.getMemory(target+rMgr.getRegister(1), 1);  //�޸𸮿��� Ÿ���ּ��� �� ����Ʈ�� ��������
			int mem = rMgr.byteToInt(tmp);
			rMgr.setRegister(0, mem);  //������ �� ����Ʈ�� A reg�� ����ֱ�
			targetAddr = Integer.toHexString(target);
		}
		else if(op.equals("DC")) {  //WD
			addLog("WD");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);
			loc+=3;
			tmp = rMgr.getMemory(loc+target, 1);  //���� output device���� �޸𸮿��� ��������
			String dev = ""+(int)tmp[0];
			if(dev.equals("5")) dev = "05";
			rMgr.writeDevice(dev, (char)rMgr.getRegister(0), 1);  //�� ����Ʈ�� �о A�� ����ֱ�
			targetAddr = Integer.toHexString(target+loc);
		}
		else if(op.equals("3C")) {  //J
			addLog("J");
			tmp = rMgr.getMemory(loc, 3);
			int target = getAddress(tmp,3);  
			loc+=3;
			if(indirect==2&&im==0) {  //indirect Jump�� ���
				loc = target;
				targetAddr = ""+target;
			}
			else{  //�⺻ Jump�� ���
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
			int regV = rMgr.getRegister(0);  // A���������� ���� �����´�.
			data = rMgr.intToChar(regV);
			rMgr.setMemory(loc+target, data, 3);  //�� ���� �޸𸮿� �����Ѵ�.
			targetAddr = Integer.toHexString(target+loc);
		}
		rMgr.setRegister(8, loc);  //PC���� �Ź� ��ȭ��Ų��.
	}
	
	String charToString(char[] data, int num) {  //char�迭�� ��Ʈ������ �����. instList�� ��ɾ���� ������ ����Ѵ�.
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
	 * ���� ��� instruction�� ����� ����� ���δ�.
	 */
	public void allStep() {
		while(true) {
			oneStep();
			if(rMgr.getRegister(8)==0) break;
		}
	}
	
	/**
	 * �� �ܰ踦 ������ �� ���� ���õ� ����� ���⵵�� �Ѵ�.
	 */
	public void addLog(String log) {
		logList.add(log);
	}	
	
	public int getAddress(char[] data,int type){  //�ּҸ� �����ϰ� ã�� ���� �Լ�
		int result = 0;
		if(type == 2){ //2����
			result += data[1];
		}
		else if(type == 3){ //3����
			if((data[1]&0xF)==0xF) {   //Ÿ�� �ּ� ��밪�� ���̳ʽ��� ���
				result += data[1];
				result = result << 8;
				result += data[2];
				result = result &0x0FFF;	 
				result = (int)((char)result)|0xFFFFF000;
			}
			else { //Ÿ�� �ּ� ��밪�� �÷����� ���
				result += data[1];
				result = result << 8;
				result += data[2];
				result = result &0x0FFF;
			}
		}
		else if(type ==4){  //4����
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
