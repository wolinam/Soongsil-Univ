%
% Project #1 :: Input signal x(n) is transmitted through a channel that
%               is described by y(n)=a*y(n-1)+x(n)+b*x(n-1). (a=0.8, b=-0.9)
%               At the receiver, the signal
%               is contaminated by a AWGN. We will design the inverse system
%               that can recover the input signal. 
% 



close all;
clear all;



#1. �⺻ ���� & sampling theorem���� Fs���ϱ�-------------------------------------------------------------

F = [50, 150, 300, 400 ];  % 4 tones in Hz
Fs = 900; % minimum sampling frequency (Hz) ->800
          %Fs > (2*fmax) :: �� �ֱ⿡ �ּ� 2 ���� �̻��� �����Ͱ� �ʿ�
          %Fs > 2*400 = 800 //��Ȯ�� 800�ϸ� 2���� 1�� �ȳ����� 1�� ������ ��. ���� �� ũ��

N = 9000; %Fs/N = ���ļ��ػ�
K=N;
n = [0:N-1];
#------------------------------------------------------------------------------------------------------





#2. 0.8Fs, Fs, 1.5Fs�� �̿��Ͽ� ��ȣ ����-----------------------------------------------------------------
%��ȣ�߻� :: x=cos(2*pi*f/Fs*n)
%x = cos(2*pi*f/Fs*n); %n�� ũ��� ����
%analog�� �ٲ� ���� Fs�� ���ϰ� �ݴ�� discrete�� �ٲ� ���� Fs�� ����

fsc = [0.8 1.0 1.5];
x1=zeros(size(n));
x2=zeros(size(n));
x3=zeros(size(n));

for f=F
  x1=x1+cos(2*pi*f/(fsc(1)*Fs)*n+0); %0.8Fs�� �̿��Ͽ� ������ ��ȣ
                                     %4���� ���� ������ ����
  x2=x2+cos(2*pi*f/(fsc(2)*Fs)*n+0); 
  x3=x3+cos(2*pi*f/(fsc(3)*Fs)*n+0);
end
#-----------------------------------------------------------------------------------------------------





#3. ������ ��ȣ�� ���ؼ� ��հ��� �Ŀ����ϱ�----------------------------------------------------------------
##m=sum(x)/N; %mean(x);
##p = sum(x.^2)/N; %0.5 :: p=var(x) ==> average((x-mx)^2)
Mx1 = sum(x1)/N;      % mean(x); % mean value of x(n)
Px1 = sum(x1.^2)/N;      %var(x);  % power of x(n)
Mx2 = sum(x2)/N;
Px2 = sum(x2.^2)/N;
Mx3 = sum(x3)/N;
Px3 = sum(x3.^2)/N; 
#------------------------------------------------------------------------------------------------------





#4. ������ ��ȣ�鿡 ���� DTFT�� ���ϰ�, �� ����� plot �Լ��� �̿��Ͽ� �׸���----------------------------------
%����Ʈ�� ���ϱ� :: X(w)���ϱ� : DTFT ==> DTFS
w = (0:(K-1))*(pi/K);
Xw1 = x1*exp(-j*n'*w); % DTFT of x(n) --> x(n)e^(-jwn)
                       %Xw(0)���� Xw(K-1)���� ���� :: complex spectrum
Xw2 = x2*exp(-j*n'*w);
Xw3 = x3*exp(-j*n'*w);

%x�� -> Hz
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





#5-6. (x3��) ä�� ����� y ���ϱ�-------------------------------------------------------------------------
%channel :: y(n) = a*y(n-1)+x(n)+b*x(n-1) (a=0.8,b=-09) n=1,2,...,N
%Y(z) = a*Y(z)z^-1 + X(z) + b*X(z)*z^-1 ==> H(z)=Y(z)/X(z)

a=0.8; %�и��� ���
b=-0.9; %������ ���
y(1) = x3(1); %n=1 :: y(1) = a*y(0) + x(1) + b*x(0) = x(1)

for m=2:N %n=2,...,N :: y(n) = a*y(n-1)+x(n)+b*x(n-1)
  y(m) = a*y(m-1)+x3(m)+b*x3(m-1);
end
 
figure(2);
subplot(1,1,1);
stem(y(end-20:end)); %ä���� ����� y�׸���
set(gca,'fontsize',15);
xlabel('n','fontsize',17); ylabel('y(n)','fontsize',17); title('x3 -> channel -> y','fontsize',25);
#--------------------------------------------------------------------------------------------------------





#7.ä�� h(n)�� ���� DTFT H(w)�� ���ϰ� �׸���-----------------------------------------------------------------
figure(3);
Hw = (1+b*exp(-j*w))./(1-a*exp(-j*w));
plot(w*(fsc(3)*Fs)/(2*pi),abs(Hw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('H(w) by DTFT','fontsize',25);
#----------------------------------------------------------------------------------------------------------





#8. y(n)�� ���� DTFT Y(w)�� ���ϰ�, �̸� �׸� �� ���� ���� ����� �񱳺м�-----------------------------------
Yw = y*exp(-j*n'*w);
figure(4)
##subplot(2,1,1);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Yw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('Y(w)','fontsize',25);

#####7������ H(w)�� ����� ���ߴ��� �˻����� X(w)H(w)�� ���� Y(W)���� ������ ��
##subplot(2,1,2);
##plot(w*(fsc(3)*Fs)/(2*pi),abs(Xw3.*Hw'));
##set(gca,'fontsize',15);
##xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('X(w)H(w)','fontsize',25);
#----------------------------------------------------------------------------------------------------------





#9.H(z) -> pole-zero diagram & H(w) -> freqz �׸���-----------------------------------------------------------
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

%H(w) : high-pass filter������ ���ļ�����
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
%SNR : signal-Noise_ratio (��ȣ �� ������)
%SNR_dB 10*log10(Ps/Pn) Ps�� Pn�� ������ 0.
SNR = 10^(SNR_dB/10);
n_gain = sqrt(Px3/SNR/Pn);
yn=y+n_gain*noise;
#---------------------------------------------------------------------------------------------------------





#11. Yn(n)�� DTFT Yn(w)-----------------------------------------------------------------------------------
Ynw = yn*exp(-j*n'*w);
figure(7);
%Y(w)�� Yn(w)��
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
zplane(A,B); %H(z) = 1/Hi(z) ���� ����
set(gca,'fontsize',15);
xlabel('Re(z)','fontsize',17); ylabel('Im(z)','fontsize',17); title('inverse system Hi(z)','fontsize',25);

%���ļ����� HI(w)
Hiw = freqz(A,B,K);
subplot(1,2,2);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Hiw));
set(gca,'fontsize',15);
xlabel('Hz','fontsize',17); ylabel('magnitude','fontsize',17); title('inverse system Hi(w)','fontsize',25);
%low pass���� ���� ���ðŶ� ����
#-----------------------------------------------------------------------------------------------------------





#13. inverse system�� ����Ͽ� ������ xr--------------------------------------------------------------------------
##xr(1)=yn(1);
##for j=2:N
##  xr(j)=(-b)*xr(j-1)+yn(j)-a*yn(j-1);
##end
xr=filter(A,B,yn); %filter(�����ǰ��, �и��ǰ��, �Է�)

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






