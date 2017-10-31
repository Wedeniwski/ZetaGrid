; -- Machine type P5
; mark_description "Intel(R) C++ Compiler for 32-bit applications, Version 7.1   Build 20030307Z";
; mark_description "-I.. -I../../../arithmetic/doubledouble -Qvc6 -Qlocation,link,D:\\Compiler\\Microsoft Visual Studio\\VC98\\B";
; mark_description "in -DNPREP=1048576 -G5 -O2 -Qrcd -c -FofastSum2.obj -FA";
;ident "Intel(R) C++ Compiler for 32-bit applications, Version 7.1   Build 20030307Z"
;ident "-I.. -I../../../arithmetic/doubledouble -Qvc6 -Qlocation,link,D:\Compiler\Microsoft Visual Studio\VC98\Bin -DNPREP=1048576 -G5"
	.486P
 	.387
_TEXT	SEGMENT DWORD PUBLIC USE32 'CODE'
_TEXT	ENDS
_DATA	SEGMENT DWORD PUBLIC USE32 'DATA'
	ALIGN 004H
_DATA	ENDS
_BSS	SEGMENT DWORD PUBLIC USE32 'BSS'
	ALIGN 004H
_BSS	ENDS
_RDATA	SEGMENT DWORD PUBLIC USE32 'DATA'
	ALIGN 004H
_RDATA	ENDS
_TLS	SEGMENT DWORD PUBLIC USE32 'TLS'
	ALIGN 004H
_TLS	ENDS
_DATA1	SEGMENT DWORD PUBLIC USE32 'DATA'
	ALIGN 004H
_DATA1	ENDS
_TEXT1	SEGMENT DWORD PUBLIC USE32 'CODE'
	ALIGN 004H
_TEXT1	ENDS
	ASSUME	CS:FLAT,DS:FLAT,SS:FLAT
_BSS	SEGMENT DWORD PUBLIC USE32 'BSS'
?TWO_PI@@4NB	DD 2 DUP (?)	; pad
?GRID_INV@@4NB	DD 2 DUP (?)	; pad
_BSS	ENDS
_DATA1	SEGMENT DWORD PUBLIC USE32 'DATA'
?HALF@@4NB	DD 000000000H,03fe00000H	; xf64
_DATA1	ENDS
_DATA	SEGMENT DWORD PUBLIC USE32 'DATA'
_DATA	ENDS
_TEXT	SEGMENT DWORD PUBLIC USE32 'CODE'
;	COMDAT ?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z
; -- Begin  ?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z
; mark_begin;
IF @Version GE 612
  .MMX
  MMWORD TEXTEQU <QWORD>
ENDIF
IF @Version GE 614
  .XMM
  XMMWORD TEXTEQU <OWORD>
ENDIF
       ALIGN     4
	PUBLIC ?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z
?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z	PROC NEAR
; parameter 1: 8 + ebx
; parameter 2: 16 + ebx
; parameter 3: 20 + ebx
; parameter 4: 24 + ebx
; parameter 5: 32 + ebx
; parameter 6: 36 + ebx
.B1.1:                          ; Preds .B1.0
        push      ebx                                           ;63.1
        mov       ebx, esp                                      ;63.1
        and       esp, -8                                       ;63.1
        push      ebp                                           ;63.1
        push      ebp                                           ;63.1
        mov       ebp, DWORD PTR [ebx+4]                        ;63.1
        mov       DWORD PTR [esp+4], ebp                        ;63.1
        mov       ebp, esp                                      ;63.1
        sub       esp, 208                                      ;63.1
        fld       QWORD PTR [ebx+8]                             ;57.6
        mov       ecx, DWORD PTR [ebx+16]                       ;57.6
        fld       QWORD PTR [ebx+24]                            ;57.6
        mov       eax, DWORD PTR [ebx+32]                       ;57.6
        mov       edx, DWORD PTR [ebx+36]                       ;57.6
        mov       DWORD PTR [ebp-180], esi                      ;57.6
        mov       DWORD PTR [ebp-184], edi                      ;57.6
        fstp      st(0)                                         ;57.6
        fstp      st(0)                                         ;57.6
                                ; LOE eax edx ecx
