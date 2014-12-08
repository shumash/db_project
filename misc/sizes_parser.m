clear all;
fid = fopen('sizes.txt');
C = textscan(fid, '%d %s %d %s %d %s %d %s %d %s %d %d %d %d');
images = C{end};
patches = C{end - 3};


plot(images, patches);
hold on;
m = 500^2;
n = 10^2;
ppi = m / n;

plot(images, ppi*images, '--r');

% images = double(images);
% patches = double(patches);
% [logitCoef,dev] = glmfit(images,patches,'normal','logit');
% logitFit = glmval(logitCoef, images, 'logit');
% plot(images,logitFit,'g-');
