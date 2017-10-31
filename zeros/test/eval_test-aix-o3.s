.set r0,0; .set SP,1; .set RTOC,2; .set r3,3; .set r4,4
.set r5,5; .set r6,6; .set r7,7; .set r8,8; .set r9,9
.set r10,10; .set r11,11; .set r12,12; .set r13,13; .set r14,14
.set r15,15; .set r16,16; .set r17,17; .set r18,18; .set r19,19
.set r20,20; .set r21,21; .set r22,22; .set r23,23; .set r24,24
.set r25,25; .set r26,26; .set r27,27; .set r28,28; .set r29,29
.set r30,30; .set r31,31
.set fp0,0; .set fp1,1; .set fp2,2; .set fp3,3; .set fp4,4
.set fp5,5; .set fp6,6; .set fp7,7; .set fp8,8; .set fp9,9
.set fp10,10; .set fp11,11; .set fp12,12; .set fp13,13; .set fp14,14
.set fp15,15; .set fp16,16; .set fp17,17; .set fp18,18; .set fp19,19
.set fp20,20; .set fp21,21; .set fp22,22; .set fp23,23; .set fp24,24
.set fp25,25; .set fp26,26; .set fp27,27; .set fp28,28; .set fp29,29
.set fp30,30; .set fp31,31
.set MQ,0; .set XER,1; .set FROM_RTCU,4; .set FROM_RTCL,5; .set FROM_DEC,6
.set LR,8; .set CTR,9; .set TID,17; .set DSISR,18; .set DAR,19; .set TO_RTCU,20
.set TO_RTCL,21; .set TO_DEC,22; .set SDR_0,24; .set SDR_1,25; .set SRR_0,26
.set SRR_1,27
.set BO_dCTR_NZERO_AND_NOT,0; .set BO_dCTR_NZERO_AND_NOT_1,1
.set BO_dCTR_ZERO_AND_NOT,2; .set BO_dCTR_ZERO_AND_NOT_1,3
.set BO_IF_NOT,4; .set BO_IF_NOT_1,5; .set BO_IF_NOT_2,6
.set BO_IF_NOT_3,7; .set BO_dCTR_NZERO_AND,8; .set BO_dCTR_NZERO_AND_1,9
.set BO_dCTR_ZERO_AND,10; .set BO_dCTR_ZERO_AND_1,11; .set BO_IF,12
.set BO_IF_1,13; .set BO_IF_2,14; .set BO_IF_3,15; .set BO_dCTR_NZERO,16
.set BO_dCTR_NZERO_1,17; .set BO_dCTR_ZERO,18; .set BO_dCTR_ZERO_1,19
.set BO_ALWAYS,20; .set BO_ALWAYS_1,21; .set BO_ALWAYS_2,22
.set BO_ALWAYS_3,23; .set BO_dCTR_NZERO_8,24; .set BO_dCTR_NZERO_9,25
.set BO_dCTR_ZERO_8,26; .set BO_dCTR_ZERO_9,27; .set BO_ALWAYS_8,28
.set BO_ALWAYS_9,29; .set BO_ALWAYS_10,30; .set BO_ALWAYS_11,31
.set CR0_LT,0; .set CR0_GT,1; .set CR0_EQ,2; .set CR0_SO,3
.set CR1_FX,4; .set CR1_FEX,5; .set CR1_VX,6; .set CR1_OX,7
.set CR2_LT,8; .set CR2_GT,9; .set CR2_EQ,10; .set CR2_SO,11
.set CR3_LT,12; .set CR3_GT,13; .set CR3_EQ,14; .set CR3_SO,15
.set CR4_LT,16; .set CR4_GT,17; .set CR4_EQ,18; .set CR4_SO,19
.set CR5_LT,20; .set CR5_GT,21; .set CR5_EQ,22; .set CR5_SO,23
.set CR6_LT,24; .set CR6_GT,25; .set CR6_EQ,26; .set CR6_SO,27
.set CR7_LT,28; .set CR7_GT,29; .set CR7_EQ,30; .set CR7_SO,31
.set TO_LT,16; .set TO_GT,8; .set TO_EQ,4; .set TO_LLT,2; .set TO_LGT,1

	.rename	H.10.NO_SYMBOL{PR},""
	.rename	H.16..__srterm__0__Fv,".__srterm__0__Fv"
	.rename	H.18..__srterm__1__Fv,".__srterm__1__Fv"
	.rename	H.24.NO_SYMBOL{TC},""
	.rename	H.26.NO_SYMBOL{RO},""
	.rename	E.28.__STATIC{RW},"_$STATIC"
	.rename	H.30.__STATIC{TC},"_$STATIC"
	.rename	H.34.__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{TC},"__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv"
	.rename	H.38.__srterm__0__Fv{TC},"__srterm__0__Fv"
	.rename	H.42.__srterm__1__Fv{TC},"__srterm__1__Fv"
	.rename	H.46.__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{TC},"__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv"
	.rename	H.50.swap__3stdHd_RdT1_v{TC},"swap__3stdHd_RdT1_v"

	.lglobl	H.10.NO_SYMBOL{PR}      
	.globl	.__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv
	.globl	.__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv
	.lglobl	H.16..__srterm__0__Fv   
	.lglobl	H.18..__srterm__1__Fv   
	.globl	.swap__3stdHd_RdT1_v    
	.lglobl	H.26.NO_SYMBOL{RO}      
	.lglobl	E.28.__STATIC{RW}       
	.globl	__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}
	.lglobl	__srterm__0__Fv{DS}     
	.lglobl	__srterm__1__Fv{DS}     
	.globl	__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}
	.globl	swap__3stdHd_RdT1_v{DS} 
	.extern	.__ct__Q3_3std8ios_base4InitFv{PR}
	.extern	.atexit{PR}             
	.extern	.__ct__Q2_3std6_WinitFv{PR}
	.extern	.high__FCd{PR}          
	.extern	.unatexit{PR}           
	.extern	.__dt__Q3_3std8ios_base4InitFv{PR}
	.extern	.__dt__Q2_3std6_WinitFv{PR}


