module fsm(clk, clr_n, mode_ext,led);

input clk, clr_n, mode_ext;
output [3:0] led;


reg mode;
reg flag;

reg [3:0] led;
reg [1:0] state, next_state;

parameter TIME = 2'b00;
parameter ALARM = 2'b01;
parameter SW = 2'b10;
parameter TC = 2'b11;

//pulse generator
always@(posedge clk or negedge clr_n)
begin
	if(!clr_n)
	begin
		flag <=0;
		mode <=0;
	end
	
	else if(mode_ext == 1 && flag ==0)
	begin
		mode <=1;
		flag <= 1;
	end
	
	else if(mode_ext == 0)
	begin
		mode <=0;
		flag <= 0;
	end
	
	else
	begin
		mode <=0;
		flag <= flag;
	end
end


//state transition logic
always@(posedge clk or negedge clr_n)
begin
	if(!clr_n)
	state <= TIME;
	else
	state <= next_state;
end


//next state combinational logic
always@(state or  mode)
begin
	case(state)
		TIME:
		if(mode==1'b1) next_state <= ALARM;
		else next_state <= TIME;
		ALARM:
		if(mode==1'b1) next_state <= SW;
		else next_state <= ALARM;
		SW:
		if(mode==1'b1) next_state <= TC;
		else next_state <= SW;
		TC:
		if(mode==1'b1) next_state <= TIME;
		else next_state <= TC;
	endcase
end


//state output logic
always@(state)
begin
	case(state)
		TIME: led <= 4'b0001;
		ALARM: led <= 4'b0010;
		SW: led <= 4'b0100;
		TC: led <= 4'b1000;
	endcase
end

endmodule
