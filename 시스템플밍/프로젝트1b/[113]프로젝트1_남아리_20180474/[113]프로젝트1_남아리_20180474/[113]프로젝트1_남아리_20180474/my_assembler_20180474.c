/*
 * ȭ�ϸ� : my_assembler_20180474.c
 * ��  �� : �� ���α׷��� SIC/XE �ӽ��� ���� ������ Assembler ���α׷��� ���η�ƾ����,
 * �Էµ� ������ �ڵ� ��, ��ɾ �ش��ϴ� OPCODE�� ã�� ����Ѵ�.
 * ���� ������ ���Ǵ� ���ڿ� "00000000"���� �ڽ��� �й��� �����Ѵ�.
 */

 /*
  * ���α׷��� ����� �����Ѵ�.
  */

#define _CRT_SECURE_NO_WARNINGS
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>


#include "my_assembler_20180474.h"

/* -----------------------------------------------------------------------------------
* ���� : ����ڷ� ���� ����� ������ �޾Ƽ� ��ɾ��� OPCODE�� ã�� ����Ѵ�.
* �Ű� : ���� ����, ����� ����
* ��ȯ : ���� = 0, ���� = < 0
* ���� : ���� ����� ���α׷��� ����Ʈ ������ �����ϴ� ��ƾ�� ������ �ʾҴ�.
*		   ���� �߰������� �������� �ʴ´�.
* -----------------------------------------------------------------------------------
*/
int main(int argc, char *argv[]) {

	label_num = 0;
	if (init_my_assembler() < 0) {
		printf("init_my_assembler: ���α׷� �ʱ�ȭ�� ���� �߽��ϴ�.\n");
		return -1;
	}
	if (assem_pass1() < 0) {
		printf("assem_pass1: �н�1 �������� �����Ͽ����ϴ�.  \n");
		return -1;
	}
	if (assem_pass2() < 0) {
		printf(" assem_pass2: �н�2 �������� �����Ͽ����ϴ�.  \n");
		return -1;
	}

	output_file = "output_20180474.txt";
	symtab_file = "symtab_20180474.txt";
	literal_file = "literal_20180474.txt";

	make_symtab_output(symtab_file);
	make_literal_output(literal_file);
	make_objectcode_output(output_file) ;

	//make_opcode_output(output_file);

}


/* -----------------------------------------------------------------------------------
* ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�.
* �Ű� : ����
* ��ȯ : �������� = 0 , ���� �߻� = -1
* ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ�
*		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
*		   �����Ͽ���.
* -----------------------------------------------------------------------------------
*/

int init_my_assembler(void) {

	int result;

	if ((result = init_inst_file("inst.data")) < 0)
		return -1;
	if ((result = init_input_file("input.txt")) < 0)
		return -1;

	return result;
}


/* -----------------------------------------------------------------------------------
* ���� : �ӽ��� ���� ��� �ڵ��� ������ �о� ���� ��� ���̺�(inst_table)��
*        �����ϴ� �Լ��̴�.
* �Ű� : ���� ��� ����
* ��ȯ : �������� = 0 , ���� < 0
* ���� : ���� ������� ������ ������ ����.
*
*	===============================================================================
*		   | �̸� | ���� | ���� �ڵ� | ���۷����� ���� | NULL|
*	===============================================================================
*
* -----------------------------------------------------------------------------------
*/

int init_inst_file(char* inst_file) {
	FILE* file = NULL;
	int count = 0;//��ɾ ���� ���� ���̺� ������ �� ����ϴ� �ε���
	char c; // ���ڸ� �ϳ��� �޾ƿ� fgetc
	char tmp[100]; //c�� �ѱ��ھ� �а� ���⿡ ���� ����
	int idx = 0; // �ѱ��ھ� �о�� �� ����ϴ� �ε���

	file = fopen(inst_file, "r");

	if (file == NULL) {
		printf("inst_file�� ���µ��� �����Ͽ����ϴ�.\n");
		return -1;
	}

	while ((c = fgetc(file)) != EOF) { //�ѱ��ھ� �о��
		if (c != '\n') 
			tmp[idx++] = c;
		
		else { //��ɾ �� �о�����
			tmp[idx] = '\0';//���ڿ����� ó��
			//strtok ��ū������ ����
			strcpy(inst_table[count].name, strtok(tmp, "\t")); //ù��° �Ǳ���(��ɾ�)�� name�� ����
			inst_table[count].type_format = atoi(strtok(NULL, "\t")); //ascii to integer, format
			inst_table[count].opcode = strtol(strtok(NULL, "\t"), NULL, 16); //���� ���ڿ��� 16������ ǥ��, opcode
			inst_table[count].operands = atoi(strtok(NULL, "\t")); // ���۷��尹��
	
			count++;
			inst_index++; //�Ѱ���
			idx = 0;
		}
	}
	return 0;
}

/* ----------------------------------------------------------------------------------
 * ���� : ����� �� �ҽ��ڵ带 �о� �ҽ��ڵ� ���̺�(input_data)�� �����ϴ� �Լ��̴�.
 * �Ű� : ������� �ҽ����ϸ�
 * ��ȯ : �������� = 0 , ���� < 0
 * ���� : ���δ����� �����Ѵ�.
 *
 * ----------------------------------------------------------------------------------
 */


int init_input_file(char* input_file) {
	FILE* file = NULL;
	char c; // ������ ����
	char tmp[200]; // �ӽÿ�
	int instruction_cnt = 0;// ��ɾ� ����
	int line_instruction_cnt = 0; // ���ٿ��ִ� ���ڼ�

	file = fopen(input_file, "r");
	if (file == NULL) {
		printf("input_file�� ���µ��� �־� �����ϼ̽��ϴ�.\n");
		return -1;
	}

	while ((c = fgetc(file)) != EOF) { //������ ���� EOF����
		if (c == '\n') { //������ ������ 
			tmp[line_instruction_cnt] = '\0'; // ������ null���� �־ ����
			line_instruction_cnt = 0; //�Ʊ� ������ i���� ����
			input_data[instruction_cnt] = (char*)malloc(strlen(tmp));
			strcpy(input_data[instruction_cnt], tmp); //������ ������ null���� �־����Ƿ� ��ɾ� �� ���� ��

			instruction_cnt++;
		}
		//���ڿ� ����
		else tmp[line_instruction_cnt++] = c;
	}
	line_num = instruction_cnt; //������Ͽ� static int�� ���ǵ�

	fclose(file);
	return 0;
}


/* -----------------------------------------------------------------------------------
* ���� : �ҽ� �ڵ带 �о�� ��ū������ �м��ϰ� ��ū ���̺��� �ۼ��ϴ� �Լ��̴�.
*        �н� 1�� ���� ȣ��ȴ�.
* �Ű� : �ҽ��ڵ��� ���ι�ȣ
* ��ȯ : �������� = 0 , ���� < 0
* ���� : my_assembler ���α׷������� ���δ����� ��ū �� ������Ʈ ������ �ϰ� �ִ�.
* -----------------------------------------------------------------------------------
*/