# .text section
	.file	"eval_test.cpp"         


	.csect	H.10.NO_SYMBOL{PR}      
.__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv:# 0x00000000 (H.10.NO_SYMBOL)
	stm	r30,-8(SP)
	l	r30,T.30.__STATIC(RTOC)
	l	r31,T.24.NO_SYMBOL(RTOC)
	mfspr	r0,LR
	lfs	fp1,64(r31)
	lfd	fp0,24(r30)
	fm	fp2,fp0,fp1
	lfd	fp0,32(r30)
	lfs	fp1,68(r31)
	fm	fp0,fp0,fp1
	st	r0,8(SP)
	stu	SP,-64(SP)
	stfd	fp0,88(r30)
	stfd	fp2,80(r30)
	oril	r3,r30,0x0000
	bl	.__ct__Q3_3std8ios_base4InitFv{PR}
	oril	r0,r0,0x0000
	l	r3,T.38.__srterm__0__Fv(RTOC)
	bl	.atexit{PR}
	oril	r0,r0,0x0000
	cal	r3,1(r30)
	bl	.__ct__Q2_3std6_WinitFv{PR}
	oril	r0,r0,0x0000
	l	r3,T.42.__srterm__1__Fv(RTOC)
	bl	.atexit{PR}
	oril	r0,r0,0x0000
	lfd	fp0,88(r31)
	lfd	fp1,80(r31)
	lfd	fp2,96(r31)
	lfd	fp3,104(r31)
	stfd	fp1,8(r30)
	stfd	fp0,16(r30)
	stfd	fp3,32(r30)
	stfd	fp2,24(r30)
	lfd	fp1,72(r31)
	bl	.high__FCd{PR}
	oril	r0,r0,0x0000
	fmr	fp0,fp0
	stfd	fp1,40(r30)
	lfd	fp1,112(r31)
	bl	.high__FCd{PR}
	oril	r0,r0,0x0000
	fmr	fp0,fp0
	stfd	fp1,48(r30)
	lfd	fp1,88(r31)
	bl	.high__FCd{PR}
	oril	r0,r0,0x0000
	fmr	fp0,fp0
	stfd	fp1,56(r30)
	lfd	fp1,96(r31)
	bl	.high__FCd{PR}
	oril	r0,r0,0x0000
	fmr	fp0,fp0
	stfd	fp1,64(r30)
	lfd	fp1,120(r31)
	bl	.high__FCd{PR}
	oril	r0,r0,0x0000
	fmr	fp0,fp0
	stfd	fp1,72(r30)
	l	r12,72(SP)
	cal	SP,64(SP)
	mtspr	LR,r12
	lm	r30,-8(SP)
	bcr	BO_ALWAYS,CR0_LT
	.long	0x00000000
