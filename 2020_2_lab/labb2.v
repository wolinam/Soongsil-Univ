module labb2(clk, clr_n, select, seg_out);

input clk, clr_n, select;
output [6:0] seg_out;

wire [3:0] mux_out, up_count, down_count;

up_counter UP (.clk(clk), .clr_n(clr_n), .up_count(up_count));
down_counter DC (.clk(clk), .clr_n(clr_n), .down_count(down_count));	
mux_4bit2x1 MUX (.up_count(up_count),.down_count(down_count), .select(select), .mux_out(mux_out));
bin7seg B2S (.mux_out(mux_out), .seg_out(seg_out));

endmodule