int token_parsing(int index) {

	//����
	if (index < 0)
		return -1;

	//���� index���ٰ� ���ݱ��� �߰��� ltorg line ������ ���ؾ� ���� ��ġ�� ������ index�� ��
	symbol_token[index + ltorg_line_sum].extents = extents;

	if (input_data[index][0] == '.') { //index������ ù��° ���ڰ� �ּ��� ��
		symbol_token[index + ltorg_line_sum].label = ".";
		symbol_token[index + ltorg_line_sum].operator1 = NULL;
		symbol_token[index + ltorg_line_sum].operand[0] = NULL;
		symbol_token[index + ltorg_line_sum].operand[1] = NULL;
		symbol_token[index + ltorg_line_sum].comment = "\0";
		return 0;
	}
	else if (input_data[index][0] == '\t' || input_data[index][0] == ' ') { //���� ���� ��
		char* tmp;
		char* tmp_data = (char*)malloc(strlen(input_data[index]) + 1); // �ӽÿ����� ������ �Ҵ��Ѵ�.
		char* tmp_token = NULL; // �ӽÿ�
		char* tmp_operand = NULL;

		strcpy(tmp_data, input_data[index]); //�ӽÿ뿡�� ���
		symbol_token[index + ltorg_line_sum].label = NULL;
		tmp_token = strtok(tmp_data, " \t");

		//ltorg_line_sum�� liter_sum�� ����� ����
		symbol_token[index + ltorg_line_sum].operator1 = (char*)malloc(strlen(tmp_token) + 1);// �ӽø�ŭ �����Ҵ�

		strcpy(symbol_token[index + ltorg_line_sum].operator1, tmp_token); // �ӽÿ��� �����Ѵ�.

		tmp_token = strtok(NULL, "\n"); // ���۷����͸� ���� ������ ���ڿ��� �״�� ���

		if (tmp_token == NULL || tmp_token[0] == '\t' || tmp_token[0] == ' ') { //���۷��尡 ���� ��
			if (strcmp(symbol_token[index + ltorg_line_sum].operator1, "LTORG") == 0) { //operator1 == LTORG �� �� ó��
				//LTORG�� ������ ���� ��� literal�� ������
				for (int i = 0; i < liter_sum; i++) { //literal table �ȿ� �ִ� �� literal�� ������ŭ �ݺ�
					if (liter_tab[i].extents == symbol_token[index + ltorg_line_sum].extents) { //�ش� ������ �����ϸ�
						symbol_token[index + (++ltorg_line_sum)].operator1 = "*"; //LTORG ���� �ٿ� *ǥ �ְ�
						symbol_token[index + ltorg_line_sum].operand[0] = (char*)malloc(strlen(liter_tab[i].literal) + 1);
						strcpy(symbol_token[index + ltorg_line_sum].operand[0], liter_tab[i].literal); //���۷��� �ڸ��� literal ����
						symbol_token[index + ltorg_line_sum].extents = extents;
					}
				}
			}

			if (tmp_token != NULL) { //���۷���� ���µ� �ڿ� �ʿ���� ������ �־ tmp�� null�� �ƴ� ��
				char* tmp2_token = (char*)malloc(strlen(tmp_token) + 1);
				strcpy(tmp2_token, tmp_token);
				symbol_token[index + ltorg_line_sum].operand[0] = NULL;
				symbol_token[index + ltorg_line_sum].operand[1] = NULL;
				symbol_token[index + ltorg_line_sum].comment = strtok(tmp2_token, "\t"); //�ڿ� �پ��ִ� ������ comment�� ����
				return 0;
			}
		}
		else { //���۷��尡 ���࿡ �����Ѵٸ�.
			tmp_operand = strtok(tmp_token, "\t"); //�޾Ƶ��� ������ ������ ������(�ڿ� ���ʿ��� ���� ��������)
			tmp = strtok(tmp_operand, ",");//���۷��尡 �ϳ��� �ִ°� �ƴϹǷ� �� ��ǥ�� ���۷��带 ������
			symbol_token[index + ltorg_line_sum].operand[0] = (char*)malloc(strlen(tmp) + 1);
			strcpy(symbol_token[index + ltorg_line_sum].operand[0], tmp); // ù ��° ���۷���

			if (symbol_token[index + ltorg_line_sum].operand[0][0] == '=') { // ���۷����� ù��° ���ڰ� =�� ��. =C'EOF' �̷���. ���ͷ� ���̺� ������ ���ش�.
				if (search_placed_literal(symbol_token[index + ltorg_line_sum].operand[0], extents) != 0) {
					liter_tab[liter_sum].literal = (char*)malloc(strlen(symbol_token[index + ltorg_line_sum].operand[0])); //���� �Ҵ�
					strcpy(liter_tab[liter_sum].literal, symbol_token[index + ltorg_line_sum].operand[0]); //���ͷ� ���̺� ����
					liter_tab[liter_sum++].extents = extents; //�ش� ������ȣ update
				}
			}
			tmp = strtok(NULL, ","); //���� ���� �״�� ��� �� �ٽ� ��ǥ�� ����. �׸��� �װ� tmp�� ����
			if (tmp != NULL) { //���۷��尡 2�����
				symbol_token[index + ltorg_line_sum].operand[1] = (char*)malloc(strlen(tmp) + 1);
				strcpy(symbol_token[index + ltorg_line_sum].operand[1], tmp);
				tmp = strtok(NULL, ",");// �� ���� �ݺ�
				if (tmp != NULL) { //���۷��尡 3�����
					symbol_token[index + ltorg_line_sum].operand[2] = (char*)malloc(strlen(tmp) + 1);
					strcpy(symbol_token[index + ltorg_line_sum].operand[2], tmp);
				}
			}
			else if (strcmp(symbol_token[index + ltorg_line_sum].operator1, "END") == 0) {  //LTORG�� ������ ���� ���ͷ��� �������� ó��
				for (int i = 0; i < liter_sum; i++) {
					if (liter_tab[i].extents == symbol_token[index + ltorg_line_sum].extents) {
						symbol_token[index + (++ltorg_line_sum)].operator1 = "*"; //�������, �����ٿ� * =X'05'�� ����
						symbol_token[index + ltorg_line_sum].operand[0] = (char*)malloc(strlen(liter_tab[i].literal) + 1);
						strcpy(symbol_token[index + ltorg_line_sum].operand[0], liter_tab[i].literal);
						symbol_token[index + ltorg_line_sum].extents = extents;
					}
				}
			}
			free(tmp_data);  //�ڸ�Ʈ �ֱ�
			tmp_data = (char*)malloc(strlen(input_data[index]) + 1);
			strcpy(tmp_data, input_data[index]);
			strtok(tmp_data, " \t");
			strtok(NULL, "\t");
			tmp_token = strtok(NULL, "\t");
			if (tmp_token != NULL) {
				symbol_token[index + ltorg_line_sum].comment = (char*)malloc(strlen(tmp_token) + 1);
				strcpy(symbol_token[index + ltorg_line_sum].comment, tmp_token);
			}
			return 0;
		}
	}
	else { //���� �ִ� ���
		char* tmp;
		char* tmp_data = (char*)malloc(strlen(input_data[index]) + 1); //�����Ҵ�
		char* tmp_token = NULL; // �ӽÿ�
		char* tmp_operand = NULL;
	
		strcpy(tmp_data, input_data[index]);//���
		tmp_token = strtok(tmp_data, " \t");//������ ����
		symbol_token[index + ltorg_line_sum].label = (char*)malloc(strlen(tmp_token) + 1);//�ӽÿ뿡 �����Ҵ�
		strcpy(symbol_token[index + ltorg_line_sum].label, tmp_token); //��ū �и��� ���� label�� ����

		tmp_token = strtok(NULL, " \t");//���� ������ �ٽ� ������ ����
		symbol_token[index + ltorg_line_sum].operator1 = (char*)malloc(strlen(tmp_token) + 1);//�ӽÿ뿡 �����Ҵ�
		strcpy(symbol_token[index + ltorg_line_sum].operator1, tmp_token); // operator����
		if (strcmp(symbol_token[index + ltorg_line_sum].operator1, "CSECT") == 0) extents++; //���� ������ �����ش�.

		tmp_token = strtok(NULL, "\n");  // operator ������ ������ ����

		if (tmp_token == NULL || tmp_token[0] == '\t' || tmp_token[0] == ' ') { //operand ���� ��
			if (tmp_token != NULL) { //operand�� ������ �ڸ�Ʈ�� ����
				char* tmp2_token = (char*)malloc(strlen(tmp_token) + 1);
				strcpy(tmp2_token, tmp_token);//�ڸ�Ʈ ����
				symbol_token[index + ltorg_line_sum].operand[0] = NULL;
				symbol_token[index + ltorg_line_sum].operand[1] = NULL;
				symbol_token[index + ltorg_line_sum].comment = strtok(tmp2_token, "\t");//�ڸ�Ʈ ����
				return 0;
			}
		}
		else { //���۷��尡 �ִٸ�
			tmp_operand = strtok(tmp_token, "\t");
			tmp = strtok(tmp_operand, ",");//���۷��尡 �� ���� �� �ֱ� ������ ������ ���
			symbol_token[index + ltorg_line_sum].operand[0] = (char*)malloc(strlen(tmp) + 1);
			strcpy(symbol_token[index + ltorg_line_sum].operand[0], tmp); // �� ��° ���۷��� ����
			if (symbol_token[index + ltorg_line_sum].operand[0][0] == '=') { // ���۷��忡 �����Ѱ� ���ͷ����� Ȯ��, ���� ���ͷ��̸�
				if (search_placed_literal(symbol_token[index + ltorg_line_sum].operand[0], extents) != 0) {
					liter_tab[liter_sum].literal = (char*)malloc(strlen(symbol_token[index + ltorg_line_sum].operand[0])); //�����Ҵ�
					strcpy(liter_tab[liter_sum].literal, symbol_token[index + ltorg_line_sum].operand[0]); //���ͷ�����
					liter_tab[liter_sum++].extents = extents;
				}
			}
			tmp = strtok(NULL, ",");
			if (tmp != NULL) { //�� ��° ���۷���
				symbol_token[index + ltorg_line_sum].operand[1] = (char*)malloc(strlen(tmp) + 1);
				strcpy(symbol_token[index + ltorg_line_sum].operand[1], tmp);
				tmp = strtok(NULL, ",");
				if (tmp != NULL) { //�� ��° ���۷���
					symbol_token[index + ltorg_line_sum].operand[2] = (char*)malloc(strlen(tmp) + 1);
					strcpy(symbol_token[index + ltorg_line_sum].operand[2], tmp);
				}
			}
			free(tmp_data); //�ڸ�Ʈ�� �ִ°� ����
			tmp_data = (char*)malloc(strlen(input_data[index]) + 1);
			strcpy(tmp_data, input_data[index]);
			tmp_token = strtok(tmp_data, " \t");
			tmp_token = strtok(NULL, " \t");
			tmp_token = strtok(NULL, "\t");
			tmp_token = strtok(NULL, "\t");
			if (tmp_token != NULL) {
				symbol_token[index + ltorg_line_sum].comment = (char*)malloc(strlen(tmp_token) + 1);
				strcpy(symbol_token[index + ltorg_line_sum].comment, tmp_token);
			}
		}
		label_num++;
	}
	return 0;
}
/* -----------------------------------------------------------------------------------
* ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�.
* �Ű� : ��ū ������ ���е� ���ڿ�
* ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0
* ���� :
*
* -----------------------------------------------------------------------------------
*/

