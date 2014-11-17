function plot_ip_pairs(ivec, pvec, n, m)
%ivec is the vector of image counts
%pvec is the vector of pointer counts
%n is patch height (assume square)
%m is the image height (assume square)

hold on



isize = length(ivec);
psize = length(pvec);

icost = zeros(isize, psize);
pcost = zeros(isize, psize);

count = 1;

for i = 1:1:length(ivec)
    for p = 1:1:length(pvec)
        
        icost(i, p) = 3 * ivec(i) * (m / n)^2 + 8 * pvec(p) * n^2;
        pcost(i, p) = 3 * ivec(i) * m^2;
        
        count = count + 1;
    end
end

surf(ivec, pvec, icost');
surf(ivec, pvec, pcost');

hold off