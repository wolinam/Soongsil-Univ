%single tone

%xa = cos(2*pi*f*t) analog singal (주기 신호) --> 주파수 (f성분 하나만 존재)
%x = cos((2*pi*f/Fs*n) digital singnal (w * Fs = Omega)
%f = 400(Hz)

close all; %열려있는 창(그림)닫기
clear all; %변수 다 지우기

f=[50 150 300 400]; %400hz
Fs = 800; %Fs > (2*fmax) :: 한 주기에 최소 2 샘플 이상의 데이터가 필요
          %Fs > 2*400 = 800 //정확히 800하면 2분의 1이 안나오고 1이 나오게 됨. 조금 더 크게
Fs = 850;
fsc = [0.8 1.0 1.5];
N=10000;
K=N;

%신호발생 :: x=cos(2*pi*f/Fs*n)
%x = cos(2*pi*f/Fs*n); %n의 크기로 나옴
%analog로 바꿀 때는 Fs를 곱하고 반대로 discrete로 바꿀 때는 Fs를 나눔

n = (0:N-1);

x1=zeros(size(x));
x2=zeros(size(x));
x3=zeros(size(x));
for f=F %f=f(m)
  x1=x1+cos(2*pi*f/fsc(1)*Fs*n+0); 
  %4개의 톤이 더해진 형태
  x2=x2+cos(2*pi*f/fsc(2)*Fs*n+0); 
  x3=x3+cos(2*pi*f/fsc(3)*Fs*n+0); 

end

##figure(1);
##stem(x(end-20:end);
%ctrl + r 하면 주석처리됨

##m=sum(x)/N; %mean(x);
##p = sum(x.^2)/N; %0.5 :: p=var(x) ==> average((x-mx)^2)
m1=sum(x1)/N; %mean(x);
p1 = sum(x1.^2)/N; %0.5*#of tones
%스펙트럼 구하기 :: X(w)구하기 : DTFT ==> DTFS

w = (0:(K-1))*(pi/K); %이렇게 하는거 추천 pi/K,2*pi/K,...,pi
%w =  0:(pi/K):pi; 이거보다 위의 식으로

Xw = x*exp(-j*n'*w); %Xw(0)부터 Xw(K-1)까지 나옴 :: complex spectrum


##figure(2);
##plot(w*Fs/(2*pi),abs(Xw));
%아날로그 주파수인 Fs를 곱해야 헤르츠 단위가 나옴
%N을 1000으로 줄이면, 원래 10000일 때 400에서 거의 정확하게 피크되었던것에서 사이드가 생긴다.
%그러므로 샘플값을 크게 해주면 좋음. 하지만 메모리 문제때문에 무작정 키울 수 없음


%만약 Fs를 810에서 600으로 낮추면 aliasing이 일어남 -> 400에서 피크되지 않고 200에서 피크됨
% alias는 별명. 자기자신을 나타내는게 하나만 있는 것이 아님. 디지털은 2pi 아날로그는 Fs를 주기로 갖음
%그러므로 400-Fs = 400-2Fs = alias임
%좌우대칭이므로 400신호가 있으면 -400신호도 있음. -400+600=200해서 200에 피크가 나옴

%Fs = 1000 이면 2배 넘는 것을 만족하므로 aliasing이 일어나면 안됨. 0~500인 관심범위를 벗어났기 때문에 aliasing이 일어나지 않는 것임.


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

%%노이즈 섞기
noise = randn(size(y));
Pn = var(noise);
SNR_dB = 20; % noise가 1/100임
%SNR : signal-Noise_ratio (신호 대 잡음비)
%SNR_dB 10*log10(Ps/Pn) Ps와 Pn이 같으면 0.
%-10이면 잡음이 신호보다 커지는데 이때 복구가 가능한지 확인 - 플젝
SNR = 10^(XNR_dB/10);
n_gain = sqrt(Px/SNR/Pn);
yn=y+n_gain*noise;
