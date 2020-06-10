package des.command

/**
  * 此枚举类限制了操作类型为Encrypt,或者是Decrypt。
  * @author Li Junhu
  */
object Operations extends Enumeration {

  type Operation = Value

  val ENCRYPT : Operation = Value("encrypt")
  val DECRYPT : Operation = Value("decrypt")

}