.B1.2:                          ; Preds .B1.6 .B1.1
        mov       edx, DWORD PTR [ecx+8]                        ;66.16
        mov       esi, DWORD PTR [ecx+12]                       ;66.16
        mov       DWORD PTR [ebp-160], edx                      ;66.5
        mov       DWORD PTR [ebp-156], esi                      ;66.5
        mov       edi, DWORD PTR [ecx+24]                       ;66.41
        mov       edx, DWORD PTR [ecx+28]                       ;66.41
        mov       DWORD PTR [ebp-136], edi                      ;66.30
        mov       DWORD PTR [ebp-132], edx                      ;66.30
        mov       edx, DWORD PTR [ecx+40]                       ;66.66
        mov       esi, DWORD PTR [ecx+44]                       ;66.66
        mov       DWORD PTR [ebp-112], edx                      ;66.55
        mov       DWORD PTR [ebp-108], esi                      ;66.55
        mov       edx, DWORD PTR [ecx+56]                       ;66.91
        mov       esi, DWORD PTR [ecx+60]                       ;66.91
        mov       DWORD PTR [ebp-88], edx                       ;66.80
        mov       DWORD PTR [ebp-84], esi                       ;66.80
        fld       QWORD PTR [ecx]                               ;71.56
        fmul      QWORD PTR [ebx+8]                             ;71.56
        fstp      QWORD PTR [ebp-64]                            ;71.56
                                ; LOE eax ecx
.B1.10:                         ; Preds .B1.2
; Begin ASM
        fld       QWORD PTR ?TWO_PI@@4NB                        ;71.56
        fld       QWORD PTR [ebp-64]                            ;71.56
        fprem                                                   ;71.56
        fstp      QWORD PTR [ebp-56]                            ;71.56
        fstp      st(1)                                         ;71.56
; End ASM
                                ; LOE eax ecx
.B1.3:                          ; Preds .B1.10
        fld       QWORD PTR [ebp-56]                            ;71.56
        fsubr     QWORD PTR [ebx+24]                            ;71.56
        fabs                                                    ;71.56
        fmul      QWORD PTR ?GRID_INV@@4NB                      ;71.66
        fsub      QWORD PTR ?HALF@@4NB                          ;71.75
        fistp     DWORD PTR [ebp-192]                           ;71.75
        mov       edx, DWORD PTR [ebp-192]                      ;71.75
        add       edx, edx                                      ;71.75
        fld       QWORD PTR [ecx+16]                            ;72.56
        fmul      QWORD PTR [ebx+8]                             ;72.56
        fstp      QWORD PTR [ebp-48]                            ;72.56
                                ; LOE eax edx ecx
.B1.11:                         ; Preds .B1.3
; Begin ASM
        fld       QWORD PTR ?TWO_PI@@4NB                        ;72.56
        fld       QWORD PTR [ebp-48]                            ;72.56
        fprem                                                   ;72.56
        fstp      QWORD PTR [ebp-40]                            ;72.56
        fstp      st(1)                                         ;72.56
; End ASM
                                ; LOE eax edx ecx
.B1.4:                          ; Preds .B1.11
        fld       QWORD PTR [ebp-40]                            ;72.56
        fsubr     QWORD PTR [ebx+24]                            ;72.56
        fabs                                                    ;72.56
        fmul      QWORD PTR ?GRID_INV@@4NB                      ;72.66
        fsub      QWORD PTR ?HALF@@4NB                          ;72.75
        fistp     DWORD PTR [ebp-192]                           ;72.75
        mov       esi, DWORD PTR [ebp-192]                      ;72.75
        add       esi, esi                                      ;72.75
        mov       DWORD PTR [ebp-172], esi                      ;72.75
        fld       QWORD PTR [ecx+32]                            ;73.56
        fmul      QWORD PTR [ebx+8]                             ;73.56
        fstp      QWORD PTR [ebp-32]                            ;73.56
                                ; LOE eax edx ecx
.B1.12:                         ; Preds .B1.4
; Begin ASM
        fld       QWORD PTR ?TWO_PI@@4NB                        ;73.56
        fld       QWORD PTR [ebp-32]                            ;73.56
        fprem                                                   ;73.56
        fstp      QWORD PTR [ebp-24]                            ;73.56
        fstp      st(1)                                         ;73.56
; End ASM
                                ; LOE eax edx ecx
.B1.5:                          ; Preds .B1.12
        fld       QWORD PTR [ebp-24]                            ;73.56
        fsubr     QWORD PTR [ebx+24]                            ;73.56
        fabs                                                    ;73.56
        fmul      QWORD PTR ?GRID_INV@@4NB                      ;73.66
        fsub      QWORD PTR ?HALF@@4NB                          ;73.75
        fistp     DWORD PTR [ebp-192]                           ;73.75
        mov       esi, DWORD PTR [ebp-192]                      ;73.75
        add       esi, esi                                      ;73.75
        mov       DWORD PTR [ebp-176], esi                      ;73.75
        fld       QWORD PTR [ecx+48]                            ;74.56
        fmul      QWORD PTR [ebx+8]                             ;74.56
        fstp      QWORD PTR [ebp-16]                            ;74.56
                                ; LOE eax edx ecx
.B1.13:                         ; Preds .B1.5
; Begin ASM
        fld       QWORD PTR ?TWO_PI@@4NB                        ;74.56
        fld       QWORD PTR [ebp-16]                            ;74.56
        fprem                                                   ;74.56
        fstp      QWORD PTR [ebp-8]                             ;74.56
        fstp      st(1)                                         ;74.56
