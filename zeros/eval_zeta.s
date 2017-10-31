	.file	"eval_zeta.cpp"
.lcomm __ZSt8__ioinit,16
.lcomm _PI_HIGH,16
.lcomm _PI_INV_HIGH,16
.lcomm _PI_SL8_HIGH,16
.lcomm _TWO_PI_HIGH,16
.lcomm _TWO_PI_INV_HIGH,16
.globl _sqrtinvMem
	.bss
	.align 4
_sqrtinvMem:
	.space 20
.globl __ZN14FreeSqrtinvMem6activeE
	.align 4
__ZN14FreeSqrtinvMem6activeE:
	.space 4
.globl _NPREP
	.data
	.align 4
_NPREP:
	.long	524288
.globl _NPREP2
	.bss
	.align 4
_NPREP2:
	.space 4
.globl _GRID
	.align 8
_GRID:
	.space 8
.globl _GRID2_INV
	.align 8
_GRID2_INV:
	.space 8
.globl _sleepMode
	.align 4
_sleepMode:
	.space 4
.globl _sleepCounter
	.align 4
_sleepCounter:
	.space 4
.globl __ZN8EvalZeta7runningE
	.align 4
__ZN8EvalZeta7runningE:
	.space 4
.globl __ZN8EvalZeta14fastSumZMethodE
	.align 4
__ZN8EvalZeta14fastSumZMethodE:
	.space 4
.globl __ZN8EvalZeta8cosValueE
	.align 4
__ZN8EvalZeta8cosValueE:
	.space 4
.globl __ZN8EvalZeta9TWO_PI_DDE
	.align 8
__ZN8EvalZeta9TWO_PI_DDE:
	.space 16
.globl __ZN8EvalZeta13TWO_PI_INV_DDE
	.align 8
__ZN8EvalZeta13TWO_PI_INV_DDE:
	.space 16
.globl __ZN8EvalZeta3DC0E
	.align 32
__ZN8EvalZeta3DC0E:
	.space 624
.globl __ZN8EvalZeta3DC1E
	.align 32
__ZN8EvalZeta3DC1E:
	.space 624
.globl __ZN8EvalZeta3DC2E
	.align 32
__ZN8EvalZeta3DC2E:
	.space 624
.globl __ZN8EvalZeta3DC3E
	.align 32
__ZN8EvalZeta3DC3E:
	.space 624
.globl __ZN8EvalZeta8DDC0_LOWE
	.align 32
__ZN8EvalZeta8DDC0_LOWE:
	.space 624
.globl __ZN8EvalZeta8DDC1_LOWE
	.align 32
__ZN8EvalZeta8DDC1_LOWE:
	.space 624
.globl __ZN8EvalZeta8DDC2_LOWE
	.align 32
__ZN8EvalZeta8DDC2_LOWE:
	.space 624
.globl __ZN8EvalZeta8DDC3_LOWE
	.align 32
__ZN8EvalZeta8DDC3_LOWE:
	.space 624
.globl __ZN8EvalZeta9DDC0_HIGHE
	.align 32
__ZN8EvalZeta9DDC0_HIGHE:
	.space 624
.globl __ZN8EvalZeta9DDC1_HIGHE
	.align 32
__ZN8EvalZeta9DDC1_HIGHE:
	.space 624
.globl __ZN8EvalZeta9DDC2_HIGHE
	.align 32
__ZN8EvalZeta9DDC2_HIGHE:
	.space 624
.globl __ZN8EvalZeta9DDC3_HIGHE
	.align 32
__ZN8EvalZeta9DDC3_HIGHE:
	.space 624
.globl __ZN8EvalZeta7DC0_LOWE
	.align 32
__ZN8EvalZeta7DC0_LOWE:
	.space 312
.globl __ZN8EvalZeta7DC1_LOWE
	.align 32
__ZN8EvalZeta7DC1_LOWE:
	.space 312
.globl __ZN8EvalZeta7DC2_LOWE
	.align 32
__ZN8EvalZeta7DC2_LOWE:
	.space 312
.globl __ZN8EvalZeta7DC3_LOWE
	.align 32
__ZN8EvalZeta7DC3_LOWE:
	.space 312
.globl __ZN8EvalZeta8DC0_HIGHE
	.align 32
__ZN8EvalZeta8DC0_HIGHE:
	.space 312
.globl __ZN8EvalZeta8DC1_HIGHE
	.align 32
__ZN8EvalZeta8DC1_HIGHE:
	.space 312
.globl __ZN8EvalZeta8DC2_HIGHE
	.align 32
__ZN8EvalZeta8DC2_HIGHE:
	.space 312
.globl __ZN8EvalZeta8DC3_HIGHE
	.align 32
__ZN8EvalZeta8DC3_HIGHE:
	.space 312
.globl _HALF
	.data
	.align 8
_HALF:
	.long	0
	.long	1071644672
.globl _CONST_TWO_PI
	.align 8
_CONST_TWO_PI:
	.long	1413754136
	.long	1075388923
	.text
	.align 8
LC0:
	.long	1413754136
	.long	1074340347
	.align 8
LC1:
	.long	1841940610
	.long	1070882608
	.align 8
LC2:
	.long	1413754136
	.long	1071194619
	.align 8
LC3:
	.long	1413754136
	.long	1075388923
	.align 8
LC4:
	.long	1841940610
	.long	1069834032
	.align 8
LC5:
	.long	1841940611
	.long	1069834032
	.align 2
	.align 16
	.def	__Z41__static_initialization_and_destruction_0ii;	.scl	3;	.type	32;	.endef
__Z41__static_initialization_and_destruction_0ii:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$24, %esp
	movl	%edi, -4(%ebp)
	movl	12(%ebp), %edi
	movl	%esi, -8(%ebp)
	movl	8(%ebp), %esi
	cmpl	$65535, %edi
	movl	%ebx, -12(%ebp)
	je	L107
L1:
	movl	-12(%ebp), %ebx
	movl	-8(%ebp), %esi
	movl	-4(%ebp), %edi
	movl	%ebp, %esp
	popl	%ebp
	ret
	.align 16
L107:
	cmpl	$1, %esi
	je	L108
L12:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L109
L19:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L110
L26:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L111
L33:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L112
L40:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L113
L47:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L114
L54:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L115
L61:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L116
L68:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L117
L75:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L118
L82:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L119
L89:
	cmpl	$65535, %edi
	jne	L1
	cmpl	$1, %esi
	je	L120
L96:
	cmpl	$65535, %edi
	jne	L1
	testl	%esi, %esi
	jne	L1
	movl	$__ZSt8__ioinit, 8(%ebp)
	movl	-12(%ebp), %ebx
	movl	-8(%ebp), %esi
	movl	-4(%ebp), %edi
	movl	%ebp, %esp
	popl	%ebp
	jmp	__ZNSt8ios_base4InitD1Ev
L120:
	movl	$__ZN8EvalZeta9DDC3_HIGHE, %eax
	movl	$38, %edx
	fldz
L98:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L98
	fstp	%st(0)
	jmp	L96
L119:
	movl	$__ZN8EvalZeta9DDC2_HIGHE, %eax
	movl	$38, %edx
	fldz
L91:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L91
	fstp	%st(0)
	jmp	L89
L118:
	movl	$__ZN8EvalZeta9DDC1_HIGHE, %eax
	movl	$38, %edx
	fldz
L84:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L84
	fstp	%st(0)
	jmp	L82
L117:
	movl	$__ZN8EvalZeta9DDC0_HIGHE, %eax
	movl	$38, %edx
	fldz
L77:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L77
	fstp	%st(0)
	jmp	L75
L116:
	movl	$__ZN8EvalZeta8DDC3_LOWE, %eax
	movl	$38, %edx
	fldz
L70:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L70
	fstp	%st(0)
	jmp	L68
L115:
	movl	$__ZN8EvalZeta8DDC2_LOWE, %eax
	movl	$38, %edx
	fldz
L63:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L63
	fstp	%st(0)
	jmp	L61
	.align 16
L114:
	movl	$__ZN8EvalZeta8DDC1_LOWE, %eax
	movl	$38, %edx
	fldz
L56:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L56
	fstp	%st(0)
	jmp	L54
L113:
	movl	$__ZN8EvalZeta8DDC0_LOWE, %eax
	movl	$38, %edx
	fldz
L49:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L49
	fstp	%st(0)
	jmp	L47
L112:
	movl	$__ZN8EvalZeta3DC3E, %eax
	movl	$38, %edx
	fldz
L42:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L42
	fstp	%st(0)
	jmp	L40
L111:
	movl	$__ZN8EvalZeta3DC2E, %eax
	movl	$38, %edx
	fldz
L35:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L35
	fstp	%st(0)
	jmp	L33
L110:
	movl	$__ZN8EvalZeta3DC1E, %eax
	movl	$38, %edx
	fldz
	.align 16
L28:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L28
	fstp	%st(0)
	jmp	L26
L109:
	movl	_NPREP, %ebx
	leal	2(%ebx,%ebx), %ecx
	sall	$3, %ecx
	movl	%ecx, (%esp)
	sall	$4, %ebx
	call	__Znaj
	movl	%eax, __ZN8EvalZeta8cosValueE
	fldz
	cmpl	$1, %esi
	fstl	8(%eax,%ebx)
	jne	L106
	fstl	__ZN8EvalZeta9TWO_PI_DDE
	fstl	__ZN8EvalZeta9TWO_PI_DDE+8
	jne	L106
	fstl	__ZN8EvalZeta13TWO_PI_INV_DDE
	fstl	__ZN8EvalZeta13TWO_PI_INV_DDE+8
	jne	L106
	movl	$__ZN8EvalZeta3DC0E, %eax
	movl	$38, %edx
	.align 16
L21:
	fstl	(%eax)
	decl	%edx
	fstl	8(%eax)
	addl	$16, %eax
	cmpl	$-1, %edx
	jne	L21
L106:
	fstp	%st(0)
	jmp	L19
L108:
	movl	$__ZSt8__ioinit, (%esp)
	call	__ZNSt8ios_base4InitC1Ev
	cmpl	$1, %esi
	jne	L12
	fldl	LC0
	fstpl	(%esp)
	call	__Z4highd
	cmpl	$1, %esi
	fstpl	_PI_HIGH
	jne	L12
	fldl	LC1
	fstpl	(%esp)
	call	__Z4highd
	cmpl	$1, %esi
	fstpl	_PI_INV_HIGH
	jne	L12
	fldl	LC2
	fstpl	(%esp)
	call	__Z4highd
	cmpl	$1, %esi
	fstpl	_PI_SL8_HIGH
	jne	L12
	fldl	LC3
	fstpl	(%esp)
	call	__Z4highd
	cmpl	$1, %esi
	fstpl	_TWO_PI_HIGH
	jne	L12
	fldl	LC4
	fstpl	(%esp)
	call	__Z4highd
	cmpl	$1, %esi
	fstpl	_TWO_PI_INV_HIGH
	jne	L12
	movl	$0, %edx
	movl	$0, %ebx
	movl	$0, %ecx
	movl	%edx, _sqrtinvMem
	movl	$0, %eax
	movl	$0, %edx
	movl	%ebx, _sqrtinvMem+12
	movl	%ecx, _sqrtinvMem+16
	movl	%eax, _sqrtinvMem+8
	movl	%edx, _sqrtinvMem+4
	jne	L12
	movl	_NPREP, %ecx
	movl	%ecx, %eax
	shrl	$31, %eax
	addl	%eax, %ecx
	sarl	%ecx
	cmpl	$1, %esi
	movl	%ecx, _NPREP2
	jne	L12
	fildl	_NPREP
	fdivrl	LC3
	fstpl	_GRID
	jne	L12
	movl	_NPREP, %edx
	addl	%edx, %edx
	pushl	%edx
	fildl	(%esp)
	addl	$4, %esp
	fmull	LC5
	fstpl	_GRID2_INV
	jmp	L12
	.align 2
	.align 16
.globl __ZN14FreeSqrtinvMem3getEiRP6lnSqrtRP12doubledoubleS5_
	.def	__ZN14FreeSqrtinvMem3getEiRP6lnSqrtRP12doubledoubleS5_;	.scl	2;	.type	32;	.endef
__ZN14FreeSqrtinvMem3getEiRP6lnSqrtRP12doubledoubleS5_:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	pushl	%esi
	pushl	%ebx
	subl	$92, %esp
	movl	12(%ebp), %edx
	cmpl	_sqrtinvMem+12, %edx
	jg	L122
	incl	_sqrtinvMem+16
	movl	_sqrtinvMem+4, %esi
	movl	20(%ebp), %ebx
	movl	_sqrtinvMem, %ecx
	movl	16(%ebp), %edi
	movl	%esi, (%ebx)
	movl	_sqrtinvMem+8, %edx
	movl	%ecx, (%edi)
	movl	24(%ebp), %ecx
	movl	%edx, (%ecx)
L121:
	leal	-12(%ebp), %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L122:
	addl	$500, 12(%ebp)
	movl	12(%ebp), %esi
	incl	%esi
	movl	%esi, %edi
	sall	$4, %edi
	movl	%edi, (%esp)
	call	__Znaj
	movl	16(%ebp), %ebx
	movl	%eax, (%ebx)
	movl	%edi, (%esp)
	call	__Znaj
	movl	12(%ebp), %ecx
	movl	%eax, %edx
	cmpl	$-1, %ecx
	je	L124
	fldz
	.align 16
L125:
	fstl	(%edx)
	decl	%ecx
	fstl	8(%edx)
	addl	$16, %edx
	cmpl	$-1, %ecx
	jne	L125
	fstp	%st(0)
L124:
L130:
	movl	20(%ebp), %ecx
	sall	$4, %esi
	movl	%eax, (%ecx)
	movl	%esi, (%esp)
	call	__Znaj
	movl	12(%ebp), %ecx
	movl	%eax, %edx
	cmpl	$-1, %ecx
	je	L132
	fldz
	.align 16
L133:
	fstl	(%edx)
	decl	%ecx
	fstl	8(%edx)
	addl	$16, %edx
	cmpl	$-1, %ecx
	jne	L133
	fstp	%st(0)
L132:
L138:
	movl	$1, %edi
	movl	24(%ebp), %esi
	cmpl	12(%ebp), %edi
	movl	%eax, (%esi)
	jg	L161
	.align 16
L152:
	pushl	%edi
	movl	20(%ebp), %edx
	leal	-40(%ebp), %ecx
	fildl	(%esp)
	movl	%edi, %esi
	addl	$4, %esp
	movl	$0, -32(%ebp)
	movl	(%edx), %ebx
	sall	$4, %esi
	movl	%ecx, 4(%esp)
	leal	-56(%ebp), %edx
	incl	%edi
	movl	%edx, (%esp)
	addl	%esi, %ebx
	movl	$0, -28(%ebp)
	fstl	-80(%ebp)
	fstpl	-40(%ebp)
	call	__Z3logRK12doubledouble
	fldl	-48(%ebp)
	subl	$4, %esp
	movl	16(%ebp), %eax
	fldl	-56(%ebp)
	fxch	%st(1)
	movl	24(%ebp), %ecx
	leal	-72(%ebp), %edx
	fstl	-32(%ebp)
	fstpl	8(%ebx)
	fstl	-40(%ebp)
	fldl	-32(%ebp)
	fxch	%st(1)
	fstpl	(%ebx)
	movl	(%eax), %ebx
	leal	-40(%ebp), %eax
	faddl	-40(%ebp)
	fstpl	(%ebx,%esi)
	movl	(%ecx), %ebx
	fldl	-80(%ebp)
	movl	%eax, 4(%esp)
	addl	%esi, %ebx
	movl	%edx, (%esp)
	fstpl	-40(%ebp)
	movl	$0, -32(%ebp)
	movl	$0, -28(%ebp)
	call	__Z5recipRK12doubledouble
	subl	$4, %esp
	leal	-72(%ebp), %ecx
	leal	-56(%ebp), %edx
	movl	%ecx, 4(%esp)
	movl	%edx, (%esp)
	call	__Z4sqrtRK12doubledouble
	fldl	-48(%ebp)
	subl	$4, %esp
	movl	16(%ebp), %eax
	fldl	-56(%ebp)
	fxch	%st(1)
	cmpl	12(%ebp), %edi
	fstl	-32(%ebp)
	fstpl	8(%ebx)
	fstl	-40(%ebp)
	fldl	-32(%ebp)
	fxch	%st(1)
	fstpl	(%ebx)
	movl	(%eax), %ebx
	faddl	-40(%ebp)
	fstpl	8(%ebx,%esi)
	jle	L152
L161:
	cmpl	$1, __ZN14FreeSqrtinvMem6activeE
	jne	L121
	movl	_sqrtinvMem+16, %edi
	testl	%edi, %edi
	jne	L121
	movl	_sqrtinvMem, %eax
	movl	$1, %esi
	movl	%esi, _sqrtinvMem+16
	testl	%eax, %eax
	jne	L162
L155:
	movl	16(%ebp), %eax
	movl	(%eax), %ebx
	movl	_sqrtinvMem+4, %eax
	movl	%ebx, _sqrtinvMem
	testl	%eax, %eax
	jne	L163
L157:
	movl	20(%ebp), %edi
	movl	_sqrtinvMem+8, %eax
	movl	(%edi), %ecx
	testl	%eax, %eax
	movl	%ecx, _sqrtinvMem+4
	jne	L164
L159:
	movl	24(%ebp), %ebx
	movl	12(%ebp), %edx
	movl	(%ebx), %esi
	movl	%edx, _sqrtinvMem+12
	movl	%esi, _sqrtinvMem+8
	jmp	L121
L164:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L159
L163:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L157
L162:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L155
	.align 2
	.align 16
.globl __ZN14FreeSqrtinvMem4freeERP6lnSqrtRP12doubledoubleS5_
	.def	__ZN14FreeSqrtinvMem4freeERP6lnSqrtRP12doubledoubleS5_;	.scl	2;	.type	32;	.endef
__ZN14FreeSqrtinvMem4freeERP6lnSqrtRP12doubledoubleS5_:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$24, %esp
	movl	%ebx, -12(%ebp)
	movl	_sqrtinvMem+16, %eax
	movl	12(%ebp), %ebx
	movl	%esi, -8(%ebp)
	testl	%eax, %eax
	movl	16(%ebp), %esi
	movl	%edi, -4(%ebp)
	movl	20(%ebp), %edi
	jle	L174
	movl	(%ebx), %edx
	cmpl	%edx, _sqrtinvMem
	je	L175
L166:
	testl	%edx, %edx
	jne	L176
L169:
	movl	$0, (%ebx)
	movl	(%esi), %eax
	testl	%eax, %eax
	jne	L177
L171:
	movl	$0, (%esi)
	movl	(%edi), %eax
	testl	%eax, %eax
	jne	L178
L173:
	movl	$0, (%edi)
L165:
	movl	-12(%ebp), %ebx
	movl	-8(%ebp), %esi
	movl	-4(%ebp), %edi
	movl	%ebp, %esp
	popl	%ebp
	ret
	.align 16
L178:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L173
	.align 16
L177:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L171
	.align 16
L176:
	movl	%edx, (%esp)
	call	__ZdaPv
	jmp	L169
	.align 16
L175:
	decl	%eax
	movl	%eax, _sqrtinvMem+16
	jmp	L165
	.align 16
L174:
	movl	(%ebx), %edx
	jmp	L166
	.align 8
LC12:
	.long	1413754136
	.long	1075388923
	.align 8
