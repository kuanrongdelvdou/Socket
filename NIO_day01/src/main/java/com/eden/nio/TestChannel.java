package com.eden.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;
import java.util.RandomAccess;
import java.util.Set;

import org.junit.Test;

/**
 * 
 * 一、 通道: 负责原节点和目标节点的链接! ----> 可以理解为一条链接哈尔滨到北京的一条铁路!. 在 Java 中主要负责缓冲区中数据的传输,
 * Channe 本身不存储任何数据, 因此需要配合缓冲区进行传输数据!
 * 
 * 二、通道的主要实现类: 所有的通道都在 java.nio.channels.Channel 下面
 * 
 * java.nio.channels.Channel 接口主要实现类
 * 		 |---FileChannel 
 * 		 |---SocketChannel
 * 		 |---ServerSocketChannel 
 * 		 |---DatagramChannel
 * 
 * 		1> FileChannel : 文件通道, 主要用于操作本地文件的
 * 
 * 		2> SocketChannel、ServerSocketChannel: 和 Socket 相关的, 用于网络TPC/IP 的网络 IO
 * 
 * 		3> DatagramChannel: 也是网络 IO, 用于 UDP
 * 
 * 三、获取通道的三种方式
 * 
 * 		1> Java 针对支持通道的类提供了 getChannel() 方法
 * 
 * 		    [1] 本地 IO 支持通道的类有:
 * 
 * 			FileInputStream/FileOutPutStream/RandomAccessFile
 * 
 * 		    [2] 网络 IO 支持通道的类有:
 * 
 * 			Socket/ServerSocket/DatagramSocket
 * 
 * 		2> 在 JDK 1.7 中的 NIO.2, 针对哥哥通道提供了一个静态方法 open()
 * 
 * 		3> 在 JDK 1.7 中的 NIO.2 的 Files 工具类的 newByteChannel() 方法
 * 
 * 四、通道之间的数据传输
 * 
 * 		1> transferFrom()
 * 
 * 		2> transferTo()
 * 
 * 五、分散(Scattering)与聚集(Gather)
 * 
 * 		1> 分散读取(Scattering Reads): 将通道中的数据分散到多个缓冲区中!
 * 
 * 		2> 聚集写入(Gather Writes) : 将多个缓冲区中的数据聚集到通道中
 * 
 * 六、字符集 Charset
 * 
 * 		1> 编码: 字符串 ---> 字节数组(看的懂的 ---> 看不懂的);
 * 
 * 		3> 解码: 字节数据 ---> 字符串(看不懂的 ---> 看的懂的);
 * 
 * @author Administrator
 *
 */
public class TestChannel {
	
	 // 5. 字符集
	@Test
	public void test6() throws CharacterCodingException{
		
		
		Charset cs1 = Charset.forName("GBK");
		
		// 获取编码器
		CharsetEncoder ce = cs1.newEncoder();
		
		// 获取解码器
		CharsetDecoder cd = cs1.newDecoder();
		
		CharBuffer cBuf = CharBuffer.allocate(1024);
		
		cBuf.put("郝爷威武!!");
		cBuf.flip();
		
		// 编码(将 charbufer ---> bytebuf)
		ByteBuffer bBuf = ce.encode(cBuf);
		
		// 看看有没有编码成功
		System.out.println("-------如下是编码之后的信息---------");
		for(int i = 0; i < bBuf.limit(); i ++){
			System.out.println(bBuf.get());
		}
		
		// 解码
		bBuf.flip();
		CharBuffer cBuf2 = cd.decode(bBuf);
		System.out.println(cBuf2.toString());
	}
	
