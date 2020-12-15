`timescale 1ns/100ps

module lab3_tb;

reg clk, clr_n, cnt, load;
reg [2:0] num;
wire [7:0] led;
wire [6:0] seg_out;
lab3 UUT(.clk(clk), .clr_n(clr_n), .cnt(cnt), .load(load), .num(num), .led(led), .seg_out(seg_out));

initial
   clk = 1'b0;
   //clk = 0;


always begin
   #5 clk = ~ clk;
end

initial
begin
   clr_n = 1'b0;
   cnt = 1'b1;
   load = 1'b0;
   num = 3'b110;
   
	#4 clr_n = 1'b1; //초기화 stop
	#70 cnt = 1'b0; //counter 동작 중지
	#20 cnt = 1'b1; //counter 다시 동작
	#30 load = 1'b1; //num으로 초기화
	#20 load = 1'b0; //다시 num부터 count
	
end
endmodule