LC13:
	.long	1841940611
	.long	1069834032
	.align 2
	.align 16
.globl __ZN8EvalZeta17setFastSumZMethodEi
	.def	__ZN8EvalZeta17setFastSumZMethodEi;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta17setFastSumZMethodEi:
	pushl	%ebp
	movl	$524288, %ecx
	movl	%esp, %ebp
	pushl	%ebx
	subl	$4, %esp
	movl	$524288, %ebx
	movl	8(%ebp), %eax
	movl	_NPREP, %edx
	movl	%ecx, _NPREP
	cmpl	$1, %eax
	je	L190
	cmpl	$2, %eax
	je	L191
	cmpl	$3, %eax
	je	L192
L181:
	pushl	%ebx
	fildl	(%esp)
	fdivrl	LC12
	movl	%eax, __ZN8EvalZeta14fastSumZMethodE
	movl	%ebx, %eax
	shrl	$31, %eax
	leal	(%eax,%ebx), %ecx
	sarl	%ecx
	movl	%ecx, _NPREP2
	leal	(%ebx,%ebx), %ecx
	movl	%ecx, (%esp)
	fstpl	_GRID
	fildl	(%esp)
	addl	$4, %esp
	cmpl	%ebx, %edx
	fmull	LC13
	fstpl	_GRID2_INV
	je	L179
	movl	__ZN8EvalZeta8cosValueE, %eax
	testl	%eax, %eax
	jne	L193
L187:
	leal	2(%ebx,%ebx), %edx
	sall	$3, %edx
	sall	$4, %ebx
	movl	%edx, (%esp)
	call	__Znaj
	movl	%eax, __ZN8EvalZeta8cosValueE
	fldz
	fstpl	8(%eax,%ebx)
L179:
	popl	%eax
	popl	%ebx
	popl	%ebp
	ret
	.align 16
L193:
	movl	%eax, (%esp)
	call	__ZdaPv
	movl	_NPREP, %ebx
	jmp	L187
	.align 16
L192:
	movl	$4194304, %ecx
	movl	$4194304, %ebx
	.align 16
L189:
	movl	%ecx, _NPREP
	jmp	L181
	.align 16
L191:
	movl	$2097152, %ecx
	movl	$2097152, %ebx
	jmp	L189
	.align 16
L190:
	movl	$1048576, %ecx
	movl	$1048576, %ebx
	jmp	L189
	.align 2
	.align 16
.globl __ZN8EvalZetaC2EP6Output
	.def	__ZN8EvalZetaC2EP6Output;	.scl	2;	.type	32;	.endef
__ZN8EvalZetaC2EP6Output:
	pushl	%ebp
	movl	%esp, %ebp
	movl	8(%ebp), %ecx
	incl	__ZN14FreeSqrtinvMem6activeE
	movl	12(%ebp), %eax
	movl	$0, (%ecx)
	movl	$0, 24(%ecx)
	movl	%eax, 4(%ecx)
	movl	$0, 20(%ecx)
	movl	$0, 32(%ecx)
	movl	$0, 28(%ecx)
	movl	$0, 8(%ecx)
	movl	$0, 12(%ecx)
	movl	$0, 16(%ecx)
	popl	%ebp
	movl	%ecx, __ZN8EvalZeta7runningE
	ret
	.align 2
	.align 16
.globl __ZN8EvalZetaC1EP6Output
	.def	__ZN8EvalZetaC1EP6Output;	.scl	2;	.type	32;	.endef
__ZN8EvalZetaC1EP6Output:
	pushl	%ebp
	movl	%esp, %ebp
	movl	8(%ebp), %ecx
	incl	__ZN14FreeSqrtinvMem6activeE
	movl	12(%ebp), %eax
	movl	$0, (%ecx)
	movl	$0, 24(%ecx)
	movl	%eax, 4(%ecx)
	movl	$0, 20(%ecx)
	movl	$0, 32(%ecx)
	movl	$0, 28(%ecx)
	movl	$0, 8(%ecx)
	movl	$0, 12(%ecx)
	movl	$0, 16(%ecx)
	popl	%ebp
	movl	%ecx, __ZN8EvalZeta7runningE
	ret
	.align 8
LC17:
	.long	1413754136
	.long	1075388923
	.align 2
	.align 16
.globl __ZN8EvalZeta4sumZEdidPd
	.def	__ZN8EvalZeta4sumZEdidPd;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta4sumZEdidPd:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	pushl	%esi
	pushl	%ebx
	subl	$172, %esp
	movl	20(%ebp), %esi
	fldl	12(%ebp)
	movl	8(%ebp), %edx
	movl	%esi, %eax
	fldl	24(%ebp)
	movl	%esi, %ecx
	andl	$3, %eax
	movl	8(%edx), %ebx
	sall	$4, %ecx
	cmpl	$2, %eax
	leal	(%ecx,%ebx), %edx
	je	L217
	cmpl	$2, %eax
	jg	L212
	testl	%eax, %eax
	je	L198
	.align 16
L197:
	addl	$32, %ebx
	cmpl	%edx, %ebx
	movl	%ebx, -148(%ebp)
	jae	L219
	movl	_sleepMode, %eax
	testl	%eax, %eax
	jle	L214
	fstpl	16(%esp)
	movl	32(%ebp), %edi
	movl	__ZN8EvalZeta8cosValueE, %esi
	movl	%edx, 12(%esp)
	movl	%edi, 28(%esp)
	movl	%esi, 24(%esp)
	movl	%ebx, 8(%esp)
	fstpl	(%esp)
	call	__Z13fastSumZSleepdPK6lnSqrtS1_dPKdPd
	.align 16
L213:
	movl	32(%ebp), %eax
	fldl	(%eax)
	fadd	%st(0), %st
	fstpl	(%eax)
	fldl	8(%eax)
	fadd	%st(0), %st
	fstpl	8(%eax)
	addl	$172, %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L214:
	fxch	%st(1)
	movl	%edx, -116(%ebp)
	movl	32(%ebp), %ebx
	movl	__ZN8EvalZeta8cosValueE, %edx
	fstpl	-112(%ebp)
	fstpl	-128(%ebp)
	movl	%edx, -132(%ebp)
	movl	-148(%ebp), %edx
	movl	%ebx, -136(%ebp)
/APP
	fldl       -112(%ebp)                
fldl       -128(%ebp)                
fstp       %st(0)           
fstp       %st(0)           
.align     4                 
B12:                         
fldl       _CONST_TWO_PI     
fldl       8(%edx)          
fldl       24(%edx)         
fldl       40(%edx)         
fldl       56(%edx)         
fldl       (%edx)           
fmull      -112(%ebp)                
fxch       %st(4)           
fstpl      -40(%ebp)               
fxch       %st(2)           
fstpl      -32(%ebp)               
fstpl      -48(%ebp)               
fstpl      -56(%ebp)               
fprem                        
fstp       %st(1)           
fsubrl     -128(%ebp)                
fldl       16(%edx)         
fmull      -112(%ebp)                
fstpl      -64(%ebp)               
fabs                         
fmull      _GRID2_INV        
fsubl      _HALF             
fistpl     -104(%ebp)                
movl       -104(%ebp),%ecx          
fldl       _CONST_TWO_PI     
fldl       -64(%ebp)               
fprem                        
fstp       %st(1)           
fsubrl     -128(%ebp)                
fldl       32(%edx)         
fmull      -112(%ebp)                
fstpl      -72(%ebp)               
fabs                         
fmull      _GRID2_INV        
fsubl      _HALF             
fistpl     -104(%ebp)                
movl       -104(%ebp),%esi          
fldl       _CONST_TWO_PI     
fldl       -72(%ebp)               
fprem                        
fstp       %st(1)           
fsubrl     -128(%ebp)                
fldl       48(%edx)         
fmull      -112(%ebp)                
fstpl      -80(%ebp)                
fabs                         
fmull      _GRID2_INV        
fsubl      _HALF             
fistpl     -104(%ebp)                
movl       -104(%ebp),%edi          
fldl       _CONST_TWO_PI     
fldl       -80(%ebp)                
fprem                        
fstp       %st(1)           
fsubrl     -128(%ebp)                
movl       -132(%ebp),%eax          
fldl       (%eax,%ecx,8)   
fldl       8(%eax,%ecx,8)  
fldl       -40(%ebp)               
movl       -136(%ebp),%ecx          
fldl       8(%eax,%esi,8)  
fldl       (%eax,%edi,8)   
fldl       8(%eax,%edi,8)  
fxch       %st(5)           
fxch       %st(4)           
fmul       %st(3),%st      
fxch       %st(4)           
fmulp      %st,%st(3)      
fxch       %st(1)           
fxch       %st(1)           
fstl       -88(%ebp)                
fstpl      -104(%ebp)                
fxch       %st(3)           
fxch       %st(4)           
fabs                         
fmull      _GRID2_INV        
fsubl      _HALF             
fistpl     -104(%ebp)                
fldl       (%eax,%esi,8)   
movl       -104(%ebp),%esi          
fldl       (%eax,%esi,8)   
fldl       8(%eax,%esi,8)  
fxch       %st(2)           
fxch       %st(1)           
fstl       -96(%ebp)                
fstpl      -104(%ebp)                
fxch       %st(1)           
fldl       -32(%ebp)               
fmul       %st,%st(5)      
fmulp      %st,%st(2)      
fxch       %st(4)           
faddp      %st,%st(3)      
faddp      %st,%st(1)      
fldl       -48(%ebp)               
fmul       %st,%st(4)      
fmull      -88(%ebp)                
fxch       %st(4)           
faddp      %st,%st(2)      
faddp      %st,%st(3)      
fldl       -56(%ebp)               
fmul       %st,%st(2)      
fmull      -96(%ebp)                
fxch       %st(2)           
faddp      %st,%st(1)      
faddl      (%ecx)           
fstpl      (%ecx)           
faddp      %st,%st(1)      
faddl      8(%ecx)          
fstpl      8(%ecx)          
addl       $64,%edx         
cmpl       -116(%ebp),%edx          
jb         B12
/NO_APP
	jmp	L213
	.align 16
L219:
	fstp	%st(0)
	fstp	%st(0)
	jmp	L213
	.align 16
L198:
	fldl	LC17
	fld	%st(2)
	movl	32(%ebp), %edi
	fmull	(%edx)
	fld	%st(1)
	fxch	%st(1)
/APP
	fprem
/NO_APP
	fstp	%st(1)
	fnstcw	-138(%ebp)
	fsubr	%st(2), %st
	movzwl	-138(%ebp), %ecx
	fabs
	orw	$3072, %cx
	fmull	_GRID2_INV
	movw	%cx, -140(%ebp)
	movl	__ZN8EvalZeta8cosValueE, %ecx
	fldcw	-140(%ebp)
	fistpl	-144(%ebp)
	fldcw	-138(%ebp)
	movl	-144(%ebp), %esi
	fldl	8(%ecx,%esi,8)
	fmull	8(%edx)
	faddl	(%edi)
	fstpl	(%edi)
	fldl	8(%edx)
	subl	$16, %edx
	fmull	(%ecx,%esi,8)
	faddl	8(%edi)
	fstpl	8(%edi)
L202:
	fld	%st(2)
	fld	%st(1)
	fxch	%st(1)
	fmull	(%edx)
/APP
	fprem
/NO_APP
	fstp	%st(1)
	fnstcw	-138(%ebp)
	fsubr	%st(2), %st
	movzwl	-138(%ebp), %edi
	fabs
	orw	$3072, %di
	fmull	_GRID2_INV
	movw	%di, -140(%ebp)
	movl	32(%ebp), %edi
	fldcw	-140(%ebp)
	fistpl	-144(%ebp)
	fldcw	-138(%ebp)
	movl	-144(%ebp), %esi
	fldl	8(%ecx,%esi,8)
	fmull	8(%edx)
	faddl	(%edi)
	fstpl	(%edi)
	fldl	8(%edx)
	subl	$16, %edx
	fmull	(%ecx,%esi,8)
	faddl	8(%edi)
	fstpl	8(%edi)
L206:
	fld	%st(2)
	fmull	(%edx)
/APP
	fprem
/NO_APP
	fstp	%st(1)
	fnstcw	-138(%ebp)
	fsubr	%st(1), %st
	movzwl	-138(%ebp), %edi
	fabs
	orw	$3072, %di
	fmull	_GRID2_INV
	movw	%di, -140(%ebp)
	movl	32(%ebp), %edi
	fldcw	-140(%ebp)
	fistpl	-144(%ebp)
	fldcw	-138(%ebp)
	movl	-144(%ebp), %esi
	fldl	8(%ecx,%esi,8)
	fmull	8(%edx)
	faddl	(%edi)
	fstpl	(%edi)
	fldl	8(%edx)
	fmull	(%ecx,%esi,8)
	faddl	8(%edi)
	fstpl	8(%edi)
	jmp	L197
	.align 16
L212:
	cmpl	$3, %eax
	jne	L197
	fldl	LC17
	movl	__ZN8EvalZeta8cosValueE, %ecx
	jmp	L202
	.align 16
L217:
	fldl	LC17
	movl	__ZN8EvalZeta8cosValueE, %ecx
	jmp	L206
	.align 8
LC19:
	.long	1413754136
	.long	1075388923
	.align 2
	.align 16
.globl __ZN8EvalZeta5sumDZEdidPd
	.def	__ZN8EvalZeta5sumDZEdidPd;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta5sumDZEdidPd:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	pushl	%esi
	pushl	%ebx
	subl	$108, %esp
	movl	20(%ebp), %esi
	fldl	12(%ebp)
	movl	32(%ebp), %edi
	fldl	24(%ebp)
	fxch	%st(1)
	fstl	-32(%ebp)
	fstpl	(%esp)
	fstpl	-40(%ebp)
	call	__Z4highd
	fstpl	-48(%ebp)
	fldl	-32(%ebp)
	fstpl	(%esp)
	call	__Z3lowd
	cmpl	$1, %esi
	fstpl	-56(%ebp)
	jle	L232
	movl	$32, %ebx
	decl	%esi
	.align 16
L230:
	fldl	-48(%ebp)
	movl	8(%ebp), %eax
	fldl	LC19
	movl	8(%eax), %ecx
	fldl	(%ecx,%ebx)
	fmul	%st, %st(2)
	fmull	-56(%ebp)
	fxch	%st(2)
/APP
	fprem
/NO_APP
	fxch	%st(2)
/APP
	fprem
/NO_APP
	fstp	%st(1)
	fldl	-40(%ebp)
	fxch	%st(1)
	fstpl	-104(%ebp)
	fsubp	%st, %st(1)
	fstpl	(%esp)
	call	_cos
	fldl	-104(%ebp)
	fxch	%st(1)
	fstpl	-64(%ebp)
	fldl	-40(%ebp)
	fsubp	%st, %st(1)
	fstpl	(%esp)
	call	_cos
	fldl	-64(%ebp)
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	fstp	%st(1)
	sahf
	jbe	L227
	fstpl	-104(%ebp)
	movl	8(%ebp), %eax
	movl	8(%eax), %ecx
	fldl	8(%ecx,%ebx)
	fstpl	(%esp)
	call	__Z3lowd
	fmull	-64(%ebp)
	movl	8(%ebp), %eax
	movl	8(%eax), %ecx
	faddl	(%edi)
	fstpl	(%edi)
	fldl	8(%ecx,%ebx)
	fstpl	(%esp)
	call	__Z4highd
	fldl	-104(%ebp)
	fmulp	%st, %st(1)
L233:
	faddl	8(%edi)
	fstpl	8(%edi)
	addl	$16, %ebx
	decl	%esi
	jne	L230
L232:
	fldl	(%edi)
	fadd	%st(0), %st
	fstpl	(%edi)
	fldl	8(%edi)
	fadd	%st(0), %st
	fstpl	8(%edi)
	addl	$108, %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L227:
	fstpl	-104(%ebp)
	movl	8(%ebp), %eax
	movl	8(%eax), %ecx
	fldl	8(%ecx,%ebx)
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-104(%ebp)
	movl	8(%ebp), %eax
	fmulp	%st, %st(1)
	movl	8(%eax), %ecx
	faddl	(%edi)
	fstpl	(%edi)
	fldl	8(%ecx,%ebx)
	fstpl	(%esp)
	call	__Z4highd
	fmull	-64(%ebp)
	jmp	L233
	.align 8
LC21:
	.long	1413754136
	.long	1075388923
	.align 2
	.align 16
.globl __ZN8EvalZeta10sumDZSleepEdidPd
	.def	__ZN8EvalZeta10sumDZSleepEdidPd;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta10sumDZSleepEdidPd:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	pushl	%esi
	pushl	%ebx
	subl	$92, %esp
	movl	8(%ebp), %esi
	fldl	12(%ebp)
	movl	32(%ebp), %edi
	fstpl	-32(%ebp)
	fldl	24(%ebp)
	fstpl	-40(%ebp)
	fldl	-32(%ebp)
	fstpl	(%esp)
	call	__Z4highd
	fstpl	-48(%ebp)
	fldl	-32(%ebp)
	fstpl	(%esp)
	call	__Z3lowd
	movl	$2, -60(%ebp)
	movl	20(%ebp), %edx
	cmpl	%edx, -60(%ebp)
	fstpl	-56(%ebp)
	jg	L247
	movl	$32, %ebx
	.align 16
L245:
	movl	(%esi), %edx
	incl	%edx
	cmpl	_sleepMode, %edx
	movl	%edx, (%esi)
	jge	L249
L239:
	fldl	-48(%ebp)
	movl	8(%esi), %ecx
	fldl	LC21
	fldl	(%ecx,%ebx)
	fmul	%st, %st(2)
	fmull	-56(%ebp)
	fxch	%st(2)
/APP
	fprem
/NO_APP
	fxch	%st(2)
/APP
	fprem
/NO_APP
	fstp	%st(1)
	fldl	-40(%ebp)
	fxch	%st(1)
	fstpl	-88(%ebp)
	fsubp	%st, %st(1)
	fstpl	(%esp)
	call	_cos
	fldl	-88(%ebp)
	fxch	%st(1)
	fstpl	-72(%ebp)
	fldl	-40(%ebp)
	fsubp	%st, %st(1)
	fstpl	(%esp)
	call	_cos
	fldl	-72(%ebp)
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	fstp	%st(1)
	sahf
	jbe	L242
	fstpl	-88(%ebp)
	movl	8(%esi), %ecx
	fldl	8(%ecx,%ebx)
	fstpl	(%esp)
	call	__Z3lowd
	fmull	-72(%ebp)
	movl	8(%esi), %edx
	faddl	(%edi)
	fstpl	(%edi)
	fldl	8(%edx,%ebx)
	fstpl	(%esp)
	call	__Z4highd
	fldl	-88(%ebp)
	fmulp	%st, %st(1)
L248:
	faddl	8(%edi)
	fstpl	8(%edi)
	incl	-60(%ebp)
	addl	$16, %ebx
	movl	20(%ebp), %edx
	cmpl	%edx, -60(%ebp)
	jle	L245
L247:
	fldl	(%edi)
	fadd	%st(0), %st
	fstpl	(%edi)
	fldl	8(%edi)
	fadd	%st(0), %st
	fstpl	8(%edi)
	addl	$92, %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L242:
	fstpl	-88(%ebp)
	movl	8(%esi), %ecx
	fldl	8(%ecx,%ebx)
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-88(%ebp)
	movl	8(%esi), %edx
	fmulp	%st, %st(1)
	faddl	(%edi)
	fstpl	(%edi)
	fldl	8(%edx,%ebx)
	fstpl	(%esp)
	call	__Z4highd
	fmull	-72(%ebp)
	jmp	L248
	.align 16