int search_opcode(char* str) {
	if (str[0] == '+') { //4����
		str = str + 1; //�ϳ��� �÷��ش�.
	}
	//�ε����� Ž���Ѵ�.
	for (int i = 0; i < inst_index; i++) {
		if (strcmp(str, inst_table[i].name) == 0) //���ؼ� ������ �ε��� ��ȯ
			return i;
	}
	if (str == NULL)
		return -1;
	return -1;
}

/* -----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �н�1������ �����ϴ� �Լ��̴�.
*		   �н�1������..
*		   1. ���α׷� �ҽ��� ��ĵ�Ͽ� �ش��ϴ� ��ū������ �и��Ͽ� ���α׷� ���κ� ��ū
*		   ���̺��� �����Ѵ�.
*
* �Ű� : ����
* ��ȯ : ���� ���� = 0 , ���� = < 0
* ���� : ���� �ʱ� ���������� ������ ���� �˻縦 ���� �ʰ� �Ѿ �����̴�.
*	  ���� ������ ���� �˻� ��ƾ�� �߰��ؾ� �Ѵ�.
*
* -----------------------------------------------------------------------------------
*/

static int assem_pass1(void) { 
	//��ū�Ľ� �Լ��� ȣ���Ͽ� ���̺��� �����Ѵ�.

	int check_index = 0;
	int opcode_idx; // opcode �ε���
	int check_sum = 0;
	token_table[check_index];

	locctr = 0; //location counter, ����� static int�� ����
	int extents = 0; // symbol�� extents��(������)�� �����ϱ� ���ؼ�

	//token_parsing ����, �׸��� ���� ���� �߻��ϸ� -1
	for (int i = 0; i < line_num; i++) {
		if (token_parsing(i) < 0) return -1;
	}

	token_line = line_num + ltorg_line_sum; //�Է¹޾Ҵ� �ҽ��ڵ� ���μ����� LTORG ���� ������ ���� ���� table�� �� ���� ��
	check_index++;
	check_sum += check_index;
	token_table[check_index];

	for (int i = 0; i < token_line; i++) {
		if (symbol_token[i].operator1 == NULL) { //operator�� NULL�� �� 
			location_counter_index[i] = -1; //-1 �������� ���� ���ǹ� ����
			check_sum++;
			continue;
		}

		opcode_idx = search_opcode(symbol_token[i].operator1);//search_opcode�� ã�Ƽ� ��ȯ�� �ε��� ��ȣ�� opcode_idx�� ����

		if (opcode_idx < 0) { //��ɾ �ƴ� �� (���þ� ���)
			if (strcmp(symbol_token[i].operator1, "RESW") == 0) { //RESERVED WORD�϶��� ó��
				location_counter_index[i] = locctr; //�ش� location counter�� location index�� ����
				locctr += 3 * atoi(symbol_token[i].operand[0]); // 1����� 3����Ʈ�̹Ƿ� 3�� ������ ���� ���Ѵ�.
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "RESB") == 0) {//RESERVED BYTE
				location_counter_index[i] = locctr;
				locctr += atoi(symbol_token[i].operand[0]); //1byte�̹Ƿ� �׳� �����ش� **atoi ���ڿ��� ���ڷ� ��ȯ
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "EXTDEF") == 0 //RESW RESB �ƴ� ����� ó��
				|| strcmp(symbol_token[i].operator1, "END") == 0
				|| strcmp(symbol_token[i].operator1, "LTORG") == 0
				|| strcmp(symbol_token[i].operator1, "EXTREF") == 0
				) {
				location_counter_index[i] = -1; // location count�� ���� ���� ���� ó��
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "CSECT") == 0) { //CSECT�϶�
				location_counter_index[i] = locctr;
				extents++; //������ �ٲ�Ƿ� ++
				locctr = 0;
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "EQU") == 0) { //EQU�� ���� �޸𸮸� ��Ƹ��� �����Ƿ� 3�� ������ �ʴ´�.
				location_counter_index[i] = locctr;
				if (symbol_token[i].operand[0][0] == '*'); //���� ����. �׳� BUFEND ǥ��
				else {
					char* tmp;
					char* tmp_token;
					char opcode[2]; // +(�÷���),-(���̳ʽ�),*(���ϱ�),%(������),/(������) ���� �����ڸ� �����Ͽ��ش�.
					int value_one;
					int value_two;
					check_sum++;

					tmp = (char*)malloc(strlen(symbol_token[i].operand[0]) + 1);//�����Ҵ�

					strcpy(tmp, symbol_token[i].operand[0]); //����
					for (int j = 0; symbol_token[i].operand[0][j] != '\0'; j++) { //���� ��� BUFEND-BUFFER�� ���� ������ �ѱ��ھ� for�� ���� ��. ������ ã��
						if (symbol_token[i].operand[0][j] == '/' || //�����ڰ� ������
							symbol_token[i].operand[0][j] == '%' ||
							symbol_token[i].operand[0][j] == '*' ||
							symbol_token[i].operand[0][j] == '-' ||
							symbol_token[i].operand[0][j] == '+') {
							opcode[1] = '\0'; //���߿� ������ ���� ������ ����. �׷��� ���ڿ� ������ null�� �־���ϹǷ� ũ�� �ϳ� �� ���� �����ڿ� null����
							opcode[0] = symbol_token[i].operand[0][j]; //���� '-' \0 ����
							check_sum++;
						}
					}
					tmp_token = strtok(tmp, opcode); //������ �ձ��� �ڸ��� tmp_token�� ����
					for (int k = 0; k < symbol_index; k++) { //index��ŭ for�� �ݺ��ؼ�
						if (strcmp(tmp_token, sym_table[k].symbol) == 0) //symbol�� tmp_token��. ���� ��� BUFEND�� �ٸ� symbol��
							value_one = sym_table[k].addr; //���ؼ� �ش� index�� ã�� addr ��ȯ ex.1033����
						check_sum++;
					}
					tmp_token = strtok(NULL, "\0"); //������ ���� ����
					for (int k = 0; k < symbol_index; k++) {
						if (strcmp(tmp_token, sym_table[k].symbol) == 0)
							value_two = sym_table[k].addr; //���� ���� �ݺ� ex.0033����
						check_sum++;
					}
					//������, value_one, value_two �޾Ƽ� ���. ��� �� locctr�� ����
					if (opcode[0] == '+')	locctr = value_one + value_two;
					else if (opcode[0] == '-') 	locctr = value_one - value_two;
					else if (opcode[0] == '*')	locctr = value_one * value_two;
					else if (opcode[0] == '%')	locctr = value_one % value_two;
					else if (opcode[0] == '/')	locctr = value_one / value_two;
					token_table[check_index]->operand;
					location_counter_index[i] = locctr; //locctr ����
					check_sum++;
				}
			}

			//����
			else if (strcmp(symbol_token[i].operator1, "START") == 0)
				location_counter_index[i] = locctr;

			else if (strcmp(symbol_token[i].operator1, "*") == 0) { //���ͷ��� �ִ� *�� ó��
				location_counter_index[i] = locctr;

				if (symbol_token[i].operand[0][1] == 'C') { //������� C = 'EOF'
					int ch_num = 0;
					for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++) //EOF
						ch_num++; //���� �� ī��Ʈ
					locctr += ch_num;
					check_sum++;
				}
				else if (symbol_token[i].operand[0][1] == 'X') { //������� X='05'
					int x_num = 0;
					check_sum++;
					for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++) //05
						x_num++; //���� ī��Ʈ
					locctr += (x_num / 2); //16������ ǥ���Ǵ� addr�� 6����. 2���ھ� 1byte. �� 6���ڴϱ� 3byte�� 1word
				}
				location_counter_index[i + 1] = locctr; 
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "BYTE") == 0) { //���þ BYTE�� ��, ������ ���� ���� �ݺ�
				location_counter_index[i] = locctr;
				if (symbol_token[i].operand[0][0] == 'C') { //C'EOF'�̷���
					strcmp(token_table[check_index]->operand[1], "C");
					int ch_num = 0;
					check_sum++;
					for (int j = 2; symbol_token[i].operand[0][j] != '\''; j++)
						ch_num++;
					locctr += ch_num;
					strcmp(token_table[check_index]->operand[1], "W");
				}
				else if (symbol_token[i].operand[0][0] == 'X') { //X'F1'�̷���
					int x_num = 0;
					for (int j = 2; symbol_token[i].operand[0][j] != '\''; j++)
						x_num++;
					locctr += (x_num / 2);
					check_sum++;
				}
			}
			else if (strcmp(symbol_token[i].operator1, "WORD") == 0) {
				location_counter_index[i] = locctr;
				locctr += 3; //word�� 3
				check_sum++;
			}
		}
		else { //��ɾ��� ��
			location_counter_index[i] = locctr;
			if (inst_table[opcode_idx].type_format == 1) 	locctr += 1; // 1format
			else if (inst_table[opcode_idx].type_format == 2) locctr += 2; //2format
			else { //3,4 format
				if (strchr(symbol_token[i].operator1, '+') == NULL) locctr += 3; //3format
				else locctr += 4;
				check_sum++;
			}
		}
		if (symbol_token[i].label != NULL) { //�ɺ����̺� ����
			if (search_symbol(symbol_token[i].label, extents) != 0) { //���� ���� �ɺ��� ������ �������ش�.
				strcpy(sym_table[symbol_index].symbol, symbol_token[i].label);
				sym_table[symbol_index].addr = location_counter_index[i];
				sym_table[symbol_index].extents = extents;
				symbol_index++;
				check_sum++;
			}
		}
	}
	for (int i = 0; i < liter_sum; i++) {
		for (int j = 0; j < token_line; j++) {
			if (symbol_token[j].operand[0] != NULL && strcmp(symbol_token[j].operand[0], liter_tab[i].literal) == 0 && symbol_token[j].extents == liter_tab[i].extents && symbol_token[j].operator1[0] == '*')
				liter_tab[i].addr = location_counter_index[j];
			check_sum++;
		}
	}
	return 0;
}


