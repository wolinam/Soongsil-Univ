module counter4(clk, clr_n, qout);

input	clk, clr_n;
output [3:0] qout;

reg [3:0] qout;

always@(posedge clk or negedge clr_n)
begin
	if(!clr_n)
	  qout <= 4'b0000;
	else 
	  qout <= qout + 4'b1;
end

endmodule
