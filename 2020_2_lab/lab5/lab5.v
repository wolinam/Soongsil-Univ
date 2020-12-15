module lab5(clk, clr_n, load, num,sel, seg_h1, seg_h0, seg_m1, seg_m0, seg_s1, seg_s0);

input clk, clr_n, load;
input [3:0] num;
input[2:0] sel;

output [6:0] seg_h1;
output [6:0] seg_h0;
output [6:0] seg_m1;
output [6:0] seg_m0;
output [6:0] seg_s1;
output [6:0] seg_s0;

wire [3:0] w_h1;
wire [3:0] w_h0;
wire [3:0] w_m1;
wire [3:0] w_m0;
wire [3:0] w_s1;
wire [3:0] w_s0;

wire clk_1hz;
wire load_n;

clk_div DIV(.clk(clk),.clr_n(clr_n),.clk_1hz(clk_1hz));
clock DCLOCK(.clr_n(clr_n), .clk(clk_1hz), .load(load_n), .num(num), .sel(sel), .h1(w_h1), .h0(w_h0), .m1(w_m1), .m0(w_m0), .s1(w_s1), .s0(w_s0));
invert_button INV(.button(load),.n_button(load_n));

bin7seg HOUR1(.qout(w_h1), .seg_out(seg_h1));
bin7seg HOUR0(.qout(w_h0), .seg_out(seg_h0));

bin7seg MINUTE1(.qout(w_m1), .seg_out(seg_m1));
bin7seg MINUTE0(.qout(w_m0), .seg_out(seg_m0));

bin7seg SEC1(.qout(w_s1), .seg_out(seg_s1));
bin7seg SEC0(.qout(w_s0), .seg_out(seg_s0));

endmodule
