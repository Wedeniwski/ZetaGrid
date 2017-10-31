mkdir %3
cd %3
set value=%1
set range=%2
call ..\zeta_zeros %value% %range% %3
cd ..
fc %3\zeta_zeros_%value%_%range%.txt 0\zeta_zeros_%value%_%range%.txt
