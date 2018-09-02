package com.eden.hao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

/**
 * 一、 使用 NIO 完成网络通信的三个核心
 * 
 * 		1> 通道(Channel) : 负责链接
 * 
 * 		2> 缓冲区(Buffer): 负责数据的存取
 * 
 * 		3> 选择器(Selector) : 是 SelectableChannel 的多路复用器. 用于监控 SelectableChannel 的 IO 状况!
 * 
 * 		   > 啥是 SelectableChannel 呢? 我们知道 Channel 是 java.nio.channels 包下的一个接口
 * 		   java.nio.channels.Channel 接口:
 * 				|--- SelectableChannel (是一个抽象类, 主要的几个实现类如下: )
 * 						|--- SocketChannel
 * 						|--- ServerSocketChannel
 * 						|--- DatagramChannel
 * 
 * 						|--- Pipe.SinkChannel
 * 						|--- Pipe.SourceChannel
 * 				
 * 
 * 
 * 
 * @author Administrator
 *
 */
public class TestBlockingNIO {

	// 客户端
	@Test
	public void client() throws IOException{
		
		//1. 获取通道 (只要是你要完成数据的传输, 不管是本地的还是网络的,都得先获取通道)
		SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
		
		//2. 分配指定大小的缓冲区
		ByteBuffer buf = ByteBuffer.allocate(1024);
		
		//3. 读取本地文件, 并发送到服务器端(读取本地文件也需要 channel)
		FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
		//循环读取
		while(inChannel.read(buf) != -1){
			
			// 读取数据前需要把缓冲区切换到读取模式
			buf.flip();
			sChannel.write(buf);
			buf.clear();
		}
		
		//4. 最后不要忘记关闭通道
		inChannel.close();
		sChannel.close();
	}
	
	// 服务端
	@Test
	public void server() throws IOException{
		
		//1. 获取通道
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		
		//2. 绑定链接
		ssChannel.bind(new InetSocketAddress(9898));
		
		//3. 获取客户端链接的通道
		SocketChannel sChannel = ssChannel.accept();
		
		//4. 分配一个指定大小的缓冲区
		ByteBuffer buf = ByteBuffer.allocate(1024);
		
		//5. 接收端客户端的数据, 并保存到本地
		FileChannel outChannel = FileChannel.open(Paths.get("2.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
		
		while(sChannel.read(buf) != -1){
			buf.flip();
			outChannel.write(buf);
		}
		
		//6. 最后不要忘记关闭通道
		sChannel.close();
		outChannel.close();
		ssChannel.close();
	}
	
}
