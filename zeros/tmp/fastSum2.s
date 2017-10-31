
fastSum2.obj:     file format pe-i386

Disassembly of section .text:
Disassembly of section .data:

00000000 <___link>:
	...
Disassembly of section .rdata:
Disassembly of section .tls$:
Disassembly of section .data1:

00000000 <?HALF@@4NB>:
   0:	00 00                	add    %al,(%eax)
   2:	00 00                	add    %al,(%eax)
   4:	00 00                	add    %al,(%eax)
   6:	e0 3f                	loopne 47 <?PI@@4NB+0x1f>

00000008 <_2il0floatpacket.7>:
   8:	00 00                	add    %al,(%eax)
   a:	00 00                	add    %al,(%eax)
   c:	00 00                	add    %al,(%eax)
   e:	30 41 00             	xor    %al,0x0(%ecx)

00000010 <_2il0floatpacket.5>:
  10:	00 00                	add    %al,(%eax)
  12:	00 00                	add    %al,(%eax)
  14:	00 00                	add    %al,(%eax)
  16:	e0 3f                	loopne 57 <?PI@@4NB+0x2f>

00000018 <_2il0floatpacket.3>:
  18:	18 2d 44 54 fb 21    	sbb    %ch,0x21fb5444
  1e:	19 40 83             	sbb    %eax,0xffffff83(%eax)

00000020 <_2il0floatpacket.1>:
  20:	83 c8 c9             	or     $0xffffffc9,%eax
  23:	6d                   	insl   (%dx),%es:(%edi)
  24:	30 5f d4             	xor    %bl,0xffffffd4(%edi)
  27:	3f                   	aas    

00000028 <?PI@@4NB>:
  28:	18 2d 44 54 fb 21    	sbb    %ch,0x21fb5444
  2e:	09                   	.byte 0x9
  2f:	40                   	inc    %eax
Disassembly of section .text1:
Disassembly of section .CRT$XCU:

00000000 <__init_0>:
   0:	00 00                	add    %al,(%eax)
	...
Disassembly of section .text:

00000000 <?fastFmod@@YANNN@Z>:
   0:	55                   	push   %ebp
   1:	8b ec                	mov    %esp,%ebp
   3:	83 ec 08             	sub    $0x8,%esp
   6:	dd 45 10             	fldl   0x10(%ebp)
   9:	dd 45 08             	fldl   0x8(%ebp)
   c:	d9 f8                	fprem  
   e:	dd 5d f8             	fstpl  0xfffffff8(%ebp)
  11:	dd d9                	fstp   %st(1)
  13:	dd 45 f8             	fldl   0xfffffff8(%ebp)
  16:	8b e5                	mov    %ebp,%esp
  18:	5d                   	pop    %ebp
  19:	c3                   	ret    
  1a:	90                   	nop    
  1b:	90                   	nop    
Disassembly of section .text:

