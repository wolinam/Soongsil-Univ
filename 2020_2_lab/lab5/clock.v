module clock(clr_n, clk, load, num, sel, h1, h0, m1, m0,s1,s0);

input clr_n, clk, load;
input[3:0] num;
input[2:0] sel;

output[3:0]h1;
output[3:0]h0;
output[3:0]m1;
output[3:0]m0;
output[3:0]s1;
output[3:0]s0;

reg[1:0] hour1;
reg[3:0] hour0;
reg[2:0] minute1;
reg[3:0] minute0;
reg[2:0] sec1;
reg[3:0] sec0;

reg flag, load_cmd;

always@(posedge clk or negedge clr_n)
begin
	if(!clr_n)
		begin
			load_cmd <= 0;
			flag <=0;
		end
	else if(load ==1 && flag ==0) //push button
		begin
			load_cmd <= 1;
			flag <=1;
		end
	else if(load == 0) //no push
		begin
			load_cmd <= 0;
			flag <=0;
		end
	else //ing
		begin
			load_cmd <= 0;
			flag <=flag;
		end
end




//---------------------------------------------------------------------------------------------------
always@(posedge clk or negedge clr_n)
begin
	if(!clr_n) sec0 <= 4'b0;
	else if(load_cmd && sel == 3'b000) sec0<= num;
	else if(sec0 == 4'd9) sec0 <= 4'b0;
	else sec0 <= sec0 + 1'b1;
end

assign tc_s0 = (sec0 == 4'd9)? 1'b1:1'b0;


//---------------------------------------------------------------------------------------------------
always@(posedge clk or negedge clr_n)
begin
	if(!clr_n) sec1 <= 3'b0;
	else if(load_cmd && sel == 3'b001) sec1<= num[2:0];
	else if(sec1 == 3'd5 && tc_s0) sec1 <= 3'b0; //59s -> 00s
	else if(tc_s0) sec1<= sec1 + 1'b1; //not 59s, such as 39s, 49s still increase 
	else sec1 <= sec1;
end

assign tc_s1 = (sec1 == 3'd5 && tc_s0)? 1'b1:1'b0;





//---------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------
always@(posedge clk or negedge clr_n)
begin
	if(!clr_n) minute0 <= 4'b0;
	else if(load_cmd && sel == 3'b010) minute0 <= num;
	else if(minute0 == 4'd9 && tc_s0 && tc_s1) minute0 <= 4'b0; 
	else if(tc_s1) minute0 <= minute0 + 1'b1; 
	else minute0 <= minute0;
end

assign tc_m0 = (minute0 == 4'd9 && tc_s1)? 1'b1:1'b0;


//---------------------------------------------------------------------------------------------------
always@(posedge clk or negedge clr_n)
begin
	if(!clr_n) minute1 <= 3'b0;
	else if(load_cmd && sel == 3'b011) minute1 <= num[2:0];
	else if(minute1 == 3'd5 && tc_m0 && tc_s0 && tc_s1) minute1 <= 3'b0; 
	else if(tc_m0) minute1 <= minute1 + 1'b1; 
	else minute1 <= minute1;
end

assign tc_m1 = (minute1 == 3'd5 && tc_m0)? 1'b1:1'b0;





//---------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------
always@(posedge clk or negedge clr_n)
begin
	if(!clr_n) hour0 <= 4'b0;
	else if(load_cmd && sel == 3'b100) hour0 <= num;
	else if ((hour0 == 4'b1001 && tc_m1) || (hour0 == 4'b0011 && hour1 == 2'b10 && tc_m1)) hour0 <= 4'b0; 
	else if(tc_m1) hour0 <= hour0 + 1'b1; 
	else hour0 <= hour0;
end

assign tc_h0 = ((hour0 == 4'b1001 && tc_m1) || (hour0 == 4'b0011 && hour1 == 2'b10 && tc_m1))? 1'b1:1'b0;

//---------------------------------------------------------------------------------------------------
always@(posedge clk or negedge clr_n)
begin
	if(!clr_n) hour1 <= 2'b0;
	else if(load_cmd && sel == 3'b101) hour1 <= num[1:0];
	else if(hour0 == 4'b0011 && hour1 == 2'b10 &&tc_m1) hour1 <= 2'b0; 
	else if(hour0 == 4'b1001 && tc_m1) hour1 <= hour1 + 1'b1; 
	else hour1 <= hour1;
end




assign h1 = {2'b00,hour1};
assign h0 = hour0;
assign m1 = {1'b0, minute1};
assign m0 = minute0;
assign s1 = {1'b0, sec1};
assign s0 = sec0;


endmodule

