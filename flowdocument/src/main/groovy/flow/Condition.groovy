package flow

/**
 * Created by zpeugh on 5/22/15.
 */
class Condition {

	int conditionID
	String conditionName
	String conditionLabel
	def conditionParameters
	int conditionVersion

	public String toString() {
		return "[Condition Name: ${this.conditionName}, Label: ${this.conditionLabel}, Parameters: ${this.conditionParameters.toString()} ]"
	}


}
