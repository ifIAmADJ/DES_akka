package des

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import scala.concurrent.{Await, Future}

object Main extends App {

  private val dESActorFactory = ActorSystem("DESActorFactory")

  private val keyGeneratorProxy: ActorRef = dESActorFactory.actorOf(Props[keyGenerator], "keyGenerator")
  private val dESUtil: ActorRef = dESActorFactory.actorOf(Props(DESUtil(keyGeneratorProxy)), "DESUtil")


  var key_64_str: String = "0011000100110010001100110011010000110101001101100011011100111000"
  val key_64: Array[Byte] = key_64_str.getBytes
  //这一步是将上述字符串的0101转化成Byte形式的0101.下同。

  for (i <- key_64.indices) key_64(i) = (key_64(i) - '0').toByte

  print("64位密钥>>")
  key_64.show

  var plain_64_str = "0011000000110001001100100011001100110100001101010011011000110111"
  val plainText: Array[Byte] = plain_64_str.getBytes
  for (i <- plainText.indices) plainText(i) = (plainText(i) - '0').toByte
  print("64位明文>>")
  plainText.show

  private val futureEncrypt: Future[Any] = dESUtil ? (key_64, plainText, command.Operations.ENCRYPT)

  private val encrypt: Array[Byte] = Await.result(futureEncrypt, timeout.duration).asInstanceOf[Array[Byte]]

  print("64位密文>>")
  encrypt.show

  private val futureDecrypt: Future[Any] = dESUtil ? (key_64, encrypt, command.Operations.DECRYPT)

  private val decrypt: Array[Byte] = Await.result(futureDecrypt, timeout.duration).asInstanceOf[Array[Byte]]

  print("还原明文>>")
  decrypt.show

  System.exit(-1)
}
