`timescale 1ns/100ps

module lab1_tb;

reg clk, clr_n;
wire [3:0] qout;
wire [6:0] seg_out;

lab1 DUT(.clk(clk), .clr_n(clr_n), .qout(qout), .seg_out(seg_out));

initial
   clk = 1'b0;


always begin
   #5 clk = ~ clk;
end

initial
begin
   clr_n = 1'b0;
	#4 clr_n = 1'b1;
end

endmodule