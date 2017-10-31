  __asm {
        mov       ebx, ebp                                      ;63.1
        //and       esp, -8                                       ;63.1
        mov       ebp, DWORD PTR [ebx+4]                        ;63.1
        mov       DWORD PTR [esp+4], ebp                        ;63.1
        mov       ebp, esp                                      ;63.1
        sub       esp, 208                                      ;63.1
        fld       QWORD PTR [ebx+8]                             ;57.6
        mov       edx, DWORD PTR [ebx+16]                       ;57.6
        fld       QWORD PTR [ebx+24]                            ;57.6
        mov       ecx, DWORD PTR [ebx+32]                       ;57.6
        mov       eax, DWORD PTR [ebx+36]                       ;57.6
        mov       DWORD PTR [ebp-20], esi                       ;57.6
        fstp      st(0)                                         ;57.6
        mov       DWORD PTR [ebp-24], edi                       ;57.6
        fstp      st(0)                                         ;57.6
        ALIGN     4
                                ; LOE eax edx ecx
$B1$2:                          ; Preds .B1.6 .B1.1
        fld       QWORD PTR TWO_PI                              ;71.54
        fld       QWORD PTR [edx+8]                             ;66.16
        fld       QWORD PTR [edx+24]                            ;66.41
        fld       QWORD PTR [edx+40]                            ;66.66
        fld       QWORD PTR [edx+56]                            ;66.91
        fld       QWORD PTR [edx]                               ;71.54
        fmul      QWORD PTR [ebx+8]                             ;71.54
        fxch      st(4)                                         ;66.5
        fstp      QWORD PTR [ebp-208]                           ;66.5
        fxch      st(2)                                         ;66.30
        fstp      QWORD PTR [ebp-184]                           ;66.30
        fstp      QWORD PTR [ebp-160]                           ;66.55
        fstp      QWORD PTR [ebp-136]                           ;66.80

        fprem                                                   ;71.54
        fstp      st(1)                                         ;71.54

        fsubr     QWORD PTR [ebx+24]                            ;71.54
        fld       QWORD PTR [edx+16]                            ;72.54
        fmul      QWORD PTR [ebx+8]                             ;72.54
        fstp      QWORD PTR [ebp-96]                            ;72.54
        fabs                                                    ;71.54
        fmul      QWORD PTR GRID2_INV                           ;71.64
        fsub      QWORD PTR HALF                                ;71.74
        fistp     DWORD PTR [ebp-32]                            ;71.74
        mov       ecx, DWORD PTR [ebp-32]                       ;71.74

        fld       QWORD PTR TWO_PI                              ;72.54
        fld       QWORD PTR [ebp-96]                            ;72.54
        fprem                                                   ;72.54
        fstp      st(1)                                         ;72.54

        fsubr     QWORD PTR [ebx+24]                            ;72.54
        fld       QWORD PTR [edx+32]                            ;73.54
        fmul      QWORD PTR [ebx+8]                             ;73.54
        fstp      QWORD PTR [ebp-80]                            ;73.54
        fabs                                                    ;72.54
        fmul      QWORD PTR GRID2_INV                           ;72.64
        fsub      QWORD PTR HALF                                ;72.74
        fistp     DWORD PTR [ebp-32]                            ;72.74
        mov       esi, DWORD PTR [ebp-32]                       ;72.74

        fld       QWORD PTR TWO_PI                              ;73.54
        fld       QWORD PTR [ebp-80]                            ;73.54
        fprem                                                   ;73.54
        fstp      st(1)                                         ;73.54

        fsubr     QWORD PTR [ebx+24]                            ;73.54
        fld       QWORD PTR [edx+48]                            ;74.54
        fmul      QWORD PTR [ebx+8]                             ;74.54
        fstp      QWORD PTR [ebp-64]                            ;74.54
        fabs                                                    ;73.54
        fmul      QWORD PTR GRID2_INV                           ;73.64
        fsub      QWORD PTR HALF                                ;73.74
        fistp     DWORD PTR [ebp-32]                            ;73.74
        mov       edi, DWORD PTR [ebp-32]                       ;73.74

        fld       QWORD PTR TWO_PI                              ;74.54
        fld       QWORD PTR [ebp-64]                            ;74.54
        fprem                                                   ;74.54
        fstp      st(1)                                         ;74.54

        fsubr     QWORD PTR [ebx+24]                            ;74.54
        mov       eax, DWORD PTR [ebx+32]                       ;108.16
        fld       QWORD PTR [eax+ecx*8]                         ;108.16
        fld       QWORD PTR [eax+ecx*8+8]                       ;108.43
        fld       QWORD PTR [ebp-208]                           ;112.29
        mov       ecx, DWORD PTR [ebx+36]                       ;112.89
        fld       QWORD PTR [eax+esi*8+8]                       ;109.43
        fld       QWORD PTR [eax+edi*8]                         ;110.16
        fld       QWORD PTR [eax+edi*8+8]                       ;110.43
        fxch      st(5)                                         ;108.5
        fst       QWORD PTR [ebp-200]                           ;108.5
        fxch      st(4)                                         ;108.32
        fst       QWORD PTR [ebp-192]                           ;108.32
        fmul      st, st(3)                                     ;112.29
        fxch      st(4)                                         ;113.29
        fmulp     st(3), st                                     ;113.29
        fxch      st(1)                                         ;109.32
        fst       QWORD PTR [ebp-168]                           ;109.32
        fxch      st(1)                                         ;110.16
        fst       QWORD PTR [ebp-16]                            ;110.16
        fstp      QWORD PTR [ebp-152]                           ;110.5
        fxch      st(3)                                         ;110.32
        fst       QWORD PTR [ebp-144]                           ;110.32
        fxch      st(4)                                         ;74.54
        fabs                                                    ;74.54
        fmul      QWORD PTR GRID2_INV                           ;74.64
        fsub      QWORD PTR HALF                                ;74.74
        fistp     DWORD PTR [ebp-32]                            ;74.74
        fld       QWORD PTR [eax+esi*8]                         ;109.16
        mov       esi, DWORD PTR [ebp-32]                       ;111.17
        fld       QWORD PTR [eax+esi*8]                         ;111.17
        fld       QWORD PTR [eax+esi*8+8]                       ;111.45
        fxch      st(2)                                         ;109.5
        fst       QWORD PTR [ebp-176]                           ;109.5
        fxch      st(1)                                         ;111.17
        fst       QWORD PTR [ebp-8]                             ;111.17
        fstp      QWORD PTR [ebp-128]                           ;111.5
        fxch      st(1)                                         ;111.33
        fst       QWORD PTR [ebp-120]                           ;111.33
        fld       QWORD PTR [ebp-184]                           ;112.49
        fmul      st(5), st                                     ;112.49
        fmulp     st(2), st                                     ;113.49
        fxch      st(4)                                         ;112.5
        faddp     st(3), st                                     ;112.5
        faddp     st(1), st                                     ;113.5
        fld       QWORD PTR [ebp-160]                           ;112.69
        fmul      st(4), st                                     ;112.69
        fmul      QWORD PTR [ebp-16]                            ;113.69
        fxch      st(4)                                         ;112.49
        faddp     st(2), st                                     ;112.49
        faddp     st(3), st                                     ;113.49
        fld       QWORD PTR [ebp-136]                           ;112.89
        fmul      st(2), st                                     ;112.89
        fmul      QWORD PTR [ebp-8]                             ;113.89
        fxch      st(2)                                         ;112.69
        faddp     st(1), st                                     ;112.69
        fadd      QWORD PTR [ecx]                               ;112.89
        fstp      QWORD PTR [ecx]                               ;112.5
        faddp     st(1), st                                     ;113.69
        fadd      QWORD PTR [ecx+8]                             ;113.89
        fstp      QWORD PTR [ecx+8]                             ;113.5
        add       edx, 64                                       ;114.5
        cmp       edx, DWORD PTR [ebx+20]                       ;115.16
        jb        $B1$2         ; Prob 99%                      ;115.16

        mov       esi, DWORD PTR [ebp-20]                       ;
        mov       edi, DWORD PTR [ebp-24]                       ;
        mov       esp, ebp                                      ;116.1
        mov       ebp, ebx                                      ;116.1
  }
