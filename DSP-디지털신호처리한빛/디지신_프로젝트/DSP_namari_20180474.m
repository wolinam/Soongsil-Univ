%
% Project #1 :: Input signal x(n) is transmitted through a channel that
%               is described by y(n)=a*y(n-1)+x(n)+b*x(n-1). (a=0.8, b=-0.9)
%               At the receiver, the signal
%               is contaminated by a AWGN. We will design the inverse system
%               that can recover the input signal. 
% 



close all;
clear all;



#1. 기본 셋팅 & sampling theorem으로 Fs구하기-------------------------------------------------------------

F = [50, 150, 300, 400 ];  % 4 tones in Hz
Fs = 900; % minimum sampling frequency (Hz) ->800
          %Fs > (2*fmax) :: 한 주기에 최소 2 샘플 이상의 데이터가 필요
          %Fs > 2*400 = 800 //정확히 800하면 2분의 1이 안나오고 1이 나오게 됨. 조금 더 크게

N = 9000; %Fs/N = 주파수해상도
K=N;
n = [0:N-1];
#------------------------------------------------------------------------------------------------------





#2. 0.8Fs, Fs, 1.5Fs를 이용하여 신호 생성-----------------------------------------------------------------
%신호발생 :: x=cos(2*pi*f/Fs*n)
%x = cos(2*pi*f/Fs*n); %n의 크기로 나옴
%analog로 바꿀 때는 Fs를 곱하고 반대로 discrete로 바꿀 때는 Fs를 나눔

fsc = [0.8 1.0 1.5];
x1=zeros(size(n));
x2=zeros(size(n));
x3=zeros(size(n));

for f=F
  x1=x1+cos(2*pi*f/(fsc(1)*Fs)*n+0); %0.8Fs를 이용하여 생성한 신호
                                     %4개의 톤이 더해진 형태
  x2=x2+cos(2*pi*f/(fsc(2)*Fs)*n+0); 
  x3=x3+cos(2*pi*f/(fsc(3)*Fs)*n+0);
end
#-----------------------------------------------------------------------------------------------------





#3. 각각의 신호에 대해서 평균값과 파워구하기----------------------------------------------------------------
##m=sum(x)/N; %mean(x);
##p = sum(x.^2)/N; %0.5 :: p=var(x) ==> average((x-mx)^2)
Mx1 = sum(x1)/N;      % mean(x); % mean value of x(n)
Px1 = sum(x1.^2)/N;      %var(x);  % power of x(n)
Mx2 = sum(x2)/N;
Px2 = sum(x2.^2)/N;
Mx3 = sum(x3)/N;
Px3 = sum(x3.^2)/N; 
#------------------------------------------------------------------------------------------------------





