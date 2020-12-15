module up_counter(clk, clr_n, up_count);

input	clk, clr_n;
output [3:0] up_count;

reg [3:0] up_count;

always@(posedge clk or negedge clr_n)
begin
	if(!clr_n)
	  up_count <= 4'b0000;
	else 
	  up_count <= up_count + 4'b1;
end

endmodule