# traceback table
	.byte	0x00			# VERSION=0
	.byte	0x09			# LANG=TB_CPLUSPLUS
	.byte	0x22			# IS_GL=0,IS_EPROL=0,HAS_TBOFF=1
					# INT_PROC=0,HAS_CTL=0,TOCLESS=0
					# FP_PRESENT=1,LOG_ABORT=0
	.byte	0x01			# INT_HNDL=0,NAME_PRESENT=0
					# USES_ALLOCA=0,CL_DIS_INV=WALK_ONCOND
					# SAVES_CR=0,SAVES_LR=1
	.byte	0x80			# STORES_BC=1,FPR_SAVED=0
	.byte	0x02			# GPR_SAVED=2
	.byte	0x00			# FIXEDPARMS=0
	.byte	0x00			# FLOATPARMS=0,PARMSONSTK=0
	.long	0x00000100		# TB_OFFSET
# End of traceback table
.__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv:# 0x00000110 (H.10.NO_SYMBOL+0x110)
	mfspr	r0,LR
	stu	SP,-64(SP)
	l	r3,T.42.__srterm__1__Fv(RTOC)
	st	r0,72(SP)
	bl	.unatexit{PR}
	oril	r0,r0,0x0000
	cmpi	0,r3,0
	bc	BO_IF,CR0_EQ,__L140
	l	r3,T.38.__srterm__0__Fv(RTOC)
	bl	.unatexit{PR}
	oril	r0,r0,0x0000
	b	__L150
__L140:                                 # 0x00000140 (H.10.NO_SYMBOL+0x140)
	bl	H.18..__srterm__1__Fv
	l	r3,T.38.__srterm__0__Fv(RTOC)
	bl	.unatexit{PR}
	oril	r0,r0,0x0000
__L150:                                 # 0x00000150 (H.10.NO_SYMBOL+0x150)
	cmpi	0,r3,0
	bc	BO_IF,CR0_EQ,__L168
	l	r12,72(SP)
	cal	SP,64(SP)
	mtspr	LR,r12
	bcr	BO_ALWAYS,CR0_LT
__L168:                                 # 0x00000168 (H.10.NO_SYMBOL+0x168)
	bl	H.16..__srterm__0__Fv
	l	r12,72(SP)
	cal	SP,64(SP)
	mtspr	LR,r12
	bcr	BO_ALWAYS,CR0_LT
	.long	0x00000000
# traceback table
	.byte	0x00			# VERSION=0
	.byte	0x09			# LANG=TB_CPLUSPLUS
	.byte	0x20			# IS_GL=0,IS_EPROL=0,HAS_TBOFF=1
					# INT_PROC=0,HAS_CTL=0,TOCLESS=0
					# FP_PRESENT=0,LOG_ABORT=0
	.byte	0x01			# INT_HNDL=0,NAME_PRESENT=0
					# USES_ALLOCA=0,CL_DIS_INV=WALK_ONCOND
					# SAVES_CR=0,SAVES_LR=1
	.byte	0x80			# STORES_BC=1,FPR_SAVED=0
	.byte	0x00			# GPR_SAVED=0
	.byte	0x00			# FIXEDPARMS=0
	.byte	0x00			# FLOATPARMS=0,PARMSONSTK=0
	.long	0x0000006c		# TB_OFFSET
# End of traceback table
H.16..__srterm__0__Fv:                  # 0x0000018c (H.10.NO_SYMBOL+0x18c)
	mfspr	r0,LR
	stu	SP,-64(SP)
	cal	r4,2(r0)
	l	r3,T.30.__STATIC(RTOC)
	st	r0,72(SP)
	bl	.__dt__Q3_3std8ios_base4InitFv{PR}
	oril	r0,r0,0x0000
	l	r12,72(SP)
	cal	SP,64(SP)
	mtspr	LR,r12
	bcr	BO_ALWAYS,CR0_LT
	.long	0x00000000
