/*
* my_assembler �Լ��� ���� ���� ���� �� ��ũ�θ� ��� �ִ� ��� �����̴�.
*
*/
#define MAX_INST 256
#define MAX_LINES 5000

#define MAX_COLUMNS 4
#define MAX_OPERAND 3 

/*
 * instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� ����ü �����̴�.
 * ������ ������ instruction set�� ��Ŀ� ���� ���� �����ϵ�
 * ���� ���� �ϳ��� instruction�� �����Ѵ�.
 */

typedef struct obj {
	char name[10]; //��ɾ� �̸�
	unsigned char opcode; //opcode
	int type_format; //����
	int operands; //operand�� ����
}obj;

obj inst_table[MAX_INST];
char *inst[MAX_INST][MAX_COLUMNS];
int inst_index;


/*
 * ����� �� �ҽ��ڵ带 �Է¹޴� ���̺��̴�. ���� ������ ������ �� �ִ�.
 */
char *input_data[MAX_LINES];
static int line_num;

int label_num;

/*
 * ����� �� �ҽ��ڵ带 ��ū������ �����ϱ� ���� ����ü �����̴�.
 * operator�� renaming�� ����Ѵ�.
 * nixbpe�� 8bit �� ���� 6���� bit�� �̿��Ͽ� n,i,x,b,p,e�� ǥ���Ѵ�.
 */

struct token_unit {
	char *label;
	char *operator1;
	char *operand[MAX_OPERAND];
	char *comment;
	char nixpbpe;
	int extents; //����
};
typedef struct token_unit token;
token *token_table[MAX_LINES]; 
token symbol_token[MAX_LINES];
static int token_line;


/*
 * �ɺ��� �����ϴ� ����ü�̴�.
 * �ɺ� ���̺��� �ɺ� �̸�, �ɺ��� ��ġ�� �����ȴ�.
 * ���� ������Ʈ���� ���ȴ�.
 */
struct symbol_unit {
	char symbol[10];
	int extents; //���� ����
	int addr;
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES]; 

/*
* ���ͷ��� �����ϴ� ����ü�̴�.
* ���ͷ� ���̺��� ���ͷ��� �̸�, ���ͷ��� ��ġ�� �����ȴ�.
* ���� ������Ʈ���� ���ȴ�.
*/
typedef struct literal_unit {
	char* literal;
	int addr;
	int extents;
}literal;
literal liter_tab[MAX_LINES]; // ���ͷ� ���̺��̴�.

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

int symbol_index; // �ɺ����̺� ����Ǿ� �ִ� �ɺ��� �ε����̴�.
int op_tab[MAX_LINES];//�����ڵ忡 ���� �������ִ� ���̺� ����Ʈ�̴�.
int location_counter_index[MAX_LINES]; // �ε����� ���� location counter�� ó���ϱ� �����̴�.
int form_list[MAX_LINES]; //���Ŀ� ���� ���� �������ִ� ����Ʈ�̴�.
int extents;

/* ���� ������Ʈ���� ����ϰ� �Ǵ� �Լ�*/
void make_objectcode_output(char *file_name);
void make_symtab_output(char *file_name);
void make_literal_output(char *file_name);

int ltorg_line_sum;  // LTORG �� ���� �߰��� ���μ�
int liter_sum; // ���ͷ� ���̺� �ȿ� �ִ� �� ���ͷ��� ����

int search_placed_literal(char *str, int extents);
int token_parsing(int index);
int search_symbol(char *str, int extents);