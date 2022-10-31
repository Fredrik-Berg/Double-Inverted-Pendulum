%% Constants, variables and matrices
clc
clear all
close all

%Index 0 = cart, index 1 = inner pendulum, index 2 = outer pendulum
m_0 = 5.0; %kg
m_1 = 0.382; %kg
m_2 = 0.618; %kg
L_1 = 0.382; %m
L_2 = 0.618; %m
g = 9.81; %m/s^2
h = 0.02; %s   delta_t = h?

%Total length of pendulums divided by 2
l_1 = L_1/2;
l_2 = L_2/2;

%Inertia of pendulum arms
I_1 = (m_1*L_1^2)/12;
I_2 = (m_2*L_2^2)/12;

%Intermediate constants
d_1 = m_0 + m_1 + m_2;
d_2 = m_1*l_1 + m_2*L_1;
d_3 = m_2*l_2;
d_4 = m_1*l_1^2 + m_2*L_1^2 + I_1;
d_5 = m_2*L_1*l_2;
d_6 = m_2*l_2^2 + I_2;
f_1 = (m_1*l_1 + m_2*L_1)*g;
f_2 = m_2*l_2*g;

%Position, angle, angle velocity
syms theta_0; %wheeled cart position
syms theta_1; %inner pendulum angle
syms theta_2; %outer pendulum angle

syms theta_0_dot;
syms theta_1_dot;
syms theta_2_dot;

theta = [theta_0, theta_1, theta_2];
theta_dot = [theta_0_dot, theta_1_dot, theta_2_dot];

%Matrices of the lagrange equation for DIPC,
%   D*theta_dotdot + C*theta_dot + G = H*u
D = [d_1 d_2*cos(theta_1) d_3*cos(theta_2);
    d_2*cos(theta_1) d_4 d_5*cos(theta_1 - theta_2);
    d_3*cos(theta_2) d_5*cos(theta_1 - theta_2) d_6];

C = [0 -d_2*sin(theta_1)*theta_1_dot -d_3*sin(theta_2)*theta_2_dot;
    0 0 d_5*sin(theta_1 - theta_2)*theta_2_dot;
    0 -d_5*sin(theta_1 - theta_2)*theta_1_dot 0];

G = [0;
    -f_1*sin(theta_1);
    -f_2*sin(theta_2)];

H = [1;
    0;
    0];

%% Down-down equilibrium (pi, pi)
clc

x_0 = [0; pi; pi; 0; 0; 0];

dGx0 = [0 0 0; 0 -f_1*cos(x_0(2,1)) 0; 0 0 -f_2*cos(x_0(3,1))]; 

Dx0 = [d_1 d_2*cos(x_0(2,1)) d_3*cos(x_0(3,1));
    d_2*cos(x_0(2,1)) d_4 d_5*cos(x_0(2,1) - x_0(3,1));
    d_3*cos(x_0(3,1)) d_5*cos(x_0(2,1) - x_0(3,1)) d_6];

%Define A & B
Add = [zeros(3,3) eye(3); -Dx0\dGx0 zeros(3,3)];
Bdd = [zeros(3,1); Dx0\H];

sysd = c2d(ss(Add, Bdd, 0*eye(6), 0), h); %C = D = 0 ??
[Phidd, Gamdd, Cd, Dd] = ssdata(sysd);

%Define Q, R, N
%Q = eye(6);
Q = diag([5 100 100 4 120 120]);
%diag([10 60 80 0 0 0])
R = 1;
N = 0;

%Obtaining the control law
Kdd = dlqr(Phidd, Gamdd, Q, R, N);

%Save
save('Kdowndown.txt', 'Kdd', '-ascii');

%% Down-up equilibrium (pi, 0)
clc

x_0 = [0; pi; 0; 0; 0; 0];

dGx0 = [0 0 0; 0 -f_1*cos(x_0(2,1)) 0; 0 0 -f_2*cos(x_0(3,1))]; 

Dx0 = [d_1 d_2*cos(x_0(2,1)) d_3*cos(x_0(3,1));
    d_2*cos(x_0(2,1)) d_4 d_5*cos(x_0(2,1) - x_0(3,1));
    d_3*cos(x_0(3,1)) d_5*cos(x_0(2,1) - x_0(3,1)) d_6];

%Define A & B
Adu = [zeros(3,3) eye(3); -Dx0\dGx0 zeros(3,3)];
Bdu = [zeros(3,1); Dx0\H];

sysd = c2d(ss(Adu, Bdu, 0*eye(6), 0), h); %C = D = 0 ??
[Phidu, Gamdu, Cdu, Ddu] = ssdata(sysd);

%Define Q, R, N
%Q = diag([1 1 1 1 1 1]);
Q = diag([5 100 200 20 500 700]);
%diag([10 60 80 0 0 0])
R = 1;
N = 0;

%Obtaining the control law
Kdu = dlqr(Phidu, Gamdu, Q, R, N);

%Save
save('Kdownup.txt', 'Kdu', '-ascii');

%% Up-down equilibrium (0, pi)
clc

x_0 = [0; 0; pi; 0; 0; 0];

dGx0 = [0 0 0; 0 -f_1*cos(x_0(2,1)) 0; 0 0 -f_2*cos(x_0(3,1))]; 

Dx0 = [d_1 d_2*cos(x_0(2,1)) d_3*cos(x_0(3,1));
    d_2*cos(x_0(2,1)) d_4 d_5*cos(x_0(2,1) - x_0(3,1));
    d_3*cos(x_0(3,1)) d_5*cos(x_0(2,1) - x_0(3,1)) d_6];

%Define A & B
Aud = [zeros(3,3) eye(3); -Dx0\dGx0 zeros(3,3)];
Bud = [zeros(3,1); Dx0\H];

sysd = c2d(ss(Aud, Bud, 0*eye(6), 0), h); %C = D = 0 ??
[Phiud, Gamud, Cud, Dud] = ssdata(sysd);

%Define Q, R, N
Q = diag([1 150 100 4 600 400]);
%Q = diag([1 1 1 1 1 1]);
R = 1;
N = 0;

%Obtaining the control law
Kud = dlqr(Phiud, Gamud, Q, R, N);

%Save
save('Kupdown.txt', 'Kud', '-ascii');

%% Up-up equilibrium (0, 0)
clc

x_0 = [0; 0; 0; 0; 0; 0];

dGx0 = [0 0 0; 0 -f_1*cos(x_0(2,1)) 0; 0 0 -f_2*cos(x_0(3,1))]; 

Dx0 = [d_1 d_2*cos(x_0(2,1)) d_3*cos(x_0(3,1));
    d_2*cos(x_0(2,1)) d_4 d_5*cos(x_0(2,1) - x_0(3,1));
    d_3*cos(x_0(3,1)) d_5*cos(x_0(2,1) - x_0(3,1)) d_6];

%Define A & B
Auu = [zeros(3,3) eye(3); -Dx0\dGx0 zeros(3,3)];
Buu = [zeros(3,1); Dx0\H];

sysd = c2d(ss(Auu, Buu, 0*eye(6), 0), h); %C = D = 0 ??
[Phiuu, Gamuu, Cuu, Duu] = ssdata(sysd);

%Define Q, R, N
%Q = diag([5 300 300 20 700 700]);
Q = diag([1 300 500 4 700 900]);
%Q = diag([1 300 400 1 1 1]);
R = 1;
N = 0;

%Obtaining the control law
Kuu = dlqr(Phiuu, Gamuu, Q, R, N);

%Save
save('Kupup.txt', 'Kuu', '-ascii');