# traceback table
	.byte	0x00			# VERSION=0
	.byte	0x09			# LANG=TB_CPLUSPLUS
	.byte	0x20			# IS_GL=0,IS_EPROL=0,HAS_TBOFF=1
					# INT_PROC=0,HAS_CTL=0,TOCLESS=0
					# FP_PRESENT=0,LOG_ABORT=0
	.byte	0x01			# INT_HNDL=0,NAME_PRESENT=0
					# USES_ALLOCA=0,CL_DIS_INV=WALK_ONCOND
					# SAVES_CR=0,SAVES_LR=1
	.byte	0x80			# STORES_BC=1,FPR_SAVED=0
	.byte	0x00			# GPR_SAVED=0
	.byte	0x00			# FIXEDPARMS=0
	.byte	0x00			# FLOATPARMS=0,PARMSONSTK=0
	.long	0x0000002c		# TB_OFFSET
# End of traceback table
H.18..__srterm__1__Fv:                  # 0x000001c8 (H.10.NO_SYMBOL+0x1c8)
	mfspr	r0,LR
	l	r3,T.30.__STATIC(RTOC)
	stu	SP,-64(SP)
	cal	r4,2(r0)
	cal	r3,1(r3)
	st	r0,72(SP)
	bl	.__dt__Q2_3std6_WinitFv{PR}
	oril	r0,r0,0x0000
	l	r12,72(SP)
	cal	SP,64(SP)
	mtspr	LR,r12
	bcr	BO_ALWAYS,CR0_LT
	.long	0x00000000
# traceback table
	.byte	0x00			# VERSION=0
	.byte	0x09			# LANG=TB_CPLUSPLUS
	.byte	0x20			# IS_GL=0,IS_EPROL=0,HAS_TBOFF=1
					# INT_PROC=0,HAS_CTL=0,TOCLESS=0
					# FP_PRESENT=0,LOG_ABORT=0
	.byte	0x01			# INT_HNDL=0,NAME_PRESENT=0
					# USES_ALLOCA=0,CL_DIS_INV=WALK_ONCOND
					# SAVES_CR=0,SAVES_LR=1
	.byte	0x80			# STORES_BC=1,FPR_SAVED=0
	.byte	0x00			# GPR_SAVED=0
	.byte	0x00			# FIXEDPARMS=0
	.byte	0x00			# FLOATPARMS=0,PARMSONSTK=0
	.long	0x00000030		# TB_OFFSET
# End of traceback table
.swap__3stdHd_RdT1_v:                   # 0x00000208 (H.10.NO_SYMBOL+0x208)
	lfd	fp0,0(r4)
	lfd	fp1,0(r3)
	stfd	fp0,0(r3)
	stfd	fp1,0(r4)
	bcr	BO_ALWAYS,CR0_LT
	.long	0x00000000
# traceback table
	.byte	0x00			# VERSION=0
	.byte	0x09			# LANG=TB_CPLUSPLUS
	.byte	0x22			# IS_GL=0,IS_EPROL=0,HAS_TBOFF=1
					# INT_PROC=0,HAS_CTL=0,TOCLESS=0
					# FP_PRESENT=1,LOG_ABORT=0
	.byte	0x00			# INT_HNDL=0,NAME_PRESENT=0
					# USES_ALLOCA=0,CL_DIS_INV=WALK_ONCOND
					# SAVES_CR=0,SAVES_LR=0
	.byte	0x00			# STORES_BC=0,FPR_SAVED=0
	.byte	0x00			# GPR_SAVED=0
	.byte	0x00			# FIXEDPARMS=0
	.byte	0x00			# FLOATPARMS=0,PARMSONSTK=0
	.long	0x00000014		# TB_OFFSET
# End of traceback table
	.long	0x00000000              # "\0\0\0\0"
# End	csect	H.10.NO_SYMBOL{PR}

# .data section


	.toc	                        # 0x00000230 
T.34.__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv:
	.tc	H.34.__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{TC},__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}
T.30.__STATIC:
	.tc	H.30.__STATIC{TC},E.28.__STATIC{RW}
T.24.NO_SYMBOL:
	.tc	H.24.NO_SYMBOL{TC},H.26.NO_SYMBOL{RO}