/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ ��ɾ� ���� OPCODE�� ��ϵ� ǥ(���� 4��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*        ���� ���� 4�������� ���̴� �Լ��̹Ƿ� ������ ������Ʈ������ ������ �ʴ´�.
* -----------------------------------------------------------------------------------
*/

/* ----------------------------------------------------------------------------------
void make_opcode_output(char* file_name)
{
	 add your code here
	FILE* fp = NULL;
	fp = fopen(file_name, "wt");
	for (int i = 0; i <= line_num; i++) {
		if (symbol_token[i].label != NULL) //���� �ִ� ���
			fprintf(fp, "%s \t", symbol_token[i].label);
		else
			fputs("\t", fp); //���� ������ �׳� ��ġ��

		if (symbol_token[i].operator1 != NULL) //���۷����Ͱ� �ִ°��
			fprintf(fp, "%s \t", symbol_token[i].operator1);
		else
			fputs("\t", fp); //���۷����� ���� ���

		if (symbol_token[i].operand[2] != NULL) //����° ���۷��尡 �ִ� ���
			fprintf(fp, "%s,%s,%s \t", symbol_token[i].operand[0], symbol_token[i].operand[1], symbol_token[i].operand[2]);
		else if (symbol_token[i].operand[1] != NULL) //�ι�°������ ���۷��尡 ���� ��
			fprintf(fp, "%s,%s \t\t\t", symbol_token[i].operand[0], symbol_token[i].operand[1]);
		else if (symbol_token[i].operand[0] != NULL) //���۷��尡 �ϳ��� ���� ��
			fprintf(fp, "%s \t\t\t", symbol_token[i].operand[0]);
		else //���۷��尡 ���� ��
			fputs("\t \t \t ", fp);


		if (symbol_token[i].operator1 != NULL) { //���۷����Ͱ� ������
			int j = search_opcode(symbol_token[i].operator1); //�Լ��� ȣ���ؼ� ���۷����͸� ���� ��� ���̺��� ��� �ε����� �ش��ϴ��� ã��
			if (j != -1) //���۷����Ͱ� ��ɾ �ƴ� ���þ��϶�
				fprintf(fp, "%X \t \t", inst_table[j].opcode);
			else
				fputs("\t \t", fp);
		}

		if (symbol_token[i].comment != NULL) //�ڸ�Ʈ�� ���� ��
			fprintf(fp, "%s \n", symbol_token[i].comment);
		else
			fputs("\n", fp);
	}

	//���� �ڵ�� ����. �ܼ�ȭ�鿡�� ���� ���� �ۼ���.
	for (int i = 0; i <= line_num; i++) {
		if (symbol_token[i].label != NULL)
			printf("%s \t", symbol_token[i].label);
		else
			puts("\t");

		if (symbol_token[i].operator1 != NULL)
			printf("%s \t", symbol_token[i].operator1);
		else
			puts("\t");

		if (symbol_token[i].operand[2] != NULL)
			printf("%s,%s,%s \t", symbol_token[i].operand[0], symbol_token[i].operand[1], symbol_token[i].operand[2]);
		else if (symbol_token[i].operand[1] != NULL)
			printf("%s,%s \t\t\t", symbol_token[i].operand[0], symbol_token[i].operand[1]);
		else if (symbol_token[i].operand[0] != NULL)
			printf("%s \t\t\t", symbol_token[i].operand[0]);
		else
			puts("\t \t \t ");


		if (symbol_token[i].operator1 != NULL) {
			int j = search_opcode(symbol_token[i].operator1);
			if (j != -1)
				printf("%X \t \t", inst_table[j].opcode);
			else
				puts("\t \t");
		}

		if (symbol_token[i].comment != NULL)
			printf("%s \n", symbol_token[i].comment);
		else
			puts("\n");
	}
	//fputs("ari", fp); 
}
--------------------------------------------------------------------------------------*/

//pass1���� ����ϱ����� ���� �߰�, tab�� �ִ��� ������ Ȯ��
int search_symbol(char* str, int extents) { 
	for (int i = 0; i < symbol_index; i++) {
		if (strcmp(sym_table[i].symbol, str) == 0)
			if (extents == sym_table[i].extents) return 0;
	}
	return -1;
}
int search_placed_literal(char* str, int extents) {
	for (int i = 0; i < liter_sum; i++) {
		if (strcmp(liter_tab[i].literal, str) == 0)
			if (extents == liter_tab[i].extents)return 0;
	}
	return -1;
}

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ SYMBOL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char* file_name) //symtab���
{
	printf("symtab\n\n");
	for (int i = 0; i <= line_num + ltorg_line_sum; i++) {
		if (symbol_token[i].label != NULL && symbol_token[i].label != ".") { //���� �ִ� ���
			if (location_counter_index[i] != -1 && symbol_token[i].operator1 != NULL) {
				if ((strcmp(symbol_token[i].label, "RDREC") == 0) || (strcmp(symbol_token[i].label, "WRREC") == 0)) {
					printf("%s \t %X \n", symbol_token[i].label, 0);
				}
				else
					printf("%s \t %X \n", symbol_token[i].label, location_counter_index[i]);
			}
		}
	}
	printf("\n\n");
	/* add your code here }*/
	FILE* fp = NULL;
	fp = fopen(file_name, "wt");
	
	for (int i = 0; i <= line_num + ltorg_line_sum; i++) {
		if (symbol_token[i].label != NULL && symbol_token[i].label != ".") { //���� �ִ� ���
			if (location_counter_index[i] != -1 && symbol_token[i].operator1 != NULL) {
				if ((strcmp(symbol_token[i].label, "RDREC") == 0) || (strcmp(symbol_token[i].label, "WRREC") == 0)) {
					fprintf(fp, "%s \t %X \n", symbol_token[i].label,0);
				}
				else
					fprintf(fp, "%s \t %X \n", symbol_token[i].label,location_counter_index[i]);
			}
		}
	}
}

	/* ----------------------------------------------------------------------------------
	* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
	*        ���⼭ ��µǴ� ������ LITERAL�� �ּҰ��� ����� TABLE�̴�.
	* �Ű� : ������ ������Ʈ ���ϸ�
	* ��ȯ : ����
	* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
	*        ȭ�鿡 ������ش�.
	*
	* -----------------------------------------------------------------------------------
	*/
