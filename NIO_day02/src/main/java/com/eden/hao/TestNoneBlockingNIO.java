package com.eden.hao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;

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
public class TestNoneBlockingNIO {

	//客户端
	@Test
	public void client() throws IOException, InterruptedException{
		
		//1. 获取通道
		SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
		
		//2. 切换成非阻塞模式
		sChannel.configureBlocking(false);
		
		//3. 分配一个指定大小的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		//4. 发送数据给服务器端(就发送当前时间给服务器吧 --> 使用 JDK 1.8 的 API 获取时间)
//		buffer.put(LocalDateTime.now().toString().getBytes());
//		buffer.flip();
//		sChannel.write(buffer);
//		buffer.clear();
		
		//4.1 搞一个循环发送数据
		Scanner scan = new Scanner(System.in);
		while(scan.hasNext()){
			
			String str = scan.next();
			if(str.equals("exit")){
				break;
			}
			buffer.put((LocalDateTime.now().toString() + ":" + str).getBytes());
			buffer.flip();
			sChannel.write(buffer);
			buffer.clear();
		}
		
		//5. 关闭通道
		sChannel.close();
	}
	
	//服务器端
	@Test
	public void server() throws IOException{
		//1. 获取通道
		ServerSocketChannel ssChannel  = ServerSocketChannel.open();
		
		//2. 切换成非阻塞模式
		ssChannel.configureBlocking(false);
		
		//3. 绑定链接
		ssChannel.bind(new InetSocketAddress(9898));
		
		//4. 获取一个选择器
		Selector selector = Selector.open();
		
		
		
		/**
		 * 5. 将通道注册到 选择器上面, 有两个参数
		 * 
		 * 	1> 第一个参数是选择器.
		 * 
		 *  2> 第二个是选择键: 选择键作用就是监控你通道的什么状态! 或者监控你通道的什
		 *  
		 *     么事件!(监控你通道到底是读状态还是写状态还是接收状态)
		 */
		// 现在我服务器是要接收客户端的信息, 所以 "监听接收事件"
		ssChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		//6. 轮询式获取选择器上已经 "准备就绪" 的事件
		//selector.select() 如果 大于 0 , 那么表示至少有一个已经准备就绪了! 
		while(selector.select() > 0){
			
			//7.  获取当前选择器中的 "选择键(已就绪的监听事件)"  
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			
			while(it.hasNext()){
				
				//8. 获取准备 "就绪" 的事件
				SelectionKey sk = it.next();
				
				//9. 判断具体是什么事件准备就绪(因为不同的事件就绪了我可以做不同的操作)
				if(sk.isAcceptable()){
					
					//10. 若 "接收就绪", 获取客户端链接
					SocketChannel sChannel = ssChannel.accept();
					
					//11. 切换为非阻塞模式
					sChannel.configureBlocking(false);
					
					//12. 将该通道注册到选择器上, 并且监听通道的读取就绪事件
					sChannel.register(selector, SelectionKey.OP_READ);
					
				//判断是否是读就绪, 如果就绪就开始读取数据	
				}else if(sk.isReadable()){
					
					//13. 获取当前选择器上 "读就绪" 状态的通道
					SocketChannel sChannel = (SocketChannel) sk.channel();
					
					//14. 读取数据
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					
					int len = 0;
					while((len = sChannel.read(buffer)) > 0){
						
						buffer.flip();
						System.out.println(new String(buffer.array(), 0, len));
						buffer.clear();
					}
					
				}
				
				//15. 取消选择键 SelectKey(SelectKey 用完了之后必须取消掉 --> 如果不取消的话他就一直有效)
				// 比如说有一个通道已经链接完成了, 你没有取消 SelectKey , 那么下次还会链接完成
				it.remove();
			}
		}
	}
	
}
