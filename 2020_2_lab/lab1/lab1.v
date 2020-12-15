module lab1(clk, clr_n, qout, seg_out);

input clk, clr_n;
output [3:0]  qout;
output [6:0]  seg_out;

// For Simulation

counter4 CNTR (.clk(clk), .clr_n(clr_n), .qout(qout));	
bin7seg SEG (.qout(qout), .seg_out(seg_out));


// FPGA implementation
/*
wire clk_1hz;

clk_div CDIV(.clk(clk),  .clr_n(clr_n), .clk_1hz(clk_1hz));
counter4 CNTR (.clk(clk_1hz), .clr_n(clr_n), .qout(qout));
bin7seg SEG (.qout(qout), .seg_out(seg_out));
*/
endmodule
