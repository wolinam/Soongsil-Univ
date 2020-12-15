module lab4(clk, clr_n, mode_ext,led);

input clk, clr_n, mode_ext;
output [3:0] led;

fsm FSM(.clk(clk), .clr_n(clr_n), .mode_ext(mode_ext),.led(led));

endmodule