; End ASM
                                ; LOE eax edx ecx
.B1.6:                          ; Preds .B1.13
        mov       DWORD PTR [ebp-168], ecx                      ;
        fld       QWORD PTR [ebp-8]                             ;74.56
        fsubr     QWORD PTR [ebx+24]                            ;74.56
        fabs                                                    ;74.56
        fmul      QWORD PTR ?GRID_INV@@4NB                      ;74.66
        fsub      QWORD PTR ?HALF@@4NB                          ;74.75
        fistp     DWORD PTR [ebp-192]                           ;74.75
        mov       esi, DWORD PTR [ebp-192]                      ;74.75
        add       esi, esi                                      ;74.75
        mov       edi, DWORD PTR [eax+edx*8]                    ;108.16
        mov       ecx, DWORD PTR [eax+edx*8+4]                  ;108.16
        mov       DWORD PTR [ebp-152], edi                      ;108.5
        mov       DWORD PTR [ebp-148], ecx                      ;108.5
        mov       ecx, DWORD PTR [eax+edx*8+8]                  ;108.43
        mov       edx, DWORD PTR [eax+edx*8+12]                 ;108.43
        mov       DWORD PTR [ebp-144], ecx                      ;108.32
        mov       DWORD PTR [ebp-140], edx                      ;108.32
        mov       ecx, DWORD PTR [ebp-172]                      ;109.16
        mov       edx, DWORD PTR [eax+ecx*8]                    ;109.16
        mov       edi, DWORD PTR [eax+ecx*8+4]                  ;109.16
        mov       DWORD PTR [ebp-128], edx                      ;109.5
        mov       DWORD PTR [ebp-124], edi                      ;109.5
        mov       edx, DWORD PTR [eax+ecx*8+8]                  ;109.43
        mov       ecx, DWORD PTR [eax+ecx*8+12]                 ;109.43
        mov       DWORD PTR [ebp-120], edx                      ;109.32
        mov       DWORD PTR [ebp-116], ecx                      ;109.32
        mov       edx, DWORD PTR [ebp-176]                      ;110.16
        mov       ecx, DWORD PTR [eax+edx*8]                    ;110.16
        mov       edi, DWORD PTR [eax+edx*8+4]                  ;110.16
        mov       DWORD PTR [ebp-104], ecx                      ;110.5
        mov       DWORD PTR [ebp-100], edi                      ;110.5
        mov       ecx, DWORD PTR [eax+edx*8+8]                  ;110.43
        mov       edx, DWORD PTR [eax+edx*8+12]                 ;110.43
        mov       DWORD PTR [ebp-96], ecx                       ;110.32
        mov       DWORD PTR [ebp-92], edx                       ;110.32
        mov       edx, DWORD PTR [eax+esi*8]                    ;111.17
        mov       ecx, DWORD PTR [eax+esi*8+4]                  ;111.17
        mov       DWORD PTR [ebp-80], edx                       ;111.5
        mov       DWORD PTR [ebp-76], ecx                       ;111.5
        mov       edx, DWORD PTR [eax+esi*8+8]                  ;111.45
        mov       ecx, DWORD PTR [eax+esi*8+12]                 ;111.45
        mov       DWORD PTR [ebp-72], edx                       ;111.33
        mov       DWORD PTR [ebp-68], ecx                       ;111.33
        fld       QWORD PTR [ebp-160]                           ;112.20
        fld       QWORD PTR [ebp-136]                           ;112.40
        fld       QWORD PTR [ebp-112]                           ;112.60
        fld       QWORD PTR [ebp-88]                            ;112.80
        fld       QWORD PTR [ebp-144]                           ;112.29
        fmul      st, st(4)                                     ;112.29
        fld       QWORD PTR [ebp-120]                           ;112.49
        fmul      st, st(4)                                     ;112.49
        faddp     st(1), st                                     ;112.5
        fld       QWORD PTR [ebp-96]                            ;112.69
        fmul      st, st(3)                                     ;112.69
        faddp     st(1), st                                     ;112.49
        fld       QWORD PTR [ebp-72]                            ;112.89
        fmul      st, st(2)                                     ;112.89
        faddp     st(1), st                                     ;112.69
        mov       edx, DWORD PTR [ebx+36]                       ;112.89
        fadd      QWORD PTR [edx]                               ;112.89
        fstp      QWORD PTR [edx]                               ;112.5
        fxch      st(3)                                         ;113.29
        fmul      QWORD PTR [ebp-152]                           ;113.29
        fxch      st(2)                                         ;113.49
        fmul      QWORD PTR [ebp-128]                           ;113.49
        faddp     st(2), st                                     ;113.5
        fmul      QWORD PTR [ebp-104]                           ;113.69
        faddp     st(1), st                                     ;113.49
        fxch      st(1)                                         ;113.89
        fmul      QWORD PTR [ebp-80]                            ;113.89
        faddp     st(1), st                                     ;113.69
        fadd      QWORD PTR [edx+8]                             ;113.89
        fstp      QWORD PTR [edx+8]                             ;113.5
        mov       ecx, DWORD PTR [ebp-168]                      ;114.5
        add       ecx, 64                                       ;114.5
        cmp       ecx, DWORD PTR [ebx+20]                       ;115.16
        jb        .B1.2         ; Prob 99%                      ;115.16
                                ; LOE eax ecx
