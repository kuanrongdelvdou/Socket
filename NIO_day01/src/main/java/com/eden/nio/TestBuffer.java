package com.eden.nio;

import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * 一、 缓冲区(Buffer): 在 Java NIO 中负责数据的存取. 缓冲区实质就是数组(底层就是数组, 数组是干什么的呢? ---> 用于存储不同数据类型的数据)
 * 
 *		1> 根据数据类型的不同(boolean 除外), 提供了相应的缓冲区:  	
 *
 *			* ByteBuffer
 *
 *			* CharBuffer
 *
 *			* ShortBuffer
 *
 *			* IntBuffer
 *
 *			* LongBuffer
 *
 *			* FloatBuffer
 *
 *			* DoubleBuffer
 *
 *		2> 上述缓冲区的管理方式几乎一致, 通过 allocate() 获取缓冲区!
 *
 *二、缓冲区存取数据的两个核心方法
 *
 *		1> put() : 存入数据到缓冲区
 *
 *		2> get() : 获取缓冲区中的数据
 *
 *三、缓冲区的四个核心属性
 *
 *		1> capacity: 容量, 表示缓冲区中最大存储数据的容量, 一旦申明不能变更(数组的大小在初始化的时候就定下来了, 不能改变)!
 *
 *		2> limit: 界限, 表示缓冲区中可以操作数据的大小(说白了就是: limit 后的数据不能进行读写).
 *
 *		3> position: 位置, 表示缓冲区正在操作数据的位置!
 *
 *		4> mark : 标记, 表示记录当前 position 的位置. 可以通过 reset() 恢复到 mark 的位置!
 *
 *
 *四、直接缓冲区与非直接缓冲区
 *
 *		1> 非直接缓冲区: 通过 allocate() 方法分配缓冲区, 将缓冲区建立在 JVM 的内存中
 *
 *		2> 直接缓冲区: 通过 allocateDirect() 方法分配直接缓冲区, 将缓冲区建立在物理内存中.
 * @author Administrator
 *
 */
public class TestBuffer {
	
	@Test
	public void test3(){
		
		// 分配直接缓冲区
		ByteBuffer buf = ByteBuffer.allocateDirect(1024);
		
		// isDirect() 方法可以判断缓冲区是否是直接缓冲区
		System.out.println(buf.isDirect());
		
	}
	
	@Test
	public void test2(){
		String str = "abcde";
		ByteBuffer buf = ByteBuffer.allocate(1024);
		
		buf.put(str.getBytes());
		
		buf.flip();
		
		byte[] dst = new byte[buf.limit()];
		
		buf.get(dst, 0, 2);
		System.out.println(new String(dst, 0, 2));
		
		System.out.println("----- position 是: " + buf.position());
		
		// makr() : 标记一下
		buf.mark();
		buf.get(dst, 2, 2);
		System.out.println(new String(dst, 2, 2));
		System.out.println("----- position 是: " + buf.position());
		
		// reset(): 恢复到 mark 的位置
		buf.reset();
		System.out.println("------reset() 之后 position 是: " + buf.position());
		
		// 判断缓冲区中是否还有剩余的数据
		if(buf.hasRemaining()){
			
			// 获取缓冲区中可以操作的数量
			System.out.println("缓冲区可操作的数量还剩下: " + buf.remaining());
		}
	}

	@Test
	public void test1(){
		
		String str = "abcde";
		
		// 1. 分配一个指定大小的缓冲区(分配了一个指定大小的缓冲区)
		ByteBuffer buf = ByteBuffer.allocate(1024);
		
		System.out.println("----------------调用 allocate() 方法以后----------------");
		System.out.println("缓冲区的位置: " + buf.position());
		System.out.println("缓冲区的界限: " + buf.limit());
		System.out.println("缓冲区的容量: " + buf.capacity());
		
		// 2.利用 put() 存入数据到缓冲区  ---> 现在 buf 是写数据的模式
		buf.put(str.getBytes());
		
		System.out.println("----------------调用 put() 方法以后--------------------" );
		System.out.println("缓冲区的位置: " + buf.position());
		System.out.println("缓冲区的界限: " + buf.limit());
		System.out.println("缓冲区的容量: " + buf.capacity());
		
		// 3. 切换buffer 到读取数据的模式
		buf.flip();
		System.out.println("----------------调用 flip() 方法切换到读数据模式--------");
		System.out.println("缓冲区的位置: " + buf.position());
		System.out.println("缓冲区的界限: " + buf.limit());
		System.out.println("缓冲区的容量: " + buf.capacity());
		
		// 4. 利用 get() 方法读取缓冲区中的数据
		byte[] dst = new byte[buf.limit()];
		buf.get(dst);
		System.out.println(new String(dst, 0, dst.length));
		
		System.out.println("----------------调用 get() 方法以后--------------------");
		System.out.println("缓冲区的位置: " + buf.position());
		System.out.println("缓冲区的界限: " + buf.limit());
		System.out.println("缓冲区的容量: " + buf.capacity());
		
		// 5. rewind() 方法: 可重复读数据
		buf.rewind();
		System.out.println("----------------调用 rewind() 方法以后-----------------");
		System.out.println("缓冲区的位置: " + buf.position());
		System.out.println("缓冲区的界限: " + buf.limit());
		System.out.println("缓冲区的容量: " + buf.capacity());
		
		// 6. clear() : 清空缓冲区 ---> 但是缓冲区中的数据依然存在, 这些数据处于 "被遗忘" 的状态
		// 什么是被遗忘? ---> 就是说缓冲区中的 "位置"、"界限" 等重要属性都变了, 编程多少了不知道 ---> 既然不知道那就全部恢复到最初状态吧!
		System.out.println("----------------调用 clear() 方法以后-----------------");
		System.out.println("缓冲区的位置: " + buf.position());
		System.out.println("缓冲区的界限: " + buf.limit());
		System.out.println("缓冲区的容量: " + buf.capacity());
		
		// 缓冲区的数据还是存在的 ---> 还是能打印出来数据的!
		System.out.println((char)buf.getChar());
		
	}
	
}
