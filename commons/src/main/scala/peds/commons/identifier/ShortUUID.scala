package peds.commons.identifier

import com.eaio.uuid.UUID
import scala.collection.JavaConversions._
import org.apache.commons.codec.binary.Base64
import java.nio.ByteBuffer


object ShortUUID {
  def apply(): ShortUUID = uuidToShort( new UUID )

  implicit def uuidToShort( uuid: UUID ): ShortUUID = {
    val bb = ByteBuffer allocate 16
    bb putLong uuid.getClockSeqAndNode
    bb putLong uuid.getTime
    bb.flip
    val result = ShortUUID( Base64 encodeBase64URLSafeString bb.array )
    bb.clear
    result
  }

  implicit def short2UUID( short: ShortUUID ): UUID = short.toUUID

  val nilUUID: ShortUUID = uuidToShort( UUID.nilUUID )
}

case class ShortUUID( value: String ) extends Equals {
  def this() = this( ShortUUID.uuidToShort( new UUID ).toString )

  def isNil: Boolean = this == ShortUUID.nilUUID

  def toUUID: UUID = {
    val bytes = Base64 decodeBase64 value
    val bb = ByteBuffer allocate 16
    bb put bytes
    bb.flip
    val clock = bb.getLong
    val time = bb.getLong
    new UUID( time, clock )
  }

  override def canEqual( rhs: Any ): Boolean = rhs.isInstanceOf[ShortUUID]

  override def equals( rhs: Any ): Boolean = rhs match {
    case that: ShortUUID => {
      if ( this eq that ) true
      else {
        ( that.## == this.## ) &&
        ( that canEqual this ) &&
        ( that.value == this.value )
      }
    }

    case _ => false
  }

  override def hashCode: Int = 41 * ( 41 + value.## )

  override def toString: String = if ( this == ShortUUID.nilUUID ) "<nil short uuid>" else value
}