module invert_button(button,n_button);
input button;
output n_button;

assign n_button = ~button;
endmodule

