`timescale 1ns/100ps

module lab4_tb;

reg clk, clr_n, mode_ext;
wire [3:0] led;

lab4 DUT(.clk(clk), .clr_n(clr_n), .mode_ext(mode_ext),.led(led));

initial
   clk = 1'b0;


always begin
   #5 clk = ~ clk;
end


initial
begin
   clr_n = 1'b0;
   #4 clr_n = 1'b1; //초기화 stop
end
 
 
initial
begin
	mode_ext = 1'b0;
	//1
	#3
	#20 mode_ext = 1'b1; //button push -> state change
	#20 mode_ext = 1'b0; //don't push -> state stay
	//2
	#20 mode_ext = 1'b1; 
	#20 mode_ext = 1'b0; 
	//3
	#20 mode_ext = 1'b1; 
	#20 mode_ext = 1'b0; 
	//4
	#20 mode_ext = 1'b1; 
	#20 mode_ext = 1'b0; 
	//5
	#20 mode_ext = 1'b1; 
	#20 mode_ext = 1'b0; 
	//6
	#20 mode_ext = 1'b1; 
	#20 mode_ext = 1'b0; 
	
end
endmodule
