package core

import rdd.JSecondarySortKey

/**
	* Created by hadoop on 16-7-11.
	*/
class SecondarySortKey(val first: Int, val secondary: Int) extends Ordered[JSecondarySortKey] with Serializable {

	def compare(other: JSecondarySortKey): Int = {
		if (this.first - other.getFirst != 0) {
			this.first - other.getFirst
		} else {
			this.secondary - other.getSecond
		}
	}

}
