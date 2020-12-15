module decoder3x8(i,d);

input [2:0] i;
output [7:0] d;

reg [7:0] d;

always@(*)
begin
	case(i)
		3'b000 : d <= 8'b00000001;
		3'b001 : d <= 8'b00000010;
		3'b010 : d <= 8'b00000100;
		3'b011 : d <= 8'b00001000;
		3'b100 : d <= 8'b00010000;
		3'b101 : d <= 8'b00100000;
		3'b110 : d <= 8'b01000000;
		3'b111 : d <= 8'b10000000;
		
		default : d <= 8'b00000001;
	endcase
end

endmodule