void make_literal_output(char* file_name) //liter tab ���
{
	printf("literaltab\n\n");
	for (int i = 0; i <= line_num + ltorg_line_sum; i++) {
		if ((symbol_token[i].operator1) == "*") {
			for (int j = 3; j < strlen(symbol_token[i].operand[0]) - 1; j++)
				printf("%C", symbol_token[i].operand[0][j]);
			printf("\t");
			printf("%X \n", location_counter_index[i]);

		}
	}

	/* add your code here }*/	
	FILE* fp = NULL;
	fp = fopen(file_name, "wt");
	for (int i = 0; i <= line_num + ltorg_line_sum; i++) {
		if ((symbol_token[i].operator1) == "*") {
			for (int j = 3; j<strlen(symbol_token[i].operand[0])-1; j++)
				fprintf(fp, "%C", symbol_token[i].operand[0][j]);
			fprintf(fp, "\t");
			fprintf(fp, "%X \n", location_counter_index[i]);

		}
	}
	
}

		/* ----------------------------------------------------------------------------------
		* ���� : ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ �����ϴ� �Լ��̴�.
		*		   �н� 2������ ���α׷��� ����� �ٲٴ� �۾��� ���� ������ ����ȴ�.
		*		   ������ ���� �۾��� ����Ǿ� ����.
		*		   1. ������ �ش� ����� ��ɾ ����� �ٲٴ� �۾��� �����Ѵ�.
		* �Ű� : ����
		* ��ȯ : �������� = 0, �����߻� = < 0
		* ���� :
		* -----------------------------------------------------------------------------------
		*/
