module counter3(clk, clr_n, cnt, load, num, qout);

input clk, clr_n, cnt, load;
input [2:0] num;

output [2:0] qout;
reg [2:0] qout;

always@(posedge clk or negedge clr_n)
begin
	if(!clr_n)
	  qout <= 3'b0;
	// reset(clear)
	
	else if(load) qout <= num;
	// load가 1일 때는 num에 저장된 값으로 초기화
	else if(cnt) qout<=qout + 1'b1;
	// reset 동작 안할때는 그냥 정상적으로 count
	else 
	  qout <= qout;
end

endmodule