L249:
	movl	$0, (%esp)
	call	__sleep
	movl	$0, (%esi)
	jmp	L239
	.align 2
	.align 16
.globl __ZN8EvalZeta5sumDDEdiRK12doubledoubleRS0_S3_
	.def	__ZN8EvalZeta5sumDDEdiRK12doubledoubleRS0_S3_;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta5sumDDEdiRK12doubledoubleRS0_S3_:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	movl	$2, %edi
	pushl	%esi
	pushl	%ebx
	subl	$92, %esp
	fldl	12(%ebp)
	cmpl	20(%ebp), %edi
	fstpl	-64(%ebp)
	jg	L260
	leal	-40(%ebp), %esi
	.align 16
L258:
	movl	_sleepMode, %edx
	testl	%edx, %edx
	jle	L255
	movl	8(%ebp), %eax
	movl	(%eax), %ecx
	incl	%ecx
	cmpl	_sleepMode, %ecx
	movl	%ecx, (%eax)
	jge	L261
L255:
	movl	8(%ebp), %eax
	movl	%edi, %ebx
	leal	-64(%ebp), %edx
	sall	$4, %ebx
	incl	%edi
	movl	12(%eax), %ecx
	addl	%ebx, %ecx
	fldl	(%ecx)
	fstpl	-40(%ebp)
	fldl	8(%ecx)
	movl	%edx, 4(%esp)
	movl	%esi, (%esp)
	fstpl	-32(%ebp)
	call	__ZN12doubledoublemLERKd
	movl	%esi, (%esp)
	movl	24(%ebp), %ecx
	movl	%ecx, 4(%esp)
	call	__ZN12doubledoublemIERKS_
	movl	%esi, 4(%esp)
	leal	-56(%ebp), %edx
	movl	%edx, (%esp)
	call	__Z3cosRK12doubledouble
	fldl	-56(%ebp)
	subl	$4, %esp
	movl	8(%ebp), %eax
	fstpl	-40(%ebp)
	movl	16(%eax), %ecx
	fldl	-48(%ebp)
	movl	%esi, (%esp)
	addl	%ecx, %ebx
	movl	%ebx, 4(%esp)
	fstpl	-32(%ebp)
	call	__ZN12doubledoublemLERKS_
	movl	%esi, 4(%esp)
	movl	28(%ebp), %ecx
	movl	%ecx, (%esp)
	call	__ZN12doubledoublepLERKS_
	movl	%esi, 4(%esp)
	movl	32(%ebp), %ebx
	movl	%ebx, (%esp)
	call	__ZN12doubledoublepLERKS_
	cmpl	20(%ebp), %edi
	jle	L258
L260:
	movl	$0, -72(%ebp)
	movl	28(%ebp), %ebx
	leal	-72(%ebp), %edx
	movl	%edx, 4(%esp)
	leal	-80(%ebp), %edi
	movl	$1073741824, -68(%ebp)
	movl	%ebx, (%esp)
	call	__ZN12doubledoublemLERKd
	movl	%edi, 4(%esp)
	movl	32(%ebp), %esi
	movl	$0, -80(%ebp)
	movl	$1073741824, -76(%ebp)
	movl	%esi, (%esp)
	call	__ZN12doubledoublemLERKd
	leal	-12(%ebp), %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L261:
	movl	$0, (%esp)
	call	__sleep
	movl	8(%ebp), %edx
	movl	$0, (%edx)
	jmp	L255
	.align 2
	.align 16
.globl __ZN8EvalZeta5sumDDERK12doubledoubleiS2_RS0_S3_
	.def	__ZN8EvalZeta5sumDDERK12doubledoubleiS2_RS0_S3_;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta5sumDDERK12doubledoubleiS2_RS0_S3_:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	movl	$2, %edi
	pushl	%esi
	pushl	%ebx
	subl	$76, %esp
	cmpl	16(%ebp), %edi
	jg	L272
	leal	-40(%ebp), %esi
	.align 16
L270:
	movl	_sleepMode, %edx
	testl	%edx, %edx
	jle	L267
	movl	8(%ebp), %eax
	movl	(%eax), %ecx
	incl	%ecx
	cmpl	_sleepMode, %ecx
	movl	%ecx, (%eax)
	jge	L273
L267:
	movl	8(%ebp), %eax
	movl	%edi, %ebx
	incl	%edi
	sall	$4, %ebx
	movl	12(%ebp), %edx
	movl	12(%eax), %ecx
	addl	%ebx, %ecx
	fldl	(%ecx)
	fstpl	-40(%ebp)
	fldl	8(%ecx)
	movl	%edx, 4(%esp)
	movl	%esi, (%esp)
	fstpl	-32(%ebp)
	call	__ZN12doubledoublemLERKS_
	movl	%esi, (%esp)
	movl	20(%ebp), %ecx
	movl	%ecx, 4(%esp)
	call	__ZN12doubledoublemIERKS_
	movl	%esi, 4(%esp)
	leal	-56(%ebp), %edx
	movl	%edx, (%esp)
	call	__Z3cosRK12doubledouble
	fldl	-56(%ebp)
	subl	$4, %esp
	movl	8(%ebp), %eax
	fstpl	-40(%ebp)
	movl	16(%eax), %ecx
	fldl	-48(%ebp)
	movl	%esi, (%esp)
	addl	%ecx, %ebx
	movl	%ebx, 4(%esp)
	fstpl	-32(%ebp)
	call	__ZN12doubledoublemLERKS_
	movl	%esi, 4(%esp)
	movl	24(%ebp), %ecx
	movl	%ecx, (%esp)
	call	__ZN12doubledoublepLERKS_
	movl	%esi, 4(%esp)
	movl	28(%ebp), %ebx
	movl	%ebx, (%esp)
	call	__ZN12doubledoublepLERKS_
	cmpl	16(%ebp), %edi
	jle	L270
L272:
	movl	$0, -64(%ebp)
	movl	24(%ebp), %ebx
	leal	-64(%ebp), %edx
	movl	%edx, 4(%esp)
	leal	-72(%ebp), %edi
	movl	$1073741824, -60(%ebp)
	movl	%ebx, (%esp)
	call	__ZN12doubledoublemLERKd
	movl	%edi, 4(%esp)
	movl	28(%ebp), %esi
	movl	$0, -72(%ebp)
	movl	$1073741824, -68(%ebp)
	movl	%esi, (%esp)
	call	__ZN12doubledoublemLERKd
	leal	-12(%ebp), %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L273:
	movl	$0, (%esp)
	call	__sleep
	movl	8(%ebp), %edx
	movl	$0, (%edx)
	jmp	L267
.lcomm _ZZN8EvalZeta5evalZEdRdS0_E16lastLargeValueAt,16
LC30:
	.ascii ".... Large value at \0"
LC31:
	.ascii ": \0"
	.align 4
LC28:
	.long	1128136704
	.align 2
	.align 16
.globl __ZN8EvalZeta5evalZEdRdS0_
	.def	__ZN8EvalZeta5evalZEdRdS0_;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta5evalZEdRdS0_:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	leal	-40(%ebp), %edi
	pushl	%esi
	leal	-56(%ebp), %esi
	pushl	%ebx
	subl	$188, %esp
	fldl	12(%ebp)
	fstpl	-144(%ebp)
	call	_clock
	movl	%eax, -156(%ebp)
	leal	-144(%ebp), %ecx
	leal	-40(%ebp), %edx
	movl	%ecx, 8(%esp)
	movl	%edx, (%esp)
	movl	$__ZN8EvalZeta13TWO_PI_INV_DDE, 4(%esp)
	call	__ZmlRK12doubledoubleRKd
	subl	$4, %esp
	movl	%edi, 4(%esp)
	movl	%esi, (%esp)
	call	__Z4sqrtRK12doubledouble
	fnstcw	-146(%ebp)
	subl	$4, %esp
	fldl	-48(%ebp)
	leal	-88(%ebp), %ecx
	movzwl	-146(%ebp), %ebx
	leal	-72(%ebp), %edx
	faddl	-56(%ebp)
	movl	$0, -80(%ebp)
	orw	$3072, %bx
	movw	%bx, -148(%ebp)
	fldcw	-148(%ebp)
	fistpl	-152(%ebp)
	fldcw	-146(%ebp)
	movl	$0, -76(%ebp)
	movl	-152(%ebp), %ebx
	pushl	%ebx
	fildl	(%esp)
	addl	$4, %esp
	movl	%ecx, 4(%esp)
	movl	%edx, (%esp)
	movl	%ebx, 8(%esp)
	fstpl	-88(%ebp)
	call	__ZmlRK12doubledoublei
	subl	$4, %esp
	leal	-72(%ebp), %eax
	leal	-40(%ebp), %edx
	movl	%eax, 4(%esp)
	movl	%edx, (%esp)
	call	__ZltRK12doubledoubleS1_
	testb	%al, %al
	je	L276
	decl	%ebx
	leal	-104(%ebp), %edi
L278:
	fldl	-144(%ebp)
	movl	$0, -120(%ebp)
	leal	-120(%ebp), %eax
	movl	%eax, 8(%esp)
	movl	$0, -116(%ebp)
	fstpl	(%esp)
	movl	$0, -112(%ebp)
	movl	$0, -108(%ebp)
	call	__Z5thetadR12doubledouble
	fldl	-120(%ebp)
	movl	%edi, (%esp)
	movl	$__ZN8EvalZeta13TWO_PI_INV_DDE, 4(%esp)
	fstpl	-104(%ebp)
	fldl	-112(%ebp)
	fstpl	-96(%ebp)
	call	__ZN12doubledoublemLERKS_
	movl	%edi, 4(%esp)
	leal	-88(%ebp), %ecx
	movl	%ecx, (%esp)
	call	__Z5floorRK12doubledouble
	fldl	-88(%ebp)
	subl	$4, %esp
	movl	%edi, (%esp)
	movl	$__ZN8EvalZeta9TWO_PI_DDE, 4(%esp)
	fstpl	-104(%ebp)
	fldl	-80(%ebp)
	fstpl	-96(%ebp)
	call	__ZN12doubledoublemLERKS_
	movl	%edi, 4(%esp)
	leal	-120(%ebp), %edi
	movl	%edi, (%esp)
	call	__ZN12doubledoublemIERKS_
	fldl	-112(%ebp)
	faddl	-120(%ebp)
	fstpl	(%esp)
	call	_cos
	movl	20(%ebp), %esi
	fstl	(%esi)
	fstpl	(%esp)
	call	__Z3lowd
	movl	20(%ebp), %eax
	fstpl	-136(%ebp)
	fldl	(%eax)
	fstpl	(%esp)
	call	__Z4highd
	movl	%ebx, 12(%esp)
	movl	8(%ebp), %ecx
	leal	-136(%ebp), %edx
	fstpl	-128(%ebp)
	fldl	-112(%ebp)
	faddl	-120(%ebp)
	movl	%edx, 24(%esp)
	movl	%ecx, (%esp)
	fstpl	16(%esp)
	fldl	-144(%ebp)
	fstpl	4(%esp)
	call	__ZN8EvalZeta4sumZEdidPd
	fldl	-136(%ebp)
	movl	20(%ebp), %edi
	leal	-72(%ebp), %eax
	movl	24(%ebp), %esi
	leal	-88(%ebp), %edx
	leal	-56(%ebp), %ecx
	fstpl	(%edi)
	leal	-40(%ebp), %edi
	fldl	-128(%ebp)
	fstpl	(%esi)
	movl	8(%ebp), %esi
	movl	$0, -88(%ebp)
	movl	$0, -84(%ebp)
	movl	$0, -80(%ebp)
	movl	$0, -76(%ebp)
	movl	$0, -72(%ebp)
	movl	$0, -68(%ebp)
	movl	$0, -64(%ebp)
	movl	$0, -60(%ebp)
	movl	%eax, 20(%esp)
	movl	%edx, 16(%esp)
	movl	%ebx, 12(%esp)
	movl	%ecx, 8(%esp)
	movl	%edi, 4(%esp)
	movl	%esi, (%esp)
	call	__ZN8EvalZeta16errorComputationERK12doubledoubleS2_iRS0_S3_
	testb	$1, %bl
	jne	L288
	fldl	-80(%ebp)
	movl	24(%ebp), %ebx
	movl	20(%ebp), %edx
	faddl	-88(%ebp)
	fsubrl	(%ebx)
	fstpl	(%ebx)
	fldl	-64(%ebp)
	faddl	-72(%ebp)
	fsubrl	(%edx)
L299:
	fstpl	(%edx)
	call	_clock
	flds	LC28
	movl	20(%ebp), %esi
	movl	-156(%ebp), %ecx
	movl	8(%ebp), %edi
	fldl	(%esi)
	subl	%ecx, %eax
	addl	%eax, 32(%edi)
	fabs
	incl	24(%edi)
	fucompp
	fnstsw	%ax
	sahf
	jbe	L274
	fldl	-144(%ebp)
	fld1
	fldl	_ZZN8EvalZeta5evalZEdRdS0_E16lastLargeValueAt
	fsub	%st(2), %st
	fabs
	fucompp
	fnstsw	%ax
	sahf
	jbe	L296
	fstp	%st(0)
	cmpb	$0, _coutLog
	jne	L300
L298:
	movl	$LC30, 4(%esp)
	movl	8(%ebp), %ebx
	movl	4(%ebx), %ecx
	movl	568(%ecx), %edx
	movl	%edx, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	fldl	-144(%ebp)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	movl	%eax, (%esp)
	movl	$LC31, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	20(%ebp), %edi
	fldl	(%edi)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	movl	%eax, (%esp)
	movl	$10, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c
	movl	$0, 4(%esp)
	movl	8(%ebp), %eax
	movl	4(%eax), %esi
	movl	%esi, (%esp)
	call	__ZN6Output5logLnEb
	fldl	-144(%ebp)
L296:
	fstpl	_ZZN8EvalZeta5evalZEdRdS0_E16lastLargeValueAt
L274:
	leal	-12(%ebp), %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
L300:
	movl	$LC30, 4(%esp)
	movl	$__ZSt4cout, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	fldl	-144(%ebp)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	movl	%eax, (%esp)
	movl	$LC31, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	20(%ebp), %ebx
	fldl	(%ebx)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	movl	%eax, (%esp)
	movl	$__ZSt4endlIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_, 4(%esp)
	call	__ZNSolsEPFRSoS_E
	jmp	L298
	.align 16
L288:
	fldl	-64(%ebp)
	movl	24(%ebp), %edx
	faddl	-72(%ebp)
	faddl	(%edx)
	fstpl	(%edx)
	movl	20(%ebp), %edx
	fldl	-80(%ebp)
	faddl	-88(%ebp)
	faddl	(%edx)
	jmp	L299
	.align 16
L276:
	movl	$0, -112(%ebp)
	leal	1(%ebx), %esi
	leal	-120(%ebp), %eax
	pushl	%esi
	leal	-104(%ebp), %edi
	fildl	(%esp)
	addl	$4, %esp
	movl	%eax, 4(%esp)
	movl	$0, -108(%ebp)
	movl	%esi, 8(%esp)
	movl	%edi, (%esp)
	fstpl	-120(%ebp)
	call	__ZmlRK12doubledoublei
	subl	$4, %esp
	leal	-40(%ebp), %edx
	movl	%edi, 4(%esp)
	movl	%edx, (%esp)
	call	__ZgeRK12doubledoubleS1_
	testb	%al, %al
	je	L278
	movl	%esi, %ebx
	jmp	L278
LC36:
	.ascii ".... Call sumMZ at \0"
LC37:
	.ascii ", MZ=\0"
	.align 2
	.align 16
.globl __ZN8EvalZeta6evalDZEdRdS0_
	.def	__ZN8EvalZeta6evalDZEdRdS0_;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta6evalDZEdRdS0_:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	pushl	%esi
	leal	-56(%ebp), %esi
	pushl	%ebx
	subl	$188, %esp
	movl	8(%ebp), %edi
	fldl	12(%ebp)
	fstpl	-144(%ebp)
	call	_clock
	movl	%eax, -156(%ebp)
	leal	-144(%ebp), %ecx
	leal	-40(%ebp), %edx
	movl	%ecx, 8(%esp)
	movl	%edx, (%esp)
	movl	$__ZN8EvalZeta13TWO_PI_INV_DDE, 4(%esp)
	call	__ZmlRK12doubledoubleRKd
	subl	$4, %esp
	leal	-40(%ebp), %eax
	movl	%eax, 4(%esp)
	movl	%esi, (%esp)
	call	__Z4sqrtRK12doubledouble
	fnstcw	-146(%ebp)
	subl	$4, %esp
	fldl	-48(%ebp)
	leal	-88(%ebp), %ecx
	movzwl	-146(%ebp), %ebx
	leal	-72(%ebp), %edx
	faddl	-56(%ebp)
	movl	$0, -80(%ebp)
	orw	$3072, %bx
	movw	%bx, -148(%ebp)
	fldcw	-148(%ebp)
	fistpl	-152(%ebp)
	fldcw	-146(%ebp)
	movl	$0, -76(%ebp)
	movl	-152(%ebp), %ebx
	pushl	%ebx
	fildl	(%esp)
	addl	$4, %esp
	movl	%ecx, 4(%esp)
	movl	%edx, (%esp)
	movl	%ebx, 8(%esp)
	fstpl	-88(%ebp)
	call	__ZmlRK12doubledoublei
	subl	$4, %esp
	leal	-72(%ebp), %eax
	leal	-40(%ebp), %edx
	movl	%eax, 4(%esp)
	movl	%edx, (%esp)
	call	__ZltRK12doubledoubleS1_
	testb	%al, %al
	je	L303
	decl	%ebx
L305:
	fldl	-144(%ebp)
	movl	$0, -120(%ebp)
	leal	-120(%ebp), %eax
	movl	$0, -116(%ebp)
	leal	-88(%ebp), %esi
	movl	%eax, 8(%esp)
	fstpl	(%esp)
	movl	$0, -112(%ebp)
	movl	$0, -108(%ebp)
	call	__Z5thetadR12doubledouble
	leal	-120(%ebp), %edx
	leal	-104(%ebp), %ecx
	movl	%edx, 4(%esp)
	movl	%ecx, (%esp)
	call	__Z3cosRK12doubledouble
	fldl	-96(%ebp)
	subl	$4, %esp
	faddl	-104(%ebp)
	fstpl	(%esp)
	call	__Z4highd
	movl	$0, 12(%esp)
	fstpl	4(%esp)
	movl	$0, 16(%esp)
	movl	%esi, (%esp)
	call	__ZN12doubledoubleC1Edd
	fldl	-96(%ebp)
	faddl	-104(%ebp)
	fstpl	(%esp)
	call	__Z3lowd
	cmpb	$0, _coutLog
	fstpl	-104(%ebp)
	movl	$0, -96(%ebp)
	movl	$0, -92(%ebp)
	jne	L320
