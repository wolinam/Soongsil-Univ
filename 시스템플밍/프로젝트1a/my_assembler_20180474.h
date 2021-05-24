/*
* my_assembler 함수를 위한 변수 선언 및 매크로를 담고 있는 헤더 파일이다.
*
*/
#define MAX_INST 256
#define MAX_LINES 5000

#define MAX_COLUMNS 4
#define MAX_OPERAND 3 

/*
 * instruction 목록 파일로 부터 정보를 받아와서 생성하는 구조체 변수이다.
 * 구조는 각자의 instruction set의 양식에 맞춰 직접 구현하되
 * 라인 별로 하나의 instruction을 저장한다.
 */

typedef struct obj {
	char name[10]; //명령어 이름
	unsigned char opcode; //opcode
	int type_format; //형식
	int operands; //operand의 개수
}obj;

obj inst_table[MAX_INST];
char *inst[MAX_INST][MAX_COLUMNS];
int inst_index;


/*
 * 어셈블리 할 소스코드를 입력받는 테이블이다. 라인 단위로 관리할 수 있다.
 */
char *input_data[MAX_LINES];
static int line_num;

int label_num;

/*
 * 어셈블리 할 소스코드를 토큰단위로 관리하기 위한 구조체 변수이다.
 * operator는 renaming을 허용한다.
 * nixbpe는 8bit 중 하위 6개의 bit를 이용하여 n,i,x,b,p,e를 표시한다.
 */

struct token_unit {
	char *label;
	char *operator1;
	char *operand[MAX_OPERAND];
	char *comment;
	char nixpbpe;
	int extents; //구역
};
typedef struct token_unit token;
token *token_table[MAX_LINES]; 
token symbol_token[MAX_LINES];
static int token_line;


/*
 * 심볼을 관리하는 구조체이다.
 * 심볼 테이블은 심볼 이름, 심볼의 위치로 구성된다.
 * 추후 프로젝트에서 사용된다.
 */
struct symbol_unit {
	char symbol[10];
	int extents; //구역 나눔
	int addr;
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES]; 

/*
* 리터럴을 관리하는 구조체이다.
* 리터럴 테이블은 리터럴의 이름, 리터럴의 위치로 구성된다.
* 추후 프로젝트에서 사용된다.
*/
typedef struct literal_unit {
	char* literal;
	int addr;
	int extents;
}literal;
literal liter_tab[MAX_LINES]; // 리터럴 테이블이다.

static int locctr; //location counter
//---------------------------------------------------

static char *input_file;
static char *output_file;
static char *symtab_file;
static char *literal_file;

int init_my_assembler(void);
static int assem_pass1(void);
static int assem_pass2(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);
int search_opcode(char *str);
//void make_opcode_output(char* file_name);

int symbol_index; // 심볼테이블에 저장되어 있는 심볼의 인덱스이다.
int op_tab[MAX_LINES];//오피코드에 대해 관리해주는 테이블 리스트이다.
int location_counter_index[MAX_LINES]; // 인덱스에 따른 location counter를 처리하기 위함이다.
int form_list[MAX_LINES]; //형식에 대한 것을 관리해주는 리스트이다.
int extents;

/* 추후 프로젝트에서 사용하게 되는 함수*/
void make_objectcode_output(char *file_name);
void make_symtab_output(char *file_name);
void make_literal_output(char *file_name);

int ltorg_line_sum;  // LTORG 의 새로 추가된 라인수
int liter_sum; // 리터럴 테이블 안에 있는 총 리터럴의 개수

int search_placed_literal(char *str, int extents);
int token_parsing(int index);
int search_symbol(char *str, int extents);