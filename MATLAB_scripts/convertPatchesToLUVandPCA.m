%% load patches for conversion
clear
addpath(genpath('../colorspace/'));

load('patches_100_5.mat');

%% convert patches from RGB to Luv

newpatches = nan(size(patches));
npatches = size(patches,4);
for i = 1:npatches
    
    % print some output once in a while
    if mod(i,1000)==0 
        fprintf('%d of %d\n',i,npatches);
    end
    
    % convert current patch from RGB to Luv space
    newpatches(:,:,:,i) = colorspace(['Luv','<-RGB'],uint8(patches(:,:,:,i)));
end

%% save Luv version of patches
patches = newpatches;
save('patches_luv_100_5','patches');

%% prepare data for PCA
% the reshaping converts each patch into a vector, column-wise, and
% then concatenates all the color channels together
X = reshape(patches,size(patches,1)*size(patches,2)*size(patches,3),[]);

%% calculates eigenvectors and eigenvalues
[eigvecs,score,eigvals] = princomp(X');

%% write eigenvectors and eigenvalues to files
fid = fopen('pca_eigenvecs','w');
for i = 1:size(eigvecs,2)
    fprintf(fid,'%g ',eigvecs(:,i));
    fprintf(fid,'\n');
end
fclose(fid);
fid = fopen('pca_eigenvals','w');
for i = 1:length(eigvals)
    fprintf(fid,'%g \n',eigvals(i));
end
fclose(fid);


