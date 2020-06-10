package des

import command.Operations
import org.apache.logging.log4j.scala.Logging

/**
  * DES加密的核心组件，内部封装了轮函数，并根据参数进行加密，或者解密操作。
  * @author Li Junhu
  */
object DESCore extends Logging {

  /**
    * 轮函数，是DES的核心函数。
    *
    * @param A 传入32位（4字节）信息，来源于D
    * @param K 传入48位（6字节）子密钥
    * @return
    */
  def f(A: Array[Byte], K: Array[Byte]): Array[Byte] = {

    var t: Array[Byte] = new Array[Byte](48)
    val r: Array[Byte] = new Array[Byte](32)

    val result = new Array[Byte](32)

    //使用E进行选择运算,将32位扩充为48位。
    for (i <- 0 until 48) t(i) = A(E(i) - 1)
    for (i <- 0 until 48) t /^= K

    //送入S盒。
    var (i, a) = (0, 0)

    //i = 0 , 6 , 12 , 18 , 24 , 32 , 40 , 48
    while (i < 48) {

      val j: Int = t(i) * 2 + t(i + 5) //取b1,b6 根据8421码组合成int数值。
      val k: Int = t(i + 1) * 8 + t(i + 2) * 4 + t(i + 3) * 2 + t(i + 4) //取b2,b3,b4,b5

      /*
        *  println(Integer.toBinaryString(16).substring(1) =>生成0000
        */
      val b: Array[Byte] = Integer.toBinaryString(S(i / 6)(j)(k) + 16).substring(1).getBytes


      for (n <- 0 until 4) r(a + n) = (b(n) - '0').toByte

      i += 6
      a += 4
    }

    //置换运算P
    for (i <- 0 until 32) {
      result(i) = r(P(i) - 1)
    }

    result
  }


  /**
    * 利用IP矩阵进行置换。
    *
    * @param text 需要输入64位数据
    * @return 返回数据。
    */
  def reverseInitialPermutation(text: Array[Byte]): Array[Byte] = {

    val bytes = new Array[Byte](64)

    for (i <- 0 until 64) bytes(i) = text(rIP(i) - 1)

    bytes

  }

  /**
    * 传入64位信息，该组件将基于命令对该信息进行加密或者解密。
    * @param text 64位Byte数组
    * @param K 密钥
    * @param Command 该参数是Operation枚举类的一个实例。根据其指令判断对该64位信息采取加密/解密动作。
    * @return 返回处理后的64位信息。
    */
  def exec(text:Array[Byte], K: Array[Array[Byte]], Command: Operations.Operation): Array[Byte] = {

    val byte64 = new Array[Byte](64)
    val L: Array[Array[Byte]] = Array.ofDim[Byte](17, 32)
    val R: Array[Array[Byte]] = Array.ofDim[Byte](17, 32)

    Command match {
      //若判断为加密
      case Operations.ENCRYPT =>

        logger.info("STEP[3/5] => 执行加密操作。")
        //填充LR
        for (i <- 0 until 32) {
          L(0)(i) = text(i)
          R(0)(i) = text(i + 32)
        }

        //加密通话，外币外币
        for (i <- 1 to 16) {
          L(i) = R(i - 1)
          R(i) = L(i - 1) /^ f(R(i - 1), K(i - 1))
        }

        //将最后得到的L16(右),R16(左)合并
        for (i <- 0 until 32) {
          byte64(i) = R(16)(i)
          byte64(i + 32) = L(16)(i)
        }

      //若判断为解密
      case Operations.DECRYPT =>


        logger.info("STEP[3/5] => 执行解密操作。")
        //填充LR
        for (i <- 0 until 32) {
          R(16)(i) = text(i)
          L(16)(i) = text(i + 32)
        }

        //解密通话，Are you good 马来西亚
        var i = 16
        while (i >= 1) {
          L(i - 1) = R(i) /^ f(L(i), K(i - 1))
          R(i - 1) = L(i)
          R(i) = L(i - 1) /^ f(R(i - 1), K(i - 1))
          i -= 1
        }

        for (i <- 0 until 32) {
          byte64(i) = L(0)(i)
          byte64(i + 32) = R(0)(i)
        }

    }

    logger.info("STEP[4/5] => 执行rIP转置。")
    reverseInitialPermutation(byte64)

  }

}