L312:
	movl	$LC36, 4(%esp)
	movl	4(%edi), %esi
	movl	568(%esi), %edx
	leal	-120(%ebp), %esi
	movl	%edx, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	fldl	-144(%ebp)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	fldl	-144(%ebp)
	movl	%esi, 16(%esp)
	leal	-88(%ebp), %eax
	movl	%ebx, 12(%esp)
	leal	-104(%ebp), %ecx
	movl	%eax, 24(%esp)
	leal	-136(%ebp), %esi
	movl	%ecx, 20(%esp)
	fstpl	4(%esp)
	movl	%edi, (%esp)
	call	__ZN8EvalZeta5sumDDEdiRK12doubledoubleRS0_S3_
	movl	$0, -72(%ebp)
	xorl	%edx, %edx
	xorl	%ecx, %ecx
	movl	%edx, -136(%ebp)
	leal	-56(%ebp), %eax
	leal	-72(%ebp), %edx
	movl	%ecx, -132(%ebp)
	leal	-40(%ebp), %ecx
	movl	$0, -68(%ebp)
	movl	$0, -64(%ebp)
	movl	$0, -60(%ebp)
	movl	$0, -128(%ebp)
	movl	$0, -124(%ebp)
	movl	%esi, 20(%esp)
	movl	%edx, 16(%esp)
	movl	%ebx, 12(%esp)
	movl	%eax, 8(%esp)
	movl	%ecx, 4(%esp)
	movl	%edi, (%esp)
	call	__ZN8EvalZeta16errorComputationERK12doubledoubleS2_iRS0_S3_
	testb	$1, %bl
	jne	L315
	leal	-72(%ebp), %eax
	leal	-88(%ebp), %ecx
	movl	%eax, 4(%esp)
	leal	-104(%ebp), %ebx
	movl	%ecx, (%esp)
	call	__ZN12doubledoublemIERKS_
	movl	%esi, 4(%esp)
	movl	%ebx, (%esp)
	call	__ZN12doubledoublemIERKS_
L316:
	fldl	-96(%ebp)
	movl	20(%ebp), %edx
	movl	24(%ebp), %ecx
	faddl	-104(%ebp)
	cmpb	$0, _coutLog
	fstpl	(%edx)
	fldl	-80(%ebp)
	faddl	-88(%ebp)
	fstpl	(%ecx)
	jne	L321
L319:
	movl	$LC37, 4(%esp)
	leal	-104(%ebp), %ebx
	movl	4(%edi), %ecx
	leal	-88(%ebp), %esi
	movl	568(%ecx), %edx
	movl	%edx, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	%ebx, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%eax, (%esp)
	movl	$44, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c
	movl	%eax, (%esp)
	movl	%esi, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%eax, (%esp)
	movl	$10, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c
	movl	$0, 4(%esp)
	movl	4(%edi), %ecx
	movl	%ecx, (%esp)
	call	__ZN6Output5logLnEb
	call	_clock
	incl	20(%edi)
	movl	-156(%ebp), %edx
	subl	%edx, %eax
	addl	%eax, 28(%edi)
	leal	-12(%ebp), %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L321:
	movl	$LC37, 4(%esp)
	leal	-104(%ebp), %ebx
	leal	-88(%ebp), %esi
	movl	$__ZSt4cout, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	%ebx, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%eax, (%esp)
	movl	$44, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c
	movl	%eax, (%esp)
	movl	%esi, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%eax, (%esp)
	movl	$__ZSt4endlIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_, 4(%esp)
	call	__ZNSolsEPFRSoS_E
	jmp	L319
	.align 16
L315:
	movl	%esi, 4(%esp)
	leal	-88(%ebp), %ebx
	leal	-72(%ebp), %esi
	movl	%ebx, (%esp)
	call	__ZN12doubledoublepLERKS_
	movl	%esi, 4(%esp)
	leal	-104(%ebp), %edx
	movl	%edx, (%esp)
	call	__ZN12doubledoublepLERKS_
	jmp	L316
	.align 16
L320:
	movl	$LC36, 4(%esp)
	movl	$__ZSt4cout, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	fldl	-144(%ebp)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	movl	%eax, (%esp)
	movl	$__ZSt5flushIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_, 4(%esp)
	call	__ZNSolsEPFRSoS_E
	jmp	L312
	.align 16
L303:
	movl	$0, -112(%ebp)
	leal	1(%ebx), %esi
	leal	-120(%ebp), %eax
	pushl	%esi
	leal	-104(%ebp), %ecx
	fildl	(%esp)
	addl	$4, %esp
	movl	%eax, 4(%esp)
	movl	%ecx, (%esp)
	movl	$0, -108(%ebp)
	movl	%esi, 8(%esp)
	fstpl	-120(%ebp)
	call	__ZmlRK12doubledoublei
	subl	$4, %esp
	leal	-104(%ebp), %eax
	leal	-40(%ebp), %edx
	movl	%eax, 4(%esp)
	movl	%edx, (%esp)
	call	__ZgeRK12doubledoubleS1_
	testb	%al, %al
	je	L305
	movl	%esi, %ebx
	jmp	L305
	.align 2
	.align 16
.globl __ZN8EvalZeta6evalDZERK12doubledoubleRdS3_
	.def	__ZN8EvalZeta6evalDZERK12doubledoubleRdS3_;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta6evalDZERK12doubledoubleRdS3_:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	pushl	%esi
	leal	-56(%ebp), %esi
	pushl	%ebx
	subl	$172, %esp
	movl	8(%ebp), %edi
	call	_clock
	movl	%eax, -148(%ebp)
	movl	12(%ebp), %ecx
	leal	-40(%ebp), %eax
	movl	%eax, (%esp)
	movl	%ecx, 8(%esp)
	movl	$__ZN8EvalZeta13TWO_PI_INV_DDE, 4(%esp)
	call	__ZmlRK12doubledoubleS1_
	subl	$4, %esp
	leal	-40(%ebp), %edx
	movl	%edx, 4(%esp)
	movl	%esi, (%esp)
	call	__Z4sqrtRK12doubledouble
	fnstcw	-138(%ebp)
	subl	$4, %esp
	fldl	-48(%ebp)
	leal	-88(%ebp), %ecx
	movzwl	-138(%ebp), %ebx
	leal	-72(%ebp), %eax
	faddl	-56(%ebp)
	movl	$0, -80(%ebp)
	orw	$3072, %bx
	movw	%bx, -140(%ebp)
	fldcw	-140(%ebp)
	fistpl	-144(%ebp)
	fldcw	-138(%ebp)
	movl	$0, -76(%ebp)
	movl	-144(%ebp), %ebx
	pushl	%ebx
	fildl	(%esp)
	addl	$4, %esp
	movl	%ecx, 4(%esp)
	movl	%eax, (%esp)
	movl	%ebx, 8(%esp)
	fstpl	-88(%ebp)
	call	__ZmlRK12doubledoublei
	subl	$4, %esp
	leal	-72(%ebp), %edx
	leal	-40(%ebp), %eax
	movl	%edx, 4(%esp)
	movl	%eax, (%esp)
	call	__ZltRK12doubledoubleS1_
	testb	%al, %al
	je	L324
	decl	%ebx
L326:
	movl	$0, -120(%ebp)
	movl	12(%ebp), %ecx
	leal	-104(%ebp), %edx
	movl	$0, -116(%ebp)
	leal	-120(%ebp), %esi
	movl	$0, -112(%ebp)
	fldl	(%ecx)
	movl	$0, -108(%ebp)
	fstpl	-104(%ebp)
	fldl	8(%ecx)
	movl	%edx, (%esp)
	movl	%esi, 4(%esp)
	leal	-88(%ebp), %esi
	fstpl	-96(%ebp)
	call	__Z5theta12doubledoubleRS_
	leal	-120(%ebp), %eax
	leal	-104(%ebp), %ecx
	movl	%eax, 4(%esp)
	movl	%ecx, (%esp)
	call	__Z3cosRK12doubledouble
	fldl	-96(%ebp)
	subl	$4, %esp
	faddl	-104(%ebp)
	fstpl	(%esp)
	call	__Z4highd
	movl	$0, 12(%esp)
	fstpl	4(%esp)
	movl	$0, 16(%esp)
	movl	%esi, (%esp)
	call	__ZN12doubledoubleC1Edd
	fldl	-96(%ebp)
	faddl	-104(%ebp)
	fstpl	(%esp)
	call	__Z3lowd
	cmpb	$0, _coutLog
	fstpl	-104(%ebp)
	movl	$0, -96(%ebp)
	movl	$0, -92(%ebp)
	jne	L342
L334:
	movl	$LC36, 4(%esp)
	movl	4(%edi), %ecx
	movl	568(%ecx), %edx
	movl	%edx, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	12(%ebp), %esi
	movl	%esi, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%ebx, 8(%esp)
	movl	12(%ebp), %esi
	leal	-88(%ebp), %ecx
	movl	%ecx, 20(%esp)
	leal	-104(%ebp), %eax
	leal	-120(%ebp), %edx
	movl	%eax, 16(%esp)
	movl	%edx, 12(%esp)
	movl	%esi, 4(%esp)
	leal	-136(%ebp), %esi
	movl	%edi, (%esp)
	call	__ZN8EvalZeta5sumDDERK12doubledoubleiS2_RS0_S3_
	movl	$0, -72(%ebp)
	xorl	%ecx, %ecx
	xorl	%edx, %edx
	movl	%ecx, -136(%ebp)
	leal	-56(%ebp), %eax
	leal	-72(%ebp), %ecx
	movl	%edx, -132(%ebp)
	leal	-40(%ebp), %edx
	movl	$0, -68(%ebp)
	movl	$0, -64(%ebp)
	movl	$0, -60(%ebp)
	movl	$0, -128(%ebp)
	movl	$0, -124(%ebp)
	movl	%esi, 20(%esp)
	movl	%ecx, 16(%esp)
	movl	%ebx, 12(%esp)
	movl	%eax, 8(%esp)
	movl	%edx, 4(%esp)
	movl	%edi, (%esp)
	call	__ZN8EvalZeta16errorComputationERK12doubledoubleS2_iRS0_S3_
	testb	$1, %bl
	jne	L337
	leal	-72(%ebp), %eax
	leal	-88(%ebp), %edx
	movl	%eax, 4(%esp)
	leal	-104(%ebp), %ebx
	movl	%edx, (%esp)
	call	__ZN12doubledoublemIERKS_
	movl	%esi, 4(%esp)
	movl	%ebx, (%esp)
	call	__ZN12doubledoublemIERKS_
L338:
	fldl	-96(%ebp)
	movl	16(%ebp), %esi
	movl	20(%ebp), %edx
	faddl	-104(%ebp)
	cmpb	$0, _coutLog
	fstpl	(%esi)
	fldl	-80(%ebp)
	faddl	-88(%ebp)
	fstpl	(%edx)
	jne	L343
L341:
	movl	$LC37, 4(%esp)
	leal	-104(%ebp), %ebx
	movl	4(%edi), %edx
	movl	568(%edx), %esi
	movl	%esi, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	%ebx, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%eax, (%esp)
	movl	$44, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c
	movl	%eax, (%esp)
	leal	-88(%ebp), %ecx
	movl	%ecx, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%eax, (%esp)
	movl	$10, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c
	movl	$0, 4(%esp)
	movl	4(%edi), %edx
	movl	%edx, (%esp)
	call	__ZN6Output5logLnEb
	call	_clock
	incl	20(%edi)
	movl	-148(%ebp), %esi
	subl	%esi, %eax
	addl	%eax, 28(%edi)
	leal	-12(%ebp), %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L343:
	movl	$LC37, 4(%esp)
	leal	-104(%ebp), %ebx
	movl	$__ZSt4cout, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	%ebx, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%eax, (%esp)
	movl	$44, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c
	movl	%eax, (%esp)
	leal	-88(%ebp), %ecx
	movl	%ecx, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%eax, (%esp)
	movl	$__ZSt4endlIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_, 4(%esp)
	call	__ZNSolsEPFRSoS_E
	jmp	L341
	.align 16
L337:
	movl	%esi, 4(%esp)
	leal	-88(%ebp), %ebx
	leal	-104(%ebp), %esi
	movl	%ebx, (%esp)
	call	__ZN12doubledoublepLERKS_
	movl	%esi, (%esp)
	leal	-72(%ebp), %ecx
	movl	%ecx, 4(%esp)
	call	__ZN12doubledoublepLERKS_
	jmp	L338
	.align 16
L342:
	movl	$LC36, 4(%esp)
	movl	$__ZSt4cout, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	12(%ebp), %esi
	movl	%esi, 4(%esp)
	call	__ZlsRSoRK12doubledouble
	movl	%eax, (%esp)
	movl	$__ZSt5flushIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_, 4(%esp)
	call	__ZNSolsEPFRSoS_E
	jmp	L334
	.align 16
L324:
	movl	$0, -112(%ebp)
	leal	1(%ebx), %esi
	leal	-120(%ebp), %edx
	pushl	%esi
	leal	-104(%ebp), %ecx
	fildl	(%esp)
	addl	$4, %esp
	movl	%edx, 4(%esp)
	movl	%ecx, (%esp)
	movl	$0, -108(%ebp)
	movl	%esi, 8(%esp)
	fstpl	-120(%ebp)
	call	__ZmlRK12doubledoublei
	subl	$4, %esp
	leal	-104(%ebp), %edx
	leal	-40(%ebp), %eax
	movl	%edx, 4(%esp)
	movl	%eax, (%esp)
	call	__ZgeRK12doubledoubleS1_
	testb	%al, %al
	je	L326
	movl	%esi, %ebx
	jmp	L326
	.align 2
	.align 16
.globl __ZN8EvalZeta16errorComputationERK12doubledoubleS2_iRS0_S3_
	.def	__ZN8EvalZeta16errorComputationERK12doubledoubleS2_iRS0_S3_;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta16errorComputationERK12doubledoubleS2_iRS0_S3_:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	leal	-40(%ebp), %edi
	pushl	%esi
	xorl	%esi, %esi
	pushl	%ebx
	subl	$252, %esp
	movl	$1073741824, %ebx
	movl	16(%ebp), %edx
	movl	20(%ebp), %eax
	fldl	(%edx)
	fstpl	-40(%ebp)
	fldl	8(%edx)
	movl	%edi, (%esp)
	leal	-40(%ebp), %edi
	movl	%eax, 4(%esp)
	fstpl	-32(%ebp)
	call	__ZN12doubledoublemIEi
	movl	%esi, -208(%ebp)
	leal	-208(%ebp), %ecx
	leal	-40(%ebp), %eax
	movl	%ecx, 4(%esp)
	leal	-40(%ebp), %esi
	movl	%ebx, -204(%ebp)
	leal	-56(%ebp), %ebx
	movl	%eax, (%esp)
	call	__ZN12doubledoublemLERKd
	movl	%edi, (%esp)
	fld1
	leal	-216(%ebp), %edx
	movl	%edx, 4(%esp)
	leal	-72(%ebp), %edi
	fstpl	-216(%ebp)
	call	__ZN12doubledoublemIERKd
	fldl	-40(%ebp)
	fstpl	-72(%ebp)
	fldl	-32(%ebp)
	fstl	(%esp)
	fstpl	-64(%ebp)
	call	__Z3lowd
	fldl	-72(%ebp)
	fxch	%st(1)
	fstl	-64(%ebp)
	fstl	-48(%ebp)
	fstpl	-32(%ebp)
	fstl	-56(%ebp)
	fstpl	-40(%ebp)
	movl	%esi, 4(%esp)
	movl	$37, %esi
	movl	%ebx, (%esp)
	call	__Z3sqrRK12doubledouble
	fldl	-48(%ebp)
	subl	$4, %esp
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-56(%ebp)
	fxch	%st(1)
	movl	$0, 12(%esp)
	leal	-56(%ebp), %ecx
	fstl	-48(%ebp)
	fstpl	-64(%ebp)
	fldl	__ZN8EvalZeta7DC0_LOWE+304
	fxch	%st(1)
	fstpl	-72(%ebp)
	movl	%ecx, (%esp)
	movl	$0, 16(%esp)
	fstpl	4(%esp)
	call	__ZN12doubledoubleC1Edd
	fldl	__ZN8EvalZeta8DC1_HIGHE+304
	movl	$0, 12(%esp)
	leal	-88(%ebp), %edx
	movl	%edx, (%esp)
	movl	$0, 16(%esp)
	fstpl	4(%esp)
	call	__ZN12doubledoubleC1Edd
	fldl	__ZN8EvalZeta7DC2_LOWE+304
	movl	$0, 12(%esp)
	leal	-104(%ebp), %eax
	movl	%eax, (%esp)
	movl	$0, 16(%esp)
	fstpl	4(%esp)
	call	__ZN12doubledoubleC1Edd
	fldl	__ZN8EvalZeta8DC3_HIGHE+304
	movl	$0, 12(%esp)
	leal	-120(%ebp), %edx
	movl	$0, 16(%esp)
	movl	%edx, (%esp)
	fstpl	4(%esp)
	call	__ZN12doubledoubleC1Edd
	.align 16
