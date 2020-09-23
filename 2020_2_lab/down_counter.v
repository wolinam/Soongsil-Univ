module down_counter(clk, clr_n, down_count);

input	clk, clr_n;
output [3:0] down_count;

reg [3:0] down_count;

always@(posedge clk or negedge clr_n)
begin
	if(!clr_n)
	  down_count <= 4'b1111;
	else 
	  down_count <= down_count - 4'b1;
end

endmodule
