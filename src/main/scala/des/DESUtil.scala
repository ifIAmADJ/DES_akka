package des

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.{Await, Future}

/**
  * 继承了Actor特质，负责对64位信息的接收和处理流程。
  * @param keyGenerator 需要在构造时绑定keyGenerator的一个引用。
  * @author Li Junhu
  */
case class DESUtil(keyGenerator: ActorRef) extends Actor with Logging {

  /**
    * 利用IP矩阵进行初始置换。
    *
    * @param text 需要输入64位数据
    * @return 返回数据。
    */
  def initialPermutation(text: Array[Byte]): Array[Byte] = {

    val bytes = new Array[Byte](64)

    for (i <- 0 until 64) bytes(i) = text(IP(i) - 1)

    bytes

  }

  /**
    * 与keyGeneratorRef进行通讯，实现异步编程。
    * 在等待16组子密钥传回期间，会首先对获取的信息进行转置。（利用IP矩阵）
    * 随后，当得到K之后，将会把所有的信息发送到DESCore中进行处理。
    * @return 在处理完毕后，会向主线程回复消息。
    */
  override def receive: Receive = {
    /*
    * msg._1:密钥
    * msg._2:明文/密文
    * msg._3:操作
    * */
    case msg: (Array[Byte], Array[Byte], command.Operations.Operation) =>

      val future: Future[Any] = keyGenerator ? msg._1

      logger.info("STEP[1/5] => 向keyGenerator请求子密钥序列。")
      val K: Array[Array[Byte]] = Await.result(future,timeout.duration).asInstanceOf[Array[Array[Byte]]]

      //被初始转置后的信息。
      val text: Array[Byte] = initialPermutation(msg._2)
      logger.info("STEP[2/5] => 对64位信息进行初始转置。")

      //向主函数回执消息。
      logger.info("STEP[5/5] => 主线程回送消息。")
      sender() ! DESCore.exec(text, K, msg._3)

  }

}