L356:
	movl	%edi, 4(%esp)
	leal	-56(%ebp), %ebx
	movl	%ebx, (%esp)
	leal	0(,%esi,8), %ebx
	call	__ZN12doubledoublemLERKS_
	leal	__ZN8EvalZeta7DC0_LOWE(%ebx), %ecx
	leal	-56(%ebp), %edx
	movl	%ecx, 4(%esp)
	movl	%edx, (%esp)
	call	__ZN12doubledoublepLERKd
	movl	%edi, 4(%esp)
	leal	-88(%ebp), %eax
	movl	%eax, (%esp)
	call	__ZN12doubledoublemLERKS_
	leal	__ZN8EvalZeta8DC1_HIGHE(%ebx), %ecx
	leal	-88(%ebp), %edx
	movl	%ecx, 4(%esp)
	movl	%edx, (%esp)
	call	__ZN12doubledoublepLERKd
	movl	%edi, 4(%esp)
	leal	-104(%ebp), %eax
	movl	%eax, (%esp)
	call	__ZN12doubledoublemLERKS_
	leal	__ZN8EvalZeta7DC2_LOWE(%ebx), %ecx
	addl	$__ZN8EvalZeta8DC3_HIGHE, %ebx
	leal	-104(%ebp), %edx
	movl	%ecx, 4(%esp)
	movl	%edx, (%esp)
	call	__ZN12doubledoublepLERKd
	movl	%edi, 4(%esp)
	leal	-120(%ebp), %eax
	movl	%eax, (%esp)
	call	__ZN12doubledoublemLERKS_
	movl	%ebx, 4(%esp)
	leal	-120(%ebp), %ecx
	movl	%ecx, (%esp)
	call	__ZN12doubledoublepLERKd
	decl	%esi
	jns	L356
	movl	16(%ebp), %eax
	leal	-152(%ebp), %esi
	movl	%esi, (%esp)
	movl	%eax, 4(%esp)
	call	__Z5recipRK12doubledouble
	fldl	-144(%ebp)
	subl	$4, %esp
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-152(%ebp)
	leal	-40(%ebp), %ecx
	leal	-136(%ebp), %edx
	movl	%ecx, (%esp)
	movl	%edx, 4(%esp)
	fstpl	-136(%ebp)
	fstl	-144(%ebp)
	fstpl	-128(%ebp)
	call	__ZN12doubledoublemLERKS_
	movl	12(%ebp), %ebx
	fldl	(%ebx)
	fstpl	-168(%ebp)
	fldl	8(%ebx)
	leal	-40(%ebp), %ebx
	fstl	(%esp)
	fstpl	-160(%ebp)
	call	__Z4highd
	fldl	-168(%ebp)
	fxch	%st(1)
	leal	-88(%ebp), %edx
	leal	-40(%ebp), %eax
	fstl	-160(%ebp)
	fxch	%st(1)
	fstpl	-152(%ebp)
	fstpl	-144(%ebp)
	movl	%edx, (%esp)
	movl	%eax, 4(%esp)
	call	__ZN12doubledoublemLERKS_
	movl	%esi, 4(%esp)
	leal	-104(%ebp), %ecx
	movl	%ecx, (%esp)
	call	__ZN12doubledoubledVERKS_
	movl	%ebx, 4(%esp)
	leal	-120(%ebp), %eax
	leal	-104(%ebp), %ebx
	movl	%eax, (%esp)
	call	__ZN12doubledoublemLERKS_
	movl	%esi, 4(%esp)
	leal	-120(%ebp), %edx
	movl	%edx, (%esp)
	call	__ZN12doubledoubledVERKS_
	fldl	-56(%ebp)
	movl	24(%ebp), %esi
	leal	-88(%ebp), %ecx
	fstpl	(%esi)
	fldl	-48(%ebp)
	fstpl	8(%esi)
	movl	%ecx, 4(%esp)
	movl	%esi, (%esp)
	call	__ZN12doubledoublemIERKS_
	movl	%ebx, 4(%esp)
	movl	24(%ebp), %edx
	leal	-184(%ebp), %ebx
	movl	%edx, (%esp)
	call	__ZN12doubledoublepLERKS_
	movl	24(%ebp), %esi
	leal	-120(%ebp), %ecx
	movl	%ecx, 4(%esp)
	movl	%esi, (%esp)
	call	__ZN12doubledoublemIERKS_
	movl	%ebx, (%esp)
	leal	-136(%ebp), %eax
	movl	%eax, 4(%esp)
	call	__Z4sqrtRK12doubledouble
	fldl	-176(%ebp)
	subl	$4, %esp
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-184(%ebp)
	fxch	%st(1)
	movl	24(%ebp), %edx
	leal	-168(%ebp), %ecx
	movl	%ecx, 4(%esp)
	fstl	-176(%ebp)
	fxch	%st(1)
	fstpl	-168(%ebp)
	movl	%edx, (%esp)
	fstpl	-160(%ebp)
	call	__ZN12doubledoublemLERKS_
	movl	16(%ebp), %esi
	leal	-40(%ebp), %eax
	movl	20(%ebp), %ebx
	fldl	(%esi)
	fstpl	-40(%ebp)
	fldl	8(%esi)
	leal	-224(%ebp), %esi
	movl	%ebx, 4(%esp)
	leal	-40(%ebp), %ebx
	fstpl	-32(%ebp)
	movl	%eax, (%esp)
	call	__ZN12doubledoublemIEi
	movl	%esi, 4(%esp)
	movl	$1073741824, %edx
	xorl	%ecx, %ecx
	movl	%edx, -220(%ebp)
	leal	-168(%ebp), %esi
	movl	%ecx, -224(%ebp)
	movl	%ebx, (%esp)
	call	__ZN12doubledoublemLERKd
	fld1
	leal	-232(%ebp), %ecx
	leal	-40(%ebp), %eax
	movl	%ecx, 4(%esp)
	fstpl	-232(%ebp)
	movl	%eax, (%esp)
	call	__ZN12doubledoublemIERKd
	fldl	-40(%ebp)
	fstpl	-168(%ebp)
	fldl	-32(%ebp)
	fstl	(%esp)
	fstpl	-160(%ebp)
	call	__Z4highd
	fldl	-168(%ebp)
	fxch	%st(1)
	movl	%esi, (%esp)
	leal	-40(%ebp), %edx
	movl	$37, %esi
	fstl	-160(%ebp)
	fstl	-176(%ebp)
	fxch	%st(1)
	fstl	-184(%ebp)
	fstpl	-40(%ebp)
	fstpl	-32(%ebp)
	movl	%edx, 4(%esp)
	call	__Z3sqrRK12doubledouble
	fldl	-160(%ebp)
	subl	$4, %esp
	fstpl	(%esp)
	call	__Z4highd
	fldl	-168(%ebp)
	fxch	%st(1)
	fstl	-160(%ebp)
	fstl	-176(%ebp)
	fstpl	-64(%ebp)
	fldl	__ZN8EvalZeta8DC0_HIGHE+304
	fxch	%st(1)
	fstl	-184(%ebp)
	fstpl	-72(%ebp)
	fldz
	fxch	%st(1)
	fstpl	-56(%ebp)
	fldl	__ZN8EvalZeta7DC1_LOWE+304
	fxch	%st(1)
	fstl	-48(%ebp)
	fstl	-80(%ebp)
	fstl	-96(%ebp)
	fxch	%st(1)
	fstpl	-88(%ebp)
	fldl	__ZN8EvalZeta8DC2_HIGHE+304
	fxch	%st(1)
	fstpl	-112(%ebp)
	fstpl	-104(%ebp)
	fldl	__ZN8EvalZeta7DC3_LOWE+304
	fstpl	-120(%ebp)
	.align 16
L381:
	movl	%edi, 4(%esp)
	leal	-56(%ebp), %ebx
	movl	%ebx, (%esp)
	leal	0(,%esi,8), %ebx
	call	__ZN12doubledoublemLERKS_
	leal	__ZN8EvalZeta8DC0_HIGHE(%ebx), %edx
	leal	-56(%ebp), %eax
	movl	%edx, 4(%esp)
	movl	%eax, (%esp)
	call	__ZN12doubledoublepLERKd
	movl	%edi, 4(%esp)
	leal	-88(%ebp), %ecx
	movl	%ecx, (%esp)
	call	__ZN12doubledoublemLERKS_
	leal	__ZN8EvalZeta7DC1_LOWE(%ebx), %edx
	leal	-88(%ebp), %eax
	movl	%edx, 4(%esp)
	movl	%eax, (%esp)
	call	__ZN12doubledoublepLERKd
	movl	%edi, 4(%esp)
	leal	-104(%ebp), %ecx
	movl	%ecx, (%esp)
	call	__ZN12doubledoublemLERKS_
	leal	__ZN8EvalZeta8DC2_HIGHE(%ebx), %edx
	addl	$__ZN8EvalZeta7DC3_LOWE, %ebx
	leal	-104(%ebp), %eax
	movl	%edx, 4(%esp)
	movl	%eax, (%esp)
	call	__ZN12doubledoublepLERKd
	movl	%edi, 4(%esp)
	leal	-120(%ebp), %ecx
	movl	%ecx, (%esp)
	call	__ZN12doubledoublemLERKS_
	movl	%ebx, 4(%esp)
	leal	-120(%ebp), %ebx
	movl	%ebx, (%esp)
	call	__ZN12doubledoublepLERKd
	decl	%esi
	jns	L381
	movl	16(%ebp), %edx
	leal	-168(%ebp), %ebx
	leal	-40(%ebp), %esi
	movl	%ebx, (%esp)
	leal	-184(%ebp), %ebx
	movl	%edx, 4(%esp)
	call	__Z5recipRK12doubledouble
	fldl	-160(%ebp)
	subl	$4, %esp
	fstpl	(%esp)
	call	__Z4highd
	fldl	-168(%ebp)
	movl	%esi, (%esp)
	leal	-136(%ebp), %ecx
	leal	-40(%ebp), %esi
	movl	%ecx, 4(%esp)
	fstl	-184(%ebp)
	fstpl	-136(%ebp)
	fstl	-160(%ebp)
	fstl	-176(%ebp)
	fstpl	-128(%ebp)
	call	__ZN12doubledoublemLERKS_
	movl	12(%ebp), %edi
	fldl	(%edi)
	fstpl	-168(%ebp)
	fldl	8(%edi)
	leal	-120(%ebp), %edi
	fstl	-160(%ebp)
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-168(%ebp)
	fxch	%st(1)
	leal	-88(%ebp), %edx
	leal	-40(%ebp), %eax
	fstl	-160(%ebp)
	fxch	%st(1)
	fstpl	-184(%ebp)
	fstpl	-176(%ebp)
	movl	%edx, (%esp)
	movl	%eax, 4(%esp)
	call	__ZN12doubledoublemLERKS_
	movl	%ebx, 4(%esp)
	leal	-104(%ebp), %ecx
	leal	-120(%ebp), %ebx
	movl	%ecx, (%esp)
	call	__ZN12doubledoubledVERKS_
	movl	%esi, 4(%esp)
	leal	-104(%ebp), %esi
	movl	%edi, (%esp)
	call	__ZN12doubledoublemLERKS_
	movl	%ebx, (%esp)
	leal	-184(%ebp), %eax
	leal	-120(%ebp), %ebx
	movl	%eax, 4(%esp)
	call	__ZN12doubledoubledVERKS_
	fldl	-56(%ebp)
	movl	28(%ebp), %ecx
	leal	-88(%ebp), %edx
	fstpl	(%ecx)
	fldl	-48(%ebp)
	fstpl	8(%ecx)
	movl	%ecx, (%esp)
	movl	%edx, 4(%esp)
	call	__ZN12doubledoublemIERKS_
	movl	%esi, 4(%esp)
	movl	28(%ebp), %edi
	leal	-168(%ebp), %esi
	movl	%edi, (%esp)
	call	__ZN12doubledoublepLERKS_
	movl	%ebx, 4(%esp)
	movl	28(%ebp), %edx
	movl	%edx, (%esp)
	call	__ZN12doubledoublemIERKS_
	leal	-200(%ebp), %ecx
	leal	-136(%ebp), %eax
	movl	%eax, 4(%esp)
	movl	%ecx, (%esp)
	call	__Z4sqrtRK12doubledouble
	fldl	-192(%ebp)
	subl	$4, %esp
	fstpl	(%esp)
	call	__Z4highd
	fldl	-200(%ebp)
	fxch	%st(1)
	movl	%esi, 4(%esp)
	movl	28(%ebp), %edi
	fstl	-192(%ebp)
	fstpl	-160(%ebp)
	movl	%edi, (%esp)
	fstpl	-168(%ebp)
	call	__ZN12doubledoublemLERKS_
	leal	-12(%ebp), %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
LC51:
	.ascii ".... We make a shift at \0"
LC53:
	.ascii "....to\0"
	.align 32
LC54:
	.ascii "Serious difficulties in finding a good dz. We stop!\0"
	.align 4
LC50:
	.long	1056964608
	.align 8
LC52:
	.long	2061584302
	.long	1072672276
	.align 2
	.align 16
.globl __ZN8EvalZeta5evalZEdRd
	.def	__ZN8EvalZeta5evalZEdRd;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta5evalZEdRd:
	pushl	%ebp
	movl	%esp, %ebp
	leal	-24(%ebp), %edx
	pushl	%edi
	pushl	%esi
	pushl	%ebx
	subl	$108, %esp
	movl	20(%ebp), %ebx
	fldl	12(%ebp)
	movl	8(%ebp), %esi
	fstpl	-88(%ebp)
	fldl	(%ebx)
	movl	%edx, 16(%esp)
	leal	-32(%ebp), %edx
	movl	%edx, 12(%esp)
	fstpl	4(%esp)
	movl	%esi, (%esp)
	call	__ZN8EvalZeta5evalZEdRdS0_
	fldl	-32(%ebp)
	fldz
	fucom	%st(1)
	fnstsw	%ax
	sahf
	jbe	L399
	fldl	-24(%ebp)
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	sahf
	ja	L398
	fstp	%st(1)
L399:
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	sahf
	jbe	L453
	fldl	-24(%ebp)
	fucom	%st(2)
	fnstsw	%ax
	sahf
	jbe	L445
	fxch	%st(1)
	fxch	%st(2)
L398:
	fxch	%st(2)
	faddp	%st, %st(1)
	fmuls	LC50
L402:
	fucom	%st(1)
	fnstsw	%ax
	fstp	%st(1)
	sahf
	je	L458
L395:
	addl	$108, %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L458:
	jp	L395
	fstp	%st(0)
	fldl	(%ebx)
	movl	%esi, (%esp)
	leal	-48(%ebp), %ecx
	leal	-40(%ebp), %edi
	movl	%edi, 16(%esp)
	movl	%ecx, 12(%esp)
	fstpl	4(%esp)
	call	__ZN8EvalZeta6evalDZEdRdS0_
	fldl	-48(%ebp)
	fldz
	fucom	%st(1)
	fnstsw	%ax
	sahf
	jbe	L408
	fldl	-40(%ebp)
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	sahf
	ja	L407
	fstp	%st(1)
L408:
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	sahf
	jbe	L454
	fldl	-40(%ebp)
	fucom	%st(2)
	fnstsw	%ax
	sahf
	jbe	L447
	fxch	%st(1)
	fxch	%st(2)
L407:
	fxch	%st(2)
	faddp	%st, %st(1)
	fmuls	LC50
L411:
	fucom	%st(1)
	fnstsw	%ax
	fstp	%st(1)
	sahf
	jne	L395
	jp	L395
	fstp	%st(0)
	movl	$1, %edi
	.align 16
L438:
	cmpb	$0, _coutLog
	jne	L459
L418:
	movl	$LC51, 4(%esp)
	movl	4(%esi), %ecx
	movl	568(%ecx), %edx
	movl	%edx, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	fldl	(%ebx)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	fldl	-88(%ebp)
	leal	-56(%ebp), %ecx
	leal	-64(%ebp), %edx
	fsubrl	(%ebx)
	fmull	LC52
	faddl	-88(%ebp)
	fstl	(%ebx)
	movl	%ecx, 16(%esp)
	movl	%edx, 12(%esp)
	fstpl	4(%esp)
	movl	%esi, (%esp)
	call	__ZN8EvalZeta5evalZEdRdS0_
	fldl	-64(%ebp)
	fldz
	fucom	%st(1)
	fnstsw	%ax
	sahf
	jbe	L422
	fldl	-56(%ebp)
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	sahf
	ja	L448
	fstp	%st(1)
L422:
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	sahf
	jbe	L455
	fldl	-56(%ebp)
	fucom	%st(2)
	fnstsw	%ax
	sahf
	jbe	L450
	fstp	%st(2)
	fxch	%st(1)
L421:
	faddp	%st, %st(1)
	fmuls	LC50
L456:
	fstpl	-96(%ebp)
	cmpb	$0, _coutLog
	jne	L460
L426:
	movl	$LC53, 4(%esp)
	movl	4(%esi), %edx
	movl	568(%edx), %ecx
	movl	%ecx, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	fldl	(%ebx)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	movl	%eax, (%esp)
	movl	$10, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c
	movl	$0, 4(%esp)
	movl	4(%esi), %edx
	movl	%edx, (%esp)
	call	__ZN6Output5logLnEb
	fldl	-96(%ebp)
	fldz
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	fstp	%st(1)
	sahf
	jne	L395
	jp	L395
	fstp	%st(0)
	fldl	(%ebx)
	movl	%esi, (%esp)
	leal	-72(%ebp), %edx
	leal	-80(%ebp), %ecx
	movl	%edx, 16(%esp)
	movl	%ecx, 12(%esp)
	fstpl	4(%esp)
	call	__ZN8EvalZeta6evalDZEdRdS0_
	fldl	-80(%ebp)
	fldz
	fucom	%st(1)
	fnstsw	%ax
	sahf
	jbe	L432
	fldl	-72(%ebp)
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	sahf
	ja	L431
	fstp	%st(1)
L432:
	fxch	%st(1)
	fucom	%st(1)
	fnstsw	%ax
	sahf
	jbe	L457
	fldl	-72(%ebp)
	fucom	%st(2)
	fnstsw	%ax
	sahf
	jbe	L452
	fxch	%st(1)
	fxch	%st(2)
L431:
	fxch	%st(2)
	faddp	%st, %st(1)
	fmuls	LC50
L435:
	fucom	%st(1)
	fnstsw	%ax
	fstp	%st(1)
	sahf
	jne	L395
	jp	L395
	fstp	%st(0)
	incl	%edi
	cmpl	$10, %edi
	jle	L438
	cmpb	$0, _coutLog
	jne	L461
L439:
	movl	$LC54, 4(%esp)
	movl	4(%esi), %ecx
	movl	568(%ecx), %edi
	movl	%edi, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	$10, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c
	movl	$0, 4(%esp)
	movl	4(%esi), %ebx
	movl	%ebx, (%esp)
	call	__ZN6Output5logLnEb
	movl	$1, (%esp)
	call	_exit
L461:
	movl	$LC54, 4(%esp)
	movl	$__ZSt4cout, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	$__ZSt4endlIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_, 4(%esp)
	call	__ZNSolsEPFRSoS_E
	jmp	L439
	.align 16
L452:
	fstp	%st(0)
L457:
	fstp	%st(0)
	fld	%st(0)
	jmp	L435
	.align 16
L460:
	movl	$LC53, 4(%esp)
	movl	$__ZSt4cout, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	fldl	(%ebx)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	movl	%eax, (%esp)
	movl	$__ZSt4endlIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_, 4(%esp)
	call	__ZNSolsEPFRSoS_E
	jmp	L426
	.align 16
L450:
	fstp	%st(0)
L455:
	fstp	%st(0)
	jmp	L456
	.align 16
L448:
	fstp	%st(0)
	jmp	L421
	.align 16
L459:
	movl	$LC51, 4(%esp)
	movl	$__ZSt4cout, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	fldl	(%ebx)
	movl	%eax, (%esp)
	fstpl	4(%esp)
	call	__ZNSolsEd
	movl	%eax, (%esp)
	movl	$__ZSt5flushIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_, 4(%esp)
	call	__ZNSolsEPFRSoS_E
	jmp	L418
L447:
	fstp	%st(0)
L454:
	fstp	%st(0)
	fld	%st(0)
	jmp	L411
	.align 16
L445:
	fstp	%st(0)
L453:
	fstp	%st(0)
	fld	%st(0)
	jmp	L402
LC58:
	.ascii "This run (LASTN=\0"
LC59:
	.ascii ", NRANGE=\0"
LC60:
	.ascii ", MDIMENS=\0"
LC61:
	.ascii ", VERSION=\0"
LC62:
	.ascii "0138\0"
LC63:
	.ascii ") was started at \0"
	.align 32
LC66:
	.ascii "+0.382683432365089771728459984030398866761344562485627041433800635627546033960089692237013785342283547148424288661\0"
	.align 32
LC67:
	.ascii "+0.437240468077520449360296467371331987073041501042363031866378610690404508264026362304924073435669639094324286079\0"
	.align 32
LC68:
	.ascii "+0.1323765754803435233240352673915105554743229955586736726649426880559566435420738501669778293204183675017943241749\0"
	.align 32
LC69:
	.ascii "-0.01360502604767418865498318870909990766070687027421894648326187950903716176818633068962485278356136895679047020906\0"
	.align 32
LC70:
	.ascii "-0.01356762197010358088791567058349920618602959696188054686917829168674934087357724499903115819721107719785647775895\0"
	.align 32
LC71:
	.ascii "-0.001623725323144465282854625294133649725659201718172548305398980308243971457730734393999432889383665122685176399873\0"
	.align 32
LC72:
	.ascii "+0.0002970535373337969078312728339951586690679333334505619667872265698183319369203982805177634566104207720143400458262\0"
	.align 32
LC73:
	.ascii "+0.0000794330087952146958801639026487950144873099152560321495071752950226903173395878801705671344477374930716659999123\0"
	.align 32