T.38.__srterm__0__Fv:
	.tc	H.38.__srterm__0__Fv{TC},__srterm__0__Fv{DS}
T.42.__srterm__1__Fv:
	.tc	H.42.__srterm__1__Fv{TC},__srterm__1__Fv{DS}
T.46.__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv:
	.tc	H.46.__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{TC},__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}
T.50.swap__3stdHd_RdT1_v:
	.tc	H.50.swap__3stdHd_RdT1_v{TC},swap__3stdHd_RdT1_v{DS}


	.csect	__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}
	.long	.__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv# "\0\0\0\0"
	.long	TOC{TC0}                # "\0\0\0020"
	.long	0x00000000              # "\0\0\0\0"
# End	csect	__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}


	.csect	__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}
	.long	.__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv# "\0\0\001\020"
	.long	TOC{TC0}                # "\0\0\0020"
	.long	0x00000000              # "\0\0\0\0"
# End	csect	__sterm80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}


	.csect	__srterm__0__Fv{DS}     
	.long	H.16..__srterm__0__Fv   # "\0\0\001\214"
	.long	TOC{TC0}                # "\0\0\0020"
	.long	0x00000000              # "\0\0\0\0"
# End	csect	__srterm__0__Fv{DS}


	.csect	__srterm__1__Fv{DS}     
	.long	H.18..__srterm__1__Fv   # "\0\0\001\310"
	.long	TOC{TC0}                # "\0\0\0020"
	.long	0x00000000              # "\0\0\0\0"
# End	csect	__srterm__1__Fv{DS}


	.csect	swap__3stdHd_RdT1_v{DS} 
	.long	.swap__3stdHd_RdT1_v    # "\0\0\002\b"
	.long	TOC{TC0}                # "\0\0\0020"
	.long	0x00000000              # "\0\0\0\0"
# End	csect	swap__3stdHd_RdT1_v{DS}


	.csect	E.28.__STATIC{RW}, 3    
	.long	__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}-__sinit80000000_x_2ftmp_2fzeta_2fsrc_2feval_5ftest_2ecpp__Fv{DS}
	.org	$-0x4
	.long	__srterm__0__Fv{DS}-__srterm__0__Fv{DS}
	.org	$-0x4
	.long	__srterm__1__Fv{DS}-__srterm__1__Fv{DS}# "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
	.long	0x00000000              # "\0\0\0\0"
# End	csect	E.28.__STATIC{RW}


	.csect	H.26.NO_SYMBOL{RO}, 3   
	.long	0x01000000              # "\001\0\0\0"
	.long	0x00000008              # "\0\0\0\b"
	.long	0x00100000              # "\0\020\0\0"
	.long	0x00080000              # "\0\b\0\0"
	.long	0x400921fb              # "@\t!\373"
	.long	0x54442d18              # "TD-\030"
	.long	0x400921fb              # "@\t!\373"
	.long	0x54442d18              # "TD-\030"
	.long	0x3fd45f30              # "?\324_0"
	.long	0x6dc9c882              # "m\311\310\202"
	.long	0x3fd921fb              # "?\331!\373"
	.long	0x54442d18              # "TD-\030"
	.long	0x401921fb              # "@\031!\373"
	.long	0x54442d18              # "TD-\030"
	.long	0x3fc45f30              # "?\304_0"
	.long	0x6dc9c882              # "m\311\310\202"
	.long	0x35800000              # "5\200\0\0"
	.long	0x49800000              # "I\200\0\0"
	.long	0x400921fb              # "@\t!\373"
	.long	0x54442d18              # "TD-\030"
	.long	0x3fd45f30              # "?\324_0"
	.long	0x6dc9c883              # "m\311\310\203"
	.long	0x3fd921fb              # "?\331!\373"
	.long	0x54442d18              # "TD-\030"
	.long	0x401921fb              # "@\031!\373"
	.long	0x54442d18              # "TD-\030"
	.long	0x3fc45f30              # "?\304_0"
	.long	0x6dc9c883              # "m\311\310\203"
	.long	0x3fd45f30              # "?\324_0"
	.long	0x6dc9c882              # "m\311\310\202"
	.long	0x3fc45f30              # "?\304_0"
	.long	0x6dc9c882              # "m\311\310\202"
# End	csect	H.26.NO_SYMBOL{RO}



# .bss section
