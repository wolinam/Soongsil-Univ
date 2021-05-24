/*
 * 화일명 : my_assembler_20180474.c
 * 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
 * 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
 * 파일 내에서 사용되는 문자열 "00000000"에는 자신의 학번을 기입한다.
 */

 /*
  * 프로그램의 헤더를 정의한다.
  */

#define _CRT_SECURE_NO_WARNINGS
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>


#include "my_assembler_20180474.h"

/* -----------------------------------------------------------------------------------
* 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
* 매계 : 실행 파일, 어셈블리 파일
* 반환 : 성공 = 0, 실패 = < 0
* 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다.
*		   또한 중간파일을 생성하지 않는다.
* -----------------------------------------------------------------------------------
*/
int main(int argc, char *argv[]) {

	label_num = 0;
	if (init_my_assembler() < 0) {
		printf("init_my_assembler: 프로그램 초기화에 실패 했습니다.\n");
		return -1;
	}
	if (assem_pass1() < 0) {
		printf("assem_pass1: 패스1 과정에서 실패하였습니다.  \n");
		return -1;
	}
	if (assem_pass2() < 0) {
		printf(" assem_pass2: 패스2 과정에서 실패하였습니다.  \n");
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
* 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다.
* 매계 : 없음
* 반환 : 정상종료 = 0 , 에러 발생 = -1
* 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기
*		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
*		   구현하였다.
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
* 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을
*        생성하는 함수이다.
* 매계 : 기계어 목록 파일
* 반환 : 정상종료 = 0 , 에러 < 0
* 주의 : 기계어 목록파일 형식은 다음과 같다.
*
*	===============================================================================
*		   | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
*	===============================================================================
*
* -----------------------------------------------------------------------------------
*/

int init_inst_file(char* inst_file) {
	FILE* file = NULL;
	int count = 0;//명령어를 한줄 한줄 테이블에 저장할 때 사용하는 인덱스
	char c; // 문자를 하나씩 받아옴 fgetc
	char tmp[100]; //c로 한글자씩 읽고 여기에 글자 저장
	int idx = 0; // 한글자씩 읽어올 때 사용하는 인덱스

	file = fopen(inst_file, "r");

	if (file == NULL) {
		printf("inst_file를 여는데에 실패하였습니다.\n");
		return -1;
	}

	while ((c = fgetc(file)) != EOF) { //한글자씩 읽어옴
		if (c != '\n') 
			tmp[idx++] = c;
		
		else { //명령어를 다 읽었으면
			tmp[idx] = '\0';//문자열로의 처리
			//strtok 토큰별로의 저장
			strcpy(inst_table[count].name, strtok(tmp, "\t")); //첫번째 탭까지(명령어)를 name에 저장
			inst_table[count].type_format = atoi(strtok(NULL, "\t")); //ascii to integer, format
			inst_table[count].opcode = strtol(strtok(NULL, "\t"), NULL, 16); //받은 문자열을 16진수로 표현, opcode
			inst_table[count].operands = atoi(strtok(NULL, "\t")); // 오퍼랜드갯수
	
			count++;
			inst_index++; //총개수
			idx = 0;
		}
	}
	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 할 소스코드를 읽어 소스코드 테이블(input_data)를 생성하는 함수이다.
 * 매계 : 어셈블리할 소스파일명
 * 반환 : 정상종료 = 0 , 에러 < 0
 * 주의 : 라인단위로 저장한다.
 *
 * ----------------------------------------------------------------------------------
 */


int init_input_file(char* input_file) {
	FILE* file = NULL;
	char c; // 문자의 저장
	char tmp[200]; // 임시용
	int instruction_cnt = 0;// 명령어 개수
	int line_instruction_cnt = 0; // 한줄에있는 글자수

	file = fopen(input_file, "r");
	if (file == NULL) {
		printf("input_file을 여는데에 있어 실패하셨습니다.\n");
		return -1;
	}

	while ((c = fgetc(file)) != EOF) { //파일을 읽음 EOF까지
		if (c == '\n') { //개행이 나오면 
			tmp[line_instruction_cnt] = '\0'; // 개행대신 null문자 넣어서 구분
			line_instruction_cnt = 0; //아까 위에서 i같은 역할
			input_data[instruction_cnt] = (char*)malloc(strlen(tmp));
			strcpy(input_data[instruction_cnt], tmp); //위에서 개행대신 null문자 넣었으므로 명령어 한 줄이 들어감

			instruction_cnt++;
		}
		//문자열 저장
		else tmp[line_instruction_cnt++] = c;
	}
	line_num = instruction_cnt; //헤더파일에 static int로 정의됨

	fclose(file);
	return 0;
}


/* -----------------------------------------------------------------------------------
* 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다.
*        패스 1로 부터 호출된다.
* 매계 : 소스코드의 라인번호
* 반환 : 정상종료 = 0 , 에러 < 0
* 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다.
* -----------------------------------------------------------------------------------
*/

int token_parsing(int index) {

	//에러
	if (index < 0)
		return -1;

	//현재 index에다가 지금까지 추가된 ltorg line 수까지 더해야 실제 위치한 라인의 index가 됨
	symbol_token[index + ltorg_line_sum].extents = extents;

	if (input_data[index][0] == '.') { //index라인의 첫번째 글자가 주석일 때
		symbol_token[index + ltorg_line_sum].label = ".";
		symbol_token[index + ltorg_line_sum].operator1 = NULL;
		symbol_token[index + ltorg_line_sum].operand[0] = NULL;
		symbol_token[index + ltorg_line_sum].operand[1] = NULL;
		symbol_token[index + ltorg_line_sum].comment = "\0";
		return 0;
	}
	else if (input_data[index][0] == '\t' || input_data[index][0] == ' ') { //라벨이 없을 때
		char* tmp;
		char* tmp_data = (char*)malloc(strlen(input_data[index]) + 1); // 임시용으로 동적을 할당한다.
		char* tmp_token = NULL; // 임시용
		char* tmp_operand = NULL;

		strcpy(tmp_data, input_data[index]); //임시용에다 백업
		symbol_token[index + ltorg_line_sum].label = NULL;
		tmp_token = strtok(tmp_data, " \t");

		//ltorg_line_sum과 liter_sum은 헤더에 선언
		symbol_token[index + ltorg_line_sum].operator1 = (char*)malloc(strlen(tmp_token) + 1);// 임시만큼 동적할당

		strcpy(symbol_token[index + ltorg_line_sum].operator1, tmp_token); // 임시용을 복사한다.

		tmp_token = strtok(NULL, "\n"); // 오퍼레이터를 없앤 직전의 문자열을 그대로 사용

		if (tmp_token == NULL || tmp_token[0] == '\t' || tmp_token[0] == ' ') { //오퍼랜드가 없을 때
			if (strcmp(symbol_token[index + ltorg_line_sum].operator1, "LTORG") == 0) { //operator1 == LTORG 일 때 처리
				//LTORG를 만나면 앞의 모든 literal을 저장함
				for (int i = 0; i < liter_sum; i++) { //literal table 안에 있는 총 literal의 개수만큼 반복
					if (liter_tab[i].extents == symbol_token[index + ltorg_line_sum].extents) { //해당 영역이 동일하면
						symbol_token[index + (++ltorg_line_sum)].operator1 = "*"; //LTORG 다음 줄에 *표 넣고
						symbol_token[index + ltorg_line_sum].operand[0] = (char*)malloc(strlen(liter_tab[i].literal) + 1);
						strcpy(symbol_token[index + ltorg_line_sum].operand[0], liter_tab[i].literal); //오퍼랜드 자리에 literal 쓰기
						symbol_token[index + ltorg_line_sum].extents = extents;
					}
				}
			}

			if (tmp_token != NULL) { //오퍼랜드는 없는데 뒤에 필요없는 문장이 있어서 tmp가 null이 아닐 때
				char* tmp2_token = (char*)malloc(strlen(tmp_token) + 1);
				strcpy(tmp2_token, tmp_token);
				symbol_token[index + ltorg_line_sum].operand[0] = NULL;
				symbol_token[index + ltorg_line_sum].operand[1] = NULL;
				symbol_token[index + ltorg_line_sum].comment = strtok(tmp2_token, "\t"); //뒤에 붙어있던 설명문을 comment에 넣음
				return 0;
			}
		}
		else { //오퍼랜드가 만약에 존재한다면.
			tmp_operand = strtok(tmp_token, "\t"); //받아들인 문장을 탭으로 구분함(뒤에 불필요한 문장 빼기위해)
			tmp = strtok(tmp_operand, ",");//오퍼랜드가 하나만 있는게 아니므로 또 쉼표로 오퍼랜드를 구분함
			symbol_token[index + ltorg_line_sum].operand[0] = (char*)malloc(strlen(tmp) + 1);
			strcpy(symbol_token[index + ltorg_line_sum].operand[0], tmp); // 첫 번째 오퍼랜드

			if (symbol_token[index + ltorg_line_sum].operand[0][0] == '=') { // 오퍼랜드의 첫번째 글자가 =일 때. =C'EOF' 이런거. 리터럴 테이블에 저장을 해준다.
				if (search_placed_literal(symbol_token[index + ltorg_line_sum].operand[0], extents) != 0) {
					liter_tab[liter_sum].literal = (char*)malloc(strlen(symbol_token[index + ltorg_line_sum].operand[0])); //공간 할당
					strcpy(liter_tab[liter_sum].literal, symbol_token[index + ltorg_line_sum].operand[0]); //리터럴 테이블에 저장
					liter_tab[liter_sum++].extents = extents; //해당 영역번호 update
				}
			}
			tmp = strtok(NULL, ","); //직전 문장 그대로 사용 후 다시 쉼표로 구분. 그리고 그걸 tmp에 저장
			if (tmp != NULL) { //오퍼랜드가 2개라면
				symbol_token[index + ltorg_line_sum].operand[1] = (char*)malloc(strlen(tmp) + 1);
				strcpy(symbol_token[index + ltorg_line_sum].operand[1], tmp);
				tmp = strtok(NULL, ",");// 앞 과정 반복
				if (tmp != NULL) { //오퍼랜드가 3개라면
					symbol_token[index + ltorg_line_sum].operand[2] = (char*)malloc(strlen(tmp) + 1);
					strcpy(symbol_token[index + ltorg_line_sum].operand[2], tmp);
				}
			}
			else if (strcmp(symbol_token[index + ltorg_line_sum].operator1, "END") == 0) {  //LTORG를 만나지 않은 리터럴들 마지막에 처리
				for (int i = 0; i < liter_sum; i++) {
					if (liter_tab[i].extents == symbol_token[index + ltorg_line_sum].extents) {
						symbol_token[index + (++ltorg_line_sum)].operator1 = "*"; //예를들면, 다음줄에 * =X'05'로 넣음
						symbol_token[index + ltorg_line_sum].operand[0] = (char*)malloc(strlen(liter_tab[i].literal) + 1);
						strcpy(symbol_token[index + ltorg_line_sum].operand[0], liter_tab[i].literal);
						symbol_token[index + ltorg_line_sum].extents = extents;
					}
				}
			}
			free(tmp_data);  //코멘트 넣기
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
	else { //라벨이 있는 경우
		char* tmp;
		char* tmp_data = (char*)malloc(strlen(input_data[index]) + 1); //동적할당
		char* tmp_token = NULL; // 임시용
		char* tmp_operand = NULL;
	
		strcpy(tmp_data, input_data[index]);//백업
		tmp_token = strtok(tmp_data, " \t");//탭으로 구분
		symbol_token[index + ltorg_line_sum].label = (char*)malloc(strlen(tmp_token) + 1);//임시용에 동적할당
		strcpy(symbol_token[index + ltorg_line_sum].label, tmp_token); //토큰 분리된 것을 label에 저장

		tmp_token = strtok(NULL, " \t");//직전 문장을 다시 탭으로 구분
		symbol_token[index + ltorg_line_sum].operator1 = (char*)malloc(strlen(tmp_token) + 1);//임시용에 동적할당
		strcpy(symbol_token[index + ltorg_line_sum].operator1, tmp_token); // operator저장
		if (strcmp(symbol_token[index + ltorg_line_sum].operator1, "CSECT") == 0) extents++; //영역 구분을 더해준다.

		tmp_token = strtok(NULL, "\n");  // operator 없어진 직전의 문장

		if (tmp_token == NULL || tmp_token[0] == '\t' || tmp_token[0] == ' ') { //operand 없을 때
			if (tmp_token != NULL) { //operand는 없지만 코멘트가 있음
				char* tmp2_token = (char*)malloc(strlen(tmp_token) + 1);
				strcpy(tmp2_token, tmp_token);//코멘트 복사
				symbol_token[index + ltorg_line_sum].operand[0] = NULL;
				symbol_token[index + ltorg_line_sum].operand[1] = NULL;
				symbol_token[index + ltorg_line_sum].comment = strtok(tmp2_token, "\t");//코멘트 저장
				return 0;
			}
		}
		else { //오퍼랜드가 있다면
			tmp_operand = strtok(tmp_token, "\t");
			tmp = strtok(tmp_operand, ",");//오퍼랜드가 더 있을 수 있기 때문에 구분자 사용
			symbol_token[index + ltorg_line_sum].operand[0] = (char*)malloc(strlen(tmp) + 1);
			strcpy(symbol_token[index + ltorg_line_sum].operand[0], tmp); // 쳣 번째 오퍼랜드 저장
			if (symbol_token[index + ltorg_line_sum].operand[0][0] == '=') { // 오퍼랜드에 저장한게 리터럴인지 확인, 만약 리터럴이면
				if (search_placed_literal(symbol_token[index + ltorg_line_sum].operand[0], extents) != 0) {
					liter_tab[liter_sum].literal = (char*)malloc(strlen(symbol_token[index + ltorg_line_sum].operand[0])); //공간할당
					strcpy(liter_tab[liter_sum].literal, symbol_token[index + ltorg_line_sum].operand[0]); //리터럴저장
					liter_tab[liter_sum++].extents = extents;
				}
			}
			tmp = strtok(NULL, ",");
			if (tmp != NULL) { //두 번째 오퍼랜드
				symbol_token[index + ltorg_line_sum].operand[1] = (char*)malloc(strlen(tmp) + 1);
				strcpy(symbol_token[index + ltorg_line_sum].operand[1], tmp);
				tmp = strtok(NULL, ",");
				if (tmp != NULL) { //세 번째 오퍼랜드
					symbol_token[index + ltorg_line_sum].operand[2] = (char*)malloc(strlen(tmp) + 1);
					strcpy(symbol_token[index + ltorg_line_sum].operand[2], tmp);
				}
			}
			free(tmp_data); //코멘트만 있는거 저장
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
* 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다.
* 매계 : 토큰 단위로 구분된 문자열
* 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0
* 주의 :
*
* -----------------------------------------------------------------------------------
*/

int search_opcode(char* str) {
	if (str[0] == '+') { //4형식
		str = str + 1; //하나를 늘려준다.
	}
	//인덱스를 탐색한다.
	for (int i = 0; i < inst_index; i++) {
		if (strcmp(str, inst_table[i].name) == 0) //비교해서 같으면 인덱스 반환
			return i;
	}
	if (str == NULL)
		return -1;
	return -1;
}

/* -----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
*		   패스1에서는..
*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
*		   테이블을 생성한다.
*
* 매계 : 없음
* 반환 : 정상 종료 = 0 , 에러 = < 0
* 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
*	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
*
* -----------------------------------------------------------------------------------
*/

static int assem_pass1(void) { 
	//토큰파싱 함수를 호출하여 테이블을 생성한다.

	int check_index = 0;
	int opcode_idx; // opcode 인덱스
	int check_sum = 0;
	token_table[check_index];

	locctr = 0; //location counter, 헤더에 static int로 선언
	int extents = 0; // symbol을 extents별(영역별)로 저장하기 위해서

	//token_parsing 실행, 그리고 만약 에러 발생하면 -1
	for (int i = 0; i < line_num; i++) {
		if (token_parsing(i) < 0) return -1;
	}

	token_line = line_num + ltorg_line_sum; //입력받았던 소스코드 라인수에다 LTORG 라인 수까지 더한 것이 table의 총 라인 수
	check_index++;
	check_sum += check_index;
	token_table[check_index];

	for (int i = 0; i < token_line; i++) {
		if (symbol_token[i].operator1 == NULL) { //operator가 NULL일 때 
			location_counter_index[i] = -1; //-1 내보내고 다음 조건문 실행
			check_sum++;
			continue;
		}

		opcode_idx = search_opcode(symbol_token[i].operator1);//search_opcode로 찾아서 반환한 인덱스 번호를 opcode_idx에 저장

		if (opcode_idx < 0) { //명령어가 아닐 때 (지시어 등등)
			if (strcmp(symbol_token[i].operator1, "RESW") == 0) { //RESERVED WORD일때의 처리
				location_counter_index[i] = locctr; //해당 location counter를 location index에 저장
				locctr += 3 * atoi(symbol_token[i].operand[0]); // 1워드당 3바이트이므로 3을 곱해준 것을 더한다.
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "RESB") == 0) {//RESERVED BYTE
				location_counter_index[i] = locctr;
				locctr += atoi(symbol_token[i].operand[0]); //1byte이므로 그냥 더해준다 **atoi 문자열을 숫자로 변환
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "EXTDEF") == 0 //RESW RESB 아닌 경우의 처리
				|| strcmp(symbol_token[i].operator1, "END") == 0
				|| strcmp(symbol_token[i].operator1, "LTORG") == 0
				|| strcmp(symbol_token[i].operator1, "EXTREF") == 0
				) {
				location_counter_index[i] = -1; // location count가 없을 때의 예외 처리
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "CSECT") == 0) { //CSECT일때
				location_counter_index[i] = locctr;
				extents++; //구역이 바뀌므로 ++
				locctr = 0;
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "EQU") == 0) { //EQU일 때는 메모리를 잡아먹지 않으므로 3을 더하지 않는다.
				location_counter_index[i] = locctr;
				if (symbol_token[i].operand[0][0] == '*'); //연산 없음. 그냥 BUFEND 표시
				else {
					char* tmp;
					char* tmp_token;
					char opcode[2]; // +(플러스),-(마이너스),*(곱하기),%(나머지),/(나누기) 등의 연산자를 저장하여준다.
					int value_one;
					int value_two;
					check_sum++;

					tmp = (char*)malloc(strlen(symbol_token[i].operand[0]) + 1);//공간할당

					strcpy(tmp, symbol_token[i].operand[0]); //복제
					for (int j = 0; symbol_token[i].operand[0][j] != '\0'; j++) { //예를 들면 BUFEND-BUFFER의 문자 끝까지 한글자씩 for문 실행 후. 연산자 찾기
						if (symbol_token[i].operand[0][j] == '/' || //연산자가 나오면
							symbol_token[i].operand[0][j] == '%' ||
							symbol_token[i].operand[0][j] == '*' ||
							symbol_token[i].operand[0][j] == '-' ||
							symbol_token[i].operand[0][j] == '+') {
							opcode[1] = '\0'; //나중에 연산을 위해 연산자 저장. 그러나 문자열 끝에는 null이 있어야하므로 크기 하나 더 만들어서 연산자와 null저장
							opcode[0] = symbol_token[i].operand[0][j]; //예시 '-' \0 저장
							check_sum++;
						}
					}
					tmp_token = strtok(tmp, opcode); //연산자 앞까지 자르고 tmp_token에 저장
					for (int k = 0; k < symbol_index; k++) { //index만큼 for문 반복해서
						if (strcmp(tmp_token, sym_table[k].symbol) == 0) //symbol과 tmp_token비교. 예를 들면 BUFEND와 다른 symbol비교
							value_one = sym_table[k].addr; //비교해서 해당 index를 찾고 addr 반환 ex.1033저장
						check_sum++;
					}
					tmp_token = strtok(NULL, "\0"); //나머지 문장 받음
					for (int k = 0; k < symbol_index; k++) {
						if (strcmp(tmp_token, sym_table[k].symbol) == 0)
							value_two = sym_table[k].addr; //위의 과정 반복 ex.0033저장
						check_sum++;
					}
					//연산자, value_one, value_two 받아서 계산. 계산 후 locctr에 저장
					if (opcode[0] == '+')	locctr = value_one + value_two;
					else if (opcode[0] == '-') 	locctr = value_one - value_two;
					else if (opcode[0] == '*')	locctr = value_one * value_two;
					else if (opcode[0] == '%')	locctr = value_one % value_two;
					else if (opcode[0] == '/')	locctr = value_one / value_two;
					token_table[check_index]->operand;
					location_counter_index[i] = locctr; //locctr 저장
					check_sum++;
				}
			}

			//시작
			else if (strcmp(symbol_token[i].operator1, "START") == 0)
				location_counter_index[i] = locctr;

			else if (strcmp(symbol_token[i].operator1, "*") == 0) { //리터럴에 있는 *의 처리
				location_counter_index[i] = locctr;

				if (symbol_token[i].operand[0][1] == 'C') { //예를들어 C = 'EOF'
					int ch_num = 0;
					for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++) //EOF
						ch_num++; //글자 수 카운트
					locctr += ch_num;
					check_sum++;
				}
				else if (symbol_token[i].operand[0][1] == 'X') { //예를들어 X='05'
					int x_num = 0;
					check_sum++;
					for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++) //05
						x_num++; //숫자 카운트
					locctr += (x_num / 2); //16진수로 표현되는 addr은 6숫자. 2숫자씩 1byte. 총 6숫자니까 3byte라서 1word
				}
				location_counter_index[i + 1] = locctr; 
				check_sum++;
			}
			else if (strcmp(symbol_token[i].operator1, "BYTE") == 0) { //지시어가 BYTE일 때, 위에랑 같은 과정 반복
				location_counter_index[i] = locctr;
				if (symbol_token[i].operand[0][0] == 'C') { //C'EOF'이런거
					strcmp(token_table[check_index]->operand[1], "C");
					int ch_num = 0;
					check_sum++;
					for (int j = 2; symbol_token[i].operand[0][j] != '\''; j++)
						ch_num++;
					locctr += ch_num;
					strcmp(token_table[check_index]->operand[1], "W");
				}
				else if (symbol_token[i].operand[0][0] == 'X') { //X'F1'이런거
					int x_num = 0;
					for (int j = 2; symbol_token[i].operand[0][j] != '\''; j++)
						x_num++;
					locctr += (x_num / 2);
					check_sum++;
				}
			}
			else if (strcmp(symbol_token[i].operator1, "WORD") == 0) {
				location_counter_index[i] = locctr;
				locctr += 3; //word라서 3
				check_sum++;
			}
		}
		else { //명령어일 때
			location_counter_index[i] = locctr;
			if (inst_table[opcode_idx].type_format == 1) 	locctr += 1; // 1format
			else if (inst_table[opcode_idx].type_format == 2) locctr += 2; //2format
			else { //3,4 format
				if (strchr(symbol_token[i].operator1, '+') == NULL) locctr += 3; //3format
				else locctr += 4;
				check_sum++;
			}
		}
		if (symbol_token[i].label != NULL) { //심볼테이블 생성
			if (search_symbol(symbol_token[i].label, extents) != 0) { //만약 라벨이 심볼에 없으면 저장해준다.
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
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 4번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*        또한 과제 4번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
* -----------------------------------------------------------------------------------
*/

/* ----------------------------------------------------------------------------------
void make_opcode_output(char* file_name)
{
	 add your code here
	FILE* fp = NULL;
	fp = fopen(file_name, "wt");
	for (int i = 0; i <= line_num; i++) {
		if (symbol_token[i].label != NULL) //라벨이 있는 경우
			fprintf(fp, "%s \t", symbol_token[i].label);
		else
			fputs("\t", fp); //라벨이 없으면 그냥 탭치기

		if (symbol_token[i].operator1 != NULL) //오퍼레이터가 있는경우
			fprintf(fp, "%s \t", symbol_token[i].operator1);
		else
			fputs("\t", fp); //오퍼레이터 없는 경우

		if (symbol_token[i].operand[2] != NULL) //세번째 오퍼랜드가 있는 경우
			fprintf(fp, "%s,%s,%s \t", symbol_token[i].operand[0], symbol_token[i].operand[1], symbol_token[i].operand[2]);
		else if (symbol_token[i].operand[1] != NULL) //두번째까지만 오퍼랜드가 있을 때
			fprintf(fp, "%s,%s \t\t\t", symbol_token[i].operand[0], symbol_token[i].operand[1]);
		else if (symbol_token[i].operand[0] != NULL) //오퍼랜드가 하나만 있을 때
			fprintf(fp, "%s \t\t\t", symbol_token[i].operand[0]);
		else //오퍼랜드가 없을 때
			fputs("\t \t \t ", fp);


		if (symbol_token[i].operator1 != NULL) { //오퍼레이터가 있으면
			int j = search_opcode(symbol_token[i].operator1); //함수를 호출해서 오퍼레이터를 기계어 목록 테이블에서 어느 인덱스에 해당하는지 찾음
			if (j != -1) //오퍼레이터가 명령어가 아닌 지시어일때
				fprintf(fp, "%X \t \t", inst_table[j].opcode);
			else
				fputs("\t \t", fp);
		}

		if (symbol_token[i].comment != NULL) //코멘트가 있을 때
			fprintf(fp, "%s \n", symbol_token[i].comment);
		else
			fputs("\n", fp);
	}

	//위에 코드와 동일. 콘솔화면에도 띄우기 위해 작성함.
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

//pass1에서 사용하기위해 새로 추가, tab에 있는지 없는지 확인
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
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 SYMBOL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char* file_name) //symtab출력
{
	printf("symtab\n\n");
	for (int i = 0; i <= line_num + ltorg_line_sum; i++) {
		if (symbol_token[i].label != NULL && symbol_token[i].label != ".") { //라벨이 있는 경우
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
		if (symbol_token[i].label != NULL && symbol_token[i].label != ".") { //라벨이 있는 경우
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
	* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
	*        여기서 출력되는 내용은 LITERAL별 주소값이 저장된 TABLE이다.
	* 매계 : 생성할 오브젝트 파일명
	* 반환 : 없음
	* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
	*        화면에 출력해준다.
	*
	* -----------------------------------------------------------------------------------
	*/
void make_literal_output(char* file_name) //liter tab 출력
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
		* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
		*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
		*		   다음과 같은 작업이 수행되어 진다.
		*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
		* 매계 : 없음
		* 반환 : 정상종료 = 0, 에러발생 = < 0
		* 주의 :
		* -----------------------------------------------------------------------------------
		*/
static int assem_pass2(void)
{
	/* add your code here }*/
	int idx_inst;//오퍼레이터의 opcode 인덱스
	int tmp;

	for (int i = 0; i < token_line; i++) {
		if (symbol_token[i].operator1 == NULL) continue; //오퍼레이터 NULL이면 생략하고(무시) 계속 다음 i로 for문 진행

		idx_inst = search_opcode(symbol_token[i].operator1);//오퍼레이터의 opcode 인덱스 찾아서 저장
		{//else 탈출 생략

			//형식마다 다르게 처리
			switch (inst_table[idx_inst].type_format) { 
			case 1: //1형식
				op_tab[i] = inst_table[idx_inst].opcode; //인덱스에 해당하는 opcode 저장
				form_list[i] = 1; //해당 인덱스의 format은 1형식
				break;

			case 2: //2형식
				op_tab[i] = inst_table[idx_inst].opcode;

				op_tab[i] = op_tab[i] << 4; //8bit opcode에 이어서 4bit 레지스터 2개 넣기위해 shift left!! 먼저 16진수의 1바이트 shift left 처리(4bit)
				if (strcmp(symbol_token[i].operand[0], "SW") == 0) op_tab[i] |= 9;
				else if (strcmp(symbol_token[i].operand[0], "PC") == 0) op_tab[i] |= 8;
				else if (strcmp(symbol_token[i].operand[0], "F") == 0) op_tab[i] |= 6;
				else if (strcmp(symbol_token[i].operand[0], "T") == 0) op_tab[i] |= 5;
				else if (strcmp(symbol_token[i].operand[0], "S") == 0) op_tab[i] |= 4;
				else if (strcmp(symbol_token[i].operand[0], "B") == 0) op_tab[i] |= 3;
				else if (strcmp(symbol_token[i].operand[0], "L") == 0) op_tab[i] |= 2;
				else if (strcmp(symbol_token[i].operand[0], "X") == 0) op_tab[i] |= 1;
				else if (strcmp(symbol_token[i].operand[0], "A") == 0) op_tab[i] |= 0;

				op_tab[i] = op_tab[i] << 4; // 16진수의 1바이트의 쉬프트레프트의 처리(4bit)
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
					//@ indirect와 #immediate 가 아닐 때 처리
					if (symbol_token[i].operand[0][0] != '@' && symbol_token[i].operand[0][0] != '#') op_tab[i] += 3; //2진수 11 추가 ni==11
					else if (symbol_token[i].operand[0][0] == '#') op_tab[i] += 1;// 2진수 1추가 ni==01
					else if (symbol_token[i].operand[0][0] == '@') op_tab[i] += 2;//2진수에서 10을 추가해준다. ni==10
				}
				else	op_tab[i] += 3; //2진수에서 11을 추가해준다. 예를 들면 RSUB. ni==11

				//xbpe
				op_tab[i] <<= 1;// X가 들어가기 위한 공간을 할당 .1bit
				if (symbol_token[i].operand[1] != NULL) {
					if (symbol_token[i].operand[1][0] == 'X') op_tab[i] |= 1; //X에 대한 공간을 1로 할당하여 준다.
				}
				op_tab[i] <<= 1; //b가 들어가기 위한 공간
				op_tab[i] |= 0; // 2진수에서 0을 추가한다.
				op_tab[i] <<= 2; // p와 e의 공간을 만들어준다.
				if (symbol_token[i].operand[0] != NULL && symbol_token[i].operator1[0] != '+') { //4형식이 아니고(3형식이고) 오퍼랜드가 있으면
					if (symbol_token[i].operand[0][0] == '@') { //오퍼랜드가 indirect일 때
						int ta = 0; // 타겟 어드레스
						int pc = 0; // Program Counter
						int disp = 0;
						op_tab[i] |= 2; //p의 2진수에서 10을 추가해준다.

						op_tab[i] <<= 12; // disp의 공간할당
						pc = location_counter_index[i + 1]; // Program counter의 값을 받는다. 다음에 실행할 명령문이므로 i+1
						for (int j = 0; j < symbol_index; j++) {
							if (symbol_token[i].operand[0] != NULL && symbol_token[i].extents == sym_table[j].extents && strcmp(sym_table[j].symbol, symbol_token[i].operand[0] + 1) == 0)
								//예를 들어 LDA LENGTH 이면 오퍼랜드 LENGTH와 심볼을 비교하여 LENGTH를 찾고 그 주소를 가져와야함.
								ta = sym_table[j].addr; //심볼을 관리하는 구조체. 헤더에 있음
						}
						disp = ta - pc;
						disp &= 0xfff;
						op_tab[i] += (ta-pc);
					}

					else if (symbol_token[i].operand[0][0] == '#') { //오퍼랜드가 #(immediate)일 때
						char* tmp = NULL;
						int index = 0;
						op_tab[i] <<= 12; // disp의 공간 할당
						tmp = (char*)malloc(strlen(symbol_token[i].operand[0])); //공간할당
						for (int j = 0; j < strlen(symbol_token[i].operand[0]) - 1; j++)
							tmp[index++] = symbol_token[i].operand[0][j + 1];
						tmp[index] = '\0';
						op_tab[i] += strtol(tmp, NULL, 16); //16진수로 변환
						free(tmp);
					}

					else if (symbol_token[i].operand[0][0] == '=') { //리터럴일때
						int pc = 0; // Program Counter
						int ta = 0; // TARGET ADDRESS
						int disp = 0;
						op_tab[i] += 2; //pe에 2진수로 10을 추가해준다.
						op_tab[i] <<= 12; // disp의 공간할당
						pc = location_counter_index[i + 1]; // Program Counter를 처리해준다.
						for (int j = 0; j < liter_sum; j++) {
							if (strcmp(symbol_token[i].operand[0], liter_tab[j].literal) == 0 && symbol_token[i].extents == liter_tab[j].extents)
								ta = liter_tab[j].addr;
						}
						disp = ta - pc;
						disp &= 0xfff;
						op_tab[i] += (ta-pc);
					}
					else {//그냥 simple addressing일 때
						int pc = 0; // Program Counter
						int ta = 0; // 타켓 어드레스
						int disp = 0;
						op_tab[i] |= 2; //pe에 2진수 10을 추가해준다.
						op_tab[i] <<= 12; // disp의 공간할당
						pc = location_counter_index[i + 1]; // program Counter를 추가
						for (int j = 0; j < symbol_index; j++) {
							if (symbol_token[i].operand[0] != NULL && symbol_token[i].extents == sym_table[j].extents && strcmp(sym_table[j].symbol, symbol_token[i].operand[0]) == 0)
								ta = sym_table[j].addr;
						}
						disp = ta - pc;
						disp &= 0xfff; //형태 맞춰주기위해
						op_tab[i] |= disp;
					}
				}
				else if (symbol_token[i].operand[0] == NULL) { //오퍼랜드가 없는 경우
					op_tab[i] += 0; //2진수로 00을 추가해준다.
					op_tab[i] <<= 12;
				}
				else if (symbol_token[i].operator1[0] == '+') { //4format이면
					int pc = 0; // Program Counter
					int ta = 0; // 타켓 주소
					op_tab[i] |= 1; // Pe를 1로 저장한다.
					op_tab[i] <<= 20; // 공간을 만들어준다.
					pc = location_counter_index[i + 1]; // 프로그램 카운터 값을 받는다.
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
				if (strcmp(symbol_token[i].operator1, "BYTE") == 0) { //BYTE일 때
					int count = 0;
					int index = 0;
					char* tmp = NULL;

					for (int j = 2; symbol_token[i].operand[0][j] != '\''; j++) //j=2부터 따옴표 나오기 전까지 count
						count++;
					tmp = (char*)malloc(count + 1); //공간 할당
					for (int j = 2; j<(count+2); j++)
						tmp[index++] = symbol_token[i].operand[0][j]; //tmp 복사
					tmp[index] = '\0';
					op_tab[i] = strtol(tmp, NULL, 16); //16진수로 변환
					free(tmp);
					form_list[i] = count / 2;
				}
				else if (strcmp(symbol_token[i].operator1, "WORD") == 0) { //WORD일 때
					form_list[i] = 3;
					if (symbol_token[i].operand[0][0] >= '0' && symbol_token[i].operand[0][0] <= '9') // WORD의 오퍼랜드가 숫자라면
						op_tab[i] = strtol(symbol_token[i].operand[0],NULL,16); //숫자 16진수 op_tab에 저장
					else { // 문자라면. 예를 들면 WORD BUFEND-BUFFER. 외부참조인지 조건문으로 확인
						for (int j = 0; j < token_line; j++) {
							if (symbol_token[j].operator1 != NULL && strcmp(symbol_token[j].operator1, "EXTREF") == 0 && symbol_token[j].extents == symbol_token[i].extents) {
								for (int k = 0; k < 3; k++) //외부참조인지 오퍼랜드를 비교하며 확인
									if (symbol_token[j].operand[k] != NULL && strstr(symbol_token[i].operand[0], symbol_token[j].operand[k]) != NULL)
										continue; //외부참조면 주소를 모르므로 000000
							}
						}
					}
				}
				else if (strcmp(symbol_token[i].operator1, "*") == 0) { //오퍼레이터가 *인 literal 처리
					if (symbol_token[i].operand[0][1] == 'C') {
						int ch_num = 0;
						for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++) {
							ch_num++;
							op_tab[i] |= symbol_token[i].operand[0][j];
							op_tab[i] <<= 8; //글자하나가 8bit임
						}
						op_tab[i] >>= 8;
						form_list[i] = ch_num;
					}
					else if (symbol_token[i].operand[0][1] == 'X') {
						int count = 0;
						char* tmp;
						int index = 0;
						for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++)//따옴표 안의 길이 측정
							count++;
						tmp = (char*)malloc(count + 1);

						for (int j = 3; symbol_token[i].operand[0][j] != '\''; j++) //따옴표 안을 복사
							tmp[index++] = symbol_token[i].operand[0][j];
						tmp[index] = '\0';
						op_tab[i] = strtol(tmp, NULL, 16); //16진수로 변환
						form_list[i] = count / 2;
						free(tmp);
					}
				}
				break;
			}
		}
	}

	/*콘솔 창에 테이블 출력
	for (int i = 0; i < token_line; i++) {
		if (location_counter_index[i] != -1 && symbol_token[i].operator1 != NULL && strcmp(symbol_token[i].operator1, "CSECT") != 0) //CSECT는 locctr 표시 안되게함
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
			* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
			*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
			* 매계 : 생성할 오브젝트 파일명
			* 반환 : 없음
			* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
			*        화면에 출력해준다.
			*
			* -----------------------------------------------------------------------------------
			*/
void make_objectcode_output(char* file_name)
{
	/* add your code here }*/

	int program_end_line = 0; // program이 끝날때의 라인 번호
	int program_start_line = 0; // 프로그램의 시작 시 라인번호
	int program_end_address = 0; // program의 마지막 location counter
	int program_start_address = 0; // program의 시작주소
	int extends_position = 0;//현재의 extents 구역

	int is_line_end = 0;//하나의 같은 라인선상에서는 0
	int is_line_new = 1; //라인이 바뀌면 1로 처리
	int current_char = 0; // 현재 라인의 char를 저장해준다.
	int tmp_data = 0; // 주소(address)를 계산할떄 써주는 임시의 라인
	int current = 0; // 현재 위치

	FILE* fp = NULL;
	fp = fopen(file_name, "w");

	char* tmp;
	tmp = (char*)malloc(7);

	char* buffer; //데이터를 담을 버퍼
	int buff_size = 0;
	int buff_length = 0;
	buffer = (char*)malloc(70); //버퍼를 70정도 할당해준다.
	buff_length = 70;

	while (is_line_end == 0) {
		while (symbol_token[current].label != NULL && symbol_token[current].label[0] == '.') { //라벨이 .이거나 아무것도 없을 때는 그냥 다음 줄
			current++;
		}
		fprintf(fp, "H%s\t", symbol_token[current].label); // program name
		if (symbol_token[current].operand[0] != NULL)
			program_start_address = atoi(symbol_token[current].operand[0]); //프로그램 이름 옆에 시작 주소 적혀 있으면 그걸로 저장
		fprintf(fp, "%06X", program_start_address); // start address
		for (int i = current + 1; i < token_line; i++) {
			//오퍼레이터가 NULL이거나 CSECT일 때 무시 또는 빠져나옴
			if (symbol_token[i].operator1 == NULL)
				continue;
			if (strcmp(symbol_token[i].operator1, "CSECT") == 0)
				break;

			//끝에서 END를 만나면 밑에 나오는 리터럴라인이 몇 줄인지 카운트하기
			else if (strcmp(symbol_token[i].operator1, "END") == 0) {
				int literal_count = 0;
				for (int j = 0; j < liter_sum; j++) {
					if (symbol_token[i].extents == liter_tab[j].extents) literal_count++;
				}
				program_end_line = i + literal_count + 1;
				//총 라인수와 리터럴 라인 수 더하고 +!해서 맨 마지막 라인 구하기
				break;
			}
			program_end_line = i;
		}
		if (program_end_line == token_line) is_line_end = 1;
		tmp_data = program_end_line;
		if (program_end_line != token_line) {
			while (strcmp(symbol_token[tmp_data].operator1, "EQU") == 0)
				tmp_data--;

			program_end_address = location_counter_index[tmp_data + 1]; // 마지막의 PC의 값이다. (EQU를 제외했음.)
		}
		else program_end_address = location_counter_index[tmp_data];

		fprintf(fp, "%06X\n", program_end_address - program_start_address); // program의 길이 계산
		for (int i = current; i <= program_end_line; i++) {
			if (symbol_token[i].operator1 == NULL)
				continue;
			else {
				if (strcmp(symbol_token[i].operator1, "EXTDEF") == 0) { //외부정의의 처리
					current = i;
					fputc('D', fp);
					current_char += 1;
					for (int j = 0; j < 3; j++) {
						if (symbol_token[i].operand[j] != NULL) {
							int sym_addr = 0;
							for (int k = 0; k < symbol_index; k++) { //심볼테이블에서 오퍼랜드를 서칭
								if (strcmp(sym_table[k].symbol, symbol_token[i].operand[j]) == 0 && sym_table[k].extents == symbol_token[i].extents)
									sym_addr = sym_table[k].addr;
							}
							current_char += (strlen(symbol_token[i].operand[j]) + 6);
							if (current_char > 72) { //제한숫자 범위를 초과한다면
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
				if (strcmp(symbol_token[i].operator1, "EXTREF") == 0) { //외부참조라면
					current = i;
					fputc('R', fp);
					current_char += 1;
					for (int j = 0; j < 3; j++) {
						if (symbol_token[i].operand[j] != NULL) {
							current_char += strlen(symbol_token[i].operand[j]) + 1;
							if (current_char > 72) { //제한길이를 넘으면
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
		int tmp_end = 0;// 끝의 위치
		while (current <= program_end_line) { //끝날 때까지 텍스트 출력
			if (is_line_new == 1) {
				sprintf(buffer, "T%06X", location_counter_index[current]);
				buff_size += 6;
				current_char += 7;
				strcat(buffer, "00"); //글자의 개수를 설정한다. 0개로
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
			if (strcmp(symbol_token[i].operator1, "EXTREF") == 0) { //외부참조 처리
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

