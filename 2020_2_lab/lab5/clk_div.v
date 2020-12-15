module clk_div (clk, clr_n, clk_1hz);

input	clk, clr_n;
output clk_1hz;

reg clk_1hz;
reg [26:0] clk_cnt;

always @(posedge clk or negedge clr_n)
   begin
	   if (!clr_n) clk_cnt <= 27'd0;
	   else if (clk_cnt == 27'd50000000) begin
		   clk_cnt <= 27'd0;
		end
		else begin
		   clk_cnt <= clk_cnt + 1'b1;
		end
	end

always @(*)
begin 
   clk_1hz <= clk_cnt[24];
end

endmodule
