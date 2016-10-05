package rdd;

import java.io.Serializable;

import scala.math.Ordered;

/**
 * 自定义二次排序的KEY
 */
@SuppressWarnings("serial")
public class JSecondarySortKey implements Ordered<JSecondarySortKey>, Serializable {
	// 需要二次排序的Key
	private int first;
	private int second;

	// 二次排序的公开构造器
	public JSecondarySortKey(int first, int second) {
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
	public boolean $greater(JSecondarySortKey other) {
		if (this.first > other.getFirst()) {
			return true;
		} else if (this.first == other.getFirst()
				&& this.second > other.getSecond()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean $greater$eq(JSecondarySortKey other) {
		if (this.$greater(other)) {
			return true;
		} else if (this.first == other.getFirst()
				&& this.second == other.getSecond()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean $less(JSecondarySortKey other) {
		if (this.first < other.getFirst()) {
			return true;
		} else if (this.first == other.getFirst()
				&& this.second < other.getSecond()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean $less$eq(JSecondarySortKey other) {
		if (this.$less(other)) {
			return true;
		} else if (this.first == other.getFirst()
				&& this.second == other.getSecond()) {
			return true;
		}
		return false;
	}

	@Override
	public int compare(JSecondarySortKey other) {
		if (this.first - other.getFirst() != 0) {
			return this.first - other.getFirst();
		} else {
			return this.second - other.getSecond();
		}
	}

	@Override
	public int compareTo(JSecondarySortKey other) {
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
		JSecondarySortKey other = (JSecondarySortKey) obj;
		if (first != other.first)
			return false;
		if (second != other.second)
			return false;
		return true;
	}

}