static int assem_pass2(void)
{
	/* add your code here }*/
	int idx_inst;//���۷������� opcode �ε���
	int tmp;

	for (int i = 0; i < token_line; i++) {
		if (symbol_token[i].operator1 == NULL) continue; //���۷����� NULL�̸� �����ϰ�(����) ��� ���� i�� for�� ����

		idx_inst = search_opcode(symbol_token[i].operator1);//���۷������� opcode �ε��� ã�Ƽ� ����
		{//else Ż�� ����

			//���ĸ��� �ٸ��� ó��
			switch (inst_table[idx_inst].type_format) { 
			case 1: //1����
				op_tab[i] = inst_table[idx_inst].opcode; //�ε����� �ش��ϴ� opcode ����
				form_list[i] = 1; //�ش� �ε����� format�� 1����
				break;

			case 2: //2����
				op_tab[i] = inst_table[idx_inst].opcode;

				op_tab[i] = op_tab[i] << 4; //8bit opcode�� �̾ 4bit �������� 2�� �ֱ����� shift left!! ���� 16������ 1����Ʈ shift left ó��(4bit)
				if (strcmp(symbol_token[i].operand[0], "SW") == 0) op_tab[i] |= 9;
				else if (strcmp(symbol_token[i].operand[0], "PC") == 0) op_tab[i] |= 8;
				else if (strcmp(symbol_token[i].operand[0], "F") == 0) op_tab[i] |= 6;
				else if (strcmp(symbol_token[i].operand[0], "T") == 0) op_tab[i] |= 5;
				else if (strcmp(symbol_token[i].operand[0], "S") == 0) op_tab[i] |= 4;
				else if (strcmp(symbol_token[i].operand[0], "B") == 0) op_tab[i] |= 3;
				else if (strcmp(symbol_token[i].operand[0], "L") == 0) op_tab[i] |= 2;
				else if (strcmp(symbol_token[i].operand[0], "X") == 0) op_tab[i] |= 1;
				else if (strcmp(symbol_token[i].operand[0], "A") == 0) op_tab[i] |= 0;

				op_tab[i] = op_tab[i] << 4; // 16������ 1����Ʈ�� ����Ʈ����Ʈ�� ó��(4bit)
				if (symbol_token[i].operand[1] == NULL) op_tab[i] |= 0;
				else if ( strcmp(symbol_token[i].operand[1], "SW") == 0) op_tab[i] |= 9;
				else if (strcmp(symbol_token[i].operand[1], "PC") == 0) op_tab[i] |= 8;
				else if (strcmp(symbol_token[i].operand[1], "F") == 0) op_tab[i] |= 6;
				else if (strcmp(symbol_token[i].operand[1], "T") == 0) op_tab[i] |= 5;
				else if (strcmp(symbol_token[i].operand[1], "S") == 0) op_tab[i] |= 4;
				else if (strcmp(symbol_token[i].operand[1], "B") == 0) op_tab[i] |= 3;
				else if (strcmp(symbol_token[i].operand[1], "L") == 0) op_tab[i] |= 2;
				else if (strcmp(symbol_token[i].operand[1], "X") == 0) op_tab[i] |= 1;
				else if (strcmp(symbol_token[i].operand[1], "A") == 0) op_tab[i] |= 0;
				form_list[i] = 2;
				break;

			case 3:
				op_tab[i] = inst_table[idx_inst].opcode;
				form_list[i] = 3;

				//ni
				if (symbol_token[i].operand[0] != NULL) {
					//@ indirect�� #immediate �� �ƴ� �� ó��
					if (symbol_token[i].operand[0][0] != '@' && symbol_token[i].operand[0][0] != '#') op_tab[i] += 3; //2���� 11 �߰� ni==11
					else if (symbol_token[i].operand[0][0] == '#') op_tab[i] += 1;// 2���� 1�߰� ni==01
					else if (symbol_token[i].operand[0][0] == '@') op_tab[i] += 2;//2�������� 10�� �߰����ش�. ni==10
				}
				else	op_tab[i] += 3; //2�������� 11�� �߰����ش�. ���� ��� RSUB. ni==11

				//xbpe
				op_tab[i] <<= 1;// X�� ���� ���� ������ �Ҵ� .1bit
				if (symbol_token[i].operand[1] != NULL) {
					if (symbol_token[i].operand[1][0] == 'X') op_tab[i] |= 1; //X�� ���� ������ 1�� �Ҵ��Ͽ� �ش�.
				}
				op_tab[i] <<= 1; //b�� ���� ���� ����
				op_tab[i] |= 0; // 2�������� 0�� �߰��Ѵ�.
				op_tab[i] <<= 2; // p�� e�� ������ ������ش�.
				if (symbol_token[i].operand[0] != NULL && symbol_token[i].operator1[0] != '+') { //4������ �ƴϰ�(3�����̰�) ���۷��尡 ������
					if (symbol_token[i].operand[0][0] == '@') { //���۷��尡 indirect�� ��
						int ta = 0; // Ÿ�� ��巹��
						int pc = 0; // Program Counter
						int disp = 0;
						op_tab[i] |= 2; //p�� 2�������� 10�� �߰����ش�.

						op_tab[i] <<= 12; // disp�� �����Ҵ�
						pc = location_counter_index[i + 1]; // Program counter�� ���� �޴´�. ������ ������ ��ɹ��̹Ƿ� i+1
						for (int j = 0; j < symbol_index; j++) {
							if (symbol_token[i].operand[0] != NULL && symbol_token[i].extents == sym_table[j].extents && strcmp(sym_table[j].symbol, symbol_token[i].operand[0] + 1) == 0)
								//���� ��� LDA LENGTH �̸� ���۷��� LENGTH�� �ɺ��� ���Ͽ� LENGTH�� ã�� �� �ּҸ� �����;���.
								ta = sym_table[j].addr; //�ɺ��� �����ϴ� ����ü. ����� ����
						}
						disp = ta - pc;
						disp &= 0xfff;
						op_tab[i] += (ta-pc);
					}

					else if (symbol_token[i].operand[0][0] == '#') { //���۷��尡 #(immediate)�� ��
						char* tmp = NULL;
						int index = 0;
						op_tab[i] <<= 12; // disp�� ���� �Ҵ�
						tmp = (char*)malloc(strlen(symbol_token[i].operand[0])); //�����Ҵ�
						for (int j = 0; j < strlen(symbol_token[i].operand[0]) - 1; j++)
							tmp[index++] = symbol_token[i].operand[0][j + 1];
						tmp[index] = '\0';
						op_tab[i] += strtol(tmp, NULL, 16); //16������ ��ȯ
						free(tmp);
					}

					else if (symbol_token[i].operand[0][0] == '=') { //���ͷ��϶�
						int pc = 0; // Program Counter
						int ta = 0; // TARGET ADDRESS
						int disp = 0;
						op_tab[i] += 2; //pe�� 2������ 10�� �߰����ش�.
						op_tab[i] <<= 12; // disp�� �����Ҵ�
						pc = location_counter_index[i + 1]; // Program Counter�� ó�����ش�.
						for (int j = 0; j < liter_sum; j++) {
							if (strcmp(symbol_token[i].operand[0], liter_tab[j].literal) == 0 && symbol_token[i].extents == liter_tab[j].extents)
								ta = liter_tab[j].addr;
						}
						disp = ta - pc;
						disp &= 0xfff;
						op_tab[i] += (ta-pc);
					}
					else {//�׳� simple addressing�� ��
						int pc = 0; // Program Counter
						int ta = 0; // Ÿ�� ��巹��
						int disp = 0;
						op_tab[i] |= 2; //pe�� 2���� 10�� �߰����ش�.
						op_tab[i] <<= 12; // disp�� �����Ҵ�
						pc = location_counter_index[i + 1]; // program Counter�� �߰�
						for (int j = 0; j < symbol_index; j++) {
							if (symbol_token[i].operand[0] != NULL && symbol_token[i].extents == sym_table[j].extents && strcmp(sym_table[j].symbol, symbol_token[i].operand[0]) == 0)
								ta = sym_table[j].addr;
						}
						disp = ta - pc;
						disp &= 0xfff; //���� �����ֱ�����
						op_tab[i] |= disp;
					}
				}
				else if (symbol_token[i].operand[0] == NULL) { //���۷��尡 ���� ���
					op_tab[i] += 0; //2������ 00�� �߰����ش�.
					op_tab[i] <<= 12;
				}
				else if (symbol_token[i].operator1[0] == '+') { //4format�̸�
					int pc = 0; // Program Counter
					int ta = 0; // Ÿ�� �ּ�
					op_tab[i] |= 1; // Pe�� 1�� �����Ѵ�.
					op_tab[i] <<= 20; // ������ ������ش�.
					pc = location_counter_index[i + 1]; // ���α׷� ī���� ���� �޴´�.
					for (int j = 0; j < symbol_index; j++) {
						if (symbol_token[i].operand[0] != NULL && symbol_token[i].extents == sym_table[j].extents && strcmp(sym_table[j].symbol, symbol_token[i].operand[0]) == 0) {
							ta = sym_table[j].addr;
						}
					}
					ta &= 0xfffff;
					op_tab[i] += ta;
					form_list[i] = 4;
				}
				break;

			default:
				if (strcmp(symbol_token[i].operator1, "BYTE") == 0) { //BYTE�� ��
					int count = 0;
					int index = 0;
					char* tmp = NULL;

					for (int j = 2; symbol_token[i].operand[0][j] != '\''; j++) //j=2���� ����ǥ ������ ������ count
						count++;
					tmp = (char*)malloc(count + 1); //���� �Ҵ�
					for (int j = 2; j<(count+2); j++)
						tmp[index++] = symbol_token[i].operand[0][j]; //tmp ����
					tmp[index] = '\0';
					op_tab[i] = strtol(tmp, NULL, 16); //16������ ��ȯ
					free(tmp);
					form_list[i] = count / 2;
				}
				else if (strcmp(symbol_token[i].operator1, "WORD") == 0) { //WORD�� ��
					form_list[i] = 3;
					if (symbol_token[i].operand[0][0] >= '0' && symbol_token[i].operand[0][0] <= '9') // WORD�� ���۷��尡 ���ڶ��
						op_tab[i] = strtol(symbol_token[i].operand[0],NULL,16); //���� 16���� op_tab�� ����
					else { // ���ڶ��. ���� ��� WORD BUFEND-BUFFER. �ܺ��������� ���ǹ����� Ȯ��
						for (int j = 0; j < token_line; j++) {
							if (symbol_token[j].operator1 != NULL && strcmp(symbol_token[j].operator1, "EXTREF") == 0 && symbol_token[j].extents == symbol_token[i].extents) {
								for (int k = 0; k < 3; k++) //�ܺ��������� ���۷��带 ���ϸ� Ȯ��
									if (symbol_token[j].operand[k] != NULL && strstr(symbol_token[i].operand[0], symbol_token[j].operand[k]) != NULL)
										continue; //�ܺ������� �ּҸ� �𸣹Ƿ� 000000
							}
						}
					}
				}
				else if (strcmp(symbol_token[i].operator1, "*") == 0) { //���۷����Ͱ� *�� literal ó��
					if (symbol_token[i].operand[0][1] == 'C') {
						int ch_num = 0;
						for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++) {
							ch_num++;
							op_tab[i] |= symbol_token[i].operand[0][j];
							op_tab[i] <<= 8; //�����ϳ��� 8bit��
						}
						op_tab[i] >>= 8;
						form_list[i] = ch_num;
					}
					else if (symbol_token[i].operand[0][1] == 'X') {
						int count = 0;
						char* tmp;
						int index = 0;
						for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++)//����ǥ ���� ���� ����
							count++;
						tmp = (char*)malloc(count + 1);

						for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++) //����ǥ ���� ����
							tmp[index++] = symbol_token[i].operand[0][j];
						tmp[index] = '\0';
						op_tab[i] = strtol(tmp, NULL, 16); //16������ ��ȯ
						form_list[i] = count / 2;
						free(tmp);
					}
				}
				break;
			}
		}
	}

	/*�ܼ� â�� ���̺� ���
	for (int i = 0; i < token_line; i++) {
		if (location_counter_index[i] != -1 && symbol_token[i].operator1 != NULL && strcmp(symbol_token[i].operator1, "CSECT") != 0) //CSECT�� locctr ǥ�� �ȵǰ���
			printf("%04X", location_counter_index[i]);
		printf("\t");
		if (symbol_token[i].label != NULL)	printf("%s", symbol_token[i].label);
		printf("\t");
		if (symbol_token[i].operator1 != NULL)  printf("%s", symbol_token[i].operator1);
		printf("\t");
		if (symbol_token[i].operand[0] != NULL) {
			printf("%s", symbol_token[i].operand[0]);
			if (symbol_token[i].operand[1] != NULL) {
				printf(",%s", symbol_token[i].operand[1]);
				if (symbol_token[i].operand[2] != NULL)
					printf(",%s", symbol_token[i].operand[2]);
			}
		}
		if (form_list[i] == 1) printf("\t\t%02X\n", op_tab[i]);
		else if (form_list[i] == 2) printf("\t\t%04X\n", op_tab[i]);
		else if (form_list[i] == 3) printf("\t\t%06X\n", op_tab[i]);
		else if (form_list[i] == 4) printf("\t\t%08X\n", op_tab[i]);
		else printf("\n");
	}
	return 0;*/
}

			/* ----------------------------------------------------------------------------------
			* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
			*        ���⼭ ��µǴ� ������ object code (������Ʈ 1��) �̴�.
			* �Ű� : ������ ������Ʈ ���ϸ�
			* ��ȯ : ����
			* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
			*        ȭ�鿡 ������ش�.
			*
			* -----------------------------------------------------------------------------------
			*/