LC74:
	.ascii "+4.65561246145045050370634021603476231240414569015306013316932838935348879193481820928464518626366832313236741193e-7\0"
	.align 32
LC75:
	.ascii "-1.432725163095510575408246312062615888246258029228068660731581897805436577213074435556392472287142822769748329508e-6\0"
	.align 32
LC76:
	.ascii "-1.035484711231294607500741567738403498882724615888283179447948653075217984108710847647177577896120460086478764151e-7\0"
	.align 32
LC77:
	.ascii "+1.235792708386173805612576262312530316510117762098112867236451610214808402930282161805187583832632897001667458326e-8\0"
	.align 32
LC78:
	.ascii "+1.788108385795490498566678140706904566454558839254644214800304281524529021049731788926650722192519086137173526827e-9\0"
	.align 32
LC79:
	.ascii "-3.391414389927035906940621897884455615248397316288972845006785754803502806891904570703993463969380667640255995188e-11\0"
	.align 32
LC80:
	.ascii "-1.632663390256590510137405297104810281346405431822126827413147731265198687738077289595025493824604361493389459115e-11\0"
	.align 32
LC81:
	.ascii "-3.78510931854122038285464720018504502639038535531200438309272315224985279176248298240356571256856962726814475742e-13\0"
	.align 32
LC82:
	.ascii "+9.32742325920172484566232063986986360002139698116915631937705187400020105037249065099538843442588188664675147467e-14\0"
	.align 32
LC83:
	.ascii "+5.22184301597813685531389314785302371037675394827206477433177228406371981681645773099043136444005380798369532226e-15\0"
	.align 32
LC84:
	.ascii "-3.350673072744263789515090357947326053042838022398065784111555892808740716677888930078701970602503964936006395104e-16\0"
	.align 32
LC85:
	.ascii "-3.412426522811726494080987104562058778608283456254096935869001796289917525710367617748381397622542683846888672015e-17\0"
	.align 32
LC86:
	.ascii "+5.75120334143239916033950179516459231161537829470064807231431453813486388545094592961884811901724585064764962844e-19\0"
	.align 32
LC87:
	.ascii "+1.489530136321150545475627775734689089370735009741635415160709048603295381414367364949760330404014020213930338814e-19\0"
	.align 32
LC88:
	.ascii "+1.256537271702141685330428176609282176536606717440674080535520482356810764118743819183518519587390237199100704507e-21\0"
	.align 32
LC89:
	.ascii "-4.72129525014342566895398813667305340706330304767749675702994903089879756535834541307555978776385327369051553303e-22\0"
	.align 32
LC90:
	.ascii "-1.32690693630396199927354130926183589457507684264465236751272493873018443686610022790536188919297368265742721815e-23\0"
	.align 32
LC91:
	.ascii "+1.105343999512141834453782254227205003182486780211827086005486828970265089129731688133254857575539823725195683922e-24\0"
	.align 32
LC92:
	.ascii "+5.49964637752746551114010449998398178325210538268839922240622794248949462106984664855058759404246021418007498035e-26\0"
	.align 32
LC93:
	.ascii "-1.823137650231802628064108980945407064129881555145279779343173540693674436633740912616549635547508188700365680468e-27\0"
	.align 32
LC94:
	.ascii "-1.568940373772088014686829823192433140971355533415866343941007167915490728982359363699569882728024654791588147859e-28\0"
	.align 32
LC95:
	.ascii "+1.583963508823801161065976053779295278390321164235705734225919985692892191820446152269080950811170989257202082911e-30\0"
	.align 32
LC96:
	.ascii "+3.4346207254372040220415367076516947462098483922500503564813783179382274162068573263112671201649692317453615905e-31\0"
	.align 32
LC97:
	.ascii "+1.702103350031701775318130755271536920853455319760507766090796235001978260644018065519766552941782676624037679422e-33\0"
	.align 32
LC98:
	.ascii "-5.99511930495781673363972633565278514210564502273708131802609647284822583102487894658236367081899707846320368051e-34\0"
	.align 32
LC99:
	.ascii "-1.04876827540944523668427321724480041261517736197636621170912258890766184956298037903925915158527893326057340676e-35\0"
	.align 32
LC100:
	.ascii "+8.42213517834932107854815160735950929394240415164747688538014234387628955118839912704841909631604183775608950552e-37\0"
	.align 32
LC101:
	.ascii "+2.584703859771955713160011615401679072208538467158450776918252923628843991474698009272117251533209327471142444888e-38\0"
	.align 32
LC102:
	.ascii "-9.34763937488998521367903976236981418745369692048179313134504611480555205177899495504433350362542373205050278136e-40\0"
	.align 32
LC103:
	.ascii "-4.56941922524370129765254861055742309974472681838538268606496265105749673629290338481038140628402537476198769926e-41\0"
	.align 32
LC104:
	.ascii "+7.5455973947653905212085611627422817804352042098369215797696318278534625566582905601529992674332954823406110951e-43\0"
	.align 32
LC105:
	.ascii "+0.026825102628375347029991403955666749659270472430643220749776752805867359134188269214280683052\0"
	.align 32
LC106:
	.ascii "-0.013784773426351853049870452589896162365948225597532513294465798867691807541688152085576031383\0"
	.align 32
LC107:
	.ascii "-0.03849125048223508222873641536318936689609880749450906478327860153836847426887901787229257659\0"
	.align 32
LC108:
	.ascii "-0.00987106629906207647201214704618854069280421459666950839994786868965757411195190486323582121\0"
	.align 32
LC109:
	.ascii "+0.0033107597608584043329090769513006978028020918561175309707694861178448645426345718015500843326\0"
	.align 32
LC110:
	.ascii "+0.0014647808577954150824977965619831119780775457722862078933453303143287255048972099659207209621\0"
	.align 32
LC111:
	.ascii "+0.000013207940624876963675161447494430967824291835406154372613530235724535011475016069716813294177\0"
	.align 32
LC112:
	.ascii "-0.00005922748701847141323223499528189568406802912492160850522823303196158210932726481656944581815\0"
	.align 32
LC113:
	.ascii "-5.980242585373448587710835074515858419335890174202825600668342888976410900581355969992952868e-6\0"
	.align 32
LC114:
	.ascii "+9.641322456169826352672985329851666875707836639273182783168607374528344107916458627247029283e-7\0"
	.align 32
LC115:
	.ascii "+1.8334733722714411760016793657832219080753603339709992766273715660151990962666208683124437739e-7\0"
	.align 32
LC116:
	.ascii "-4.467087562717833599560794227150551934657469384377660055067166840151970252514746820463948005e-9\0"
	.align 32
LC117:
	.ascii "-2.7096350821772743216926283987091937259316030722995844412753447458314425593181090759150020757e-9\0"
	.align 32
LC118:
	.ascii "-7.785288654315851046294823085209610006727820577278800382831566727931867764632910377867374876e-11\0"
	.align 32
LC119:
	.ascii "+2.3437626010893688532484550487104512273133964049734205404375275618829488523490151174368404654e-11\0"
	.align 32
LC120:
	.ascii "+1.5830172789987521642162226426287421196746979819448013053513508455771578055396438744193146485e-12\0"
	.align 32
LC121:
	.ascii "-1.2119941573723791246646344738017572576448530651660888461727279790245362198776996808868093029e-13\0"
	.align 32
LC122:
	.ascii "-1.4583781161108307017582854816989993171964777297933249751938379402944425481261805530630616476e-14\0"
	.align 32
LC123:
	.ascii "+2.8786305258131917504558212800208760753536483952839345119896953142223473009317304761040685377e-16\0"
	.align 32
LC124:
	.ascii "+8.662862902123724122528252887933104042807961436294852178958442068342689422294037865607742406e-17\0"
	.align 32
LC125:
	.ascii "+8.43072272713704127156002253146274997727634288682530999309124260276609727264253556324577217e-19\0"
	.align 32
LC126:
	.ascii "-3.630807223097346200173246181103281136955871882367415423137780078494511015098068311048451748e-19\0"
	.align 32
LC127:
	.ascii "-1.1626698212838296719413888629248324378808802522154009317733711360471269908689980684964899997e-20\0"
	.align 32
LC128:
	.ascii "+1.0975486711527531815901832833980075157643671464693844798841337840675643501161428099083592646e-21\0"
	.align 32
LC129:
	.ascii "+6.157399020468427103881470790974945857407651738028111860714322669192813619603136094113741154e-23\0"
	.align 32
LC130:
	.ascii "-2.2909280067678471513963826309991269307343977096746438696025932017457239554824102031775874946e-24\0"
	.align 32
LC131:
	.ascii "-2.2032811748848795343795982704373537544376537775704078003688163099187931890241159108319322546e-25\0"
	.align 32
LC132:
	.ascii "+2.476025180040278508285274215182918632587580796467112641727687468810950715262244429469791761e-27\0"
	.align 32
LC133:
	.ascii "+5.954277215583657802272682863953454743730467881559988360025759772968743994237177956667902438e-28\0"
	.align 32
LC134:
	.ascii "+3.2612020746795952615337563190661874830891135980274339139845795592240636373843954612255006828e-30\0"
	.align 32
LC135:
	.ascii "-1.2654035591041162243650179791261536112558798012347744231540654284163522268689446590266925924e-30\0"
	.align 32
LC136:
	.ascii "-2.4312846965496981901634636363338047426353348019050697893343812128678861564371131634064246015e-32\0"
	.align 32
LC137:
	.ascii "+2.1383011387546953739564195753197140950296216408034685099183785890974590986138498294625891461e-33\0"
	.align 32
LC138:
	.ascii "+7.167799413941061690328338683692708749333075099712339753173535550674454000240550835751326567e-35\0"
	.align 32
LC139:
	.ascii "-2.8242936072336665615525326594221813904617133033516236406598931104936386534532483771217816036e-36\0"
	.align 32
LC140:
	.ascii "-1.5006074196069282189178370454993947599747135926005292679869580497329724359381439103658938812e-37\0"
	.align 32
LC141:
	.ascii "+2.687318940531486108260118827557915092685193539193918356025160117503267595083118443609927278e-39\0"
	.align 32
LC142:
	.ascii "+2.4904195007933094154169676810430522646696390103991181635576185062669795340798460145976606456e-40\0"
	.align 32
LC143:
	.ascii "-1.1605389825678419639763678032899844219760188058814234948874818004278848389077335785169161417e-42\0"
	.align 32
LC144:
	.ascii "0.005188542830293168493784581519230959565968684337910516563725522452072126841897897\0"
	.align 32
LC145:
	.ascii "0.00030946583880634746033456743609587882366950030794878439289358718588031842799211307\0"
	.align 32
LC146:
	.ascii "-0.011335941078229373382182435255883513410249474890261540938858667942986863079981311\0"
	.align 32
LC147:
	.ascii "0.0022330457419581447720571255275803681570983979981642796951293105725719455469601119\0"
	.align 32
LC148:
	.ascii "0.005196637408862330205116926953068191888515832107618595474609879576976564736555503\0"
	.align 32
LC149:
	.ascii "0.0003439914407620833669465591357991809598418589002147317484417304617440685730817159\0"
	.align 32
LC150:
	.ascii "-0.0005910648427470582821732252303077395276588375610173232690309594523280267574834747\0"
	.align 32
LC151:
	.ascii "-0.00010229972547935857454427867522727787133943747273471360465084634256262134151394488\0"
	.align 32
LC152:
	.ascii "0.000020888392216992755408073296174175415931186305360443691531185576321472947573577868\0"
	.align 32
LC153:
	.ascii "5.927665493096535957891996484982863335742249862441772318721993759532135400499668e-6\0"
	.align 32
LC154:
	.ascii "-1.6423838362436275977690302847783780496161212669336910235253514740438053597308203e-7\0"
	.align 32
LC155:
	.ascii "-1.5161199700940682861734605397187381660081084155970349774087695236188185367222967e-7\0"
	.align 32
LC156:
	.ascii "-5.907803698206667962922790253978962060716281592010072012867608882567102850848194e-9\0"
	.align 32
LC157:
	.ascii "2.0911514859478188977745555189722580395885704419004240100593948634461745082130407e-9\0"
	.align 32
LC158:
	.ascii "1.7815649583292351053799701878847486656009684349096795930345702159393431817224899e-10\0"
	.align 32
LC159:
	.ascii "-1.6164072455353830752855769444473857776802820362311211738301273256347933803317603e-11\0"
	.align 32
LC160:
	.ascii "-2.3806962496667615707210740380135849781560242513320681741897741346182257287784557e-12\0"
	.align 32
LC161:
	.ascii "5.398265295542594918182004148336822987325682997025679714114209585960532400298625e-14\0"
	.align 32
LC162:
	.ascii "1.9750142196969515273308733588451725185221802033544774174325263225894059186101604e-14\0"
	.align 32
LC163:
	.ascii "2.3332868732882634831048153005923547597095168404970213704335126869670513579626541e-16\0"
	.align 32
LC164:
	.ascii "-1.1187517610048080208200483808971615892736704619952328379264891944667482662087555e-16\0"
	.align 32
LC165:
	.ascii "-4.164009488883767188501122836433308161241527720525451717634563860759792248049601e-18\0"
	.align 32
LC166:
	.ascii "4.446081109291883028903043500928743324161025016190532684886900866475208961810096e-19\0"
	.align 32
LC167:
	.ascii "2.8546114783637144545733874269779565433096490605491768968430556415535467088959418e-20\0"
	.align 32
LC168:
	.ascii "-1.19132314300378943049718475052661810410453703351011923973272939551201153470687e-21\0"
	.align 32
LC169:
	.ascii "-1.2981634360736498946709902313291029349864968257004043970653583551747007944948404e-22\0"
	.align 32
LC170:
	.ascii "1.6123763178033262338779658663222193262651519787452057819826151396265479872321981e-24\0"
	.align 32
LC171:
	.ascii "4.382497519887344059655258424644950704152853920980276818591850390318129841119647e-25\0"
	.align 32
LC172:
	.ascii "2.7186389576555759138820356271448872774947633995904411012405376808492919628590506e-27\0"
	.align 32
LC173:
	.ascii "-1.1458896506774580369743945579295750204092666668699824045661401768984212546138787e-27\0"
	.align 32
LC174:
	.ascii "-2.4415318181927522978909188671073094149415439741835244151526434958860319358446682e-29\0"
	.align 32
LC175:
	.ascii "2.3505675086790434606664221960472243599181441248526662245549521169413326741733033e-30\0"
	.align 32
LC176:
	.ascii "8.669258995621298717800714563004282859479548850038258850838724790295883213896554e-32\0"
	.align 32
LC177:
	.ascii "-3.723977985489462680382745552671764711614778698594250205029269305778538368069886e-33\0"
	.align 32
LC178:
	.ascii "-2.1646033266321799468220479128490325581663125661515692147696595451120812363930903e-34\0"
	.align 32
LC179:
	.ascii "4.203457751935555749203270075437513679883585299088583490129455804307114122286297e-36\0"
	.align 32
LC180:
	.ascii "4.244052494804297215797687543359423065727104705085375433170467492601781347640104e-37\0"
	.align 32
LC181:
	.ascii "-2.1231392753906157383843052537658427398583379292318669518502402669687755858823324e-39\0"
	.align 32
LC182:
	.ascii "-6.813496373118564864349052387612632119903467123774393160507783275039315832205749e-40\0"
	.align 32
LC183:
	.ascii "0.0013397160907194569042698357299452281238563539531678386569289925594833800871811798\0"
	.align 32
LC184:
	.ascii "-0.003744215136379393704664161864462396581284315042446001020488345395501319713045551\0"
	.align 32
LC185:
	.ascii "0.001330317891932146812031854722402410509897088246099457996340123183515196888263071\0"
	.align 32
LC186:
	.ascii "0.0022654660765471787114760319905210068874119513448871865405689358390986959711214106\0"
	.align 32
LC187:
	.ascii "-0.0009548499998506730415112255157650113355104637663298519791633424726946992806658956\0"
	.align 32
LC188:
	.ascii "-0.0006010038458963603912075805875795611286932555907537579329769784072390871266471527\0"
	.align 32
LC189:
	.ascii "0.00010128858286776621953344349418087858288813181266544865585870025117392863374779861\0"
	.align 32
LC190:
	.ascii "0.00006865733449299825642457428364865218534328592530073865273869547284481941105060958\0"
	.align 32
LC191:
	.ascii "-5.985366791538598159305933853289474476033254319520777140355079450311296076398332e-7\0"
	.align 32
LC192:
	.ascii "-3.33165985123994712904355366983830793171285955443637487051019057060568872956295e-6\0"
	.align 32
LC193:
	.ascii "-2.1919289102435081057184842192253694457056301094787176328502199928079472310677209e-7\0"
	.align 32
LC194:
	.ascii "7.890884245681494410555248261568885233534195350876097985016870653592807297722941e-8\0"
	.align 32
LC195:
	.ascii "9.41468508129526215165246515670888721434440703062285787060390697195816617233733e-9\0"
	.align 32
LC196:
	.ascii "-9.570116210883480301880722847736899414920424990844021810115468872811044469914093e-10\0"
	.align 32
LC197:
	.ascii "-1.8763137453470662796812970577763318771497261610950370939934010326350481628028716e-10\0"
	.align 32
LC198:
	.ascii "4.437837679323399327464708984967982039427175136450134427877701103829299216728465e-12\0"
	.align 32
LC199:
	.ascii "2.2426738505617353248411068573063743908847573555967383640946296048845725322165884e-12\0"
	.align 32
LC200:
	.ascii "3.627686865735243689408255637923200993091627857438909649329814867831900563004191e-14\0"
	.align 32
LC201:
	.ascii "-1.7639809550821581607831121498067405612829056369595996874090447022130186603011647e-14\0"
	.align 32
LC202:
	.ascii "-7.960765246786777757290345179277877672969068290389354910445419703993693619905826e-16\0"
	.align 32
LC203:
	.ascii "9.419651490589690763914895025694423958555439910507587271684011367592679056089096e-17\0"
	.align 32
LC204:
	.ascii "7.133103854569657824556667924637208733076137253777613086648835921659278195880674e-18\0"
	.align 32
LC205:
	.ascii "-3.289910584554624321179665258492719604389867767706730329578387560456883113849552e-19\0"
	.align 32
LC206:
	.ascii "-4.180730374898459291362924870562363545456098499893508469921618986694281682179048e-20\0"
	.align 32
LC207:
	.ascii "5.550542071646333789782116402662976359558940175392938033703401483591298082182587e-22\0"
	.align 32
LC208:
	.ascii "1.7870441906260123858717636353127488582980187518683339734105072221029271545889173e-22\0"
	.align 32
LC209:
	.ascii "1.331280396465609428629734301456596911495766449193494655875984603950395330646233e-24\0"
	.align 32
LC210:
	.ascii "-5.818610611090987516179216596088520206267870242969322822752311337488525847711228e-25\0"
	.align 32
LC211:
	.ascii "-1.4019036088526555374364967097960412447326079695863802828805235344426680330368205e-26\0"
	.align 32
LC212:
	.ascii "1.4641320211626254148997752501860798089334733220831445783786891177657679796555688e-27\0"
	.align 32
LC213:
	.ascii "6.02332655108914231894545302168540534775476763292822981011798811478407558676743e-29\0"
	.align 32
LC214:
	.ascii "-2.8064472319113607480413277200041961058924502875599284388627977593567952984627978e-30\0"
	.align 32
