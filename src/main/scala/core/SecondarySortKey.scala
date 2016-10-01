package core

/**
  * Created by hadoop on 16-7-11.
  */
class SecondarySortKey(val first: Int, val secondary: Int) extends Ordered[SecondarySortKeyJava] with Serializable {

  def compare(other: SecondarySortKeyJava): Int = {
    if (this.first - other.getFirst != 0) {
      this.first - other.getFirst
    } else {
      this.secondary - other.getSecond
    }
  }

}
