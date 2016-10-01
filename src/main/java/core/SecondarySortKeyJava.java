package core;

import java.io.Serializable;

import scala.math.Ordered;

/**
 * 自定义二次排序的KEY
 */
@SuppressWarnings("serial")
public class SecondarySortKeyJava implements Ordered<SecondarySortKeyJava>, Serializable {
	// 需要二次排序的Key
	private int first;
	private int second;

	// 二次排序的公开构造器
	public SecondarySortKeyJava(int first, int second) {
		this.first = first;
		this.second = second;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	@Override
	public boolean $greater(SecondarySortKeyJava other) {
		if (this.first > other.getFirst()) {
			return true;
		} else if (this.first == other.getFirst()
				&& this.second > other.getSecond()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean $greater$eq(SecondarySortKeyJava other) {
		if (this.$greater(other)) {
			return true;
		} else if (this.first == other.getFirst()
				&& this.second == other.getSecond()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean $less(SecondarySortKeyJava other) {
		if (this.first < other.getFirst()) {
			return true;
		} else if (this.first == other.getFirst()
				&& this.second < other.getSecond()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean $less$eq(SecondarySortKeyJava other) {
		if (this.$less(other)) {
			return true;
		} else if (this.first == other.getFirst()
				&& this.second == other.getSecond()) {
			return true;
		}
		return false;
	}

	@Override
	public int compare(SecondarySortKeyJava other) {
		if (this.first - other.getFirst() != 0) {
			return this.first - other.getFirst();
		} else {
			return this.second - other.getSecond();
		}
	}

	@Override
	public int compareTo(SecondarySortKeyJava other) {
		if (this.first - other.getFirst() != 0) {
			return this.first - other.getFirst();
		} else {
			return this.second - other.getSecond();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + first;
		result = prime * result + second;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SecondarySortKeyJava other = (SecondarySortKeyJava) obj;
		if (first != other.first)
			return false;
		if (second != other.second)
			return false;
		return true;
	}

}
