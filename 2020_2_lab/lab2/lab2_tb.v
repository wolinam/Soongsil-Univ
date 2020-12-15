`timescale 1ns/100ps

module lab2_tb;

reg clk, clr_n, select;
wire [6:0] seg_out;

labb2 UUT(.clk(clk), .clr_n(clr_n), .select(select), .seg_out(seg_out));

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

initial
begin
select = 1'b1;
#20 select = 1'b0;
#20 select = 1'b1;
#20 select = 1'b0;
#40 select = 1'b1;
#40 select = 1'b0;
#20 select = 1'b1;
end

endmodule
