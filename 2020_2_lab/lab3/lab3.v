module lab3(clk, clr_n, cnt, load, num, led, seg_out);

input clk, clr_n, cnt, load;
input [2:0] num;
output [7:0] led;
output [6:0] seg_out;

wire [2:0] w_qout;

counter3 CNTR3(.clk(clk), .clr_n(clr_n), .cnt(cnt), .load(load), .num(num), .qout(w_qout));
decoder3x8 DEC(.i(w_qout), .d(led));
bin7seg B2S (.qout({1'b0,w_qout}), .seg_out(seg_out));

endmodule