00000000 <?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z>:
   0:	53                   	push   %ebx
   1:	8b dc                	mov    %esp,%ebx
   3:	83 e4 f8             	and    $0xfffffff8,%esp
   6:	55                   	push   %ebp
   7:	55                   	push   %ebp
   8:	8b 6b 04             	mov    0x4(%ebx),%ebp
   b:	89 6c 24 04          	mov    %ebp,0x4(%esp,1)
   f:	8b ec                	mov    %esp,%ebp
  11:	81 ec f0 00 00 00    	sub    $0xf0,%esp
  17:	8b 4b 10             	mov    0x10(%ebx),%ecx
  1a:	8b 53 20             	mov    0x20(%ebx),%edx
  1d:	dd 43 08             	fldl   0x8(%ebx)
  20:	8b 43 24             	mov    0x24(%ebx),%eax
  23:	dd 43 18             	fldl   0x18(%ebx)
  26:	89 75 ec             	mov    %esi,0xffffffec(%ebp)
  29:	89 7d e8             	mov    %edi,0xffffffe8(%ebp)
  2c:	dd d8                	fstp   %st(0)
  2e:	dd d8                	fstp   %st(0)
  30:	dd 41 08             	fldl   0x8(%ecx)
  33:	dd 9d 10 ff ff ff    	fstpl  0xffffff10(%ebp)
  39:	dd 41 18             	fldl   0x18(%ecx)
  3c:	dd 9d 28 ff ff ff    	fstpl  0xffffff28(%ebp)
  42:	dd 41 28             	fldl   0x28(%ecx)
  45:	dd 9d 40 ff ff ff    	fstpl  0xffffff40(%ebp)
  4b:	dd 41 38             	fldl   0x38(%ecx)
  4e:	dd 9d 58 ff ff ff    	fstpl  0xffffff58(%ebp)
  54:	dd 01                	fldl   (%ecx)
  56:	dc 4b 08             	fmull  0x8(%ebx)
  59:	dd 9d 70 ff ff ff    	fstpl  0xffffff70(%ebp)
  5f:	dd 05 00 00 00 00    	fldl   0x0
  65:	dd 9d 78 ff ff ff    	fstpl  0xffffff78(%ebp)
  6b:	dd 85 78 ff ff ff    	fldl   0xffffff78(%ebp)
  71:	dd 85 70 ff ff ff    	fldl   0xffffff70(%ebp)
  77:	d9 f8                	fprem  
  79:	dd 5d 80             	fstpl  0xffffff80(%ebp)
  7c:	dd d9                	fstp   %st(1)
  7e:	dd 45 80             	fldl   0xffffff80(%ebp)
  81:	dc 6b 18             	fsubrl 0x18(%ebx)
  84:	d9 e1                	fabs   
  86:	dc 0d 08 00 00 00    	fmull  0x8
  8c:	dc 25 00 00 00 00    	fsubl  0x0
  92:	db 5d e0             	fistpl 0xffffffe0(%ebp)
  95:	8b 45 e0             	mov    0xffffffe0(%ebp),%eax
  98:	89 45 f4             	mov    %eax,0xfffffff4(%ebp)
  9b:	dd 41 10             	fldl   0x10(%ecx)
  9e:	dc 4b 08             	fmull  0x8(%ebx)
  a1:	dd 5d 88             	fstpl  0xffffff88(%ebp)
  a4:	dd 05 00 00 00 00    	fldl   0x0
  aa:	dd 5d 90             	fstpl  0xffffff90(%ebp)
  ad:	dd 45 90             	fldl   0xffffff90(%ebp)
  b0:	dd 45 88             	fldl   0xffffff88(%ebp)
  b3:	d9 f8                	fprem  
  b5:	dd 5d 98             	fstpl  0xffffff98(%ebp)
  b8:	dd d9                	fstp   %st(1)
  ba:	dd 45 98             	fldl   0xffffff98(%ebp)
  bd:	dc 6b 18             	fsubrl 0x18(%ebx)
  c0:	d9 e1                	fabs   
  c2:	dc 0d 08 00 00 00    	fmull  0x8
  c8:	dc 25 00 00 00 00    	fsubl  0x0
  ce:	db 5d e0             	fistpl 0xffffffe0(%ebp)
  d1:	8b 55 e0             	mov    0xffffffe0(%ebp),%edx
  d4:	dd 41 20             	fldl   0x20(%ecx)
  d7:	dc 4b 08             	fmull  0x8(%ebx)
  da:	dd 5d a0             	fstpl  0xffffffa0(%ebp)
  dd:	dd 05 00 00 00 00    	fldl   0x0
  e3:	dd 5d a8             	fstpl  0xffffffa8(%ebp)
  e6:	dd 45 a8             	fldl   0xffffffa8(%ebp)
  e9:	dd 45 a0             	fldl   0xffffffa0(%ebp)
  ec:	d9 f8                	fprem  
  ee:	dd 5d b0             	fstpl  0xffffffb0(%ebp)
  f1:	dd d9                	fstp   %st(1)
  f3:	dd 45 b0             	fldl   0xffffffb0(%ebp)
  f6:	dc 6b 18             	fsubrl 0x18(%ebx)
  f9:	d9 e1                	fabs   
  fb:	dc 0d 08 00 00 00    	fmull  0x8
 101:	dc 25 00 00 00 00    	fsubl  0x0
 107:	db 5d e0             	fistpl 0xffffffe0(%ebp)
 10a:	8b 45 e0             	mov    0xffffffe0(%ebp),%eax
 10d:	dd 41 30             	fldl   0x30(%ecx)
 110:	dc 4b 08             	fmull  0x8(%ebx)
 113:	dd 5d b8             	fstpl  0xffffffb8(%ebp)
 116:	dd 05 00 00 00 00    	fldl   0x0
 11c:	dd 5d c0             	fstpl  0xffffffc0(%ebp)
 11f:	dd 45 c0             	fldl   0xffffffc0(%ebp)
 122:	dd 45 b8             	fldl   0xffffffb8(%ebp)
 125:	d9 f8                	fprem  
 127:	dd 5d c8             	fstpl  0xffffffc8(%ebp)
 12a:	dd d9                	fstp   %st(1)
 12c:	89 4d f8             	mov    %ecx,0xfffffff8(%ebp)
 12f:	8b 7d f4             	mov    0xfffffff4(%ebp),%edi
 132:	8b 4b 20             	mov    0x20(%ebx),%ecx
 135:	dd 45 c8             	fldl   0xffffffc8(%ebp)
 138:	81 ff 00 00 08 00    	cmp    $0x80000,%edi
 13e:	dc 6b 18             	fsubrl 0x18(%ebx)
 141:	d9 e1                	fabs   
 143:	dc 0d 08 00 00 00    	fmull  0x8
 149:	dc 25 00 00 00 00    	fsubl  0x0
 14f:	db 5d e0             	fistpl 0xffffffe0(%ebp)
 152:	8b 75 e0             	mov    0xffffffe0(%ebp),%esi
 155:	89 75 f0             	mov    %esi,0xfffffff0(%ebp)
 158:	dd 04 f9             	fldl   (%ecx,%edi,8)
 15b:	dd 9d 18 ff ff ff    	fstpl  0xffffff18(%ebp)
 161:	dd 44 f9 10          	fldl   0x10(%ecx,%edi,8)
 165:	dd 9d 20 ff ff ff    	fstpl  0xffffff20(%ebp)
 16b:	dd 04 d1             	fldl   (%ecx,%edx,8)
 16e:	dd 9d 30 ff ff ff    	fstpl  0xffffff30(%ebp)
 174:	dd 44 d1 10          	fldl   0x10(%ecx,%edx,8)
 178:	dd 9d 38 ff ff ff    	fstpl  0xffffff38(%ebp)
 17e:	dd 04 c1             	fldl   (%ecx,%eax,8)
 181:	dd 9d 48 ff ff ff    	fstpl  0xffffff48(%ebp)
 187:	dd 44 c1 10          	fldl   0x10(%ecx,%eax,8)
 18b:	dd 9d 50 ff ff ff    	fstpl  0xffffff50(%ebp)
 191:	dd 04 f1             	fldl   (%ecx,%esi,8)
 194:	dd 9d 60 ff ff ff    	fstpl  0xffffff60(%ebp)
 19a:	dd 44 f1 10          	fldl   0x10(%ecx,%esi,8)
 19e:	dd 9d 68 ff ff ff    	fstpl  0xffffff68(%ebp)
 1a4:	8b 4d f8             	mov    0xfffffff8(%ebp),%ecx
 1a7:	7e 18                	jle    1c1 <?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z+0x1c1>
 1a9:	dd 85 18 ff ff ff    	fldl   0xffffff18(%ebp)
 1af:	dd 85 20 ff ff ff    	fldl   0xffffff20(%ebp)
 1b5:	dd 9d 18 ff ff ff    	fstpl  0xffffff18(%ebp)
 1bb:	dd 9d 20 ff ff ff    	fstpl  0xffffff20(%ebp)
 1c1:	81 fa 00 00 08 00    	cmp    $0x80000,%edx
 1c7:	7e 18                	jle    1e1 <?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z+0x1e1>
 1c9:	dd 85 30 ff ff ff    	fldl   0xffffff30(%ebp)
 1cf:	dd 85 38 ff ff ff    	fldl   0xffffff38(%ebp)
 1d5:	dd 9d 30 ff ff ff    	fstpl  0xffffff30(%ebp)
 1db:	dd 9d 38 ff ff ff    	fstpl  0xffffff38(%ebp)
 1e1:	3d 00 00 08 00       	cmp    $0x80000,%eax
 1e6:	7e 18                	jle    200 <?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z+0x200>
 1e8:	dd 85 48 ff ff ff    	fldl   0xffffff48(%ebp)
 1ee:	dd 85 50 ff ff ff    	fldl   0xffffff50(%ebp)
 1f4:	dd 9d 48 ff ff ff    	fstpl  0xffffff48(%ebp)
 1fa:	dd 9d 50 ff ff ff    	fstpl  0xffffff50(%ebp)
 200:	8b c6                	mov    %esi,%eax
 202:	3d 00 00 08 00       	cmp    $0x80000,%eax
 207:	7e 18                	jle    221 <?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z+0x221>
 209:	dd 85 60 ff ff ff    	fldl   0xffffff60(%ebp)
 20f:	dd 85 68 ff ff ff    	fldl   0xffffff68(%ebp)
 215:	dd 9d 60 ff ff ff    	fstpl  0xffffff60(%ebp)
 21b:	dd 9d 68 ff ff ff    	fstpl  0xffffff68(%ebp)
 221:	dd 85 10 ff ff ff    	fldl   0xffffff10(%ebp)
 227:	8b 43 24             	mov    0x24(%ebx),%eax
 22a:	83 c1 40             	add    $0x40,%ecx
 22d:	dd 85 28 ff ff ff    	fldl   0xffffff28(%ebp)
 233:	3b 4b 14             	cmp    0x14(%ebx),%ecx
 236:	dd 85 40 ff ff ff    	fldl   0xffffff40(%ebp)
 23c:	dd 85 58 ff ff ff    	fldl   0xffffff58(%ebp)
 242:	dd 85 20 ff ff ff    	fldl   0xffffff20(%ebp)
 248:	d8 cc                	fmul   %st(4),%st
 24a:	dd 85 38 ff ff ff    	fldl   0xffffff38(%ebp)
 250:	d8 cc                	fmul   %st(4),%st
 252:	de c1                	faddp  %st,%st(1)
 254:	dd 85 50 ff ff ff    	fldl   0xffffff50(%ebp)
 25a:	d8 cb                	fmul   %st(3),%st
 25c:	de c1                	faddp  %st,%st(1)
 25e:	dd 85 68 ff ff ff    	fldl   0xffffff68(%ebp)
 264:	d8 ca                	fmul   %st(2),%st
 266:	de c1                	faddp  %st,%st(1)
 268:	dc 00                	faddl  (%eax)
 26a:	dd 18                	fstpl  (%eax)
 26c:	d9 cb                	fxch   %st(3)
 26e:	dc 8d 18 ff ff ff    	fmull  0xffffff18(%ebp)
 274:	d9 ca                	fxch   %st(2)
 276:	dc 8d 30 ff ff ff    	fmull  0xffffff30(%ebp)
 27c:	de c2                	faddp  %st,%st(2)
 27e:	dc 8d 48 ff ff ff    	fmull  0xffffff48(%ebp)
 284:	de c1                	faddp  %st,%st(1)
 286:	d9 c9                	fxch   %st(1)
 288:	dc 8d 60 ff ff ff    	fmull  0xffffff60(%ebp)
 28e:	de c1                	faddp  %st,%st(1)
 290:	dc 40 08             	faddl  0x8(%eax)
 293:	dd 58 08             	fstpl  0x8(%eax)
 296:	0f 82 94 fd ff ff    	jb     30 <?fastSumZ2@@YAXNPBUlnSqrt@@0NPBNPAN@Z+0x30>
 29c:	8b 75 ec             	mov    0xffffffec(%ebp),%esi
 29f:	8b 7d e8             	mov    0xffffffe8(%ebp),%edi
 2a2:	8b e5                	mov    %ebp,%esp
 2a4:	5d                   	pop    %ebp
 2a5:	8b e3                	mov    %ebx,%esp
 2a7:	5b                   	pop    %ebx
 2a8:	c3                   	ret    
 2a9:	90                   	nop    
 2aa:	90                   	nop    
 2ab:	90                   	nop    
Disassembly of section .text:

00000000 <___sti__fastSum_cpp_823a07ca>:
   0:	53                   	push   %ebx
   1:	8b dc                	mov    %esp,%ebx
   3:	83 e4 f8             	and    $0xfffffff8,%esp
   6:	dd 05 20 00 00 00    	fldl   0x20
   c:	dd 1d 10 00 00 00    	fstpl  0x10
  12:	dd 05 18 00 00 00    	fldl   0x18
  18:	dd 1d 00 00 00 00    	fstpl  0x0
  1e:	dd 05 10 00 00 00    	fldl   0x10
  24:	dc 0d 20 00 00 00    	fmull  0x20
  2a:	dd 15 18 00 00 00    	fstl   0x18
  30:	dd 05 08 00 00 00    	fldl   0x8
  36:	de c9                	fmulp  %st,%st(1)
  38:	dd 1d 08 00 00 00    	fstpl  0x8
  3e:	8b e3                	mov    %ebx,%esp
  40:	5b                   	pop    %ebx
  41:	c3                   	ret    
  42:	90                   	nop    
  43:	90                   	nop    