	@Test
	public void test5(){
		
		// 看看有多少中字符集
		Map<String, Charset> map = Charset.availableCharsets();
		
		Set<Entry<String,Charset>> set = map.entrySet();
		
		for(Entry<String, Charset> entry : set){
			
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
	}
	
	// 4. 分散和聚集
	@Test
	public void test4() throws IOException{
		
		RandomAccessFile raf1 = new RandomAccessFile("1.txt", "rw");
		
		//1. 获取通道
		FileChannel channel1 = raf1.getChannel();
		
		//2. 分配指定大小的缓冲区(需要多个缓冲区)
		ByteBuffer buf1 = ByteBuffer.allocate(100);
		ByteBuffer buf2 = ByteBuffer.allocate(1024);
		
		//3. 分散读取通道中的数据到多个缓冲区中
		ByteBuffer[] bufs = {buf1, buf2};
		channel1.read(bufs);
		
		// 将多个缓冲区中的数据打印在控制台
		for(ByteBuffer buffer : bufs){
			
			// 缓冲区切换到去读模式
			buffer.flip();
		}
		System.out.println("------------第一个缓冲区中的数据--------------");
		System.out.println(new String(bufs[0].array(), 0, bufs[0].limit()));
		System.out.println("------------第二个缓冲区中的数据--------------");
		System.out.println(new String(bufs[1].array(), 0, bufs[1].limit()));
		
		//4. 聚集写入
		RandomAccessFile raf2 = new RandomAccessFile("2.txt", "rw");
		FileChannel channel2 = raf2.getChannel();
		
		// 将多个缓冲区中的数据聚集写入通道中
		channel2.write(bufs);
		
	}

	// 3. 通道之间的数据传输
	@Test
	public void test3() throws IOException {

		
		FileChannel inChannel = FileChannel.open(Paths.get("f:/1.zip"), StandardOpenOption.READ);

		FileChannel outChannel = FileChannel.open(Paths.get("f:/3.zip"), StandardOpenOption.WRITE,StandardOpenOption.READ, StandardOpenOption.CREATE_NEW);

		// 使用 transferTo()完成文件的复制(就这么简单的一句话!!)!
		//inChannel.transferTo(0, inChannel.size(), outChannel);
		
		// 也可以使用 transferFrom()
		outChannel.transferFrom(inChannel, 0, inChannel.size());
		
		// 上面的两句代码, 无论哪一句都可以完成文件的复制(就是将数据写入内存!)
		
	}

	// 2. 使用直接缓冲区完成文件的复制(内存映射文件)
	@Test
	public void test2() throws IOException {

		long start = System.currentTimeMillis();

		FileChannel inChannel = FileChannel.open(Paths.get("f:/1.zip"), StandardOpenOption.READ);

		FileChannel outChannel = FileChannel.open(Paths.get("f:/3.zip"), StandardOpenOption.WRITE,
				StandardOpenOption.READ, StandardOpenOption.CREATE_NEW);

		// 内存映射文件
		MappedByteBuffer inMappedBuf = inChannel.map(MapMode.READ_ONLY, 0, inChannel.size());
		MappedByteBuffer outMappedBuf = outChannel.map(MapMode.READ_WRITE, 0, inChannel.size());

		// 这种方式相较于原来的方式(test1) 有什么好处呢? 原来需要通过通道去读写, 而现在这种缓冲区不用, 因为这个缓冲区直接在内存中
		// 我现在直接往缓冲区中放数据就是直接放在了物理内存中了(就相当于直接放在了文件中)! ---> 甚至都不需要通道了!

		// 直接对缓冲区进行数据的读写操作
		byte[] dst = new byte[inMappedBuf.limit()];
		inMappedBuf.get(dst);
		outMappedBuf.put(dst);

		// 不需要通过通道读写, 直接放缓冲区中!

		inChannel.close();
		outChannel.close();

		long end = System.currentTimeMillis();
		System.out.println("使用直接缓冲区复制文件所耗费的时间为: " + (end - start));

	}

	// 1. 利用通道完成文件的复制(非直接缓冲区)
	@Test
	public void test1() {

		long start = System.currentTimeMillis();

		FileInputStream fis = null;
		FileOutputStream fos = null;

		// ① 获取通道
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			fis = new FileInputStream("f:/1.zip");

			fos = new FileOutputStream("f:/2.zip");

			inChannel = fis.getChannel();
			outChannel = fos.getChannel();

			// ② 分配指定大小的缓冲区(通道自己无法完成数据的传输, 必须配合缓冲区)
			ByteBuffer buf = ByteBuffer.allocate(1024);

			// ③ 将通道中的数据存入缓存区中
			while (inChannel.read(buf) != -1) {

				// 将缓存区的模式切换成读取数据的模式
				buf.flip();

				// ④ 将缓冲区中的数据写入通道
				outChannel.write(buf);

				// 清空缓冲区
				buf.clear();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outChannel != null) {
				try {
					outChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inChannel != null) {

				try {
					inChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("使用非直接缓冲区复制文件所耗费的时间为: " + (end - start));
	}
}
