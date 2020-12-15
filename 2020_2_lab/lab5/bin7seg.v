module bin7seg(qout, seg_out);
input [3:0] qout;
output [6:0] seg_out;

reg [6:0] seg_out;

always@(*)
	begin
	case(qout)
	   4'b0000 : seg_out <= 7'b100_0000; //0
	   4'b0001 : seg_out <= 7'b111_1001; //1
	   4'b0010 : seg_out <= 7'b010_0100; //2
	   4'b0011 : seg_out <= 7'b011_0000; //3
	   4'b0100 : seg_out <= 7'b001_1001; //4
	   4'b0101 : seg_out <= 7'b001_0010; //5
	   4'b0110 : seg_out <= 7'b000_0010; //6
	   4'b0111 : seg_out <= 7'b101_1000; //7
	   4'b1000 : seg_out <= 7'b000_0000; //8
	   4'b1001 : seg_out <= 7'b001_0000; //9
	   4'b1010 : seg_out <= 7'b000_1000;  //a
	   4'b1011 : seg_out <= 7'b000_0011;  //b
	   4'b1100 : seg_out <= 7'b100_0110; //c
	   4'b1101 : seg_out <= 7'b010_0001; //d
	   4'b1110 : seg_out <= 7'b000_0100; //e
	   4'b1111 : seg_out <= 7'b000_1110; //f
		default : seg_out <= 7'b100_0000;
	endcase
	end

endmodule