LC215:
	.ascii "-1.8065060055924548468166679975772190204281290392132976714058746943222867824547636e-31\0"
	.align 32
LC216:
	.ascii "3.779508331934081109538275143960178072816211713033459437067101283961906998805713e-33\0"
	.align 32
LC217:
	.ascii "4.214558052947562754928267311962627941300036513813646793136217341959757887604597e-34\0"
	.align 32
LC218:
	.ascii "-2.2110619283398807703089110108495251373369608027039372389420927090703989012463229e-36\0"
	.align 32
LC219:
	.ascii "-7.977857191491540240197869206644086240230293731794895306614670662176495711819498e-37\0"
	.align 32
LC220:
	.ascii "-5.134879815416697465299965219246691353718718876508043721203193132000758105946362e-39\0"
	.align 32
LC221:
	.ascii "1.2486406302153718700908292104194463596234890838129830954576262777222039185771739e-39\0"
	.align 4
LC57:
	.long	1092616192
	.align 2
	.align 16
.globl __ZN8EvalZeta4initExi
	.def	__ZN8EvalZeta4initExi;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta4initExi:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	pushl	%esi
	leal	-60(%ebp), %esi
	pushl	%ebx
	subl	$140, %esp
	movl	8(%ebp), %edi
	movl	16(%ebp), %ecx
	movl	12(%ebp), %edx
	movl	$0, 24(%edi)
	movl	$0, 20(%edi)
	movl	$0, 32(%edi)
	movl	$0, 28(%edi)
	movl	%ecx, -92(%ebp)
	movl	%esi, (%esp)
	movl	%edx, -96(%ebp)
	call	_time
	movl	%esi, (%esp)
	call	_gmtime
	fldl	_TWO_PI_HIGH
	movl	%eax, -100(%ebp)
	movl	20(%ebp), %edx
	addl	$10, %edx
	movl	%edx, %ebx
	movl	%edx, %esi
	sarl	$31, %esi
	addl	-96(%ebp), %ebx
	adcl	-92(%ebp), %esi
	pushl	%esi
	pushl	%ebx
	fildq	(%esp)
	addl	$8, %esp
	fmul	%st, %st(1)
	fstpl	(%esp)
	fstpl	-112(%ebp)
	call	_log
	fdivrl	-112(%ebp)
	movl	%ebx, (%esp)
	movl	%esi, 4(%esp)
	fstpl	8(%esp)
	call	__Z4Gramxd
	fmull	_TWO_PI_INV_HIGH
	fld	%st(0)
	fsqrt
	fucom	%st(0)
	fnstsw	%ax
	sahf
	jp	L566
	jne	L566
	fstp	%st(1)
L463:
	fnstcw	-82(%ebp)
	leal	-68(%ebp), %edx
	movzwl	-82(%ebp), %esi
	flds	LC57
	movl	$500, -68(%ebp)
	orw	$3072, %si
	faddp	%st, %st(1)
	movw	%si, -84(%ebp)
	fldcw	-84(%ebp)
	fistpl	-88(%ebp)
	fldcw	-82(%ebp)
	movl	-88(%ebp), %ebx
	cmpl	$500, %ebx
	movl	%ebx, -64(%ebp)
	jl	L465
	leal	-64(%ebp), %edx
L465:
	cmpb	$0, _coutLog
	movl	(%edx), %esi
	jne	L569
L466:
	movl	$LC58, 4(%esp)
	movl	4(%edi), %ebx
	movl	568(%ebx), %edx
	movl	%edx, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	-96(%ebp), %ecx
	movl	-92(%ebp), %ebx
	movl	%ecx, 4(%esp)
	movl	%ebx, 8(%esp)
	call	__ZNSolsEx
	movl	%eax, (%esp)
	movl	$LC59, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	(%eax), %ecx
	movl	%eax, %edx
	movl	-12(%ecx), %ebx
	addl	$4, %ebx
	movl	8(%ebx,%eax), %eax
	testb	$64, %al
	jne	L486
	testb	$8, %al
	je	L485
L486:
	movl	%edx, (%esp)
	movl	20(%ebp), %eax
	movl	%eax, 4(%esp)
	call	__ZNSolsEm
L489:
	movl	$LC60, 4(%esp)
	movl	%eax, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	(%eax), %ecx
	movl	%eax, %edx
	movl	-12(%ecx), %ebx
	addl	$4, %ebx
	movl	8(%ebx,%eax), %eax
	testb	$64, %al
	jne	L494
	testb	$8, %al
	je	L493
L494:
	movl	%esi, 4(%esp)
	movl	%edx, (%esp)
	call	__ZNSolsEm
L497:
	movl	$LC61, 4(%esp)
	movl	%eax, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	$LC62, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	$LC63, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, %ebx
	movl	-100(%ebp), %eax
	movl	%eax, (%esp)
	call	_asctime
	movl	%eax, 4(%esp)
	movl	%ebx, (%esp)
	leal	12(%edi), %ebx
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%esi, 4(%esp)
	leal	16(%edi), %ecx
	leal	8(%edi), %edx
	movl	%ecx, 16(%esp)
	movl	%ebx, 12(%esp)
	movl	%edx, 8(%esp)
	movl	$_sqrtinvMem, (%esp)
	call	__ZN14FreeSqrtinvMem3getEiRP6lnSqrtRP12doubledoubleS5_
	movl	_NPREP, %edi
	fldz
	movl	__ZN8EvalZeta8cosValueE, %esi
	sall	$4, %edi
	addl	%esi, %edi
	fldl	8(%edi)
	fucompp
	fnstsw	%ax
	sahf
	je	L570
L462:
	leal	-12(%ebp), %esp
	movl	$1, %eax
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
L570:
	jp	L462
	movl	$__ZN12doubledouble2PiE, 8(%esp)
	leal	-40(%ebp), %edi
	leal	-80(%ebp), %esi
	movl	%esi, 4(%esp)
	movl	$0, -80(%ebp)
	movl	$1073741824, -76(%ebp)
	movl	%edi, (%esp)
	call	__ZmlRKdRK12doubledouble
	fldl	-40(%ebp)
	subl	$4, %esp
	movl	$__ZN8EvalZeta9TWO_PI_DDE, 4(%esp)
	movl	%edi, (%esp)
	fstpl	__ZN8EvalZeta9TWO_PI_DDE
	fldl	-32(%ebp)
	fstpl	__ZN8EvalZeta9TWO_PI_DDE+8
	call	__Z5recipRK12doubledouble
	fldl	-40(%ebp)
	subl	$4, %esp
	movl	$LC66, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E, (%esp)
	fstpl	__ZN8EvalZeta13TWO_PI_INV_DDE
	fldl	-32(%ebp)
	fstpl	__ZN8EvalZeta13TWO_PI_INV_DDE+8
	call	__ZN12doubledoubleaSEPKc
	movl	$LC67, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+16, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC68, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+32, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC69, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+48, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC70, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+64, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC71, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+80, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC72, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+96, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC73, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+112, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC74, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+128, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC75, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+144, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC76, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+160, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC77, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+176, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC78, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+192, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC79, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+208, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC80, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+224, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC81, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+240, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC82, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+256, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC83, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+272, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC84, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+288, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC85, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+304, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC86, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+320, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC87, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+336, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC88, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+352, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC89, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+368, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC90, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+384, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC91, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+400, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC92, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+416, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC93, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+432, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC94, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+448, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC95, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+464, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC96, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+480, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC97, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+496, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC98, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+512, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC99, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+528, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC100, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+544, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC101, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+560, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC102, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+576, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC103, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+592, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC104, 4(%esp)
	movl	$__ZN8EvalZeta3DC0E+608, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC105, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC106, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+16, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC107, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+32, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC108, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+48, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC109, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+64, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC110, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+80, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC111, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+96, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC112, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+112, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC113, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+128, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC114, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+144, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC115, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+160, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC116, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+176, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC117, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+192, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC118, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+208, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC119, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+224, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC120, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+240, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC121, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+256, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC122, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+272, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC123, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+288, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC124, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+304, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC125, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+320, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC126, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+336, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC127, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+352, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC128, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+368, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC129, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+384, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC130, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+400, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC131, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+416, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC132, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+432, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC133, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+448, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC134, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+464, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC135, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+480, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC136, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+496, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC137, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+512, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC138, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+528, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC139, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+544, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC140, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+560, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC141, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+576, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC142, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+592, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC143, 4(%esp)
	movl	$__ZN8EvalZeta3DC1E+608, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC144, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC145, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+16, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC146, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+32, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC147, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+48, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC148, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+64, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC149, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+80, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC150, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+96, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC151, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+112, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC152, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+128, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC153, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+144, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC154, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+160, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC155, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+176, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC156, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+192, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC157, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+208, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC158, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+224, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC159, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+240, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC160, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+256, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC161, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+272, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC162, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+288, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC163, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+304, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC164, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+320, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC165, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+336, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC166, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+352, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC167, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+368, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC168, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+384, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC169, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+400, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC170, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+416, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC171, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+432, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC172, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+448, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC173, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+464, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC174, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+480, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC175, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+496, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC176, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+512, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC177, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+528, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC178, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+544, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC179, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+560, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC180, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+576, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC181, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+592, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC182, 4(%esp)
	movl	$__ZN8EvalZeta3DC2E+608, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC183, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC184, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+16, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC185, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+32, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC186, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+48, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC187, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+64, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC188, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+80, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC189, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+96, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC190, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+112, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC191, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+128, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC192, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+144, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC193, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+160, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC194, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+176, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC195, 4(%esp)
	xorl	%esi, %esi
	xorl	%ebx, %ebx
	movl	$__ZN8EvalZeta3DC3E+192, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC196, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+208, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC197, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+224, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC198, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+240, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC199, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+256, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC200, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+272, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC201, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+288, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC202, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+304, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC203, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+320, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC204, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+336, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC205, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+352, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC206, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+368, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC207, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+384, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC208, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+400, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC209, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+416, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC210, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+432, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC211, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+448, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC212, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+464, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC213, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+480, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC214, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+496, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC215, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+512, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC216, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+528, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC217, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+544, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC218, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+560, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC219, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+576, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC220, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+592, (%esp)
	call	__ZN12doubledoubleaSEPKc
	movl	$LC221, 4(%esp)
	movl	$__ZN8EvalZeta3DC3E+608, (%esp)
	call	__ZN12doubledoubleaSEPKc
	.align 16
L547:
	fldl	__ZN8EvalZeta3DC0E+8(%ebx)
	faddl	__ZN8EvalZeta3DC0E(%ebx)
	fstpl	(%esp)
	call	__Z3lowd
	fstpl	__ZN8EvalZeta7DC0_LOWE(,%esi,8)
	fldl	__ZN8EvalZeta3DC0E+8(%ebx)
	faddl	__ZN8EvalZeta3DC0E(%ebx)
	fstpl	(%esp)
	call	__Z4highd
	fstpl	__ZN8EvalZeta8DC0_HIGHE(,%esi,8)
	fldl	__ZN8EvalZeta3DC1E+8(%ebx)
	faddl	__ZN8EvalZeta3DC1E(%ebx)
	fstpl	(%esp)
	call	__Z3lowd
	fstpl	__ZN8EvalZeta7DC1_LOWE(,%esi,8)
	fldl	__ZN8EvalZeta3DC1E+8(%ebx)
	faddl	__ZN8EvalZeta3DC1E(%ebx)
	fstpl	(%esp)
	call	__Z4highd
	fstpl	__ZN8EvalZeta8DC1_HIGHE(,%esi,8)
	fldl	__ZN8EvalZeta3DC2E+8(%ebx)
	faddl	__ZN8EvalZeta3DC2E(%ebx)
	fstpl	(%esp)
	call	__Z3lowd
	fstpl	__ZN8EvalZeta7DC2_LOWE(,%esi,8)
	fldl	__ZN8EvalZeta3DC2E+8(%ebx)
	faddl	__ZN8EvalZeta3DC2E(%ebx)
	fstpl	(%esp)
	call	__Z4highd
	fstpl	__ZN8EvalZeta8DC2_HIGHE(,%esi,8)
	fldl	__ZN8EvalZeta3DC3E+8(%ebx)
	faddl	__ZN8EvalZeta3DC3E(%ebx)
	fstpl	(%esp)
	call	__Z3lowd
	fstpl	__ZN8EvalZeta7DC3_LOWE(,%esi,8)
	fldl	__ZN8EvalZeta3DC3E+8(%ebx)
	faddl	__ZN8EvalZeta3DC3E(%ebx)
	fstpl	(%esp)
	call	__Z4highd
	fstpl	__ZN8EvalZeta8DC3_HIGHE(,%esi,8)
	incl	%esi
	fldl	__ZN8EvalZeta3DC0E(%ebx)
	fstpl	-56(%ebp)
	fldl	__ZN8EvalZeta3DC0E+8(%ebx)
	fstl	-48(%ebp)
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-56(%ebp)
	fxch	%st(1)
	fstl	__ZN8EvalZeta8DDC0_LOWE+8(%ebx)
	fstpl	-48(%ebp)
	fldl	__ZN8EvalZeta3DC0E(%ebx)
	fxch	%st(1)
	fstpl	__ZN8EvalZeta8DDC0_LOWE(%ebx)
	fstpl	-40(%ebp)
	fldl	__ZN8EvalZeta3DC0E+8(%ebx)
	fstl	-32(%ebp)
	fstpl	(%esp)
	call	__Z4highd
	fldl	-40(%ebp)
	fxch	%st(1)
	fstl	__ZN8EvalZeta9DDC0_HIGHE+8(%ebx)
	fstpl	-48(%ebp)
	fldl	__ZN8EvalZeta3DC1E(%ebx)
	fxch	%st(1)
	fstl	__ZN8EvalZeta9DDC0_HIGHE(%ebx)
	fstpl	-56(%ebp)
	fstpl	-40(%ebp)
	fldl	__ZN8EvalZeta3DC1E+8(%ebx)
	fstl	-32(%ebp)
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-40(%ebp)
	fxch	%st(1)
	fstl	__ZN8EvalZeta8DDC1_LOWE+8(%ebx)
	fstpl	-48(%ebp)
	fldl	__ZN8EvalZeta3DC1E(%ebx)
	fxch	%st(1)
	fstl	__ZN8EvalZeta8DDC1_LOWE(%ebx)
	fstpl	-56(%ebp)
	fstpl	-40(%ebp)
	fldl	__ZN8EvalZeta3DC1E+8(%ebx)
	fstl	-32(%ebp)
	fstpl	(%esp)
	call	__Z4highd
	fldl	-40(%ebp)
	fxch	%st(1)
	fstl	__ZN8EvalZeta9DDC1_HIGHE+8(%ebx)
	fstpl	-48(%ebp)
	fldl	__ZN8EvalZeta3DC2E(%ebx)
	fxch	%st(1)
	fstl	__ZN8EvalZeta9DDC1_HIGHE(%ebx)
	fstpl	-56(%ebp)
	fstpl	-40(%ebp)
	fldl	__ZN8EvalZeta3DC2E+8(%ebx)
	fstl	-32(%ebp)
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-40(%ebp)
	fxch	%st(1)
	fstl	__ZN8EvalZeta8DDC2_LOWE+8(%ebx)
	fstpl	-48(%ebp)
	fldl	__ZN8EvalZeta3DC2E(%ebx)
	fxch	%st(1)
	fstl	__ZN8EvalZeta8DDC2_LOWE(%ebx)
	fstpl	-56(%ebp)
	fstpl	-40(%ebp)
	fldl	__ZN8EvalZeta3DC2E+8(%ebx)
	fstl	-32(%ebp)
	fstpl	(%esp)
	call	__Z4highd
	fldl	-40(%ebp)
	fxch	%st(1)
	fstl	__ZN8EvalZeta9DDC2_HIGHE+8(%ebx)
	fstpl	-48(%ebp)
	fldl	__ZN8EvalZeta3DC3E(%ebx)
	fxch	%st(1)
	fstl	__ZN8EvalZeta9DDC2_HIGHE(%ebx)
	fstpl	-56(%ebp)
	fstpl	-40(%ebp)
	fldl	__ZN8EvalZeta3DC3E+8(%ebx)
	fstl	-32(%ebp)
	fstpl	(%esp)
	call	__Z3lowd
	fldl	-40(%ebp)
	fxch	%st(1)
	fstl	__ZN8EvalZeta8DDC3_LOWE+8(%ebx)
	fstpl	-48(%ebp)
	fldl	__ZN8EvalZeta3DC3E(%ebx)
	fxch	%st(1)
	fstl	__ZN8EvalZeta8DDC3_LOWE(%ebx)
	fstpl	-56(%ebp)
	fstpl	-40(%ebp)
	fldl	__ZN8EvalZeta3DC3E+8(%ebx)
	fstl	-32(%ebp)
	fstpl	(%esp)
	call	__Z4highd
	fldl	-40(%ebp)
	fxch	%st(1)
	fstl	__ZN8EvalZeta9DDC3_HIGHE+8(%ebx)
	fstl	-32(%ebp)
	fstpl	-48(%ebp)
	fstl	__ZN8EvalZeta9DDC3_HIGHE(%ebx)
	addl	$16, %ebx
	cmpl	$38, %esi
	fstpl	-56(%ebp)
	jle	L547
	fldl	_GRID
	movl	$1, %esi
	xorl	%edi, %edi
	fchs
	fstpl	(%esp)
	call	_cos
	cmpl	_NPREP2, %esi
	fstpl	-120(%ebp)
	fld1
	jle	L552
L561:
	movl	_NPREP, %edx
	incl	%edx
	cmpl	%edx, %esi
	jle	L557
L568:
	fstp	%st(0)
	jmp	L462
	.align 16
L557:
	fldl	-120(%ebp)
	fxch	%st(1)
	movl	__ZN8EvalZeta8cosValueE, %ebx
	fstpl	-120(%ebp)
	fstpl	8(%ebx,%edi,8)
	pushl	%esi
	incl	%esi
	fildl	(%esp)
	addl	$4, %esp
	fmull	_GRID
	fstpl	(%esp)
	call	_cos
	fstl	(%ebx,%edi,8)
	movl	_NPREP, %ebx
	addl	$2, %edi
	incl	%ebx
	cmpl	%ebx, %esi
	jle	L557
	jmp	L568
	.align 16
L552:
	fldl	-120(%ebp)
	fxch	%st(1)
	movl	__ZN8EvalZeta8cosValueE, %ebx
	fstpl	-120(%ebp)
	fstpl	(%ebx,%edi,8)
	pushl	%esi
	incl	%esi
	fildl	(%esp)
	addl	$4, %esp
	fmull	_GRID
	fstpl	(%esp)
	call	_cos
	fstl	8(%ebx,%edi,8)
	addl	$2, %edi
	cmpl	_NPREP2, %esi
	jle	L552
	jmp	L561
L493:
	movl	%esi, 4(%esp)
	movl	%edx, (%esp)
	call	__ZNSolsEl
	jmp	L497
L485:
	movl	%edx, (%esp)
	movl	20(%ebp), %ecx
	movl	%ecx, 4(%esp)
	call	__ZNSolsEl
	jmp	L489
L569:
	movl	-100(%ebp), %eax
	movl	%eax, (%esp)
	call	_asctime
	movl	$LC58, 4(%esp)
	movl	%eax, %ebx
	movl	$__ZSt4cout, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	-96(%ebp), %edx
	movl	-92(%ebp), %ecx
	movl	%edx, 4(%esp)
	movl	%ecx, 8(%esp)
	call	__ZNSolsEx
	movl	%eax, (%esp)
	movl	$LC59, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, %edx
	movl	(%eax), %eax
	movl	-12(%eax), %ecx
	addl	$4, %ecx
	movl	8(%ecx,%edx), %eax
	testb	$64, %al
	jne	L470
	testb	$8, %al
	je	L469
