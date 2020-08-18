%single tone

%xa = cos(2*pi*f*t) analog singal (�ֱ� ��ȣ) --> ���ļ� (f���� �ϳ��� ����)
%x = cos((2*pi*f/Fs*n) digital singnal (w * Fs = Omega)
%f = 400(Hz)

close all; %�����ִ� â(�׸�)�ݱ�
clear all; %���� �� �����

f=[50 150 300 400]; %400hz
Fs = 800; %Fs > (2*fmax) :: �� �ֱ⿡ �ּ� 2 ���� �̻��� �����Ͱ� �ʿ�
          %Fs > 2*400 = 800 //��Ȯ�� 800�ϸ� 2���� 1�� �ȳ����� 1�� ������ ��. ���� �� ũ��
Fs = 850;
fsc = [0.8 1.0 1.5];
N=10000;
K=N;

%��ȣ�߻� :: x=cos(2*pi*f/Fs*n)
%x = cos(2*pi*f/Fs*n); %n�� ũ��� ����
%analog�� �ٲ� ���� Fs�� ���ϰ� �ݴ�� discrete�� �ٲ� ���� Fs�� ����

n = (0:N-1);

x1=zeros(size(x));
x2=zeros(size(x));
x3=zeros(size(x));
for f=F %f=f(m)
  x1=x1+cos(2*pi*f/fsc(1)*Fs*n+0); 
  %4���� ���� ������ ����
  x2=x2+cos(2*pi*f/fsc(2)*Fs*n+0); 
  x3=x3+cos(2*pi*f/fsc(3)*Fs*n+0); 

end

##figure(1);
##stem(x(end-20:end);
%ctrl + r �ϸ� �ּ�ó����

##m=sum(x)/N; %mean(x);
##p = sum(x.^2)/N; %0.5 :: p=var(x) ==> average((x-mx)^2)
m1=sum(x1)/N; %mean(x);
p1 = sum(x1.^2)/N; %0.5*#of tones
%����Ʈ�� ���ϱ� :: X(w)���ϱ� : DTFT ==> DTFS

w = (0:(K-1))*(pi/K); %�̷��� �ϴ°� ��õ pi/K,2*pi/K,...,pi
%w =  0:(pi/K):pi; �̰ź��� ���� ������

Xw = x*exp(-j*n'*w); %Xw(0)���� Xw(K-1)���� ���� :: complex spectrum


##figure(2);
##plot(w*Fs/(2*pi),abs(Xw));
%�Ƴ��α� ���ļ��� Fs�� ���ؾ� �츣�� ������ ����
%N�� 1000���� ���̸�, ���� 10000�� �� 400���� ���� ��Ȯ�ϰ� ��ũ�Ǿ����Ϳ��� ���̵尡 �����.
%�׷��Ƿ� ���ð��� ũ�� ���ָ� ����. ������ �޸� ���������� ������ Ű�� �� ����


%���� Fs�� 810���� 600���� ���߸� aliasing�� �Ͼ -> 400���� ��ũ���� �ʰ� 200���� ��ũ��
% alias�� ����. �ڱ��ڽ��� ��Ÿ���°� �ϳ��� �ִ� ���� �ƴ�. �������� 2pi �Ƴ��α״� Fs�� �ֱ�� ����
%�׷��Ƿ� 400-Fs = 400-2Fs = alias��
%�¿��Ī�̹Ƿ� 400��ȣ�� ������ -400��ȣ�� ����. -400+600=200�ؼ� 200�� ��ũ�� ����

%Fs = 1000 �̸� 2�� �Ѵ� ���� �����ϹǷ� aliasing�� �Ͼ�� �ȵ�. 0~500�� ���ɹ����� ����� ������ aliasing�� �Ͼ�� �ʴ� ����.


figure(1);
subplot(3,1,1);
plot(w*(fsc(1)*Fs)/(2*pi),abs(Xw1));
subplot(3,1,2);
plot(w*(fsc(2)*Fs)/(2*pi),abs(Xw2));
subplot(3,1,3);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Xw3));

%channel :: y(n) = a*y(n-1)+x(n)+b*x(n-1) (a=0.8,b=-09) n=1,2,...,N
%Y(z) = a*Y(z)z^-1 + X(z) + b*X(z)*z^-1 ==> H(z)=Y(z)/X(z)
%n=1 :: y(1) = a*y(0) + x(1) + b*x(0) = x(1)
%n=2,...,N :: y(n) = a*y(n-1)+x(n)+b*x(n-1)
a=0.8;
b=-0.9;
y(1) = x3(1);
for m=2:N
  y(m) = a*y(m-1)+x3(m)+b*x3(m-1);
end

figure(2);
stem(y(end-20:end));

Yw = y*exp(-j*n'*w);
subplot(2,1,2);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Yw));

%pkg load signal
zplane(2,1,2);
plot(w*(fsc(3)*Fs)/(2*pi),abs(Yw));
%Y(z)(1-az^-1)=x(z)(1+bz^-1)=>Y(z)A(Z) = X(z)B(z)
B=[1 b];
A=[1 -a];
zplane(B,A);
%high-pass filter
Hw = freqz(B,A,K);
figure(3);
plot(w*fsc(3)*Fs)/(2*pi),abs(Hw));

%%������ ����
noise = randn(size(y));
Pn = var(noise);
SNR_dB = 20; % noise�� 1/100��
%SNR : signal-Noise_ratio (��ȣ �� ������)
%SNR_dB 10*log10(Ps/Pn) Ps�� Pn�� ������ 0.
%-10�̸� ������ ��ȣ���� Ŀ���µ� �̶� ������ �������� Ȯ�� - ����
SNR = 10^(XNR_dB/10);
n_gain = sqrt(Px/SNR/Pn);
yn=y+n_gain*noise;