#4. 각각의 신호들에 대한 DTFT를 구하고, 그 결과를 plot 함수를 이용하여 그리기----------------------------------
%스펙트럼 구하기 :: X(w)구하기 : DTFT ==> DTFS
w = (0:(K-1))*(pi/K);
Xw1 = x1*exp(-j*n'*w); % DTFT of x(n) --> x(n)e^(-jwn)
                       %Xw(0)부터 Xw(K-1)까지 나옴 :: complex spectrum
Xw2 = x2*exp(-j*n'*w);
Xw3 = x3*exp(-j*n'*w);

%x축 -> Hz
figure(1);
subplot(3,1,1);
plot(w*(fsc(1)*Fs)/(2*pi),sqrt(Xw1.*conj(Xw1))); %abs(Xw1)
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('x1 DTFT -> Xw1','fontsize',25);

subplot(3,1,2);
plot(w*(fsc(2)*Fs)/(2*pi),abs(Xw2));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('x2 DTFT -> Xw2','fontsize',25);

subplot(3,1,3);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Xw3));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('x3 DTFT -> Xw3','fontsize',25);

#------------------------------------------------------------------------------------------------------





#5-6. (x3만) 채널 통과한 y 구하기-------------------------------------------------------------------------
%channel :: y(n) = a*y(n-1)+x(n)+b*x(n-1) (a=0.8,b=-09) n=1,2,...,N
%Y(z) = a*Y(z)z^-1 + X(z) + b*X(z)*z^-1 ==> H(z)=Y(z)/X(z)

a=0.8; %분모의 계수
b=-0.9; %분자의 계수
y(1) = x3(1); %n=1 :: y(1) = a*y(0) + x(1) + b*x(0) = x(1)

for m=2:N %n=2,...,N :: y(n) = a*y(n-1)+x(n)+b*x(n-1)
  y(m) = a*y(m-1)+x3(m)+b*x3(m-1);
end
 
figure(2);
subplot(1,1,1);
stem(y(end-20:end)); %채널을 통과한 y그리기
set(gca,'fontsize',15);
xlabel('n','fontsize',17); ylabel('y(n)','fontsize',17); title('x3 -> channel -> y','fontsize',25);
#--------------------------------------------------------------------------------------------------------





#7.채널 h(n)에 대한 DTFT H(w)를 구하고 그리기-----------------------------------------------------------------
figure(3);
Hw = (1+b*exp(-j*w))./(1-a*exp(-j*w));
plot(w*(fsc(3)*Fs)/(2*pi),abs(Hw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('H(w) by DTFT','fontsize',25);
#----------------------------------------------------------------------------------------------------------





#8. y(n)에 대한 DTFT Y(w)를 구하고, 이를 그린 후 위의 예측 결과와 비교분석-----------------------------------
Yw = y*exp(-j*n'*w);
figure(4)
##subplot(2,1,1);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Yw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('Y(w)','fontsize',25);

#####7번에서 H(w)를 제대로 구했는지 검사위해 X(w)H(w)한 값과 Y(W)값이 같은지 비교
##subplot(2,1,2);
##plot(w*(fsc(3)*Fs)/(2*pi),abs(Xw3.*Hw'));
##set(gca,'fontsize',15);
##xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('X(w)H(w)','fontsize',25);
#----------------------------------------------------------------------------------------------------------





#9.H(z) -> pole-zero diagram & H(w) -> freqz 그리기-----------------------------------------------------------
%pkg load signal
%Y(z)(1-az^-1)=x(z)(1+bz^-1) => Y(z)A(Z) = X(z)B(z)
%Y(z) = a*Y(z)z^-1 + X(z) + b*X(z)*z^-1 ==> H(z)=Y(z)/X(z)

%H(z)
figure(5);
B=[1 b];
A=[1 -a];
zplane(B,A);
set(gca,'fontsize',15);
xlabel('Re(z)','fontsize',17); ylabel('Im(z)','fontsize',17); title('H(z) by zplane','fontsize',25);

%H(w) : high-pass filter형태의 주파수응답
Hw = freqz(B,A,K);
figure(6);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Hw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('H(w) by freqz','fontsize',25);
#--------------------------------------------------------------------------------------------------------------





#10. +noise -> yn-----------------------------------------------------------------------------------------
%AWGN adding
noise = randn(size(y));
Pn = var(noise);

SNR_dB = 20;
%SNR : signal-Noise_ratio (신호 대 잡음비)
%SNR_dB 10*log10(Ps/Pn) Ps와 Pn이 같으면 0.
SNR = 10^(SNR_dB/10);
n_gain = sqrt(Px3/SNR/Pn);
yn=y+n_gain*noise;
#---------------------------------------------------------------------------------------------------------





#11. Yn(n)의 DTFT Yn(w)-----------------------------------------------------------------------------------
Ynw = yn*exp(-j*n'*w);
figure(7);
%Y(w)와 Yn(w)비교
subplot(2,1,1);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Yw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('Y(w)','fontsize',25);
subplot(2,1,2);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Ynw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('Yn(w)','fontsize',25);
#---------------------------------------------------------------------------------------------------------





#12. inverse system HI(z)--------------------------------------------------------------------------------
%HI(z)
figure(8);
subplot(1,2,1);
zplane(A,B); %H(z) = 1/Hi(z) 역수 관계
set(gca,'fontsize',15);
xlabel('Re(z)','fontsize',17); ylabel('Im(z)','fontsize',17); title('inverse system Hi(z)','fontsize',25);

%주파수응답 HI(w)
Hiw = freqz(A,B,K);
subplot(1,2,2);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Hiw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('inverse system Hi(w)','fontsize',25);
%low pass같은 형태 나올거라 예상
#-----------------------------------------------------------------------------------------------------------





#13. inverse system을 통과하여 복구된 xr--------------------------------------------------------------------------
##xr(1)=yn(1);
##for j=2:N
##  xr(j)=(-b)*xr(j-1)+yn(j)-a*yn(j-1);
##end
xr=filter(A,B,yn); %filter(분자의계수, 분모의계수, 입력)

figure(9);
subplot(2,1,1);
stem(x3(end-20:end));
set(gca,'fontsize',15);
xlabel('n','fontsize',17); ylabel('x3(n)','fontsize',17); title('x3','fontsize',25);

subplot(2,1,2);
stem(xr(end-20:end));
set(gca,'fontsize',15);
xlabel('n','fontsize',17); ylabel('xr(n)','fontsize',17); title('xr','fontsize',25);
#-----------------------------------------------------------------------------------------------------------------





#14. xr DTFT -> Xr(w)---------------------------------------------------------------------------------------------
xrw = xr*exp(-j*n'*w);
figure(10);
subplot(2,1,1);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Xw3));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('Xw3','fontsize',25);

subplot(2,1,2);
plot(w*(fsc(3)*Fs)/(2*pi),abs(xrw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('xr DTFT -> Xr(w)','fontsize',25);
#-----------------------------------------------------------------------------------------------------------------