L470:
	movl	%edx, (%esp)
	movl	20(%ebp), %eax
	movl	%eax, 4(%esp)
	call	__ZNSolsEm
L473:
	movl	$LC60, 4(%esp)
	movl	%eax, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, %edx
	movl	(%eax), %eax
	movl	-12(%eax), %ecx
	addl	$4, %ecx
	movl	8(%ecx,%edx), %eax
	testb	$64, %al
	jne	L478
	testb	$8, %al
	je	L477
L478:
	movl	%esi, 4(%esp)
	movl	%edx, (%esp)
	call	__ZNSolsEm
L481:
	movl	$LC61, 4(%esp)
	movl	%eax, (%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	$LC62, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	$LC63, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	%ebx, 4(%esp)
	call	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc
	movl	%eax, (%esp)
	movl	$__ZSt5flushIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_, 4(%esp)
	call	__ZNSolsEPFRSoS_E
	jmp	L466
L477:
	movl	%esi, 4(%esp)
	movl	%edx, (%esp)
	call	__ZNSolsEl
	jmp	L481
L469:
	movl	%edx, (%esp)
	movl	20(%ebp), %ecx
	movl	%ecx, 4(%esp)
	call	__ZNSolsEl
	jmp	L473
L566:
	fstp	%st(0)
	fstpl	(%esp)
	call	_sqrt
	jmp	L463
	.align 2
	.align 16
.globl __ZN8EvalZeta7destroyEv
	.def	__ZN8EvalZeta7destroyEv;	.scl	2;	.type	32;	.endef
__ZN8EvalZeta7destroyEv:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$24, %esp
	movl	%ebx, -12(%ebp)
	movl	_sqrtinvMem+16, %edx
	movl	8(%ebp), %eax
	movl	%esi, -8(%ebp)
	testl	%edx, %edx
	leal	8(%eax), %ebx
	movl	%edi, -4(%ebp)
	leal	12(%eax), %esi
	leal	16(%eax), %edi
	jle	L572
	movl	8(%eax), %ecx
	cmpl	%ecx, _sqrtinvMem
	je	L581
L572:
	movl	(%ebx), %eax
	testl	%eax, %eax
	jne	L582
L575:
	movl	$0, (%ebx)
	movl	(%esi), %eax
	testl	%eax, %eax
	jne	L583
L577:
	movl	$0, (%esi)
	movl	(%edi), %eax
	testl	%eax, %eax
	jne	L584
L579:
	movl	$0, (%edi)
L571:
	movl	-12(%ebp), %ebx
	movl	-8(%ebp), %esi
	movl	-4(%ebp), %edi
	movl	%ebp, %esp
	popl	%ebp
	ret
	.align 16
L584:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L579
	.align 16
L583:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L577
	.align 16
L582:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L575
	.align 16
L581:
	leal	-1(%edx), %ecx
	movl	%ecx, _sqrtinvMem+16
	jmp	L571
	.align 2
	.align 16
.globl __ZN8EvalZetaD2Ev
	.def	__ZN8EvalZetaD2Ev;	.scl	2;	.type	32;	.endef
__ZN8EvalZetaD2Ev:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$24, %esp
	movl	%ebx, -12(%ebp)
	movl	_sqrtinvMem+16, %edx
	movl	8(%ebp), %eax
	movl	%esi, -8(%ebp)
	testl	%edx, %edx
	leal	8(%eax), %ebx
	movl	%edi, -4(%ebp)
	leal	12(%eax), %esi
	leal	16(%eax), %edi
	jle	L587
	movl	8(%eax), %ecx
	cmpl	%ecx, _sqrtinvMem
	je	L598
L587:
	movl	(%ebx), %eax
	testl	%eax, %eax
	jne	L599
L590:
	movl	$0, (%ebx)
	movl	(%esi), %eax
	testl	%eax, %eax
	jne	L600
L592:
	movl	$0, (%esi)
	movl	(%edi), %eax
	testl	%eax, %eax
	jne	L601
L594:
	movl	$0, (%edi)
L596:
	decl	__ZN14FreeSqrtinvMem6activeE
	xorl	%eax, %eax
	movl	-12(%ebp), %ebx
	movl	%eax, __ZN8EvalZeta7runningE
	movl	-8(%ebp), %esi
	movl	-4(%ebp), %edi
	movl	%ebp, %esp
	popl	%ebp
	ret
	.align 16
L601:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L594
	.align 16
L600:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L592
	.align 16
L599:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L590
	.align 16
L598:
	leal	-1(%edx), %ecx
	movl	%ecx, _sqrtinvMem+16
	jmp	L596
	.align 2
	.align 16
.globl __ZN8EvalZetaD1Ev
	.def	__ZN8EvalZetaD1Ev;	.scl	2;	.type	32;	.endef
__ZN8EvalZetaD1Ev:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$24, %esp
	movl	%ebx, -12(%ebp)
	movl	_sqrtinvMem+16, %edx
	movl	8(%ebp), %eax
	movl	%esi, -8(%ebp)
	testl	%edx, %edx
	leal	8(%eax), %ebx
	movl	%edi, -4(%ebp)
	leal	12(%eax), %esi
	leal	16(%eax), %edi
	jle	L604
	movl	8(%eax), %ecx
	cmpl	%ecx, _sqrtinvMem
	je	L615
L604:
	movl	(%ebx), %eax
	testl	%eax, %eax
	jne	L616
L607:
	movl	$0, (%ebx)
	movl	(%esi), %eax
	testl	%eax, %eax
	jne	L617
L609:
	movl	$0, (%esi)
	movl	(%edi), %eax
	testl	%eax, %eax
	jne	L618
L611:
	movl	$0, (%edi)
L613:
	decl	__ZN14FreeSqrtinvMem6activeE
	xorl	%eax, %eax
	movl	-12(%ebp), %ebx
	movl	%eax, __ZN8EvalZeta7runningE
	movl	-8(%ebp), %esi
	movl	-4(%ebp), %edi
	movl	%ebp, %esp
	popl	%ebp
	ret
	.align 16
L618:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L611
	.align 16
L617:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L609
	.align 16
L616:
	movl	%eax, (%esp)
	call	__ZdaPv
	jmp	L607
	.align 16
L615:
	leal	-1(%edx), %ecx
	movl	%ecx, _sqrtinvMem+16
	jmp	L613
.globl __ZNSt15basic_streambufIcSt11char_traitsIcEE13_S_pback_sizeE
	.section	.rdata$_ZNSt15basic_streambufIcSt11char_traitsIcEE13_S_pback_sizeE,""
	.linkonce same_size
	.align 4
__ZNSt15basic_streambufIcSt11char_traitsIcEE13_S_pback_sizeE:
	.long	1
	.text
	.align 8
LC226:
	.long	1413754136
	.long	1075388923
	.align 2
	.align 16
	.def	__Z13fastSumZSleepdPK6lnSqrtS1_dPKdPd;	.scl	3;	.type	32;	.endef
__Z13fastSumZSleepdPK6lnSqrtS1_dPKdPd:
	pushl	%ebp
	movl	%esp, %ebp
	pushl	%edi
	pushl	%esi
	pushl	%ebx
	subl	$188, %esp
	movl	16(%ebp), %esi
	fldl	8(%ebp)
	movl	32(%ebp), %edi
	fldl	24(%ebp)
	fxch	%st(1)
	fstpl	-136(%ebp)
	fstpl	-144(%ebp)
	.align 16
L620:
	fldl	8(%esi)
	fldl	_GRID2_INV
	fxch	%st(1)
	fstl	-120(%ebp)
	fldl	24(%esi)
	fstl	-96(%ebp)
	fldl	40(%esi)
	fstl	-72(%ebp)
	fldl	56(%esi)
	fxch	%st(1)
	fstpl	-152(%ebp)
	fldl	-136(%ebp)
	fxch	%st(1)
	fstl	-48(%ebp)
	fxch	%st(1)
	fmull	(%esi)
	fxch	%st(1)
	fstpl	-160(%ebp)
	fldl	LC226
	fxch	%st(1)
/APP
	fprem
/NO_APP
	fsubrl	-144(%ebp)
	fnstcw	-122(%ebp)
	movzwl	-122(%ebp), %ebx
	fabs
	orw	$3072, %bx
	movw	%bx, -124(%ebp)
	fmul	%st(4), %st
	fldcw	-124(%ebp)
	fistpl	-128(%ebp)
	fldcw	-122(%ebp)
	fldl	-136(%ebp)
	movl	-128(%ebp), %ebx
	fmull	16(%esi)
/APP
	fprem
/NO_APP
	fsubrl	-144(%ebp)
	fnstcw	-122(%ebp)
	movzwl	-122(%ebp), %ecx
	fabs
	orw	$3072, %cx
	movw	%cx, -124(%ebp)
	fmul	%st(4), %st
	fldcw	-124(%ebp)
	fistpl	-128(%ebp)
	fldcw	-122(%ebp)
	fldl	-136(%ebp)
	movl	-128(%ebp), %ecx
	fmull	32(%esi)
/APP
	fprem
/NO_APP
	fsubrl	-144(%ebp)
	fnstcw	-122(%ebp)
	movzwl	-122(%ebp), %edx
	fabs
	orw	$3072, %dx
	movw	%dx, -124(%ebp)
	fmul	%st(4), %st
	fldcw	-124(%ebp)
	fistpl	-128(%ebp)
	fldcw	-122(%ebp)
	fldl	-136(%ebp)
	movl	-128(%ebp), %edx
	fmull	48(%esi)
/APP
	fprem
/NO_APP
	fstp	%st(1)
	fsubrl	-144(%ebp)
	fnstcw	-122(%ebp)
	movzwl	-122(%ebp), %eax
	fabs
	orw	$3072, %ax
	movw	%ax, -124(%ebp)
	fmulp	%st, %st(3)
	fxch	%st(2)
	fldcw	-124(%ebp)
	fistpl	-128(%ebp)
	fldcw	-122(%ebp)
	movl	-128(%ebp), %eax
	fldl	(%edi,%ebx,8)
	fstl	-112(%ebp)
	fstpl	-168(%ebp)
	fldl	8(%edi,%ebx,8)
	fstl	-104(%ebp)
	fmul	%st(1), %st
	fldl	(%edi,%ecx,8)
	fstl	-88(%ebp)
	fldl	8(%edi,%ecx,8)
	fxch	%st(1)
	fstpl	-176(%ebp)
	fstl	-80(%ebp)
	fmul	%st(3), %st
	fldl	(%edi,%edx,8)
	fxch	%st(2)
	faddp	%st, %st(1)
	fxch	%st(1)
	fstl	-64(%ebp)
	fldl	8(%edi,%edx,8)
	fstl	-56(%ebp)
	fldl	(%edi,%eax,8)
	fstl	-40(%ebp)
	fldl	8(%edi,%eax,8)
	fstl	-32(%ebp)
	fxch	%st(5)
	movl	36(%ebp), %edx
	fmull	-168(%ebp)
	fxch	%st(6)
	fmull	-176(%ebp)
	fxch	%st(2)
	fmull	-152(%ebp)
	fxch	%st(3)
	fmull	-152(%ebp)
	fxch	%st(6)
	faddp	%st, %st(2)
	fxch	%st(4)
	fmull	-160(%ebp)
	fxch	%st(3)
	faddp	%st, %st(2)
	fadd	%st(4), %st
	fxch	%st(3)
	fmull	-160(%ebp)
	fxch	%st(1)
	faddp	%st, %st(2)
	fxch	%st(3)
	fstpl	-152(%ebp)
	faddl	(%edx)
	fxch	%st(1)
	fadd	%st(2), %st
	fxch	%st(2)
	fstpl	-160(%ebp)
	fxch	%st(1)
	faddl	8(%edx)
	fxch	%st(1)
	fstpl	(%edx)
	fstpl	8(%edx)
	movl	_sleepCounter, %edx
	addl	$4, %edx
	cmpl	_sleepMode, %edx
	movl	%edx, _sleepCounter
	jge	L637
L635:
	addl	$64, %esi
	cmpl	20(%ebp), %esi
	jb	L620
	addl	$188, %esp
	popl	%ebx
	popl	%esi
	popl	%edi
	popl	%ebp
	ret
	.align 16
L637:
	movl	$0, (%esp)
	call	__sleep
	xorl	%edx, %edx
	movl	%edx, _sleepCounter
	jmp	L635
	.align 2
	.align 16
	.def	__GLOBAL__I_sqrtinvMem;	.scl	3;	.type	32;	.endef
__GLOBAL__I_sqrtinvMem:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$8, %esp
	movl	$65535, 4(%esp)
	movl	$1, (%esp)
	call	__Z41__static_initialization_and_destruction_0ii
	movl	%ebp, %esp
	popl	%ebp
	ret
	.section	.ctors,"w"
	.align 4
	.long	__GLOBAL__I_sqrtinvMem
	.text
	.align 2
	.align 16
	.def	__GLOBAL__D_sqrtinvMem;	.scl	3;	.type	32;	.endef
__GLOBAL__D_sqrtinvMem:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$8, %esp
	movl	$65535, 4(%esp)
	movl	$0, (%esp)
	call	__Z41__static_initialization_and_destruction_0ii
	movl	%ebp, %esp
	popl	%ebp
	ret
	.section	.dtors,"w"
	.align 4
	.long	__GLOBAL__D_sqrtinvMem
	.def	__ZNSt8ios_base4InitD1Ev;	.scl	3;	.type	32;	.endef
	.def	__ZNSolsEl;	.scl	3;	.type	32;	.endef
	.def	__ZNSolsEm;	.scl	3;	.type	32;	.endef
	.def	__ZN8EvalZeta7destroyEv;	.scl	3;	.type	32;	.endef
	.def	__ZN14FreeSqrtinvMem4freeERP6lnSqrtRP12doubledoubleS5_;	.scl	3;	.type	32;	.endef
	.def	__ZN12doubledoubleaSEPKc;	.scl	3;	.type	32;	.endef
	.def	__ZmlRKdRK12doubledouble;	.scl	3;	.type	32;	.endef
	.def	__ZN14FreeSqrtinvMem3getEiRP6lnSqrtRP12doubledoubleS5_;	.scl	3;	.type	32;	.endef
	.def	_asctime;	.scl	3;	.type	32;	.endef
	.def	__ZNSolsEx;	.scl	3;	.type	32;	.endef
	.def	_sqrt;	.scl	2;	.type	32;	.endef
	.def	_log;	.scl	2;	.type	32;	.endef
	.def	_gmtime;	.scl	3;	.type	32;	.endef
	.def	_time;	.scl	3;	.type	32;	.endef
	.def	_exit;	.scl	2;	.type	32;	.endef
	.def	__ZN12doubledoubledVERKS_;	.scl	3;	.type	32;	.endef
	.def	__ZN12doubledoublepLERKd;	.scl	3;	.type	32;	.endef
	.def	__Z3sqrRK12doubledouble;	.scl	3;	.type	32;	.endef
	.def	__ZN12doubledoublemIERKd;	.scl	3;	.type	32;	.endef
	.def	__ZN12doubledoublemIEi;	.scl	3;	.type	32;	.endef
	.def	__ZN8EvalZeta5sumDDERK12doubledoubleiS2_RS0_S3_;	.scl	3;	.type	32;	.endef
	.def	__Z5theta12doubledoubleRS_;	.scl	3;	.type	32;	.endef
	.def	__ZmlRK12doubledoubleS1_;	.scl	3;	.type	32;	.endef
	.def	__ZlsRSoRK12doubledouble;	.scl	3;	.type	32;	.endef
	.def	__ZN8EvalZeta5sumDDEdiRK12doubledoubleRS0_S3_;	.scl	3;	.type	32;	.endef
	.def	__ZSt5flushIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_;	.scl	3;	.type	32;	.endef
	.def	__ZN6Output5logLnEb;	.scl	3;	.type	32;	.endef
	.def	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_c;	.scl	3;	.type	32;	.endef
	.def	__ZNSolsEPFRSoS_E;	.scl	3;	.type	32;	.endef
	.def	__ZSt4endlIcSt11char_traitsIcEERSt13basic_ostreamIT_T0_ES6_;	.scl	3;	.type	32;	.endef
	.def	__ZNSolsEd;	.scl	3;	.type	32;	.endef
	.def	__ZStlsISt11char_traitsIcEERSt13basic_ostreamIcT_ES5_PKc;	.scl	3;	.type	32;	.endef
	.def	__ZN8EvalZeta16errorComputationERK12doubledoubleS2_iRS0_S3_;	.scl	3;	.type	32;	.endef
	.def	__ZN8EvalZeta4sumZEdidPd;	.scl	3;	.type	32;	.endef
	.def	__Z5floorRK12doubledouble;	.scl	3;	.type	32;	.endef
	.def	__Z5thetadR12doubledouble;	.scl	3;	.type	32;	.endef
	.def	__ZgeRK12doubledoubleS1_;	.scl	3;	.type	32;	.endef
	.def	__ZltRK12doubledoubleS1_;	.scl	3;	.type	32;	.endef
	.def	__ZmlRK12doubledoublei;	.scl	3;	.type	32;	.endef
	.def	__ZmlRK12doubledoubleRKd;	.scl	3;	.type	32;	.endef
	.def	_clock;	.scl	3;	.type	32;	.endef
	.def	__ZN12doubledoublepLERKS_;	.scl	3;	.type	32;	.endef
	.def	__ZN12doubledoublemLERKS_;	.scl	3;	.type	32;	.endef
	.def	__Z3cosRK12doubledouble;	.scl	3;	.type	32;	.endef
	.def	__ZN12doubledoublemIERKS_;	.scl	3;	.type	32;	.endef
	.def	__ZN12doubledoublemLERKd;	.scl	3;	.type	32;	.endef
	.def	_cos;	.scl	2;	.type	32;	.endef
	.def	__sleep;	.scl	3;	.type	32;	.endef
	.def	__Z4sqrtRK12doubledouble;	.scl	3;	.type	32;	.endef
	.def	__Z5recipRK12doubledouble;	.scl	3;	.type	32;	.endef
	.def	__Z3logRK12doubledouble;	.scl	3;	.type	32;	.endef
	.def	__ZdaPv;	.scl	3;	.type	32;	.endef
	.def	__Znaj;	.scl	3;	.type	32;	.endef
	.def	__ZN8EvalZetaD1Ev;	.scl	3;	.type	32;	.endef
	.def	__ZN8EvalZetaC1EP6Output;	.scl	3;	.type	32;	.endef
	.def	__ZN8EvalZeta6evalDZEdRdS0_;	.scl	3;	.type	32;	.endef
	.def	__ZN8EvalZeta5evalZEdRdS0_;	.scl	3;	.type	32;	.endef
	.def	__Z4Gramxd;	.scl	3;	.type	32;	.endef
	.def	__ZN12doubledoubleC1Edd;	.scl	3;	.type	32;	.endef
	.def	__Z3lowd;	.scl	3;	.type	32;	.endef
	.def	__Z4highd;	.scl	3;	.type	32;	.endef
	.def	__ZNSt8ios_base4InitC1Ev;	.scl	3;	.type	32;	.endef