void make_objectcode_output(char* file_name)
{
	/* add your code here }*/

	int program_end_line = 0; // program�� �������� ���� ��ȣ
	int program_start_line = 0; // ���α׷��� ���� �� ���ι�ȣ
	int program_end_address = 0; // program�� ������ location counter
	int program_start_address = 0; // program�� �����ּ�
	int extends_position = 0;//������ extents ����

	int is_line_end = 0;//�ϳ��� ���� ���μ��󿡼��� 0
	int is_line_new = 1; //������ �ٲ�� 1�� ó��
	int current_char = 0; // ���� ������ char�� �������ش�.
	int tmp_data = 0; // �ּ�(address)�� ����ҋ� ���ִ� �ӽ��� ����
	int current = 0; // ���� ��ġ

	FILE* fp = NULL;
	fp = fopen(file_name, "w");

	char* tmp;
	tmp = (char*)malloc(7);

	char* buffer; //�����͸� ���� ����
	int buff_size = 0;
	int buff_length = 0;
	buffer = (char*)malloc(70); //���۸� 70���� �Ҵ����ش�.
	buff_length = 70;

	while (is_line_end == 0) {
		while (symbol_token[current].label != NULL && symbol_token[current].label[0] == '.') { //���� .�̰ų� �ƹ��͵� ���� ���� �׳� ���� ��
			current++;
		}
		fprintf(fp, "H%s\t", symbol_token[current].label); // program name
		if (symbol_token[current].operand[0] != NULL)
			program_start_address = atoi(symbol_token[current].operand[0]); //���α׷� �̸� ���� ���� �ּ� ���� ������ �װɷ� ����
		fprintf(fp, "%06X", program_start_address); // start address
		for (int i = current + 1; i < token_line; i++) {
			//���۷����Ͱ� NULL�̰ų� CSECT�� �� ���� �Ǵ� ��������
			if (symbol_token[i].operator1 == NULL)
				continue;
			if (strcmp(symbol_token[i].operator1, "CSECT") == 0)
				break;

			//������ END�� ������ �ؿ� ������ ���ͷ������� �� ������ ī��Ʈ�ϱ�
			else if (strcmp(symbol_token[i].operator1, "END") == 0) {
				int literal_count = 0;
				for (int j = 0; j < liter_sum; j++) {
					if (symbol_token[i].extents == liter_tab[j].extents) literal_count++;
				}
				program_end_line = i + literal_count + 1;
				//�� ���μ��� ���ͷ� ���� �� ���ϰ� +!�ؼ� �� ������ ���� ���ϱ�
				break;
			}
			program_end_line = i;
		}
		if (program_end_line == token_line) is_line_end = 1;
		tmp_data = program_end_line;
		if (program_end_line != token_line) {
			while (strcmp(symbol_token[tmp_data].operator1, "EQU") == 0)
				tmp_data--;

			program_end_address = location_counter_index[tmp_data + 1]; // �������� PC�� ���̴�. (EQU�� ��������.)
		}
		else program_end_address = location_counter_index[tmp_data];

		fprintf(fp, "%06X\n", program_end_address - program_start_address); // program�� ���� ���
		for (int i = current; i <= program_end_line; i++) {
			if (symbol_token[i].operator1 == NULL)
				continue;
			else {
				if (strcmp(symbol_token[i].operator1, "EXTDEF") == 0) { //�ܺ������� ó��
					current = i;
					fputc('D', fp);
					current_char += 1;
					for (int j = 0; j < 3; j++) {
						if (symbol_token[i].operand[j] != NULL) {
							int sym_addr = 0;
							for (int k = 0; k < symbol_index; k++) { //�ɺ����̺��� ���۷��带 ��Ī
								if (strcmp(sym_table[k].symbol, symbol_token[i].operand[j]) == 0 && sym_table[k].extents == symbol_token[i].extents)
									sym_addr = sym_table[k].addr;
							}
							current_char += (strlen(symbol_token[i].operand[j]) + 6);
							if (current_char > 72) { //���Ѽ��� ������ �ʰ��Ѵٸ�
								fputs("\nD", fp);
								current_char = 0;
								current_char += (strlen(symbol_token[i].operand[j]) + 6);
							}
							fprintf(fp, "%s%06X", symbol_token[i].operand[j], sym_addr);
						}
					}
					fputc('\n', fp);
					current_char = 0;
				}
				if (strcmp(symbol_token[i].operator1, "EXTREF") == 0) { //�ܺ��������
					current = i;
					fputc('R', fp);
					current_char += 1;
					for (int j = 0; j < 3; j++) {
						if (symbol_token[i].operand[j] != NULL) {
							current_char += strlen(symbol_token[i].operand[j]) + 1;
							if (current_char > 72) { //���ѱ��̸� ������
								fputs("\nR", fp);
								current_char = 0;
								current_char += (strlen(symbol_token[i].operand[j]) + 1);
							}
							fprintf(fp, "%s", symbol_token[i].operand[j]);
						}
					}
					fputc('\n', fp);
					current_char = 0;
				}
			}
		}
		current++;
		program_start_line = current;
		int tmp_end = 0;// ���� ��ġ
		while (current <= program_end_line) { //���� ������ �ؽ�Ʈ ���
			if (is_line_new == 1) {
				sprintf(buffer, "T%06X", location_counter_index[current]);
				buff_size += 6;
				current_char += 7;
				strcat(buffer, "00"); //������ ������ �����Ѵ�. 0����
				buff_size += 2;
				current_char += 2;
				is_line_new = 0;
			}
			if (form_list[current] == 1) {
				current_char += 2;
				if (current_char > 69) {
					current_char -= 2;
					sprintf(tmp, "%02X", (current_char - 9) / 2);
					buff_size++;
					buffer[7] = tmp[0];
					buff_size++;

					buffer[8] = tmp[1];
					fputs(buffer, fp);
					fputc('\n', fp);
					current_char = 0;
					sprintf(buffer, "T%06X", location_counter_index[current]);
					buff_size += 7;
					current_char += 7;
					strcat(buffer, "00");
					buff_size += 2;
					current_char += 2;
					sprintf(tmp, "%02X", op_tab[current++]);
					buff_size += 2;
					strcat(buffer, tmp);
					current_char += 2;
				}
				else if (form_list[current + 1] <= 0 && symbol_token[current + 1].operator1 != NULL && strcmp(symbol_token[current + 1].operator1, "END") != 0) {
					sprintf(tmp, "%02X", op_tab[current++]);
					buff_size += 2;
					strcat(buffer, tmp);
					sprintf(tmp, "%02X", (current_char - 9) / 2);
					buff_size++;
					buffer[7] = tmp[0];
					buff_size++;
					buffer[8] = tmp[1];
					fputs(buffer, fp);
					buff_size++;
					fputc('\n', fp);
					current_char = 0;
					is_line_new = 1;
				}
				else {
					sprintf(tmp, "%02X", op_tab[current++]);
					buff_size += 2;
					strcat(buffer, tmp);
				}
			}
			else if (form_list[current] == 2) {
				current_char += 4;
				buff_length += 4;
				if (current_char > 69) {
					current_char -= 4;
					sprintf(tmp, "%02X", (current_char - 9) / 2) + 1;
					buff_size++;
					buffer[7] = tmp[0];
					buff_size++;
					buffer[8] = tmp[1];
					fputs(buffer, fp);
					fputc('\n', fp);
					current_char = 0;
					sprintf(buffer, "T%06X", location_counter_index[current]);
					buff_size += 7;
					current_char += 7;
					strcat(buffer, "00");
					buff_size += 2;
					current_char += 2;
					sprintf(tmp, "%04X", op_tab[current++]);
					buff_size += 4;
					strcat(buffer, tmp);
					current_char += 4;
				}
				else if (form_list[current + 1] <= 0 && symbol_token[current + 1].operator1 != NULL && strcmp(symbol_token[current + 1].operator1, "END") != 0) {
					sprintf(tmp, "%04X", op_tab[current++]);
					buff_size += 2;
					strcat(buffer, tmp);
					sprintf(tmp, "%02X", (current_char - 9) / 2) + 1;
					buff_size++;
					buffer[7] = tmp[0];
					buff_size++;
					buffer[8] = tmp[1];
					fputs(buffer, fp);
					buff_size++;
					fputc('\n', fp);
					current_char = 0;
					is_line_new = 1;
				}
				else {
					sprintf(tmp, "%04X", op_tab[current++]);
					buff_size += 4;
					strcat(buffer, tmp);
				}
			}
			else if (form_list[current] == 3) {
				current_char += 6;
				if (current_char > 69) {
					current_char -= 6;
					buff_size -= 6;
					sprintf(tmp, "%02X", (current_char - 8) / 2);
					buff_size++;
					buffer[7] = tmp[0];
					buff_size++;
					buffer[8] = tmp[1];
					fputs(buffer, fp);
					buff_size++;
					fputc('\n', fp);
					current_char = 0;
					sprintf(buffer, "T%06X", location_counter_index[current]);
					buff_size += 7;
					current_char += 7;
					buff_size += 2;
					strcat(buffer, "00");
					current_char += 2;
					buff_size += 2;
					sprintf(tmp, "%06X", op_tab[current++]);
					buff_size += 6;
					strcat(buffer, tmp);
					current_char += 6;
				}
				else if (form_list[current + 1] <= 0 && symbol_token[current + 1].operator1 != NULL && strcmp(symbol_token[current + 1].operator1, "END") != 0) {
					sprintf(tmp, "%06X", op_tab[current++]);
					buff_size += 6;
					strcat(buffer, tmp);
					sprintf(tmp, "%02X", (current_char - 9) / 2);
					buffer[7] = tmp[0];
					buff_size++;
					buffer[8] = tmp[1];
					buff_size++;
					fputs(buffer, fp);
					buff_size++;
					fputc('\n', fp);
					current_char = 0;
					is_line_new = 1;
				}
				else {
					sprintf(tmp, "%06X", op_tab[current++]);
					buff_size += 7;
					strcat(buffer, tmp);
				}
			}
			else if (form_list[current] == 4) {
				current_char += 8;
				buff_size += 8;
				if (current_char > 69) {
					current_char -= 8;
					buff_size -= 8;
					sprintf(tmp, "%02X", (current_char - 9) / 2);
					buffer[7] = tmp[0];
					buff_size++;
					buffer[8] = tmp[1];
					buff_size++;
					fputs(buffer, fp);
					fputc('\n', fp);
					current_char = 0;
					buff_size += 7;
					sprintf(buffer, "T%06X", location_counter_index[current]);
					current_char += 7;
					strcat(buffer, "00");
					buff_size += 2;
					current_char += 2;
					sprintf(tmp, "%08X", op_tab[current++]);
					strcat(buffer, tmp);
					buff_size += 8;;
					current_char += 8;

				}
				else if (form_list[current + 1] <= 0 && symbol_token[current + 1].operator1 != NULL && strcmp(symbol_token[current + 1].operator1, "END") != 0) {
					sprintf(tmp, "%08X", op_tab[current++]);
					buff_size += 8;
					strcat(buffer, tmp);
					sprintf(tmp, "%02X", (current_char - 9) / 2);
					buff_size++;
					buffer[7] = tmp[0];
					buff_size++;
					buffer[8] = tmp[1];
					fputs(buffer, fp);
					buff_size++;
					fputc('\n', fp);
					current_char = 0;
				}
				else {
					buff_size += 8;
					sprintf(tmp, "%08X", op_tab[current++]);
					strcat(buffer, tmp);
				}
			}
			else {
				if (symbol_token[current].operator1 != NULL && strcmp(symbol_token[current].operator1, "END") == 0)
					tmp_end = current;
				else if (symbol_token[current - 2].operator1 != NULL && strcmp(symbol_token[tmp_end].operator1, "END") == 0 && symbol_token[current].operator1 == NULL) {
					buff_size++;
					sprintf(tmp, "%02X", (current_char - 9) / 2);
					buff_size++;
					buffer[7] = tmp[0];
					buff_size++;
					buffer[8] = tmp[1];
					fputs(buffer, fp);
					fputc('\n', fp);
				}
				else {
					//buff_size;
					is_line_new = 1;
					current_char = 0;
				}
				current++;
			}
		}
		current--;
		for (int i = program_start_line - 1; i <= program_end_line; i++) {
			if (strcmp(symbol_token[i].operator1, "EXTREF") == 0) { //�ܺ����� ó��
				char* index = NULL;
				if (symbol_token[i].operand[0] != NULL) {
					for (int j = program_start_line; j <= program_end_line; j++) {
						if (symbol_token[j].operand[0] != NULL) {
							if ((index = strstr(symbol_token[j].operand[0], symbol_token[i].operand[0])) != NULL) {
								if (search_opcode(symbol_token[j].operator1) >= 0)	fprintf(fp, "M%06X05+%s\n", location_counter_index[j] + 1, symbol_token[i].operand[0]);
								else {
									if ((index - 1)[0] == '-') fprintf(fp, "M%06X06-%s\n", location_counter_index[j], symbol_token[i].operand[0]);
									else fprintf(fp, "M%06X06+%s\n", location_counter_index[j], symbol_token[i].operand[0]);
									buff_size++;
								}
							}
						}
					}
				}
				if (symbol_token[i].operand[1] != NULL) {
					for (int j = program_start_line; j <= program_end_line; j++) {
						if (symbol_token[j].operand[0] != NULL) {
							if ((index = strstr(symbol_token[j].operand[0], symbol_token[i].operand[1])) != NULL) {
								if (search_opcode(symbol_token[j].operator1) >= 0)	fprintf(fp, "M%06X05+%s\n", location_counter_index[j] + 1, symbol_token[i].operand[1]);
								else {
									if ((index - 1)[0] == '-') fprintf(fp, "M%06X06-%s\n", location_counter_index[j], symbol_token[i].operand[1]);
									else fprintf(fp, "M%06X06+%s\n", location_counter_index[j], symbol_token[i].operand[1]);
								}
							}
						}
					}
				}
				if (symbol_token[i].operand[2] != NULL) {
					for (int j = program_start_line; j <= program_end_line; j++) {
						if (symbol_token[j].operand[0] != NULL) {
							if ((index = strstr(symbol_token[j].operand[0], symbol_token[i].operand[2])) != NULL) {
								if (search_opcode(symbol_token[j].operator1) >= 0)	fprintf(fp, "M%06X05+%s\n", location_counter_index[j] + 1, symbol_token[i].operand[2]);
								else {
									if ((index - 1)[0] == '-') fprintf(fp, "M%06X06-%s\n", location_counter_index[j], symbol_token[i].operand[2]);
									else fprintf(fp, "M%06X06+%s\n", location_counter_index[j], symbol_token[i].operand[2]);
								}
							}
						}
					}
				}
				break;
			}
		}
		if (extends_position == 0) fprintf(fp, "E%06X\n\n", location_counter_index[program_start_line]);
		else fputs("E\n\n", fp);
		extends_position++;
		current++;
	}
	fclose(fp);
}

