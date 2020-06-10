package des

import akka.actor.Actor
import org.apache.logging.log4j.scala.Logging

/**
  * 继承了Actor特质，负责异步生成16组子密钥序列，并发送回DESUtil.
  * @author Li Junhu
  */
class keyGenerator extends Actor with Logging {

  /**
    * 该方法用于将一个64位密钥序列生成16组48位的子密钥序列。
    * @param key_64 输入64位密钥
    * @return 返回48位子密钥，类型为Array[ Array[Byte] ]
    */
  def generateKeys(key_64: Array[Byte]): Array[Array[Byte]] = {

    /*
    * 64个密码位，有8位作为校验位，因此实际上只有56位参与加密。
    * 这56位分为了左右28位两部分。
    * */
    var C: Array[Byte] = new Array[Byte](28)
    var D: Array[Byte] = new Array[Byte](28)
    val keys: Array[Array[Byte]] = Array.ofDim[Byte](16, 48)

    for (i <- 0 until 28) {
      C(i) = key_64(replace1C(i) - 1)
      D(i) = key_64(replace1D(i) - 1)
    }

    for (i <- 0 until 16) {

      //循环左移
      C = C << moveNum(i)
      D = D << moveNum(i)

      //置换选择2
      for (j <- 0 until 48) {

        if (replace2(j) <= 28) keys(i)(j) = C(replace2(j) - 1)
        else keys(i)(j) = D(replace2(j) - 29)
      }
    }
    keys
  }

  /**
    * 在生成完毕后，将消息回送给DESUtilRef.
    * @return 返回生成的16组48位密钥序列。
    */
  override def receive: Receive = {

    //接收的是64位密钥，
    case a: Array[Byte] =>
      logger.info("子密钥序列生成完毕。")
      sender() ! generateKeys(a)
  }
}