.B1.7:                          ; Preds .B1.6
        mov       esi, DWORD PTR [ebp-180]                      ;
        mov       edi, DWORD PTR [ebp-184]                      ;
        mov       esp, ebp                                      ;116.1
        pop       ebp                                           ;116.1
        mov       esp, ebx                                      ;116.1
        pop       ebx                                           ;116.1
        ret                                                     ;116.1
        ALIGN     4
                                ; LOE
; mark_end;
?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z ENDP
;?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z	ENDS
_TEXT	ENDS
_DATA	SEGMENT DWORD PUBLIC USE32 'DATA'
_DATA	ENDS
; -- End  ?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z
_BSS	SEGMENT DWORD PUBLIC USE32 'BSS'
?PI_INV@@4NB	DD 2 DUP (?)	; pad
?TWO_PI_INV@@4NB	DD 2 DUP (?)	; pad
_BSS	ENDS
_DATA1	SEGMENT DWORD PUBLIC USE32 'DATA'
_2il0floatpacket.7	DD 000000000H,041300000H	; xf64
_2il0floatpacket.5	DD 000000000H,03fe00000H	; xf64
_2il0floatpacket.3	DD 054442d18H,0401921fbH	; xf64
_2il0floatpacket.1	DD 06dc9c883H,03fd45f30H	; xf64
_DATA1	ENDS
_TEXT	SEGMENT DWORD PUBLIC USE32 'CODE'
;	COMDAT ___sti__fastSum_cpp_823a07ca
; -- Begin  ___sti__fastSum_cpp_823a07ca
; mark_begin;
       ALIGN     4

___sti__fastSum_cpp_823a07ca	PROC NEAR PRIVATE
.B2.1:                          ; Preds .B2.0
        push      ebx                                           ;
        mov       ebx, esp                                      ;
        and       esp, -8                                       ;
        mov       DWORD PTR ?PI_INV@@4NB, 1841940611            ;8.27
        mov       DWORD PTR ?PI_INV@@4NB+4, 1070882608          ;8.27
        mov       DWORD PTR ?TWO_PI@@4NB, 1413754136            ;9.25
        mov       DWORD PTR ?TWO_PI@@4NB+4, 1075388923          ;9.25
        fld       QWORD PTR _2il0floatpacket.5                  ;10.14
        fmul      QWORD PTR ?PI_INV@@4NB                        ;10.31
        fst       QWORD PTR ?TWO_PI_INV@@4NB                    ;10.31
        fld       QWORD PTR _2il0floatpacket.7                  ;16.14
        fmulp     st(1), st                                     ;16.31
        fstp      QWORD PTR ?GRID_INV@@4NB                      ;16.31
        mov       esp, ebx                                      ;16.31
        pop       ebx                                           ;16.31
        ret                                                     ;16.31
        ALIGN     4
                                ; LOE
; mark_end;
___sti__fastSum_cpp_823a07ca ENDP
;___sti__fastSum_cpp_823a07ca	ENDS
_TEXT	ENDS
_DATA	SEGMENT DWORD PUBLIC USE32 'DATA'
_DATA	ENDS
; -- End  ___sti__fastSum_cpp_823a07ca
_DATA	SEGMENT DWORD PUBLIC USE32 'DATA'
___link	DD 0	; u32
	DD OFFSET FLAT: ___sti__fastSum_cpp_823a07ca	; p32
	DD 0	; u32
_DATA	ENDS
_DATA1	SEGMENT DWORD PUBLIC USE32 'DATA'
?PI@@4NB	DD 054442d18H,0400921fbH	; xf64
_DATA1	ENDS
.CRT$XCU	SEGMENT DWORD PUBLIC USE32 'DATA'
	ALIGN 004H
__init_0	DD OFFSET FLAT: ___sti__fastSum_cpp_823a07ca	; p32
.CRT$XCU	ENDS
_DATA	SEGMENT DWORD PUBLIC USE32 'DATA'
EXTRN	__fltused:BYTE
EXTRN	__fltused:BYTE
_DATA	ENDS
; mark_proc_addr_taken ___sti__fastSum_cpp_823a07ca;
	END
